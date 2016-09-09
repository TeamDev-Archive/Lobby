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
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Duration;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.base.CommandContext;
import org.spine3.base.EventContext;
import org.spine3.protobuf.Timestamps;
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
import org.spine3.server.BoundedContext;
import org.spine3.server.command.Assign;
import org.spine3.server.command.CommandBus;
import org.spine3.server.command.CommandHandler;
import org.spine3.server.event.EventBus;
import org.spine3.server.procman.CommandRouted;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.protobuf.util.TimeUtil.add;
import static com.google.protobuf.util.TimeUtil.getCurrentTime;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.protobuf.Durations.ofMinutes;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.IdFactory.newOrderId;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newBoundedContext;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * A test data factory for {@link RegistrationProcessManager} and {@link RegistrationProcessManagerRepository} tests.
 *
 * @author Alexander Litus
 */
@SuppressWarnings({"UtilityClass", "MagicNumber"})
/* package */ class Given {

    private static final ProcessManagerId ID = newProcessManagerId();
    /* package */ static final OrderId ORDER_ID = newOrderId();
    /* package */ static final ConferenceId CONFERENCE_ID = newConferenceId();
    /* package */ static final ReservationId RESERVATION_ID = ReservationId.newBuilder()
                                                                           .setUuid(ORDER_ID.getUuid())
                                                                           .build();

    private final TestProcessManager processManager;
    private final BoundedContext boundedContext = newBoundedContext();

    /* package */ Given() {
        processManager = new TestProcessManager(ID);
        processManager.setCommandSender(processManager.new MockCommandSender());
        boundedContext.getCommandBus()
                      .register(new StubCommandHandler(newUuid(), boundedContext.getEventBus()));
    }

    /**
     * Creates a new process manager ID with the generated UUID.
     */
    /* package */ static ProcessManagerId newProcessManagerId() {
        return ProcessManagerId.newBuilder()
                               .setUuid(newUuid())
                               .build();
    }

    /* package */ TestProcessManager processManager(RegistrationProcess.State processState) {
        final TestProcessManager result = processManager(processState, /*isCompleted=*/false);
        return result;
    }

    /* package */ TestProcessManager processManager(RegistrationProcess.State processState, boolean isCompleted) {
        switch (processState) {
            case NOT_STARTED:
                return processManager;
            case AWAITING_RESERVATION_CONFIRMATION:
            case RESERVATION_CONFIRMED:
            case PAYMENT_RECEIVED:
                processManager.incrementState(buildState(processState, isCompleted));
                return processManager;
            case UNRECOGNIZED:
            default:
                throw new IllegalArgumentException("Unexpected state.");
        }
    }

    private RegistrationProcess buildState(RegistrationProcess.State processState, boolean isCompleted) {
        final RegistrationProcess.Builder builder = processManager.getState()
                                                                  .toBuilder()
                                                                  .setOrderId(ORDER_ID)
                                                                  .setConferenceId(CONFERENCE_ID)
                                                                  .setReservationAutoExpiration(minutesAhead(15))
                                                                  .setProcessState(processState)
                                                                  .setIsCompleted(isCompleted);
        return builder.build();
    }

    /* package */ static Timestamp reservationExpirationTimeBeforeNow() {
        final Timestamp result = Timestamps.secondsAgo(30);
        return result;
    }

    private static Timestamp minutesAhead(long minutes) {
        final Timestamp now = getCurrentTime();
        final Duration delta = ofMinutes(minutes);
        return add(now, delta);
    }

    /* package */ class TestProcessManager extends RegistrationProcessManager {

        private final List<Message> commandsSent = newLinkedList();

        private TestProcessManager(ProcessManagerId id) {
            super(id);
        }

        @Override
        @SuppressWarnings("RefusedBequest") // is overridden to do not throw an exception
        protected RegistrationProcess getDefaultState() {
            return RegistrationProcess.getDefaultInstance();
        }

        @VisibleForTesting
        @Override
        public void incrementState(RegistrationProcess newState) {
            super.incrementState(newState);
        }

        @Override
        @SuppressWarnings("RefusedBequest") // is overridden to do not throw an exception
        protected CommandBus getCommandBus() {
            return boundedContext.getCommandBus();
        }

        public List<Message> getCommandsSent() {
            return ImmutableList.copyOf(commandsSent);
        }

        private class MockCommandSender extends CommandSender {

            @Override
            protected void post(org.spine3.base.Command cmd) {
                final Message msg = cmd.getMessage();
                commandsSent.add(msg);
                super.post(cmd);
            }
        }
    }

    /**
     * A test utility class providing events.
     */
    /* package */ static class Event {

        /* package */ static final EventContext CONTEXT = EventContext.getDefaultInstance();

        private static final List<SeatQuantity> SEATS = ImmutableList.of(newSeatQuantity(5), newSeatQuantity(10));

        private Event() {
        }

        /* package */ static OrderPlaced orderPlaced() {
            final Timestamp afterNow = minutesAhead(15);
            return orderPlaced(afterNow);
        }

        /* package */ static OrderPlaced orderPlaced(Timestamp reservationExpiration) {
            final OrderPlaced.Builder builder = OrderPlaced.newBuilder()
                                                           .setOrderId(ORDER_ID)
                                                           .setConferenceId(CONFERENCE_ID)
                                                           .addAllSeat(SEATS)
                                                           .setReservationAutoExpiration(reservationExpiration);
            return builder.build();
        }

        /* package */ static OrderUpdated orderUpdated() {
            final OrderUpdated.Builder builder = OrderUpdated.newBuilder()
                                                             .setOrderId(ORDER_ID)
                                                             .addAllSeat(SEATS);
            return builder.build();
        }

        /* package */ static SeatsReserved seatsReserved() {
            final SeatsReserved.Builder builder = SeatsReserved.newBuilder()
                                                               .setReservationId(RESERVATION_ID)
                                                               .setConferenceId(CONFERENCE_ID)
                                                               .addAllReservedSeatUpdated(SEATS);
            return builder.build();
        }

        /* package */ static PaymentCompleted paymentCompleted() {
            final PaymentCompleted.Builder builder = PaymentCompleted.newBuilder()
                                                                     .setOrderId(ORDER_ID);
            return builder.build();
        }

        /* package */ static OrderConfirmed orderConfirmed() {
            final OrderConfirmed.Builder builder = OrderConfirmed.newBuilder()
                                                                 .setOrderId(ORDER_ID)
                                                                 .addAllSeat(SEATS);
            return builder.build();
        }
    }

    /**
     * A test utility class providing commands.
     */
    /* package */ static class Command {

        /* package */ static final CommandContext CONTEXT = CommandContext.getDefaultInstance();

        private Command() {
        }

        /* package */ static ExpireRegistrationProcess expireRegistrationProcess() {
            final ExpireRegistrationProcess.Builder builder = ExpireRegistrationProcess.newBuilder()
                                                                                       .setProcessManagerId(ID);
            return builder.build();
        }
    }

    private static class StubCommandHandler extends CommandHandler {

        protected StubCommandHandler(String id, EventBus eventBus) {
            super(id, eventBus);
        }

        @Assign
        public CommandRouted handle(MakeSeatReservation cmd, CommandContext context) {
            return CommandRouted.getDefaultInstance();
        }

        @Assign
        public CommandRouted handle(MarkSeatsAsReserved cmd, CommandContext context) {
            return CommandRouted.getDefaultInstance();
        }

        @Assign
        public CommandRouted handle(RejectOrder cmd, CommandContext context) {
            return CommandRouted.getDefaultInstance();
        }

        @Assign
        public CommandRouted handle(ConfirmOrder cmd, CommandContext context) {
            return CommandRouted.getDefaultInstance();
        }

        @Assign
        public CommandRouted handle(CommitSeatReservation cmd, CommandContext context) {
            return CommandRouted.getDefaultInstance();
        }

        @Assign
        public CommandRouted handle(CancelSeatReservation cmd, CommandContext context) {
            return CommandRouted.getDefaultInstance();
        }
    }
}
