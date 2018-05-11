/*
 * Copyright 2015, TeamDev. All rights reserved.
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

import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.testcase.AddSeatsCmdHandling;
import org.spine3.samples.lobby.registration.seat.availability.testcase.AddedAvailableSeatsEventApplying;
import org.spine3.samples.lobby.registration.seat.availability.testcase.CancelSeatReservationCmdHandling;
import org.spine3.samples.lobby.registration.seat.availability.testcase.CommitSeatReservationCmdHandling;
import org.spine3.samples.lobby.registration.seat.availability.testcase.EnoughSeatsAndExistPendingReservations;
import org.spine3.samples.lobby.registration.seat.availability.testcase.EnoughSeatsAndNoPendingReservations;
import org.spine3.samples.lobby.registration.seat.availability.testcase.MakeSeatReservationCmdHandling;
import org.spine3.samples.lobby.registration.seat.availability.testcase.NotEnoughSeatsAndNoPendingReservations;
import org.spine3.samples.lobby.registration.seat.availability.testcase.RemoveSeatsCmdHandling;
import org.spine3.samples.lobby.registration.seat.availability.testcase.RemovedAvailableSeatsEventApplying;
import org.spine3.samples.lobby.registration.seat.availability.testcase.SeatsReservationCancelledEventApplying;
import org.spine3.samples.lobby.registration.seat.availability.testcase.SeatsReservationCommittedEventApplying;
import org.spine3.samples.lobby.registration.seat.availability.testcase.SeatsReservedEventApplying;
import org.spine3.samples.lobby.registration.seat.availability.testcase.TestCase;
import org.spine3.samples.lobby.registration.util.EventImporter;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "MagicNumber", "ClassWithTooManyMethods",
        "OverlyCoupledClass"})
public class SeatsAvailabilityAggregateShould {

    private static final CommandContext CMD_CONTEXT = CommandContext.getDefaultInstance();

    private final SeatsAvailabilityAggregate defaultAggregate = new TestCase().givenAggregate();

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
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final MakeSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final SeatsReserved event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_MakeSeatReservation_command_and_throw_exception_if_it_is_empty() {
        final MakeSeatReservation cmd = MakeSeatReservation.getDefaultInstance();
        defaultAggregate.handle(cmd, CMD_CONTEXT);
    }

    @Test
    public void calculate_new_reserved_seat_count_if_request_less_seats_than_available() {
        final MakeSeatReservationCommandHandler handler =
                new MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 100;
        final int requestedCount = 20;
        final int reservedCount = 10;

        final int newReservedSeatCount = handler.calculateNewReservedSeatCount(availableCount, requestedCount, reservedCount);

        assertEquals(30, newReservedSeatCount);
    }

    @Test
    public void calculate_new_reserved_seat_count_if_request_all_available_seats() {
        final MakeSeatReservationCommandHandler handler =
                new MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 30;
        final int requestedCount = 30;
        final int reservedCount = 10;

        final int newReservedSeatCount = handler.calculateNewReservedSeatCount(availableCount, requestedCount, reservedCount);

        assertEquals(40, newReservedSeatCount);
    }

    @Test
    public void calculate_new_reserved_seat_count_if_request_more_seats_than_available() {
        final MakeSeatReservationCommandHandler handler =
                new MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 20;
        final int requestedCount = 30;
        final int reservedCount = 5;

        final int newReservedSeatCount = handler.calculateNewReservedSeatCount(availableCount, requestedCount, reservedCount);

        assertEquals(25, newReservedSeatCount);
    }

    @Test
    public void calculate_new_available_seat_count_if_reserved_less_seats_than_available() {
        final MakeSeatReservationCommandHandler handler =
                new MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 100;
        final int oldReservedCount = 10;
        final int newReservedCount = 20;

        final int newAvailableSeatCount = handler.calculateNewAvailableSeatCount(availableCount, oldReservedCount, newReservedCount);

        assertEquals(90, newAvailableSeatCount);
    }

    @Test
    public void calculate_new_available_seat_count_if_reserved_all_available_seats() {
        final MakeSeatReservationCommandHandler handler =
                new MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
        final int availableCount = 30;
        final int oldReservedCount = 10;
        final int newReservedCount = 40;

        final int newAvailableSeatCount = handler.calculateNewAvailableSeatCount(availableCount, oldReservedCount, newReservedCount);

        assertEquals(0, newAvailableSeatCount);
    }

    @Test
    public void calculate_new_available_seat_count_if_reserved_more_seats_than_available() {
        final MakeSeatReservationCommandHandler handler =
                new MakeSeatReservationCommandHandler(SeatsAvailability.getDefaultInstance());
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
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final CommitSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final SeatsReservationCommitted event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_CommitSeatReservation_command_and_throw_exception_if_it_is_empty() {
        final CommitSeatReservation cmd = CommitSeatReservation.getDefaultInstance();
        defaultAggregate.handle(cmd, CMD_CONTEXT);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_CommitSeatReservation_command_and_throw_exception_if_no_pending_reservation_exists() {
        final CommitSeatReservationCmdHandling testCase = new CommitSeatReservationCmdHandling.EmptyState();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
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
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final CancelSeatReservation cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final SeatsReservationCancelled event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_CancelSeatReservation_command_and_throw_exception_if_it_is_empty() {
        final CancelSeatReservation cmd = CancelSeatReservation.getDefaultInstance();
        defaultAggregate.handle(cmd, CMD_CONTEXT);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_CancelSeatReservation_command_and_throw_exception_if_no_pending_reservation_exists() {
        final CancelSeatReservationCmdHandling testCase = new CancelSeatReservationCmdHandling.EmptyState();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
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
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final AddSeats cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final AddedAvailableSeats event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_AddSeats_command_and_throw_exception_if_it_is_empty() {
        final AddSeats cmd = AddSeats.getDefaultInstance();
        defaultAggregate.handle(cmd, CMD_CONTEXT);
    }

    /**
     * RemoveSeats command handling tests.
     */

    @Test
    public void handle_RemoveSeats_command() {
        final RemoveSeatsCmdHandling testCase = new RemoveSeatsCmdHandling();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final RemoveSeats cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        final RemovedAvailableSeats event = aggregate.handle(cmd, context);

        testCase.validateResult(event, cmd);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_RemoveSeats_command_and_throw_exception_if_no_such_available_seat_exists() {
        final RemoveSeatsCmdHandling testCase = new RemoveSeatsCmdHandling.EmptyState();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final RemoveSeats cmd = testCase.givenCommand();
        final CommandContext context = testCase.givenCommandContext();

        aggregate.handle(cmd, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_RemoveSeats_command_and_throw_exception_if_it_is_empty() {
        final RemoveSeats cmd = RemoveSeats.getDefaultInstance();
        defaultAggregate.handle(cmd, CMD_CONTEXT);
    }

    /**
     * Event applying tests.
     */

    @Test
    public void apply_SeatsReserved_event_and_update_state() {
        final SeatsReservedEventApplying testCase = new SeatsReservedEventApplying();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final SeatsReserved event = testCase.givenEvent();
        final String reservationId = event.getReservationId()
                                          .getUuid();

        EventImporter.apply(aggregate, event, CMD_CONTEXT);

        final SeatsAvailability state = aggregate.getState();
        assertEquals(event.getAvailableSeatUpdatedList(), state.getAvailableSeatList());
        final SeatQuantities reservedSeats = state.getPendingReservations()
                                                  .get(reservationId);
        assertEquals(event.getReservedSeatUpdatedList(), reservedSeats.getItemList());
    }

    @Test
    public void apply_SeatsReservationCommitted_event_and_remove_pending_reservation() {
        final SeatsReservationCommittedEventApplying testCase = new SeatsReservationCommittedEventApplying();
        final SeatsReservationCommitted event = testCase.givenEvent();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();

        EventImporter.apply(aggregate, event, CMD_CONTEXT);

        final Map<String, SeatQuantities> pendingReservations = aggregate.getState()
                                                                         .getPendingReservations();
        assertEquals(0, pendingReservations.size());
    }

    @Test
    public void apply_SeatsReservationCancelled_event_and_remove_pending_reservation_and_update_available_seats() {
        final SeatsReservationCancelledEventApplying testCase = new SeatsReservationCancelledEventApplying();
        final SeatsReservationCancelled event = testCase.givenEvent();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();

        EventImporter.apply(aggregate, event, CMD_CONTEXT);

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
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final AddedAvailableSeats event = testCase.givenEvent();

        EventImporter.apply(aggregate, event, CMD_CONTEXT);

        final SeatsAvailability state = aggregate.getState();
        final SeatQuantity addedQuantity = event.getQuantity();
        final List<SeatQuantity> availableSeatsUpdated = state.getAvailableSeatList();
        assertTrue(availableSeatsUpdated.contains(addedQuantity));
    }

    @Test
    public void apply_AddedAvailableSeats_event_and_update_existing_seat_type() {
        final AddedAvailableSeatsEventApplying testCase = new AddedAvailableSeatsEventApplying.UpdatingSeatType();
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final AddedAvailableSeats event = testCase.givenEvent();

        EventImporter.apply(aggregate, event, CMD_CONTEXT);

        final List<SeatQuantity> availableSeatsUpdated = aggregate.getState()
                                                                  .getAvailableSeatList();
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
        final int removedQuantity = testCase.givenEvent()
                                            .getQuantity()
                                            .getQuantity();
        final int expectedNewQuantity = testCase.getPrimarySeat()
                                                .getQuantity() - removedQuantity;

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
        final SeatsAvailabilityAggregate aggregate = testCase.givenAggregate();
        final RemovedAvailableSeats event = testCase.givenEvent();

        EventImporter.apply(aggregate, event, CMD_CONTEXT);

        final List<SeatQuantity> availableSeatsUpdated = aggregate.getState()
                                                                  .getAvailableSeatList();
        final SeatQuantity primaryQuantity = testCase.getPrimarySeat();
        final SeatQuantity expectedResult = newSeatQuantity(primaryQuantity.getSeatTypeId(), expectedNewQuantity);
        assertTrue(availableSeatsUpdated.contains(expectedResult));
    }
}
