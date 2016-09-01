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

import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.RemoveSeats;
import org.spine3.samples.lobby.registration.seat.availability.RemovedAvailableSeats;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailability;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityAggregate;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.IdFactory.newSeatTypeId;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatsAvailabilityId;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened", "MagicNumber", "RefusedBequest"})
public class RemoveSeatsCmdHandling extends TestCase {

    private static final SeatTypeId SEAT_TYPE_ID = newSeatTypeId();
    private static final int SEAT_QUANTITY_TO_REMOVE = 20;

    private static final SeatQuantity AVAILABLE_QUANTITY = newSeatQuantity(SEAT_TYPE_ID, 80);

    private static final RemoveSeats REMOVE_SEATS = RemoveSeats.newBuilder()
                                                               .setQuantity(newSeatQuantity(SEAT_TYPE_ID, SEAT_QUANTITY_TO_REMOVE))
                                                               .setConferenceId(newConferenceId())
                                                               .build();

    public RemoveSeats givenCommand() {
        return REMOVE_SEATS;
    }

    public CommandContext givenCommandContext() {
        return CommandContext.getDefaultInstance();
    }

    @Override
    public SeatsAvailabilityAggregate givenAggregate() {
        final SeatsAvailabilityAggregate aggregate = super.givenAggregate();
        final SeatsAvailability state = aggregate.getState()
                                                 .toBuilder()
                                                 .addAvailableSeat(AVAILABLE_QUANTITY)
                                                 .build();
        aggregate.incrementStateForTest(state);
        return aggregate;
    }

    public void validateResult(RemovedAvailableSeats event, RemoveSeats cmd) {
        assertEquals(cmd.getQuantity(), event.getQuantity());
    }

    public static class EmptyState extends RemoveSeatsCmdHandling {
        @Override
        public SeatsAvailabilityAggregate givenAggregate() {
            return new SeatsAvailabilityAggregate(newSeatsAvailabilityId());
        }
    }
}
