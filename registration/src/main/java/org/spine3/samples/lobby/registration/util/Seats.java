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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.spine3.base.Identifiers;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatAssignment;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsId;
import org.spine3.samples.lobby.registration.contracts.SeatPosition;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.seat.availability.SeatQuantities;
import org.spine3.samples.lobby.registration.seat.availability.SeatsAvailabilityId;

import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static org.spine3.samples.lobby.common.util.IdFactory.newSeatTypeId;

/**
 * The utility class for working with objects related to seats.
 *
 * @author Alexander Litus
 * @see SeatQuantity
 */
@SuppressWarnings("UtilityClass")
public class Seats {

    private Seats() {
    }

    /**
     * Creates a new {@code SeatsAvailabilityId} with a random UUID value.
     */
    public static SeatsAvailabilityId newSeatsAvailabilityId() {
        final String id = Identifiers.newUuid();
        return SeatsAvailabilityId.newBuilder()
                                  .setUuid(id)
                                  .build();
    }

    /**
     * Creates a new {@code SeatAssignmentsId} with a random UUID value.
     */
    public static SeatAssignmentsId newSeatAssignmentsId() {
        final String id = Identifiers.newUuid();
        return SeatAssignmentsId.newBuilder()
                                .setUuid(id)
                                .build();
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
    @SuppressWarnings("OverloadedVarargsMethod") // OK in this case
    public static SeatQuantities newSeatQuantities(SeatQuantity... seats) {
        final SeatQuantities result = newSeatQuantities(ImmutableList.copyOf(seats));
        return result;
    }

    /**
     * Creates a new {@code SeatQuantities} instance from the given {@code seats}.
     */
    public static SeatQuantities newSeatQuantities(Iterable<SeatQuantity> seats) {
        final SeatQuantities result = SeatQuantities.newBuilder()
                                                    .addAllItem(seats)
                                                    .build();
        return result;
    }

    /**
     * Creates a new {@code SeatQuantity} instance with the given {@code uuid} and {@code quantity}.
     */
    public static SeatQuantity newSeatQuantity(String uuid, int quantity) {
        final SeatTypeId id = newSeatTypeId(uuid);
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

    /**
     * Finds a seat quantity item by the {@code id}.
     *
     * @param seats         the collection to search in
     * @param id            the ID of the item to find
     * @param defaultResult the value to return if nothing was found
     * @return the found item
     */
    public static SeatQuantity findById(Iterable<SeatQuantity> seats, final SeatTypeId id, @Nullable SeatQuantity defaultResult) {
        final SeatQuantity result = find(seats, new Predicate<SeatQuantity>() {
            @Override
            public boolean apply(@Nullable SeatQuantity seat) {
                final boolean result =
                        (seat != null) &&
                                seat.hasSeatTypeId() &&
                                seat.getSeatTypeId()
                                    .equals(id);
                return result;
            }
        }, defaultResult);
        return result;
    }

    /**
     * Finds a seat quantity item by the {@code id} or returns a default {@code SeatQuantity} instance.
     *
     * @param seats the collection to search in
     * @param id    the ID of the item to find
     * @return the found item or the default instance
     * @see SeatQuantity#getDefaultInstance()
     */
    public static SeatQuantity findById(Iterable<SeatQuantity> seats, SeatTypeId id) {
        return findById(seats, id, SeatQuantity.getDefaultInstance());
    }

    /**
     * Filters seat assignments items by the {@code seatTypeId}.
     *
     * @param seats      the collection to filter
     * @param seatTypeId the ID to use in filtering
     * @return the filtered items
     */
    public static FluentIterable<SeatAssignment> filterById(Iterable<SeatAssignment> seats, final SeatTypeId seatTypeId) {
        final Iterable<SeatAssignment> result = filter(seats, new Predicate<SeatAssignment>() {
            @Override
            public boolean apply(@Nullable SeatAssignment seat) {
                final boolean result =
                        (seat != null) &&
                                seat.hasSeatTypeId() &&
                                seat.getSeatTypeId()
                                    .equals(seatTypeId);
                return result;
            }
        });
        return FluentIterable.from(result);
    }

    /**
     * Creates a new {@code SeatPosition} with the given value.
     */
    public static SeatPosition newSeatPosition(int position) {
        return SeatPosition.newBuilder()
                           .setValue(position)
                           .build();
    }
}
