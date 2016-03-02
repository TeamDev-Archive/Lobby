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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import org.mockito.Mockito;
import org.spine3.base.EventContext;
import org.spine3.protobuf.Timestamps;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;

import java.util.List;

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
/*package*/ class Given {

    private static final ProcessManagerId ID = newProcessManagerId();
    private static final OrderId ORDER_ID = newOrderId();
    private static final ConferenceId CONFERENCE_ID = newConferenceId();

    private final TestProcessManager processManager;
    private final RegistrationProcessManager.CommandSender commandSender;

    /*package*/ Given() {
        commandSender = Mockito.mock(RegistrationProcessManager.CommandSender.class);
        processManager = new TestProcessManager(ID, commandSender);
    }

    /**
     * Creates a new process manager ID with the generated UUID.
     */
    /*package*/ static ProcessManagerId newProcessManagerId() {
        return ProcessManagerId.newBuilder().setUuid(newUuid()).build();
    }

    /*package*/ TestProcessManager processManager(RegistrationProcess.State processState) {
        switch (processState) {
            case NOT_STARTED:
                return processManager;
            case AWAITING_RESERVATION_CONFIRMATION:
            case RESERVATION_CONFIRMED:
            case PAYMENT_RECEIVED:
                processManager.incrementState(buildState(processState));
                return processManager;
            case UNRECOGNIZED:
            default:
                throw new IllegalArgumentException("Unexpected state.");
        }
    }

    private RegistrationProcess buildState(RegistrationProcess.State processState) {
        final RegistrationProcess.Builder builder = processManager.getState()
                .toBuilder()
                .setOrderId(ORDER_ID)
                .setConferenceId(CONFERENCE_ID)
                .setReservationAutoExpiration(minutesAhead(15))
                .setProcessState(processState);
        return builder.build();
    }

    /*package*/ static Timestamp reservationExpirationTimeBeforeNow() {
        final Timestamp result = Timestamps.secondsAgo(30);
        return result;
    }

    private static Timestamp minutesAhead(long minutes) {
        final Timestamp now = getCurrentTime();
        final Duration delta = ofMinutes(minutes);
        return add(now, delta);
    }

    /*package*/ RegistrationProcessManager.CommandSender getCommandSender() {
        return commandSender;
    }

    /*package*/ static class TestProcessManager extends RegistrationProcessManager {

        private TestProcessManager(ProcessManagerId id, CommandSender commandSender) {
            super(id);
            setBoundedContext(newBoundedContext());
            setCommandSender(commandSender);
        }

        @Override
        @SuppressWarnings("RefusedBequest") // is overridden to do not throw an exception
        protected RegistrationProcess getDefaultState() {
            return RegistrationProcess.getDefaultInstance();
        }

        // Is overridden to make accessible in tests.
        @Override
        public void incrementState(RegistrationProcess newState) {
            super.incrementState(newState);
        }
    }

    /*package*/ static class Event {

        /*package*/ static final EventContext CONTEXT = EventContext.getDefaultInstance();

        private static final List<SeatQuantity> SEATS = ImmutableList.of(newSeatQuantity(5), newSeatQuantity(10));

        private Event() {}

        /*package*/ static OrderPlaced orderPlaced() {
            final Timestamp afterNow = minutesAhead(15);
            return orderPlaced(afterNow);
        }

        /*package*/ static OrderPlaced orderPlaced(Timestamp reservationExpiration) {
            final OrderPlaced.Builder builder = OrderPlaced.newBuilder()
                    .setOrderId(ORDER_ID)
                    .setConferenceId(CONFERENCE_ID)
                    .addAllSeat(SEATS)
                    .setReservationAutoExpiration(reservationExpiration);
            return builder.build();
        }

        public static OrderUpdated orderUpdated() {
            final OrderUpdated.Builder builder = OrderUpdated.newBuilder()
                    .setOrderId(ORDER_ID)
                    .addAllSeat(SEATS);
            return builder.build();
        }
    }
}
