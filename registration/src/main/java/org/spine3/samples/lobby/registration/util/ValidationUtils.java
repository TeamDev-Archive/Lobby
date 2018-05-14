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

package org.spine3.samples.lobby.registration.util;

import com.google.protobuf.Message;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;

import java.util.Collection;

import static java.lang.String.format;

/**
 * The utility class for validating messages (commands, events etc).
 *
 * @author Alexander Litus
 */
@SuppressWarnings("UtilityClass")
public class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * Ensures the truth of a {@code hasId} expression.
     *
     * @param hasId   a boolean expression stating that the {@code message} has an conference ID
     * @param message a checked message
     * @throws IllegalArgumentException if {@code hasId} expression is false
     */
    public static void checkConferenceId(boolean hasId, Message message) {
        checkMessageField(hasId, "conference ID", message);
    }

    /**
     * Ensures that {@code seats} is not empty collection, each seat has an ID and a positive quantity value.
     *
     * @param seats   seats to check
     * @param message a checked message which must have the {@code seats}
     * @throws IllegalArgumentException if {@code hasId} expression is false
     */
    public static void checkSeats(Collection<SeatQuantity> seats, Message message) {
        checkMessageField(!seats.isEmpty(), "seats", message);
        for (SeatQuantity seat : seats) {
            checkMessageField(seat.hasSeatTypeId(), "seat type ID", seat);
            if (seat.getQuantity() < 0) {
                throw new IllegalArgumentException("The seat quantity must be positive.");
            }
        }
    }

    /**
     * Ensures the truth of a {@code hasField} expression.
     *
     * @param hasField  a boolean expression stating that the {@code target} message has a specific field
     * @param fieldName a name of the field which the {@code target} message must have
     * @param target    a checked message
     * @throws IllegalArgumentException if {@code hasField} expression is false
     */
    public static void checkMessageField(boolean hasField, String fieldName, Message target) {
        if (!hasField) {
            final String message = format("The field '%s' must be defined in all messages of class: %s.",
                    fieldName, target.getClass()
                                     .getName());
            throw new IllegalArgumentException(message);
        }
    }
}
