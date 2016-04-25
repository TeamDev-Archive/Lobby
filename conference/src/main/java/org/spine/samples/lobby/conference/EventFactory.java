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

package org.spine.samples.lobby.conference;

import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.samples.sample.lobby.conference.contracts.ConferencePublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUnpublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUpdated;

/**
 * @author andrii.loboda
 */
@SuppressWarnings("UtilityClass")
/* package */ class EventFactory {
    private EventFactory() {
    }

    /* package */
    static ConferenceCreated conferenceCreated(Conference conference) {
        return ConferenceCreated.newBuilder()
                                .setConference(conference)
                                .build();
    }

    /* package */
    static ConferenceUpdated conferenceUpdated(Conference updatedConference) {
        return ConferenceUpdated.newBuilder()
                                .setConference(updatedConference)
                                .build();
    }

    /* package */
    static ConferencePublished conferencePublished(Conference publishedConference) {
        return ConferencePublished.newBuilder()
                                  .setConferenceId(publishedConference.getId())
                                  .build();

    }

    /* package */
    static ConferenceUnpublished conferenceUnPublished(Conference conference) {
        return ConferenceUnpublished.newBuilder()
                                    .setConferenceId(conference.getId())
                                    .build();

    }

}
