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

package org.spine3.samples.lobby.registration.seat.availability;

import com.google.protobuf.Message;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.util.Seats;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static org.spine3.samples.lobby.registration.util.ValidationUtils.*;

/**
 * The class for validating commands and the state of {@link SeatsAvailabilityAggregate} when handling commands.
 *
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened"/** "OrBuilder" parameters are not applicable*/, "UtilityClass"})
/* package */ class SeatsAvailabilityValidator {

    private SeatsAvailabilityValidator() {
    }

    /* package */ static void validateCommand(MakeSeatReservation cmd) {
        checkReservationId(cmd.hasReservationId(), cmd);
        checkConferenceId(cmd.hasConferenceId(), cmd);
        checkSeats(cmd.getSeatList(), cmd);
    }

    /* package */ static void validateCommand(CommitSeatReservation cmd) {
        checkReservationId(cmd.hasReservationId(), cmd);
    }

    /* package */ static void validateState(SeatsAvailability state, CommitSeatReservation cmd) {
        checkExistPendingReservationsWithId(cmd.getReservationId(), state);
    }

    /* package */ static void validateCommand(CancelSeatReservation cmd) {
        checkReservationId(cmd.hasReservationId(), cmd);
        checkConferenceId(cmd.hasConferenceId(), cmd);
    }

    /* package */ static void validateState(SeatsAvailability state, CancelSeatReservation cmd) {
        checkExistPendingReservationsWithId(cmd.getReservationId(), state);
    }

    /* package */ static void validateCommand(AddSeats cmd) {
        checkSeatQuantity(cmd.hasQuantity(), cmd.getQuantity(), cmd);
    }

    /* package */ static void validateCommand(RemoveSeats cmd) {
        checkSeatQuantity(cmd.hasQuantity(), cmd.getQuantity(), cmd);
    }

    /* package */ static void checkSeatQuantity(boolean hasQuantity, SeatQuantity quantity, Message cmd) {
        checkMessageField(hasQuantity, "seat quantity", cmd);
        checkMessageField(quantity.hasSeatTypeId(), "seat type id", quantity);
        checkMessageField(quantity.getQuantity() > 0, "quantity", quantity);
    }

    /* package */ static void validateState(SeatsAvailability state, RemoveSeats cmd) {
        final List<SeatQuantity> availableSeats = state.getAvailableSeatList();
        final SeatQuantity quantityToRemove = cmd.getQuantity();
        final SeatTypeId id = quantityToRemove.getSeatTypeId();
        final SeatQuantity existingOne = Seats.findById(availableSeats, id, null);
        checkState(existingOne != null, "No such available seat, seat type ID: " + id.getUuid());
    }

    private static void checkExistPendingReservationsWithId(ReservationId reservationId, SeatsAvailability state) {
        final String id = reservationId.getUuid();
        final Map<String, SeatQuantities> pendingReservations = state.getPendingReservations();
        checkState(pendingReservations.containsKey(id), "No such pending reservation with the ID: " + id);
    }

    /**
     * Ensures the truth of a {@code hasId} expression.
     *
     * @param hasId   a boolean expression stating that the {@code message} has an reservation ID
     * @param message a checked message
     * @throws IllegalArgumentException if {@code hasId} expression is false
     */
    /* package */
    static void checkReservationId(boolean hasId, Message message) {
        checkMessageField(hasId, "reservation ID", message);
    }
}
