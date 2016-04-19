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

package org.spine3.samples.lobby.registration.seat.availability.testcase;

import com.google.common.collect.ImmutableMap;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.SeatQuantities;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailability;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityAggregate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.spine3.samples.lobby.registration.util.Seats.*;

/**
 * @author Alexander Litus
 */
public class EnoughSeatsAndExistPendingReservations extends MakeSeatReservationCmdHandling {

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
    public SeatsAvailabilityAggregate givenAggregate() {
        final SeatsAvailabilityAggregate aggregate = super.givenAggregate();
        final SeatsAvailability state = aggregate.getState().toBuilder()
                .addAllAvailableSeat(getAvailableSeats())
                .putAllPendingReservations(PENDING_RESERVATIONS)
                .build();
        aggregate.incrementStateForTest(state);
        return aggregate;
    }

    @Override
    public void checkReservedSeatsUpdated(List<SeatQuantity> reservedSeats, List<SeatQuantity> requestedSeats) {
        assertEquals(requestedSeats.size(), reservedSeats.size());

        final SeatQuantity mainSeatReserved = findById(reservedSeats, MAIN_SEAT_TYPE_ID);
        assertEquals(EXPECTED_RESERVED_MAIN_SEAT_COUNT, mainSeatReserved.getQuantity());

        final SeatQuantity workshopSeatReserved = findById(reservedSeats, WORKSHOP_SEAT_TYPE_ID);
        assertTrue(requestedSeats.contains(workshopSeatReserved));
    }
}
