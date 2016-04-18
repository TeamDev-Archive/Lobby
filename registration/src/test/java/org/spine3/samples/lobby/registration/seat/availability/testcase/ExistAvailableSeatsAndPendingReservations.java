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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.SeatQuantities;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailability;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityAggregate;

import java.util.List;

import static org.spine3.samples.lobby.common.util.IdFactory.newReservationId;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantities;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("MagicNumber")
public class ExistAvailableSeatsAndPendingReservations extends TestCase {

    private static final ReservationId RESERVATION_ID = newReservationId();

    private static final SeatQuantities RESERVED_SEATS = newSeatQuantities(
            newSeatQuantity(56), newSeatQuantity(38));

    private static final ImmutableMap<String, SeatQuantities> PENDING_RESERVATIONS = ImmutableMap.<String, SeatQuantities>builder()
            .put(RESERVATION_ID.getUuid(), RESERVED_SEATS).build();

    private static final List<SeatQuantity> AVAILABLE_SEATS = ImmutableList.of(
            newSeatQuantity(110),
            newSeatQuantity(220));

    @Override
    public SeatsAvailabilityAggregate givenAggregate() {
        final SeatsAvailabilityAggregate aggregate = super.givenAggregate();
        final SeatsAvailability state = aggregate.getState().toBuilder()
                .addAllAvailableSeat(AVAILABLE_SEATS)
                .putAllPendingReservations(PENDING_RESERVATIONS)
                .build();
        aggregate.testIncrementState(state);
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
