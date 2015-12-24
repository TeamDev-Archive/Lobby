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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.server.Assign;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.Apply;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;
import static org.spine3.samples.lobby.registration.util.CollectionUtils.findById;
import static org.spine3.samples.lobby.registration.util.MessageFactory.newSeatQuantities;
import static org.spine3.samples.lobby.registration.util.MessageFactory.newSeatQuantity;
import static org.spine3.samples.lobby.registration.util.ValidationUtils.*;

/**
 * The aggregate which manages the availability of conference seats.
 *
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened"/** "OrBuilder" parameters are not applicable*/, "OverlyCoupledClass"})
public class SeatsAvailabilityAggregate extends Aggregate<SeatsAvailabilityId, SeatsAvailability> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID type is not supported
     */
    public SeatsAvailabilityAggregate(SeatsAvailabilityId id) {
        super(id);
    }

    @Override
    protected SeatsAvailability getDefaultState() {
        return SeatsAvailability.getDefaultInstance();
    }

    @Assign
    public SeatsReserved handle(MakeSeatReservation cmd, CommandContext context) {
        Validator.validateCommand(cmd);

        final MakeSeatReservationCommandHandler handler = new MakeSeatReservationCommandHandler(getState());
        handler.handle(cmd);

        final SeatsReserved.Builder reserved = SeatsReserved.newBuilder()
                .setReservationId(cmd.getReservationId())
                .setConferenceId(cmd.getConferenceId())
                .addAllReservedSeatUpdated(handler.getReservedSeatsUpdated())
                .addAllAvailableSeatUpdated(handler.getAvailableSeatsUpdated());
        return reserved.build();
    }

    @Assign
    public SeatsReservationCommitted handle(CommitSeatReservation cmd, CommandContext context) {
        Validator.validateCommand(cmd);
        Validator.validateState(getState(), cmd);

        final SeatsReservationCommitted.Builder event = SeatsReservationCommitted.newBuilder()
                .setReservationId(cmd.getReservationId());
        return event.build();
    }

    @Assign
    public SeatsReservationCancelled handle(CancelSeatReservation cmd, CommandContext context) {
        Validator.validateCommand(cmd);
        Validator.validateState(getState(), cmd);

        final ReservationId reservationId = cmd.getReservationId();
        //noinspection LocalVariableNamingConvention
        final List<SeatQuantity> availableSeatsUpdated = newLinkedList(getState().getAvailableSeatList());
        final SeatQuantities unreservedSeats = getState().getPendingReservations().get(reservationId.getUuid());
        availableSeatsUpdated.addAll(unreservedSeats.getItemList());

        final SeatsReservationCancelled.Builder event = SeatsReservationCancelled.newBuilder()
                .setReservationId(reservationId)
                .setConferenceId(cmd.getConferenceId())
                .addAllAvailableSeatUpdated(availableSeatsUpdated);
        return event.build();
    }

    @Assign
    public AddedAvailableSeats handle(AddSeats cmd, CommandContext context) {
        Validator.validateCommand(cmd);

        final AddedAvailableSeats.Builder event = AddedAvailableSeats.newBuilder()
                .setQuantity(cmd.getQuantity());
        return event.build();
    }

    @Assign
    public RemovedAvailableSeats handle(RemoveSeats cmd, CommandContext context) {
        Validator.validateCommand(cmd);
        Validator.validateState(getState(), cmd);

        final RemovedAvailableSeats.Builder event = RemovedAvailableSeats.newBuilder()
                .setQuantity(cmd.getQuantity());
        return event.build();
    }

    @Apply
    private void apply(SeatsReserved event) {
        final SeatsAvailability.Builder state = getState().toBuilder();

        state.clearAvailableSeat();
        state.addAllAvailableSeat(event.getAvailableSeatUpdatedList());

        final Map<String, SeatQuantities> pendingReservations = state.getMutablePendingReservations();
        final String reservationId = event.getReservationId().getUuid();
        pendingReservations.put(reservationId, newSeatQuantities(event.getReservedSeatUpdatedList()));

        incrementState(state.build());
    }

    @Apply
    private void apply(SeatsReservationCommitted event) {
        final SeatsAvailability.Builder state = getState().toBuilder();

        final Map<String, SeatQuantities> pendingReservations = state.getMutablePendingReservations();
        final String reservationId = event.getReservationId().getUuid();
        pendingReservations.remove(reservationId);

        incrementState(state.build());
    }

    @Apply
    private void apply(SeatsReservationCancelled event) {
        final SeatsAvailability.Builder state = getState().toBuilder();

        final Map<String, SeatQuantities> pendingReservations = state.getMutablePendingReservations();
        final String reservationId = event.getReservationId().getUuid();
        pendingReservations.remove(reservationId);

        state.clearAvailableSeat();
        state.addAllAvailableSeat(event.getAvailableSeatUpdatedList());

        incrementState(state.build());
    }

    @Apply
    private void apply(AddedAvailableSeats event) {
        final SeatsAvailability.Builder state = getState().toBuilder();

        final List<SeatQuantity> availableSeats = state.getAvailableSeatList();
        final SeatQuantity addedQuantity = event.getQuantity();
        final SeatTypeId seatTypeId = addedQuantity.getSeatTypeId();
        final SeatQuantity existingOne = findById(availableSeats, seatTypeId, null);
        if (existingOne != null) {
            final int indexOfOldValue = availableSeats.indexOf(existingOne);
            final int newQuantity = existingOne.getQuantity() + addedQuantity.getQuantity();
            state.setAvailableSeat(indexOfOldValue, newSeatQuantity(seatTypeId, newQuantity));
        } else {
            state.addAvailableSeat(addedQuantity);
        }

        incrementState(state.build());
    }

    @Apply
    private void apply(RemovedAvailableSeats event) {
        final SeatsAvailability.Builder state = getState().toBuilder();

        final SeatQuantity removedQuantity = event.getQuantity();
        final SeatTypeId seatTypeId = removedQuantity.getSeatTypeId();
        final List<SeatQuantity> availableSeats = state.getAvailableSeatList();
        final SeatQuantity existingOne = findById(availableSeats, seatTypeId);
        final int indexOfOldValue = availableSeats.indexOf(existingOne);
        final int newQuantity = calculateNewQuantity(removedQuantity, existingOne);
        state.setAvailableSeat(indexOfOldValue, newSeatQuantity(seatTypeId, newQuantity));

        incrementState(state.build());
    }

    private static int calculateNewQuantity(SeatQuantity removedQuantity, SeatQuantity existingOne) {
        final int newQuantity = existingOne.getQuantity() - removedQuantity.getQuantity();
        if (newQuantity < 0) {
            return 0;
        }
        return newQuantity;
    }

    /**
     * The Method Object for handling {@link MakeSeatReservation} commands.
     */
    protected static class MakeSeatReservationCommandHandler {

        private final List<SeatQuantity> reservedSeatsUpdated = newLinkedList();
        private final List<SeatQuantity> availableSeatsUpdated = newLinkedList();
        private final SeatsAvailability state;

        protected MakeSeatReservationCommandHandler(SeatsAvailability state) {
            this.state = state;
        }

        /**
         * Performs all the checks needed and calculates new reserved and available quantities of seats.
         *
         * @param command a validated command to handle
         * @see #getReservedSeatsUpdated()
         * @see #getAvailableSeatsUpdated()
         */
        protected void handle(MakeSeatReservation command) {
            final List<SeatQuantity> requestedSeats = command.getSeatList();
            checkAllSeatTypesAreAvailable(requestedSeats);

            for (SeatQuantity requestedSeat : requestedSeats) {
                calculateNewSeatCount(requestedSeat, command.getReservationId());
            }
        }

        private void calculateNewSeatCount(SeatQuantity requestedSeat, ReservationId reservationId) {
            final SeatTypeId seatTypeId = requestedSeat.getSeatTypeId();
            final int availableCount = findAvailableSeatCount(seatTypeId);
            final int oldReservedCount = findReservedSeatCount(seatTypeId, reservationId);
            final int newReservedCount = calculateNewReservedSeatCount(availableCount, requestedSeat.getQuantity(), oldReservedCount);

            final SeatQuantity reservedSeatUpdated = newSeatQuantity(seatTypeId, newReservedCount);
            reservedSeatsUpdated.add(reservedSeatUpdated);

            final int newAvailableCount = calculateNewAvailableSeatCount(availableCount, oldReservedCount, newReservedCount);
            final SeatQuantity availableUpdatedSeat = newSeatQuantity(seatTypeId, newAvailableCount);
            availableSeatsUpdated.add(availableUpdatedSeat);
        }

        @VisibleForTesting
        protected int calculateNewReservedSeatCount(int availableCount, int requestedCount, int reservedCount) {
            if (requestedCount > availableCount) {
                final int remainingCount = availableCount + reservedCount;
                return remainingCount;
            }
            final int newReservedCount = reservedCount + requestedCount;
            return newReservedCount;
        }

        @VisibleForTesting
        protected int calculateNewAvailableSeatCount(int availableCount, int oldReservedCount, int newReservedCount) {
            //noinspection LocalVariableNamingConvention
            final int reservedOnThisRequestCount = newReservedCount - oldReservedCount;
            final int newAvailableCount = availableCount - reservedOnThisRequestCount;
            if (newAvailableCount < 0) {
                return 0;
            }
            return newAvailableCount;
        }

        private int findAvailableSeatCount(SeatTypeId seatTypeId) {
            final List<SeatQuantity> availableSeats = state.getAvailableSeatList();
            final SeatQuantity availableOne = findById(availableSeats, seatTypeId);
            final int seatCount = availableOne.getQuantity();
            return seatCount;
        }

        private int findReservedSeatCount(SeatTypeId seatTypeId, ReservationId reservationId) {
            final SeatQuantities quantities = state.getPendingReservations().get(reservationId.getUuid());
            final List<SeatQuantity> reservedSeats = (quantities != null) ?
                    quantities.getItemList() :
                    Collections.<SeatQuantity>emptyList();
            final SeatQuantity reservedOne = findById(reservedSeats, seatTypeId);
            final int reservedCount = reservedOne.getQuantity();
            return reservedCount;
        }

        private void checkAllSeatTypesAreAvailable(Iterable<SeatQuantity> requestedSeats) {
            final List<SeatQuantity> availableSeats = state.getAvailableSeatList();
            for (SeatQuantity requestedSeat : requestedSeats) {
                final SeatTypeId id = requestedSeat.getSeatTypeId();
                final SeatQuantity availableSeat = findById(availableSeats, id, null);
                if (availableSeat == null) {
                    throw new NoSuchElementException("No seat found with such an ID: " + id.getUuid());
                }
            }
        }

        protected List<SeatQuantity> getReservedSeatsUpdated() {
            return ImmutableList.copyOf(reservedSeatsUpdated);
        }

        protected List<SeatQuantity> getAvailableSeatsUpdated() {
            return ImmutableList.copyOf(availableSeatsUpdated);
        }
    }

    private static class Validator {

        private static void validateCommand(MakeSeatReservation cmd) {
            checkReservationId(cmd.hasReservationId(), cmd);
            checkConferenceId(cmd.hasConferenceId(), cmd);
            checkSeats(cmd.getSeatList(), cmd);
        }

        private static void validateCommand(CommitSeatReservation cmd) {
            checkReservationId(cmd.hasReservationId(), cmd);
        }

        private static void validateState(SeatsAvailability state, CommitSeatReservation cmd) {
            checkExistPendingReservationsWithId(cmd.getReservationId(), state);
        }

        private static void validateCommand(CancelSeatReservation cmd) {
            checkReservationId(cmd.hasReservationId(), cmd);
            checkConferenceId(cmd.hasConferenceId(), cmd);
        }

        private static void validateState(SeatsAvailability state, CancelSeatReservation cmd) {
            checkExistPendingReservationsWithId(cmd.getReservationId(), state);
        }

        private static void validateCommand(AddSeats cmd) {
            checkSeatQuantity(cmd.hasQuantity(), cmd.getQuantity(), cmd);
        }

        private static void validateCommand(RemoveSeats cmd) {
            checkSeatQuantity(cmd.hasQuantity(), cmd.getQuantity(), cmd);
        }

        private static void checkSeatQuantity(boolean hasQuantity, SeatQuantity quantity, Message cmd) {
            checkMessageField(hasQuantity, "seat quantity", cmd);
            checkMessageField(quantity.hasSeatTypeId(), "seat type id", quantity);
            checkMessageField(quantity.getQuantity() > 0, "quantity", quantity);
        }

        private static void validateState(SeatsAvailability state, RemoveSeats cmd) {
            final List<SeatQuantity> availableSeats = state.getAvailableSeatList();
            final SeatQuantity quantityToRemove = cmd.getQuantity();
            final SeatTypeId id = quantityToRemove.getSeatTypeId();
            final SeatQuantity existingOne = findById(availableSeats, id, null);
            checkState(existingOne != null, "No such available seat, seat type ID: " + id.getUuid());
        }

        private static void checkExistPendingReservationsWithId(ReservationId reservationId, SeatsAvailability state) {
            final String id = reservationId.getUuid();
            final Map<String, SeatQuantities> pendingReservations = state.getPendingReservations();
            checkState(pendingReservations.containsKey(id), "No such pending reservation with the ID: " + id);
        }
    }
}
