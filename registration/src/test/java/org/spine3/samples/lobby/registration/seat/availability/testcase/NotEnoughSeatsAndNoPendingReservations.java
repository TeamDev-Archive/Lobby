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

package org.spine3.samples.lobby.registration.seat.availability.testcase;

import com.google.common.collect.ImmutableList;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailability;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityAggregate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.spine3.samples.lobby.registration.util.Seats.findById;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
public class NotEnoughSeatsAndNoPendingReservations extends MakeSeatReservationCmdHandling {

    private static final int MAIN_SEAT_COUNT_AVAILABLE = 5;

    private static final int WORKSHOP_SEAT_COUNT_AVAILABLE = 30;
    private static final int EXPECTED_RESERVED_WORKSHOP_SEAT_COUNT =
            WORKSHOP_SEAT_COUNT_AVAILABLE - Command.WORKSHOP_SEAT_COUNT_REQUESTED;

    private static final List<SeatQuantity> AVAILABLE_SEATS = ImmutableList.of(
            newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_AVAILABLE),
            newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_AVAILABLE));

    @Override
    public SeatsAvailabilityAggregate givenAggregate() {
        final SeatsAvailabilityAggregate aggregate = super.givenAggregate();
        final SeatsAvailability state = aggregate.getState()
                                                 .toBuilder()
                                                 .addAllAvailableSeat(AVAILABLE_SEATS)
                                                 .build();
        aggregate.incrementAggregateState(state);
        return aggregate;
    }

    @Override
    public void checkReservedSeatsUpdated(List<SeatQuantity> reservedSeats, List<SeatQuantity> requestedSeats) {
        assertEquals(requestedSeats.size(), reservedSeats.size());

        final SeatQuantity mainSeatReserved = findById(reservedSeats, MAIN_SEAT_TYPE_ID);
        assertEquals(MAIN_SEAT_COUNT_AVAILABLE, mainSeatReserved.getQuantity());

        final SeatQuantity workshopSeatReserved = findById(reservedSeats, WORKSHOP_SEAT_TYPE_ID);
        assertTrue(requestedSeats.contains(workshopSeatReserved));
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    public void checkAvailableSeatsUpdated(List<SeatQuantity> availableSeats, List<SeatQuantity> requestedSeats) {
        assertEquals(requestedSeats.size(), availableSeats.size());

        final SeatQuantity mainSeatAvailable = findById(availableSeats, MAIN_SEAT_TYPE_ID);
        assertEquals(0, mainSeatAvailable.getQuantity());

        final SeatQuantity workshopSeatAvailable = findById(availableSeats, WORKSHOP_SEAT_TYPE_ID);
        assertEquals(EXPECTED_RESERVED_WORKSHOP_SEAT_COUNT, workshopSeatAvailable.getQuantity());
    }
}
