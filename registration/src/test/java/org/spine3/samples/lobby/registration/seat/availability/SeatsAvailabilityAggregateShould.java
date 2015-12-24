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

package org.spine3.samples.lobby.registration.seat.availability;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Message;
import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.order.OrderAggregate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Throwables.propagate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.spine3.samples.lobby.common.util.CommonMessageFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.CommonMessageFactory.newReservationId;
import static org.spine3.samples.lobby.registration.util.CollectionUtils.findById;
import static org.spine3.samples.lobby.registration.util.MessageFactory.*;
import static org.spine3.util.Identifiers.newUuid;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "TypeMayBeWeakened", "UtilityClass", "OverlyCoupledClass",
        "MagicNumber", "ClassWithTooManyMethods", "UtilityClassWithoutPrivateConstructor", "RefusedBequest",
        "LocalVariableNamingConvention", "MethodParameterNamingConvention" /*long precise names are required*/})
public class SeatsAvailabilityAggregateShould {

    @Test
    public void handle_MakeSeatReservation_command_if_enough_seats_and_no_pending_reservations_exist() {
        final MakeSeatReservationCmdHandling testCase = 
                new EnoughSeatsAndNoPendingReservations();
        makeSeatReservationCommandHandlingTest(testCase);
    }

    @Test
    public void handle_MakeSeatReservation_command_if_enough_seats_and_exist_pending_reservations() {
        final MakeSeatReservationCmdHandling testCase = 
                new EnoughSeatsAndExistPendingReservations();
        makeSeatReservationCommandHandlingTest(testCase);
    }

    @Test
    public void handle_MakeSeatReservation_command_if_request_more_seats_than_available() {
        final MakeSeatReservationCmdHandling testCase = 
                new NotEnoughSeatsAndNoPendingReservations();
        makeSeatReservationCommandHandlingTest(testCase);
    }

    private static void makeSeatReservationCommandHandlingTest(MakeSeatReservationCmdHandling testCase) {
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final MakeSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final SeatsReserved event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test
    public void calculate_new_reserved_seat_count_if_request_less_seats_than_available() {
        final SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler handler =
                new SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 100;
        final int requestedCount = 20;
        final int reservedCount = 10;

        final int newReservedSeatCount = handler.calculateNewReservedSeatCount(availableCount, requestedCount, reservedCount);

        assertEquals(30, newReservedSeatCount);
    }

    @Test
    public void calculate_new_reserved_seat_count_if_request_all_available_seats() {
        final SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler handler =
                new SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 30;
        final int requestedCount = 30;
        final int reservedCount = 10;

        final int newReservedSeatCount = handler.calculateNewReservedSeatCount(availableCount, requestedCount, reservedCount);

        assertEquals(40, newReservedSeatCount);
    }

    @Test
    public void calculate_new_reserved_seat_count_if_request_more_seats_than_available() {
        final SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler handler =
                new SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 20;
        final int requestedCount = 30;
        final int reservedCount = 5;

        final int newReservedSeatCount = handler.calculateNewReservedSeatCount(availableCount, requestedCount, reservedCount);

        assertEquals(25, newReservedSeatCount);
    }

    @Test
    public void calculate_new_available_seat_count_if_reserved_less_seats_than_available() {
        final SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler handler =
                new SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 100;
        final int oldReservedCount = 10;
        final int newReservedCount = 20;

        final int newAvailableSeatCount = handler.calculateNewAvailableSeatCount(availableCount, oldReservedCount, newReservedCount);

        assertEquals(90, newAvailableSeatCount);
    }

    @Test
    public void calculate_new_available_seat_count_if_reserved_all_available_seats() {
        final SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler handler =
                new SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 30;
        final int oldReservedCount = 10;
        final int newReservedCount = 40;

        final int newAvailableSeatCount = handler.calculateNewAvailableSeatCount(availableCount, oldReservedCount, newReservedCount);

        assertEquals(0, newAvailableSeatCount);
    }

    @Test
    public void calculate_new_available_seat_count_if_reserved_more_seats_than_available() {
        final SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler handler =
                new SeatsAvailabilityAggregate.MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 30;
        final int oldReservedCount = 10;
        final int newReservedCount = 999;

        final int newAvailableSeatCount = handler.calculateNewAvailableSeatCount(availableCount, oldReservedCount, newReservedCount);

        assertEquals(0, newAvailableSeatCount);
    }

    private abstract static class TestCase {

        private final TestSeatsAvailabilityAggregate aggregate;

        protected TestCase() {
            final SeatsAvailabilityId id = newSeatsAvailabilityId();
            aggregate = new TestSeatsAvailabilityAggregate(id);
        }

        protected TestSeatsAvailabilityAggregate aggregate() {
            return aggregate;
        }

        protected abstract TestSeatsAvailabilityAggregate givenAggregate();
    }

    private abstract static class MakeSeatReservationCmdHandling extends TestCase {

        static final ConferenceId CONFERENCE_ID = newConferenceId();
        static final ReservationId RESERVATION_ID = newReservationId();

        static final SeatTypeId MAIN_SEAT_TYPE_ID = newSeatTypeId("main-" + newUuid());
        static final SeatTypeId WORKSHOP_SEAT_TYPE_ID = newSeatTypeId("workshop-" + newUuid());

        private static final int MAIN_SEAT_COUNT_AVAILABLE = 100;
        private static final int WORKSHOP_SEAT_COUNT_AVAILABLE = 70;

        private static final List<SeatQuantity> AVAILABLE_SEATS = ImmutableList.of(
                newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_AVAILABLE),
                newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_AVAILABLE));

        protected List<SeatQuantity> getAvailableSeats() {
            //noinspection ReturnOfCollectionOrArrayField
            return AVAILABLE_SEATS;
        }

        static class Command {

            static final int MAIN_SEAT_COUNT_REQUESTED = 10;
            static final int WORKSHOP_SEAT_COUNT_REQUESTED = 7;

            static final MakeSeatReservation MAKE_SEAT_RESERVATION = MakeSeatReservation.newBuilder()
                    .setConferenceId(CONFERENCE_ID)
                    .setReservationId(RESERVATION_ID)
                    .addSeat(newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_REQUESTED))
                    .addSeat(newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_REQUESTED))
                    .build();
        }

        static class Event {

            static SeatsReserved seatsReserved(Iterable<SeatQuantity> reservedSeatsUpdated,
                                               Iterable<SeatQuantity> availableSeatsUpdated) {
                final SeatsReserved.Builder result = SeatsReserved.newBuilder()
                        .setConferenceId(CONFERENCE_ID)
                        .setReservationId(RESERVATION_ID)
                        .addAllReservedSeatUpdated(reservedSeatsUpdated)
                        .addAllAvailableSeatUpdated(availableSeatsUpdated);
                return result.build();
            }
        }
        
        protected MakeSeatReservation givenCommand() {
            return Command.MAKE_SEAT_RESERVATION;
        }
        
        protected CommandContext givenCommandContext() {
            return CommandContext.getDefaultInstance();
        }

        protected void validateResult(SeatsReserved event, MakeSeatReservation cmd) {
            assertEquals(cmd.getConferenceId(), event.getConferenceId());
            assertEquals(cmd.getReservationId(), event.getReservationId());

            final List<SeatQuantity> requestedSeats = cmd.getSeatList();
            final List<SeatQuantity> reservedSeats = event.getReservedSeatUpdatedList();
            checkReservedSeatsUpdated(reservedSeats, requestedSeats);

            final List<SeatQuantity> availableSeatsUpdated = event.getAvailableSeatUpdatedList();
            checkAvailableSeatsUpdated(availableSeatsUpdated, requestedSeats);
        }

        protected abstract void checkReservedSeatsUpdated(List<SeatQuantity> reservedSeats, List<SeatQuantity> requestedSeats);

        protected void checkAvailableSeatsUpdated(List<SeatQuantity> availableSeats, List<SeatQuantity> requestedSeats) {
            assertEquals(requestedSeats.size(), availableSeats.size());

            final int expectedMainSeatCount = MAIN_SEAT_COUNT_AVAILABLE - Command.MAIN_SEAT_COUNT_REQUESTED;
            final SeatQuantity mainSeatAvailable = findById(availableSeats, MAIN_SEAT_TYPE_ID);
            assertEquals(expectedMainSeatCount, mainSeatAvailable.getQuantity());

            final int expectedWorkshopSeatCount = WORKSHOP_SEAT_COUNT_AVAILABLE - Command.WORKSHOP_SEAT_COUNT_REQUESTED;
            final SeatQuantity workshopSeatAvailable = findById(availableSeats, WORKSHOP_SEAT_TYPE_ID);
            assertEquals(expectedWorkshopSeatCount, workshopSeatAvailable.getQuantity());
        }

        private static SeatTypeId newSeatTypeId(String uuid) {
            return SeatTypeId.newBuilder().setUuid(uuid).build();
        }
    }

    private static class EnoughSeatsAndNoPendingReservations extends MakeSeatReservationCmdHandling {

        @Override
        protected TestSeatsAvailabilityAggregate givenAggregate() {
            final TestSeatsAvailabilityAggregate aggregate = aggregate();
            final SeatsAvailability state = aggregate.getState().toBuilder()
                    .addAllAvailableSeat(getAvailableSeats())
                    .build();
            aggregate.incrementState(state);
            return aggregate;
        }

        @Override
        protected void checkReservedSeatsUpdated(List<SeatQuantity> reservedSeats, List<SeatQuantity> requestedSeats) {
            assertEquals(requestedSeats.size(), reservedSeats.size());
            assertTrue(requestedSeats.containsAll(reservedSeats));
        }
    }

    private static class EnoughSeatsAndExistPendingReservations extends MakeSeatReservationCmdHandling {

        private static final int MAIN_SEATS_PENDING_COUNT = 5;

        private static final int EXPECTED_RESERVED_MAIN_SEAT_COUNT =
                Command.MAIN_SEAT_COUNT_REQUESTED + MAIN_SEATS_PENDING_COUNT;

        private static final ImmutableMap<String, SeatQuantities> PENDING_RESERVATIONS =
                ImmutableMap.<String, SeatQuantities>builder()
                        .put(
                                RESERVATION_ID.getUuid(),
                                newSeatQuantities(newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEATS_PENDING_COUNT))
                        ).build();

        @Override
        protected TestSeatsAvailabilityAggregate givenAggregate() {
            final TestSeatsAvailabilityAggregate aggregate = aggregate();
            final SeatsAvailability state = aggregate.getState().toBuilder()
                    .addAllAvailableSeat(getAvailableSeats())
                    .putAllPendingReservations(PENDING_RESERVATIONS)
                    .build();
            aggregate.incrementState(state);
            return aggregate;
        }

        @Override
        protected void checkReservedSeatsUpdated(List<SeatQuantity> reservedSeats, List<SeatQuantity> requestedSeats) {
            assertEquals(requestedSeats.size(), reservedSeats.size());

            final SeatQuantity mainSeatReserved = findById(reservedSeats, MAIN_SEAT_TYPE_ID);
            assertEquals(EXPECTED_RESERVED_MAIN_SEAT_COUNT, mainSeatReserved.getQuantity());

            final SeatQuantity workshopSeatReserved = findById(reservedSeats, WORKSHOP_SEAT_TYPE_ID);
            assertTrue(requestedSeats.contains(workshopSeatReserved));
        }
    }

    private static class NotEnoughSeatsAndNoPendingReservations extends MakeSeatReservationCmdHandling {

        private static final int MAIN_SEAT_COUNT_AVAILABLE = 5;

        private static final int WORKSHOP_SEAT_COUNT_AVAILABLE = 30;
        private static final int EXPECTED_RESERVED_WORKSHOP_SEAT_COUNT =
                WORKSHOP_SEAT_COUNT_AVAILABLE - Command.WORKSHOP_SEAT_COUNT_REQUESTED;

        private static final List<SeatQuantity> AVAILABLE_SEATS = ImmutableList.of(
                newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_AVAILABLE),
                newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_AVAILABLE));

        @Override
        protected TestSeatsAvailabilityAggregate givenAggregate() {
            final TestSeatsAvailabilityAggregate aggregate = aggregate();
            final SeatsAvailability state = aggregate.getState().toBuilder()
                    .addAllAvailableSeat(AVAILABLE_SEATS)
                    .build();
            aggregate.incrementState(state);
            return aggregate;
        }

        @Override
        protected void checkReservedSeatsUpdated(List<SeatQuantity> reservedSeats, List<SeatQuantity> requestedSeats) {
            assertEquals(requestedSeats.size(), reservedSeats.size());

            final SeatQuantity mainSeatReserved = findById(reservedSeats, MAIN_SEAT_TYPE_ID);
            assertEquals(MAIN_SEAT_COUNT_AVAILABLE, mainSeatReserved.getQuantity());

            final SeatQuantity workshopSeatReserved = findById(reservedSeats, WORKSHOP_SEAT_TYPE_ID);
            assertTrue(requestedSeats.contains(workshopSeatReserved));
        }

        @Override
        protected void checkAvailableSeatsUpdated(List<SeatQuantity> availableSeats, List<SeatQuantity> requestedSeats) {
            assertEquals(requestedSeats.size(), availableSeats.size());

            final SeatQuantity mainSeatAvailable = findById(availableSeats, MAIN_SEAT_TYPE_ID);
            assertEquals(0, mainSeatAvailable.getQuantity());

            final SeatQuantity workshopSeatAvailable = findById(availableSeats, WORKSHOP_SEAT_TYPE_ID);
            assertEquals(EXPECTED_RESERVED_WORKSHOP_SEAT_COUNT, workshopSeatAvailable.getQuantity());
        }
    }

    public static class TestSeatsAvailabilityAggregate extends SeatsAvailabilityAggregate {

        public TestSeatsAvailabilityAggregate(SeatsAvailabilityId id) {
            super(id);
        }

        // Is overridden to make it accessible in tests.
        @Override
        public void incrementState(SeatsAvailability newState) {
            super.incrementState(newState);
        }

        void apply(SeatsReserved event) {
            invokeApplyMethod(event);
        }

        private void invokeApplyMethod(Message event) {
            try {
                //noinspection DuplicateStringLiteralInspection
                final Method apply = OrderAggregate.class.getDeclaredMethod("apply", event.getClass());
                apply.setAccessible(true);
                apply.invoke(this, event);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                propagate(e);
            }
        }
    }
}
