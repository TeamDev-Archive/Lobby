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
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.registration.seat.availability.CommitSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.SeatQuantities;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailability;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityAggregate;
import org.spine3.samples.lobby.registration.seat.availability.SeatsReservationCommitted;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.newReservationId;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantities;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods", "TypeMayBeWeakened", "MagicNumber"})
public abstract class CommitSeatReservationCmdHandling extends TestCase {

    private static final ReservationId RESERVATION_ID = newReservationId();

    private static final CommitSeatReservation COMMIT_SEAT_RESERVATION = CommitSeatReservation.newBuilder()
                                                                                              .setReservationId(RESERVATION_ID)
                                                                                              .build();

    public CommitSeatReservation givenCommand() {
        return COMMIT_SEAT_RESERVATION;
    }

    public CommandContext givenCommandContext() {
        return CommandContext.getDefaultInstance();
    }

    public void validateResult(SeatsReservationCommitted event, CommitSeatReservation cmd) {
        assertEquals(cmd.getReservationId(), event.getReservationId());
    }

    public static class ExistsPendingReservation extends CommitSeatReservationCmdHandling {

        private static final ImmutableMap<String, SeatQuantities> PENDING_RESERVATIONS =
                ImmutableMap.<String, SeatQuantities>builder()
                        .put(RESERVATION_ID.getUuid(), newSeatQuantities(newSeatQuantity(20)))
                        .build();

        @Override
        public SeatsAvailabilityAggregate givenAggregate() {
            final SeatsAvailabilityAggregate aggregate = super.givenAggregate();
            final SeatsAvailability state = aggregate.getState()
                                                     .toBuilder()
                                                     .putAllPendingReservations(PENDING_RESERVATIONS)
                                                     .build();
            aggregate.incrementAggregateState(state);
            return aggregate;
        }
    }

    @SuppressWarnings("EmptyClass")
    public static class EmptyState extends CommitSeatReservationCmdHandling {
    }
}
