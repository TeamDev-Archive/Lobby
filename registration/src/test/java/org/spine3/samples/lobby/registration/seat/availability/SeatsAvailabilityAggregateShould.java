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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Throwables.propagate;
import static org.junit.Assert.*;
import static org.spine3.samples.lobby.common.util.IdFactory.*;
import static org.spine3.samples.lobby.registration.util.CollectionUtils.findById;
import static org.spine3.samples.lobby.registration.util.Seats.*;
import static org.spine3.util.Identifiers.newUuid;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "TypeMayBeWeakened", "UtilityClass",
        "MagicNumber", "ClassWithTooManyMethods", "UtilityClassWithoutPrivateConstructor", "RefusedBequest", "EmptyClass",
        "AbstractClassWithoutAbstractMethods", "NoopMethodInAbstractClass", "LocalVariableNamingConvention"})
public class SeatsAvailabilityAggregateShould {

    private final TestSeatsAvailabilityAggregate defaultAggregate = new TestCase().givenAggregate();

    /**
     * MakeSeatReservation command handling tests.
     */

    @Test
    public void handle_MakeSeatReservation_command_if_enough_seats_and_no_pending_reservations_exist() {
        final MakeSeatReservationCmdHandling testCase = new EnoughSeatsAndNoPendingReservations();
        makeSeatReservationCommandHandlingTest(testCase);
    }

    @Test
    public void handle_MakeSeatReservation_command_if_enough_seats_and_exist_pending_reservations() {
        final MakeSeatReservationCmdHandling testCase = new EnoughSeatsAndExistPendingReservations();
        makeSeatReservationCommandHandlingTest(testCase);
    }

    @Test
    public void handle_MakeSeatReservation_command_if_request_more_seats_than_available() {
        final MakeSeatReservationCmdHandling testCase = new NotEnoughSeatsAndNoPendingReservations();
        makeSeatReservationCommandHandlingTest(testCase);
    }

    private static void makeSeatReservationCommandHandlingTest(MakeSeatReservationCmdHandling testCase) {
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final MakeSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final SeatsReserved event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_MakeSeatReservation_command_and_throw_exception_if_it_is_empty() {
        final MakeSeatReservation cmd = MakeSeatReservation.getDefaultInstance();
        defaultAggregate.handle(cmd, CommandContext.getDefaultInstance());
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

    /**
     * CommitSeatReservation command handling tests.
     */

    @Test
    public void handle_CommitSeatReservation_command_if_exists_pending_reservation() {
        final CommitSeatReservationCmdHandling testCase = new CommitSeatReservationCmdHandling.ExistsPendingReservation();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final CommitSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final SeatsReservationCommitted event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_CommitSeatReservation_command_and_throw_exception_if_it_is_empty() {
        final CommitSeatReservation cmd = CommitSeatReservation.getDefaultInstance();
        defaultAggregate.handle(cmd, CommandContext.getDefaultInstance());
    }

    @Test(expected = IllegalStateException.class)
    public void handle_CommitSeatReservation_command_and_throw_exception_if_no_pending_reservation_exists() {
        final CommitSeatReservationCmdHandling testCase = new CommitSeatReservationCmdHandling.EmptyState();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final CommitSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        aggregate.handle(cmd, context);
    }

    /**
     * CancelSeatReservation command handling tests.
     */

    @Test
    public void handle_CancelSeatReservation_command_if_exists_pending_reservation() {
        final CancelSeatReservationCmdHandling testCase = new CancelSeatReservationCmdHandling.ExistsPendingReservation();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final CancelSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final SeatsReservationCancelled event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_CancelSeatReservation_command_and_throw_exception_if_it_is_empty() {
        final CancelSeatReservation cmd = CancelSeatReservation.getDefaultInstance();
        defaultAggregate.handle(cmd, CommandContext.getDefaultInstance());
    }

    @Test(expected = IllegalStateException.class)
    public void handle_CancelSeatReservation_command_and_throw_exception_if_no_pending_reservation_exists() {
        final CancelSeatReservationCmdHandling testCase = new CancelSeatReservationCmdHandling.EmptyState();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final CancelSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        aggregate.handle(cmd, context);
    }

    /**
     * AddSeats command handling tests.
     */

    @Test
    public void handle_AddSeats_command() {
        final AddSeatsCmdHandling testCase = new AddSeatsCmdHandling();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final AddSeats cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final AddedAvailableSeats event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_AddSeats_command_and_throw_exception_if_it_is_empty() {
        final AddSeats cmd = AddSeats.getDefaultInstance();
        defaultAggregate.handle(cmd, CommandContext.getDefaultInstance());
    }

    /**
     * RemoveSeats command handling tests.
     */

    @Test
    public void handle_RemoveSeats_command() {
        final RemoveSeatsCmdHandling testCase = new RemoveSeatsCmdHandling();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final RemoveSeats cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final RemovedAvailableSeats event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_RemoveSeats_command_and_throw_exception_if_no_such_available_seat_exists() {
        final RemoveSeatsCmdHandling testCase = new RemoveSeatsCmdHandling.EmptyState();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final RemoveSeats cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        aggregate.handle(cmd, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_RemoveSeats_command_and_throw_exception_if_it_is_empty() {
        final RemoveSeats cmd = RemoveSeats.getDefaultInstance();
        defaultAggregate.handle(cmd, CommandContext.getDefaultInstance());
    }

    /**
     * Event applying tests.
     */

    @Test
    public void apply_SeatsReserved_event_and_update_state() {
        final SeatsReservedEventApplying testCase = new SeatsReservedEventApplying();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final SeatsReserved event = testCase.givenEvent();
        final String reservationId = event.getReservationId().getUuid();

        aggregate.apply(event);

        final SeatsAvailability state = aggregate.getState();
        assertEquals(event.getAvailableSeatUpdatedList(), state.getAvailableSeatList());
        final SeatQuantities reservedSeats = state.getPendingReservations().get(reservationId);
        assertEquals(event.getReservedSeatUpdatedList(), reservedSeats.getItemList());
    }

    @Test
    public void apply_SeatsReservationCommitted_event_and_remove_pending_reservation() {
        final SeatsReservationCommittedEventApplying testCase = new SeatsReservationCommittedEventApplying();
        final SeatsReservationCommitted event = testCase.givenEvent();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();

        aggregate.apply(event);

        final Map<String, SeatQuantities> pendingReservations = aggregate.getState().getPendingReservations();
        assertEquals(0, pendingReservations.size());
    }

    @Test
    public void apply_SeatsReservationCancelled_event_and_remove_pending_reservation_and_update_available_seats() {
        final SeatsReservationCancelledEventApplying testCase = new SeatsReservationCancelledEventApplying();
        final SeatsReservationCancelled event = testCase.givenEvent();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();

        aggregate.apply(event);

        final SeatsAvailability state = aggregate.getState();
        final Map<String, SeatQuantities> pendingReservations = state.getPendingReservations();
        assertEquals(0, pendingReservations.size());

        final List<SeatQuantity> availableSeatsExpected = event.getAvailableSeatUpdatedList();
        final List<SeatQuantity> availableSeatsActual = state.getAvailableSeatList();
        assertEquals(availableSeatsExpected, availableSeatsActual);
    }

    @Test
    public void apply_AddedAvailableSeats_event_and_add_new_seat_type_if_no_such_type_existed() {
        final AddedAvailableSeatsEventApplying testCase = new AddedAvailableSeatsEventApplying.AddingNewSeatType();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final AddedAvailableSeats event = testCase.givenEvent();

        aggregate.apply(event);

        final SeatsAvailability state = aggregate.getState();
        final SeatQuantity addedQuantity = event.getQuantity();
        final List<SeatQuantity> availableSeatsUpdated = state.getAvailableSeatList();
        assertTrue(availableSeatsUpdated.contains(addedQuantity));
    }

    @Test
    public void apply_AddedAvailableSeats_event_and_update_existing_seat_type() {
        final AddedAvailableSeatsEventApplying testCase = new AddedAvailableSeatsEventApplying.UpdatingSeatType();
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final AddedAvailableSeats event = testCase.givenEvent();

        aggregate.apply(event);

        final List<SeatQuantity> availableSeatsUpdated = aggregate.getState().getAvailableSeatList();
        final SeatQuantity primarySeat = testCase.getPrimarySeat();
        final SeatQuantity addedQuantity = event.getQuantity();
        final int expectedNewQuantity = primarySeat.getQuantity() + addedQuantity.getQuantity();
        final SeatQuantity expectedResult = newSeatQuantity(primarySeat.getSeatTypeId(), expectedNewQuantity);
        assertTrue(availableSeatsUpdated.contains(expectedResult));
    }

    @Test
    public void apply_RemovedAvailableSeats_event_when_removing_less_seats_than_remaining() {
        final RemovedAvailableSeatsEventApplying testCase =
                new RemovedAvailableSeatsEventApplying.RemovingLessSeatsThanRemaining();
        final int removedQuantity = testCase.givenEvent().getQuantity().getQuantity();
        final int expectedNewQuantity = testCase.getPrimarySeat().getQuantity() - removedQuantity;

        applyRemovedAvailableSeatsEventTest(testCase, expectedNewQuantity);
    }

    @Test
    public void apply_RemovedAvailableSeats_event_when_removing_more_seats_than_remaining() {
        final RemovedAvailableSeatsEventApplying testCase =
                new RemovedAvailableSeatsEventApplying.RemovingMoreSeatsThanRemaining();
        final int expectedNewQuantity = 0;
        applyRemovedAvailableSeatsEventTest(testCase, expectedNewQuantity);
    }

    private static void applyRemovedAvailableSeatsEventTest(RemovedAvailableSeatsEventApplying testCase,
                                                            int expectedNewQuantity) {
        final TestSeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final RemovedAvailableSeats event = testCase.givenEvent();

        aggregate.apply(event);

        final List<SeatQuantity> availableSeatsUpdated = aggregate.getState().getAvailableSeatList();
        final SeatQuantity primaryQuantity = testCase.getPrimarySeat();
        final SeatQuantity expectedResult = newSeatQuantity(primaryQuantity.getSeatTypeId(), expectedNewQuantity);
        assertTrue(availableSeatsUpdated.contains(expectedResult));
    }

    /**
     * Test cases.
     */

    private static class TestCase {

        private final TestSeatsAvailabilityAggregate aggregate;

        protected TestCase() {
            final SeatsAvailabilityId id = newSeatsAvailabilityId();
            aggregate = new TestSeatsAvailabilityAggregate(id);
        }

        protected TestSeatsAvailabilityAggregate givenAggregate() {
            return aggregate;
        }
    }

    private abstract static class MakeSeatReservationCmdHandling extends TestCase {

        protected static final ConferenceId CONFERENCE_ID = newConferenceId();
        protected static final ReservationId RESERVATION_ID = newReservationId();

        protected static final SeatTypeId MAIN_SEAT_TYPE_ID = newSeatTypeId("main-" + newUuid());
        protected static final SeatTypeId WORKSHOP_SEAT_TYPE_ID = newSeatTypeId("workshop-" + newUuid());

        private static final int MAIN_SEAT_COUNT_AVAILABLE = 100;
        private static final int WORKSHOP_SEAT_COUNT_AVAILABLE = 70;

        private static final List<SeatQuantity> AVAILABLE_SEATS = ImmutableList.of(
                newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_AVAILABLE),
                newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_AVAILABLE));

        protected List<SeatQuantity> getAvailableSeats() {
            //noinspection ReturnOfCollectionOrArrayField
            return AVAILABLE_SEATS;
        }

        protected static class Command {

            static final int MAIN_SEAT_COUNT_REQUESTED = 10;
            static final int WORKSHOP_SEAT_COUNT_REQUESTED = 7;

            static final MakeSeatReservation MAKE_SEAT_RESERVATION = MakeSeatReservation.newBuilder()
                    .setConferenceId(CONFERENCE_ID)
                    .setReservationId(RESERVATION_ID)
                    .addSeat(newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_REQUESTED))
                    .addSeat(newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_REQUESTED))
                    .build();
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
            final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
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
            final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
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
            final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
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

    private abstract static class CommitSeatReservationCmdHandling extends TestCase {

        private static final ReservationId RESERVATION_ID = newReservationId();

        private static final CommitSeatReservation COMMIT_SEAT_RESERVATION = CommitSeatReservation.newBuilder()
                .setReservationId(RESERVATION_ID)
                .build();

        protected CommitSeatReservation givenCommand() {
            return COMMIT_SEAT_RESERVATION;
        }

        protected CommandContext givenCommandContext() {
            return CommandContext.getDefaultInstance();
        }

        protected void validateResult(SeatsReservationCommitted event, CommitSeatReservation cmd) {
            assertEquals(cmd.getReservationId(), event.getReservationId());
        }

        private static class ExistsPendingReservation extends CommitSeatReservationCmdHandling {

            private static final ImmutableMap<String, SeatQuantities> PENDING_RESERVATIONS =
                    ImmutableMap.<String, SeatQuantities>builder()
                            .put(RESERVATION_ID.getUuid(), newSeatQuantities(newSeatQuantity(20))).build();

            @Override
            protected TestSeatsAvailabilityAggregate givenAggregate() {
                final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
                final SeatsAvailability state = aggregate.getState().toBuilder()
                        .putAllPendingReservations(PENDING_RESERVATIONS)
                        .build();
                aggregate.incrementState(state);
                return aggregate;
            }
        }

        static class EmptyState extends CommitSeatReservationCmdHandling {
        }
    }

    private abstract static class CancelSeatReservationCmdHandling extends TestCase {

        private static final ReservationId RESERVATION_ID = newReservationId();

        private static final CancelSeatReservation CANCEL_SEAT_RESERVATION = CancelSeatReservation.newBuilder()
                .setReservationId(RESERVATION_ID)
                .setConferenceId(newConferenceId())
                .build();

        protected CancelSeatReservation givenCommand() {
            return CANCEL_SEAT_RESERVATION;
        }

        protected CommandContext givenCommandContext() {
            return CommandContext.getDefaultInstance();
        }

        protected void validateResult(SeatsReservationCancelled event, CancelSeatReservation cmd) {
        }

        private static class ExistsPendingReservation extends CancelSeatReservationCmdHandling {

            private static final SeatQuantities TMP_RESERVED_SEATS = newSeatQuantities(
                    newSeatQuantity(10), newSeatQuantity(20));

            private static final ImmutableMap<String, SeatQuantities> PENDING_RESERVATIONS =
                    ImmutableMap.<String, SeatQuantities>builder()
                            .put(RESERVATION_ID.getUuid(), TMP_RESERVED_SEATS).build();

            @Override
            protected TestSeatsAvailabilityAggregate givenAggregate() {
                final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
                final SeatsAvailability state = aggregate.getState().toBuilder()
                        .putAllPendingReservations(PENDING_RESERVATIONS)
                        .build();
                aggregate.incrementState(state);
                return aggregate;
            }

            @Override
            protected void validateResult(SeatsReservationCancelled event, CancelSeatReservation cmd) {
                assertEquals(cmd.getReservationId(), event.getReservationId());
                assertEquals(cmd.getConferenceId(), event.getConferenceId());

                final List<SeatQuantity> availableSeatsActual = event.getAvailableSeatUpdatedList();
                assertEquals(TMP_RESERVED_SEATS.getItemList(), availableSeatsActual);
            }
        }

        static class EmptyState extends CancelSeatReservationCmdHandling {
        }
    }

    private static class AddSeatsCmdHandling extends TestCase {

        private static final AddSeats ADD_SEATS = AddSeats.newBuilder()
                .setQuantity(newSeatQuantity(5))
                .setConferenceId(newConferenceId())
                .build();

        protected AddSeats givenCommand() {
            return ADD_SEATS;
        }

        protected CommandContext givenCommandContext() {
            return CommandContext.getDefaultInstance();
        }

        @Override
        protected TestSeatsAvailabilityAggregate givenAggregate() {
            final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
            final SeatsAvailability state = aggregate.getState().toBuilder()
                    .addAvailableSeat(newSeatQuantity(20))
                    .build();
            aggregate.incrementState(state);
            return aggregate;
        }

        protected void validateResult(AddedAvailableSeats event, AddSeats cmd) {
            final SeatQuantity quantityToAdd = cmd.getQuantity();
            final SeatQuantity quantityAdded = event.getQuantity();
            assertEquals(quantityToAdd, quantityAdded);
        }
    }

    private static class RemoveSeatsCmdHandling extends TestCase {

        private static final SeatTypeId SEAT_TYPE_ID = newSeatTypeId();
        private static final int SEAT_QUANTITY_TO_REMOVE = 20;

        private static final SeatQuantity AVAILABLE_QUANTITY = newSeatQuantity(SEAT_TYPE_ID, 80);

        private static final RemoveSeats REMOVE_SEATS = RemoveSeats.newBuilder()
                .setQuantity(newSeatQuantity(SEAT_TYPE_ID, SEAT_QUANTITY_TO_REMOVE))
                .setConferenceId(newConferenceId())
                .build();

        protected RemoveSeats givenCommand() {
            return REMOVE_SEATS;
        }

        protected CommandContext givenCommandContext() {
            return CommandContext.getDefaultInstance();
        }

        @Override
        protected TestSeatsAvailabilityAggregate givenAggregate() {
            final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
            final SeatsAvailability state = aggregate.getState().toBuilder()
                    .addAvailableSeat(AVAILABLE_QUANTITY)
                    .build();
            aggregate.incrementState(state);
            return aggregate;
        }

        protected void validateResult(RemovedAvailableSeats event, RemoveSeats cmd) {
            assertEquals(cmd.getQuantity(), event.getQuantity());
        }

        static class EmptyState extends RemoveSeatsCmdHandling {
            @Override
            protected TestSeatsAvailabilityAggregate givenAggregate() {
                return new TestSeatsAvailabilityAggregate(newSeatsAvailabilityId());
            }
        }
    }

    private static class ExistAvailableSeatsAndPendingReservations extends TestCase {

        private static final ReservationId RESERVATION_ID = newReservationId();

        private static final SeatQuantities RESERVED_SEATS = newSeatQuantities(
                newSeatQuantity(56), newSeatQuantity(38));

        private static final ImmutableMap<String, SeatQuantities> PENDING_RESERVATIONS = ImmutableMap.<String, SeatQuantities>builder()
                .put(RESERVATION_ID.getUuid(), RESERVED_SEATS).build();

        private static final List<SeatQuantity> AVAILABLE_SEATS = ImmutableList.of(
                newSeatQuantity(110),
                newSeatQuantity(220));

        @Override
        protected TestSeatsAvailabilityAggregate givenAggregate() {
            final TestSeatsAvailabilityAggregate aggregate = super.givenAggregate();
            final SeatsAvailability state = aggregate.getState().toBuilder()
                    .addAllAvailableSeat(AVAILABLE_SEATS)
                    .putAllPendingReservations(PENDING_RESERVATIONS)
                    .build();
            aggregate.incrementState(state);
            return aggregate;
        }

        protected static List<SeatQuantity> getAvailableSeats() {
            //noinspection ReturnOfCollectionOrArrayField
            return AVAILABLE_SEATS;
        }

        protected static SeatQuantities getReservedSeats() {
            return RESERVED_SEATS;
        }

        protected static ReservationId getReservationId() {
            return RESERVATION_ID;
        }
    }

    private static class SeatsReservedEventApplying extends ExistAvailableSeatsAndPendingReservations {

        private static final SeatsReserved SEATS_RESERVED = SeatsReserved.newBuilder()
                .setConferenceId(newConferenceId())
                .setReservationId(getReservationId())
                .addReservedSeatUpdated(newSeatQuantity(256))
                .addAvailableSeatUpdated(newSeatQuantity(24)).build();

        static {
            assertNotEquals(getAvailableSeats(), SEATS_RESERVED.getAvailableSeatUpdatedList());
            assertNotEquals(getReservedSeats(), SEATS_RESERVED.getReservedSeatUpdatedList());
        }

        public SeatsReserved givenEvent() {
            return SEATS_RESERVED;
        }
    }

    private static class SeatsReservationCommittedEventApplying extends ExistAvailableSeatsAndPendingReservations {

        private static final SeatsReservationCommitted SEATS_RESERVATION_COMMITTED = SeatsReservationCommitted.newBuilder()
                .setReservationId(getReservationId()).build();

        public SeatsReservationCommitted givenEvent() {
            return SEATS_RESERVATION_COMMITTED;
        }
    }

    private static class SeatsReservationCancelledEventApplying extends ExistAvailableSeatsAndPendingReservations {

        private static final SeatsReservationCancelled SEATS_RESERVATION_CANCELLED = SeatsReservationCancelled.newBuilder()
                .setReservationId(getReservationId())
                .setConferenceId(newConferenceId())
                .addAvailableSeatUpdated(newSeatQuantity(32))
                .build();

        static {
            assertNotEquals(getAvailableSeats(), SEATS_RESERVATION_CANCELLED.getAvailableSeatUpdatedList());
        }

        public SeatsReservationCancelled givenEvent() {
            return SEATS_RESERVATION_CANCELLED;
        }
    }

    private abstract static class AddedAvailableSeatsEventApplying extends ExistAvailableSeatsAndPendingReservations {

        private static final SeatQuantity PRIMARY_SEAT = getAvailableSeats().get(0);

        protected SeatQuantity getPrimarySeat() {
            return PRIMARY_SEAT;
        }

        protected AddedAvailableSeats givenEvent() {
            final AddedAvailableSeats event = AddedAvailableSeats.newBuilder()
                    .setQuantity(getQuantityToAdd())
                    .build();
            return event;
        }

        protected abstract SeatQuantity getQuantityToAdd();

        private static class AddingNewSeatType extends AddedAvailableSeatsEventApplying {

            private final SeatQuantity quantityToAdd = newSeatQuantity(12);

            protected AddingNewSeatType() {
                assertFalse(getAvailableSeats().contains(quantityToAdd));
            }

            @Override
            protected SeatQuantity getQuantityToAdd() {
                return quantityToAdd;
            }
        }

        private static class UpdatingSeatType extends AddedAvailableSeatsEventApplying {

            private final SeatQuantity quantityToAdd = newSeatQuantity(getPrimarySeat().getSeatTypeId(), 10);

            @Override
            protected SeatQuantity getQuantityToAdd() {
                return quantityToAdd;
            }
        }
    }

    private abstract static class RemovedAvailableSeatsEventApplying extends ExistAvailableSeatsAndPendingReservations {

        private SeatQuantity primarySeat;

        protected RemovedAvailableSeats givenEvent() {
            primarySeat = getAvailableSeats().get(0);
            final int quantityToRemove = getQuantityToRemove();
            final RemovedAvailableSeats event = RemovedAvailableSeats.newBuilder()
                    .setQuantity(newSeatQuantity(primarySeat.getSeatTypeId(), quantityToRemove))
                    .build();
            return event;
        }

        protected SeatQuantity getPrimarySeat() {
            return primarySeat;
        }

        protected abstract int getQuantityToRemove();

        private static class RemovingLessSeatsThanRemaining extends RemovedAvailableSeatsEventApplying {

            @Override
            protected int getQuantityToRemove() {
                final int quantityToRemove = getPrimarySeat().getQuantity() / 2;
                return quantityToRemove;
            }
        }

        private static class RemovingMoreSeatsThanRemaining extends RemovedAvailableSeatsEventApplying {

            @Override
            protected int getQuantityToRemove() {
                final int quantityToRemove = getPrimarySeat().getQuantity() + 5;
                return quantityToRemove;
            }
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

        void apply(SeatsReservationCommitted event) {
            invokeApplyMethod(event);
        }

        void apply(SeatsReservationCancelled event) {
            invokeApplyMethod(event);
        }

        void apply(AddedAvailableSeats event) {
            invokeApplyMethod(event);
        }

        void apply(RemovedAvailableSeats event) {
            invokeApplyMethod(event);
        }

        private void invokeApplyMethod(Message event) {
            try {
                //noinspection DuplicateStringLiteralInspection
                final Method apply = SeatsAvailabilityAggregate.class.getDeclaredMethod("apply", event.getClass());
                apply.setAccessible(true);
                apply.invoke(this, event);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                propagate(e);
            }
        }
    }
}
