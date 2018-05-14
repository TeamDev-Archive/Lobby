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

package org.spine3.samples.lobby.common.util;

import com.google.protobuf.Message;
import org.spine3.base.Identifiers;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;

/**
 * The utility class containing convenience methods for identifiers creation.
 *
 * @author Alexander Litus
 * @see Message
 */
@SuppressWarnings("UtilityClass")
public class IdFactory {

    private IdFactory() {
    }

    /**
     * Creates a new {@code OrderId} with a random UUID value.
     */
    public static OrderId newOrderId() {
        final String id = Identifiers.newUuid();
        return OrderId.newBuilder()
                      .setUuid(id)
                      .build();
    }

    /**
     * Creates a new {@code ReservationId} with a random UUID value.
     */
    public static ReservationId newReservationId() {
        final String id = Identifiers.newUuid();
        return ReservationId.newBuilder()
                            .setUuid(id)
                            .build();
    }

    /**
     * Creates a new {@code ConferenceId} with a random UUID value.
     */
    public static ConferenceId newConferenceId() {
        final String id = Identifiers.newUuid();
        return ConferenceId.newBuilder()
                           .setUuid(id)
                           .build();
    }

    /**
     * Creates a new {@code SeatTypeId} with a random UUID value.
     */
    public static SeatTypeId newSeatTypeId() {
        return SeatTypeId.newBuilder()
                         .setUuid(Identifiers.newUuid())
                         .build();
    }

    /**
     * Creates a new {@code SeatAssignmentsId} with the given UUID value.
     */
    public static SeatTypeId newSeatTypeId(String uuid) {
        return SeatTypeId.newBuilder()
                         .setUuid(uuid)
                         .build();
    }
}
