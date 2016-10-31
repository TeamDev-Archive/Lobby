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
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.AddSeats;
import org.spine3.samples.lobby.registration.seat.availability.AddedAvailableSeats;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailability;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityAggregate;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened", "MagicNumber"})
public class AddSeatsCmdHandling extends TestCase {

    private static final AddSeats ADD_SEATS = AddSeats.newBuilder()
                                                      .setQuantity(newSeatQuantity(5))
                                                      .setConferenceId(newConferenceId())
                                                      .build();

    public AddSeats givenCommand() {
        return ADD_SEATS;
    }

    public CommandContext givenCommandContext() {
        return CommandContext.getDefaultInstance();
    }

    @Override
    public SeatsAvailabilityAggregate givenAggregate() {
        final SeatsAvailabilityAggregate aggregate = super.givenAggregate();
        final SeatsAvailability state = aggregate.getState()
                                                 .toBuilder()
                                                 .addAvailableSeat(newSeatQuantity(20))
                                                 .build();
        aggregate.incrementAggregateState(state);
        return aggregate;
    }

    public void validateResult(AddedAvailableSeats event, AddSeats cmd) {
        final SeatQuantity quantityToAdd = cmd.getQuantity();
        final SeatQuantity quantityAdded = event.getQuantity();
        assertEquals(quantityToAdd, quantityAdded);
    }
}
