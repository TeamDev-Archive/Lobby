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
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.util.Seats;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.collect.Lists.newLinkedList;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * The Method Object for handling {@link MakeSeatReservation} commands.
 */
@SuppressWarnings("TypeMayBeWeakened"/** "OrBuilder" parameters are not applicable*/)
/*package*/ class MakeSeatReservationCommandHandler {

    private final List<SeatQuantity> reservedSeatsUpdated = newLinkedList();
    private final List<SeatQuantity> availableSeatsUpdated = newLinkedList();
    private final SeatsAvailability state;

    /*package*/ MakeSeatReservationCommandHandler(SeatsAvailability state) {
        this.state = state;
    }

    /**
     * Performs all the checks needed and calculates new reserved and available quantities of seats.
     *
     * @param command a validated command to handle
     * @see #getReservedSeatsUpdated()
     * @see #getAvailableSeatsUpdated()
     */
    /*package*/ void handle(MakeSeatReservation command) {
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
    /*package*/ int calculateNewReservedSeatCount(int availableCount, int requestedCount, int reservedCount) {
        if (requestedCount > availableCount) {
            final int remainingCount = availableCount + reservedCount;
            return remainingCount;
        }
        final int newReservedCount = reservedCount + requestedCount;
        return newReservedCount;
    }

    @VisibleForTesting
    /*package*/ int calculateNewAvailableSeatCount(int availableCount, int oldReservedCount, int newReservedCount) {
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
        final SeatQuantity availableOne = Seats.findById(availableSeats, seatTypeId);
        final int seatCount = availableOne.getQuantity();
        return seatCount;
    }

    private int findReservedSeatCount(SeatTypeId seatTypeId, ReservationId reservationId) {
        final SeatQuantities quantities = state.getPendingReservations().get(reservationId.getUuid());
        final List<SeatQuantity> reservedSeats = (quantities != null) ?
                quantities.getItemList() :
                Collections.<SeatQuantity>emptyList();
        final SeatQuantity reservedOne = Seats.findById(reservedSeats, seatTypeId);
        final int reservedCount = reservedOne.getQuantity();
        return reservedCount;
    }

    private void checkAllSeatTypesAreAvailable(Iterable<SeatQuantity> requestedSeats) {
        final List<SeatQuantity> availableSeats = state.getAvailableSeatList();
        for (SeatQuantity requestedSeat : requestedSeats) {
            final SeatTypeId id = requestedSeat.getSeatTypeId();
            final SeatQuantity availableSeat = Seats.findById(availableSeats, id, null);
            if (availableSeat == null) {
                throw new NoSuchElementException("No seat found with such an ID: " + id.getUuid());
            }
        }
    }

    /*package*/ List<SeatQuantity> getReservedSeatsUpdated() {
        return ImmutableList.copyOf(reservedSeatsUpdated);
    }

    /*package*/ List<SeatQuantity> getAvailableSeatsUpdated() {
        return ImmutableList.copyOf(availableSeatsUpdated);
    }
}
