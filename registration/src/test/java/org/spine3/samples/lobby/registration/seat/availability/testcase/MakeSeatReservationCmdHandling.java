/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.MakeSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.SeatsReserved;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.samples.lobby.common.util.IdFactory.*;
import static org.spine3.samples.lobby.registration.util.Seats.findById;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("TypeMayBeWeakened")
public abstract class MakeSeatReservationCmdHandling extends TestCase {

    private static final ConferenceId CONFERENCE_ID = newConferenceId();
    static final ReservationId RESERVATION_ID = newReservationId();

    public static final SeatTypeId MAIN_SEAT_TYPE_ID = newSeatTypeId("main-" + newUuid());
    public static final SeatTypeId WORKSHOP_SEAT_TYPE_ID = newSeatTypeId("workshop-" + newUuid());

    private static final int MAIN_SEAT_COUNT_AVAILABLE = 100;
    private static final int WORKSHOP_SEAT_COUNT_AVAILABLE = 70;

    private static final List<SeatQuantity> AVAILABLE_SEATS = ImmutableList.of(
            newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_AVAILABLE),
            newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_AVAILABLE));

    public List<SeatQuantity> getAvailableSeats() {
        //noinspection ReturnOfCollectionOrArrayField
        return AVAILABLE_SEATS;
    }

    /**
     * A test utility class providing commands.
     */
    @SuppressWarnings("UtilityClass")
    public static class Command {

        public static final int MAIN_SEAT_COUNT_REQUESTED = 10;
        public static final int WORKSHOP_SEAT_COUNT_REQUESTED = 7;

        public static final MakeSeatReservation MAKE_SEAT_RESERVATION = MakeSeatReservation.newBuilder()
                                                                                           .setConferenceId(CONFERENCE_ID)
                                                                                           .setReservationId(RESERVATION_ID)
                                                                                           .addSeat(newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_COUNT_REQUESTED))
                                                                                           .addSeat(newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_COUNT_REQUESTED))
                                                                                           .build();

        private Command() {
        }
    }

    public MakeSeatReservation givenCommand() {
        return Command.MAKE_SEAT_RESERVATION;
    }

    public CommandContext givenCommandContext() {
        return CommandContext.getDefaultInstance();
    }

    public void validateResult(SeatsReserved event, MakeSeatReservation cmd) {
        assertEquals(cmd.getConferenceId(), event.getConferenceId());
        assertEquals(cmd.getReservationId(), event.getReservationId());

        final List<SeatQuantity> requestedSeats = cmd.getSeatList();
        final List<SeatQuantity> reservedSeats = event.getReservedSeatUpdatedList();
        checkReservedSeatsUpdated(reservedSeats, requestedSeats);

        final List<SeatQuantity> availableSeatsUpdated = event.getAvailableSeatUpdatedList();
        checkAvailableSeatsUpdated(availableSeatsUpdated, requestedSeats);
    }

    protected abstract void checkReservedSeatsUpdated(List<SeatQuantity> reservedSeats, List<SeatQuantity> requestedSeats);

    public void checkAvailableSeatsUpdated(List<SeatQuantity> availableSeats, List<SeatQuantity> requestedSeats) {
        assertEquals(requestedSeats.size(), availableSeats.size());

        final int expectedMainSeatCount = MAIN_SEAT_COUNT_AVAILABLE - Command.MAIN_SEAT_COUNT_REQUESTED;
        final SeatQuantity mainSeatAvailable = findById(availableSeats, MAIN_SEAT_TYPE_ID);
        assertEquals(expectedMainSeatCount, mainSeatAvailable.getQuantity());

        final int expectedWorkshopSeatCount = WORKSHOP_SEAT_COUNT_AVAILABLE - Command.WORKSHOP_SEAT_COUNT_REQUESTED;
        final SeatQuantity workshopSeatAvailable = findById(availableSeats, WORKSHOP_SEAT_TYPE_ID);
        assertEquals(expectedWorkshopSeatCount, workshopSeatAvailable.getQuantity());
    }
}
