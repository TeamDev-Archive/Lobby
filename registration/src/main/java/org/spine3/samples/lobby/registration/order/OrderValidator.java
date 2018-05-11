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

package org.spine3.samples.lobby.registration.order;

import com.google.protobuf.Message;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.spine3.samples.lobby.registration.util.ValidationUtils.*;

/**
 * The class for validating commands and the state of {@link OrderAggregate} when handling commands.
 *
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened"/** "OrBuilder" parameters are not applicable*/, "UtilityClass"})
/* package */ class OrderValidator {

    private OrderValidator() {
    }

    /* package */
    static void checkNotConfirmed(Order order, Message cmd) {
        final String message = format("Cannot modify a confirmed order with ID: %s; command: %s.",
                order.getId()
                     .getUuid(), cmd.getClass()
                                    .getName()
        );
        checkState(!order.getIsConfirmed(), message);
    }

    /* package */
    static void checkNewState(Order order) {
        checkState(order.hasId(), "No order ID in a new order state.");
        final String orderId = order.getId()
                                    .getUuid();
        checkState(order.hasConferenceId(), "No conference ID in a new order state, ID: " + orderId);
        checkSeats(order.getSeatList(), order);
    }

    /* package */
    static void validateCommand(RegisterToConference cmd) {
        checkOrderId(cmd.hasOrderId(), cmd);
        checkConferenceId(cmd.hasConferenceId(), cmd);
        checkSeats(cmd.getSeatList(), cmd);
    }

    /* package */
    static void validateCommand(MarkSeatsAsReserved cmd) {
        checkOrderId(cmd.hasOrderId(), cmd);
        checkMessageField(cmd.hasReservationExpiration(), "reservation expiration", cmd);
        checkSeats(cmd.getSeatList(), cmd);
    }

    /* package */
    static void validateCommand(RejectOrder cmd) {
        checkOrderId(cmd.hasOrderId(), cmd);
    }

    /* package */
    static void validateCommand(ConfirmOrder cmd) {
        checkOrderId(cmd.hasOrderId(), cmd);
    }

    /* package */
    static void validateCommand(AssignRegistrantDetails cmd) {
        checkOrderId(cmd.hasOrderId(), cmd);
        checkMessageField(cmd.hasRegistrant(), "registrant", cmd);
    }

    /**
     * Ensures the truth of a {@code hasId} expression.
     *
     * @param hasId   a boolean expression stating that the {@code message} has an order ID
     * @param message a checked message
     * @throws IllegalArgumentException if {@code hasId} expression is false
     */
    /* package */
    static void checkOrderId(boolean hasId, Message message) {
        checkMessageField(hasId, "order ID", message);
    }
}
