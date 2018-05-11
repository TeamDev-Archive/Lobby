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

import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.RemovedAvailableSeats;

import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
public abstract class RemovedAvailableSeatsEventApplying extends ExistAvailableSeatsAndPendingReservations {

    private SeatQuantity primarySeat;

    public RemovedAvailableSeats givenEvent() {
        primarySeat = getAvailableSeats().get(0);
        final int quantityToRemove = getQuantityToRemove();
        final RemovedAvailableSeats event = RemovedAvailableSeats.newBuilder()
                                                                 .setQuantity(newSeatQuantity(primarySeat.getSeatTypeId(), quantityToRemove))
                                                                 .build();
        return event;
    }

    public SeatQuantity getPrimarySeat() {
        return primarySeat;
    }

    protected abstract int getQuantityToRemove();

    public static class RemovingLessSeatsThanRemaining extends RemovedAvailableSeatsEventApplying {

        @Override
        protected int getQuantityToRemove() {
            final int quantityToRemove = getPrimarySeat().getQuantity() / 2;
            return quantityToRemove;
        }
    }

    public static class RemovingMoreSeatsThanRemaining extends RemovedAvailableSeatsEventApplying {

        @Override
        protected int getQuantityToRemove() {
            final int quantityToRemove = getPrimarySeat().getQuantity() + 5;
            return quantityToRemove;
        }
    }
}
