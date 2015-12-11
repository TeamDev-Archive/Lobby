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

package lobby.registration.projection;

import lobby.contracts.common.ConferenceId;
import lobby.contracts.common.SeatType;
import lobby.contracts.conference.*;
import lobby.registration.Conference;
import org.spine3.eventbus.Subscribe;
import org.spine3.server.projection.Projection;

import static lobby.registration.Conference.PublishingStatus.NOT_PUBLISHED;
import static lobby.registration.Conference.PublishingStatus.PUBLISHED;

/**
 * The projection of a conference.
 *
 * @see Projection
 * @author Alexander Litus
 */
@SuppressWarnings("TypeMayBeWeakened")
public class ConferenceProjection extends Projection<ConferenceId, Conference> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public ConferenceProjection(ConferenceId id) {
        super(id);
    }

    @Override
    protected Conference getDefaultState() {
        return Conference.getDefaultInstance();
    }

    @Subscribe
    public void on(ConferenceCreated event) {
        final Conference newState = convert(event.getConference());
        incrementState(newState);
    }

    @Subscribe
    public void on(ConferenceUpdated event) {
        final Conference newState = convert(event.getConference());
        incrementState(newState);
    }

    @Subscribe
    public void on(ConferencePublished event) {
        updatePublishingStatus(PUBLISHED);
    }

    @Subscribe
    public void on(ConferenceUnpublished event) {
        updatePublishingStatus(NOT_PUBLISHED);
    }

    private void updatePublishingStatus(Conference.PublishingStatus status) {
        final Conference.Builder conference = getState().toBuilder();
        conference.setPublishingStatus(status);
        incrementState(conference.build());
    }

    @Subscribe
    public void on(SeatTypeCreated event) {
        updateSeatType(event.getSeatType());
    }

    @Subscribe
    public void on(SeatTypeUpdated event) {
        updateSeatType(event.getSeatType());
    }

    private void updateSeatType(SeatType seatType) {
        final Conference.Builder conference = getState().toBuilder();
        conference.addSeatType(seatType);
        incrementState(conference.build());
    }

    private static Conference convert(lobby.contracts.conference.Conference c) {
        final Conference.Builder newState = Conference.newBuilder()
                .setId(c.getId())
                .setSlug(c.getSlug())
                .setName(c.getName())
                .setDescription(c.getDescription())
                .setLocation(c.getLocation())
                .setTagline(c.getTagline())
                .setTwitterSearch(c.getTwitterSearch())
                .setStartDate(c.getStartDate());
        return newState.build();
    }
}
