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
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.CancelSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.SeatQuantities;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailability;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityAggregate;
import org.spine3.samples.lobby.registration.seat.availability.SeatsReservationCancelled;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.IdFactory.newReservationId;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantities;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"NoopMethodInAbstractClass", "MagicNumber", "RefusedBequest", "AbstractClassWithoutAbstractMethods"})
public abstract class CancelSeatReservationCmdHandling extends TestCase {

    private static final ReservationId RESERVATION_ID = newReservationId();

    private static final CancelSeatReservation CANCEL_SEAT_RESERVATION = CancelSeatReservation.newBuilder()
                                                                                              .setReservationId(RESERVATION_ID)
                                                                                              .setConferenceId(newConferenceId())
                                                                                              .build();

    public CancelSeatReservation givenCommand() {
        return CANCEL_SEAT_RESERVATION;
    }

    public CommandContext givenCommandContext() {
        return CommandContext.getDefaultInstance();
    }

    public void validateResult(SeatsReservationCancelled event, CancelSeatReservation cmd) {
    }

    public static class ExistsPendingReservation extends CancelSeatReservationCmdHandling {

        private static final SeatQuantities TMP_RESERVED_SEATS = newSeatQuantities(
                newSeatQuantity(10), newSeatQuantity(20));

        private static final ImmutableMap<String, SeatQuantities> PENDING_RESERVATIONS =
                ImmutableMap.<String, SeatQuantities>builder()
                        .put(RESERVATION_ID.getUuid(), TMP_RESERVED_SEATS)
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

        @Override
        public void validateResult(SeatsReservationCancelled event, CancelSeatReservation cmd) {
            assertEquals(cmd.getReservationId(), event.getReservationId());
            assertEquals(cmd.getConferenceId(), event.getConferenceId());

            final List<SeatQuantity> availableSeatsActual = event.getAvailableSeatUpdatedList();
            assertEquals(TMP_RESERVED_SEATS.getItemList(), availableSeatsActual);
        }
    }

    @SuppressWarnings("EmptyClass")
    public static class EmptyState extends CancelSeatReservationCmdHandling {
    }
}
