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

package org.spine3.samples.lobby.registration.conference;

import com.google.protobuf.Descriptors;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.ConferenceSlug;
import org.spine3.samples.lobby.common.SeatType;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.samples.sample.lobby.conference.contracts.ConferencePublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUnpublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUpdated;
import org.spine3.samples.sample.lobby.conference.contracts.SeatTypeCreated;
import org.spine3.samples.sample.lobby.conference.contracts.SeatTypeUpdated;

import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.IdFactory.newSeatTypeId;

/**
 * The test data factory for {@link ConferenceProjection} and {@link ConferenceProjectionRepository} tests.
 *
 * @author Alexander Litus
 */
@SuppressWarnings("UtilityClass")
class Given {

    static final ConferenceId CONFERENCE_ID = newConferenceId();
    private static final ConferenceUnpublished CONFERENCE_UNPUBLISHED = ConferenceUnpublished.newBuilder()
                                                                                             .setConferenceId(CONFERENCE_ID)
                                                                                             .build();
    private static final ConferencePublished CONFERENCE_PUBLISHED = ConferencePublished.newBuilder()
                                                                                       .setConferenceId(CONFERENCE_ID)
                                                                                       .build();
    private static final SeatTypeId SEAT_TYPE_ID = newSeatTypeId();
    private static final String CONFERENCE_NAME = "Test Conference";
    private static final String TWITTER_SEARCH = CONFERENCE_NAME + " twitter";
    private static final String TAGLINE = CONFERENCE_NAME + " tagline";
    private static final String LOCATION = CONFERENCE_NAME + " location";
    private static final String DESCRIPTION = CONFERENCE_NAME + " description";
    private static final ConferenceSlug.Builder SLUG = ConferenceSlug.newBuilder()
                                                                     .setValue("slug");
    private static final Conference CONFERENCE = Conference.newBuilder()
                                                           .setId(CONFERENCE_ID)
                                                           .setSlug(SLUG)
                                                           .setName(CONFERENCE_NAME)
                                                           .setDescription(DESCRIPTION)
                                                           .setLocation(LOCATION)
                                                           .setTagline(TAGLINE)
                                                           .setTwitterSearch(TWITTER_SEARCH)
                                                           .build();

    private Given() {
    }

    static ConferenceCreated conferenceCreated() {
        final Conference conference = conference();
        return ConferenceCreated.newBuilder()
                                .setConference(conference)
                                .build();
    }

    static ConferenceUpdated conferenceUpdated() {
        final Conference conference = conference();
        return ConferenceUpdated.newBuilder()
                                .setConference(conference)
                                .build();
    }

    static ConferencePublished conferencePublished() {
        return CONFERENCE_PUBLISHED;
    }

    static ConferenceUnpublished conferenceUnpublished() {
        return CONFERENCE_UNPUBLISHED;
    }

    static SeatTypeCreated seatTypeCreated(int seatQuantity) {
        final SeatType seatType = newSeatType("descriptionForSeatTypeCreatedEvent", seatQuantity);
        return SeatTypeCreated.newBuilder()
                              .setSeatType(seatType)
                              .build();
    }

    static SeatTypeUpdated seatTypeUpdated(int seatQuantity) {
        final SeatType seatType = newSeatType("descriptionForSeatTypeUpdatedEvent", seatQuantity);
        return SeatTypeUpdated.newBuilder()
                              .setSeatType(seatType)
                              .build();
    }

    static SeatTypeCreated seatTypeCreated(String description, int seatQuantity) {
        final SeatType seatType = newSeatType(description, seatQuantity);
        return SeatTypeCreated.newBuilder()
                              .setSeatType(seatType)
                              .build();
    }

    static SeatTypeUpdated seatTypeUpdated(String description, int seatQuantity) {
        final SeatType seatType = newSeatType(description, seatQuantity);
        return SeatTypeUpdated.newBuilder()
                              .setSeatType(seatType)
                              .build();
    }

    static Conference conference() {
        return CONFERENCE;
    }

    static SeatType newSeatType(String description, int seatQuantity) {
        final SeatType.Builder result = SeatType.newBuilder()
                                                .setConferenceId(CONFERENCE_ID)
                                                .setId(SEAT_TYPE_ID)
                                                .setDescription(description)
                                                .setQuantityTotal(seatQuantity);
        return result.build();
    }
}
