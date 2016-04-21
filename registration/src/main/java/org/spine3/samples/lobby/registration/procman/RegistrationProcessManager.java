/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.samples.lobby.registration.procman;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Commands;
import org.spine3.base.EventContext;
import org.spine3.base.Schedule;
import org.spine3.base.UserId;
import org.spine3.protobuf.Durations;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.payment.contracts.PaymentCompleted;
import org.spine3.samples.lobby.registration.contracts.OrderConfirmed;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.order.ConfirmOrder;
import org.spine3.samples.lobby.registration.order.MarkSeatsAsReserved;
import org.spine3.samples.lobby.registration.order.RejectOrder;
import org.spine3.samples.lobby.registration.seat.availability.CancelSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.CommitSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.MakeSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.SeatsReserved;
import org.spine3.server.command.Assign;
import org.spine3.server.event.Subscribe;
import org.spine3.server.procman.CommandRouted;
import org.spine3.server.procman.ProcessManager;
import org.spine3.time.ZoneOffset;

import static com.google.protobuf.util.TimeUtil.getCurrentTime;
import static org.spine3.base.Commands.create;
import static org.spine3.protobuf.Timestamps.isAfter;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State.*;

/**
 * A process manager for registration to conference process.
 *
 * @author Alexander Litus
 */
public class RegistrationProcessManager extends ProcessManager<ProcessManagerId, RegistrationProcess> {

    @SuppressWarnings("MagicNumber")
    private static final Duration RESERVATION_EXPIRATION_OFFSET = Durations.minutes(15);

    private CommandSender commandSender;

    /**
     * Creates a new instance.
     *
     * @param id an ID for the new instance
     * @throws IllegalArgumentException if the ID type is unsupported
     */
    public RegistrationProcessManager(ProcessManagerId id) {
        super(id);
        this.commandSender = new CommandSender();
    }

    @VisibleForTesting
    /* package */ void setCommandSender(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @Subscribe
    public void on(OrderPlaced event, EventContext context) throws IllegalProcessStateFailure {
        final RegistrationProcess.State state = getState().getProcessState();
        if (state != NOT_STARTED) {
            throw newIllegalProcessStateFailure(event);
        }
        updateState(event);

        final Timestamp reservationExpiration = event.getReservationAutoExpiration();
        if (isReservationExpired(reservationExpiration)) {
            commandSender.rejectOrder(event);
        } else {
            setProcessState(AWAITING_RESERVATION_CONFIRMATION);
            commandSender.reserveSeats(event);
            commandSender.expireRegistrationProcess(event, getId());
        }
    }

    @Subscribe
    public void on(OrderUpdated event, EventContext context) throws IllegalProcessStateFailure {
        final RegistrationProcess.State state = getState().getProcessState();
        if (state == AWAITING_RESERVATION_CONFIRMATION || state == RESERVATION_CONFIRMED) {
            setProcessState(AWAITING_RESERVATION_CONFIRMATION);
            commandSender.reserveSeats(event, getState().getConferenceId());
        } else {
            throw newIllegalProcessStateFailure(event);
        }
    }

    @Subscribe
    public void on(SeatsReserved event, EventContext context) throws IllegalProcessStateFailure {
        final RegistrationProcess state = getState();
        if (state.getProcessState() == AWAITING_RESERVATION_CONFIRMATION) {
            setProcessState(RESERVATION_CONFIRMED);
            commandSender.markSeatsAsReserved(event, state);
        } else {
            throw newIllegalProcessStateFailure(event);
        }
    }

    @Subscribe
    public void on(PaymentCompleted event, EventContext context) throws IllegalProcessStateFailure {
        final RegistrationProcess.State state = getState().getProcessState();
        if (state == RESERVATION_CONFIRMED) {
            setProcessState(PAYMENT_RECEIVED);
            commandSender.confirmOrder(event);
        } else {
            throw newIllegalProcessStateFailure(event);
        }
    }

    @Subscribe
    public void on(OrderConfirmed event, EventContext context) throws IllegalProcessStateFailure {
        final RegistrationProcess.State state = getState().getProcessState();
        if (state == RESERVATION_CONFIRMED || state == PAYMENT_RECEIVED) {
            setIsCompleted(true);
            commandSender.commitSeatReservation(event);
        } else {
            throw newIllegalProcessStateFailure(event);
        }
    }

    @Assign
    public CommandRouted handle(ExpireRegistrationProcess cmd, CommandContext context) {
        final RegistrationProcess state = getState();
        if (!state.getIsCompleted()) {
            setIsCompleted(true);
            final RejectOrder rejectOrder = commandSender.newRejectOrderCommand(getState().getOrderId());
            final CancelSeatReservation cancelReservation = commandSender.newCancelSeatReservationCommand(state);
            return newRouter().of(cmd, context)
                    .add(rejectOrder)
                    .add(cancelReservation)
                    .route();
        } else {
            log().warn("Ignoring {} command which is no longer relevant, command ID: {}",
                    cmd.getClass().getSimpleName(), context.getCommandId().getUuid());
            return newRouter()
                    .of(cmd, context)
                    .route();
        }
    }

    private void setProcessState(RegistrationProcess.State processState) {
        final RegistrationProcess newState = getState().toBuilder()
                .setProcessState(processState)
                .build();
        incrementState(newState);
    }

    private void updateState(OrderPlaced event) {
        final RegistrationProcess newState = getState().toBuilder()
                .setOrderId(event.getOrderId())
                .setConferenceId(event.getConferenceId())
                .setReservationAutoExpiration(event.getReservationAutoExpiration())
                .build();
        incrementState(newState);
    }

    private static boolean isReservationExpired(Timestamp reservationAutoExpiration) {
        final Timestamp now = getCurrentTime();
        final boolean isNowAfterThanExpiration = isAfter(now, reservationAutoExpiration);
        return isNowAfterThanExpiration;
    }

    private void setIsCompleted(boolean isCompleted) {
        final RegistrationProcess newState = getState().toBuilder()
                .setIsCompleted(isCompleted)
                .build();
        incrementState(newState);
    }

    private IllegalProcessStateFailure newIllegalProcessStateFailure(Message msgHandled) {
        final ProcessManagerId id = getId();
        final RegistrationProcess.State processState = getState().getProcessState();
        final Any msgAny = Any.pack(msgHandled);
        final IllegalProcessStateFailure result = new IllegalProcessStateFailure(id, processState, msgAny);
        return result;
    }

    @VisibleForTesting // otherwise it would be private, but it is needed in tests
    @SuppressWarnings("OverlyCoupledClass")
    protected class CommandSender {

        private void reserveSeats(OrderPlaced event) {
            reserveSeats(event.getOrderId(), event.getConferenceId(), event.getSeatList());
        }

        private void reserveSeats(OrderUpdated event, ConferenceId conferenceId) {
            reserveSeats(event.getOrderId(), conferenceId, event.getSeatList());
        }

        private void reserveSeats(OrderId orderId, ConferenceId conferenceId, Iterable<SeatQuantity> seats) {
            final ReservationId reservationId = toReservationId(orderId);
            final MakeSeatReservation message = MakeSeatReservation.newBuilder()
                    .setConferenceId(conferenceId)
                    .setReservationId(reservationId)
                    .addAllSeat(seats)
                    .build();
            send(message);
        }

        private void markSeatsAsReserved(SeatsReserved event, RegistrationProcess state) {
            final MarkSeatsAsReserved message = MarkSeatsAsReserved.newBuilder()
                    .setOrderId(state.getOrderId())
                    .setReservationExpiration(state.getReservationAutoExpiration())
                    .addAllSeat(event.getReservedSeatUpdatedList())
                    .build();
            send(message);
        }

        private void rejectOrder(OrderPlaced event) {
            final RejectOrder cmd = newRejectOrderCommand(event.getOrderId());
            send(cmd);
        }

        private RejectOrder newRejectOrderCommand(OrderId orderId) {
            final RejectOrder message = RejectOrder.newBuilder()
                    .setOrderId(orderId)
                    .build();
            return message;
        }

        private void confirmOrder(PaymentCompleted event) {
            final ConfirmOrder message = ConfirmOrder.newBuilder()
                    .setOrderId(event.getOrderId())
                    .build();
            send(message);
        }

        private void commitSeatReservation(OrderConfirmed event) {
            final ReservationId reservationId = toReservationId(event.getOrderId());
            final CommitSeatReservation message = CommitSeatReservation.newBuilder()
                    .setReservationId(reservationId)
                    .build();
            send(message);
        }

        private void expireRegistrationProcess(OrderPlaced event, ProcessManagerId id) {
            final ExpireRegistrationProcess msg = ExpireRegistrationProcess.newBuilder()
                    .setProcessManagerId(id)
                    .build();
            send(msg, RESERVATION_EXPIRATION_OFFSET);
        }

        private CancelSeatReservation newCancelSeatReservationCommand(RegistrationProcess state) {
            final ReservationId reservationId = toReservationId(state.getOrderId());
            final CancelSeatReservation message = CancelSeatReservation.newBuilder()
                    .setReservationId(reservationId)
                    .setConferenceId(state.getConferenceId())
                    .build();
            return message;
        }

        private ReservationId toReservationId(OrderId orderId) {
            final ReservationId.Builder builder = ReservationId.newBuilder().setUuid(orderId.getUuid());
            return builder.build();
        }

        private void send(Message cmdMsg) {
            // TODO:2016-02-29:alexander.litus: obtain user ID and zone offset
            final CommandContext context = Commands.createContext(UserId.getDefaultInstance(), ZoneOffset.getDefaultInstance());
            final Command cmd = create(cmdMsg, context);
            post(cmd);
        }

        private void send(Message cmdMsg, Duration delay) {
            // TODO:2016-02-29:alexander.litus: obtain user ID and zone offset
            final Schedule schedule = Schedule.newBuilder()
                                           .setAfter(delay)
                                           .build();
            final CommandContext context = Commands.createContext(UserId.getDefaultInstance(), ZoneOffset.getDefaultInstance())
                    .toBuilder()
                    .setSchedule(schedule)
                    .build();
            final Command cmd = create(cmdMsg, context);
            post(cmd);
        }

        @VisibleForTesting // otherwise it would be private
        protected void post(Command cmd) {
            getCommandBus().post(cmd);
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(RegistrationProcessManager.class);
    }
}
