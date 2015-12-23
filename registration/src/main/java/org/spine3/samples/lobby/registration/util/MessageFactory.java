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

package org.spine3.samples.lobby.registration.util;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import org.spine3.money.Currency;
import org.spine3.money.Money;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.SeatQuantities;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityId;
import org.spine3.util.Identifiers;

/**
 * The utility class containing convenience methods for messages creation.
 *
 * @see Message
 * @author Alexander Litus
 */
@SuppressWarnings("UtilityClass")
public class MessageFactory {

    private MessageFactory() {
    }

    /**
     * Creates a new {@code OrderId} with a random UUID value.
     */
    public static OrderId newOrderId() {
        final String id = Identifiers.newUuid();
        return OrderId.newBuilder().setUuid(id).build();
    }

    /**
     * Creates a new {@code SeatsAvailabilityId} with a random UUID value.
     */
    public static SeatsAvailabilityId newSeatsAvailabilityId() {
        final String id = Identifiers.newUuid();
        return SeatsAvailabilityId.newBuilder().setUuid(id).build();
    }

    /**
     * Creates a new {@code ReservationId} with a random UUID value.
     */
    public static ReservationId newReservationId() {
        final String id = Identifiers.newUuid();
        return ReservationId.newBuilder().setUuid(id).build();
    }

    /**
     * Creates a new {@code ConferenceId} with a random UUID value.
     */
    public static ConferenceId newConferenceId() {
        final String id = Identifiers.newUuid();
        return ConferenceId.newBuilder().setUuid(id).build();
    }

    /**
     * Creates a new {@code SeatTypeId} with a random UUID value.
     */
    public static SeatTypeId newSeatTypeId() {
        return SeatTypeId.newBuilder().setUuid(Identifiers.newUuid()).build();
    }

    /**
     * Creates a new {@code Money} instance with the given {@code amount} and {@code currency}.
     */
    public static Money newMoney(int amount, Currency currency) {
        final Money.Builder result = Money.newBuilder()
                .setAmount(amount)
                .setCurrency(currency);
        return result.build();
    }

    /**
     * Creates a new {@code SeatQuantity} instance with the given {@code quantity} and a random UUID.
     */
    public static SeatQuantity newSeatQuantity(int quantity) {
        final String id = Identifiers.newUuid();
        final SeatQuantity result = newSeatQuantity(id, quantity);
        return result;
    }

    /**
     * Creates a new {@code SeatQuantities} instance from the given {@code seats}.
     */
    public static SeatQuantities newSeatQuantities(SeatQuantity... seats) {
        final SeatQuantities result = newSeatQuantities(ImmutableList.copyOf(seats));
        return result;
    }

    /**
     * Creates a new {@code SeatQuantities} instance from the given {@code seats}.
     */
    public static SeatQuantities newSeatQuantities(Iterable<SeatQuantity> seats) {
        final SeatQuantities result = SeatQuantities.newBuilder().addAllItem(seats).build();
        return result;
    }

    /**
     * Creates a new {@code SeatQuantity} instance with the given {@code uuid} and {@code quantity}.
     */
    public static SeatQuantity newSeatQuantity(String uuid, int quantity) {
        final SeatTypeId id = SeatTypeId.newBuilder().setUuid(uuid).build();
        final SeatQuantity result = newSeatQuantity(id, quantity);
        return result;
    }

    /**
     * Creates a new {@code SeatQuantity} instance with the given {@code id} and {@code quantity}.
     */
    public static SeatQuantity newSeatQuantity(SeatTypeId id, int quantity) {
        final SeatQuantity.Builder result = SeatQuantity.newBuilder()
                .setSeatTypeId(id)
                .setQuantity(quantity);
        return result.build();
    }
}
