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

import org.spine3.samples.lobby.registration.seat.availability.SeatsReserved;

import static org.junit.Assert.assertNotEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("MagicNumber")
public class SeatsReservedEventApplying extends ExistAvailableSeatsAndPendingReservations {

    private static final SeatsReserved SEATS_RESERVED = SeatsReserved.newBuilder()
                                                                     .setConferenceId(newConferenceId())
                                                                     .setReservationId(getReservationId())
                                                                     .addReservedSeatUpdated(newSeatQuantity(256))
                                                                     .addAvailableSeatUpdated(newSeatQuantity(24))
                                                                     .build();

    static {
        assertNotEquals(getAvailableSeats(), SEATS_RESERVED.getAvailableSeatUpdatedList());
        assertNotEquals(getReservedSeats(), SEATS_RESERVED.getReservedSeatUpdatedList());
    }

    public SeatsReserved givenEvent() {
        return SEATS_RESERVED;
    }
}
