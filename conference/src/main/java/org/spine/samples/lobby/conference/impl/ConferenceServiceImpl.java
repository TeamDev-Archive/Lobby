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

package org.spine.samples.lobby.conference.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.protobuf.Message;
import org.spine.samples.lobby.conference.ConferenceService;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.base.EventId;
import org.spine3.base.Identifiers;
import org.spine3.protobuf.AnyPacker;
import org.spine3.protobuf.Timestamps;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.SeatType;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.common.util.RandomPasswordGenerator;
import org.spine3.samples.lobby.conference.ConferenceInfo;
import org.spine3.samples.lobby.conference.CreateConferenceResponse;
import org.spine3.samples.lobby.conference.EditableConferenceInfo;
import org.spine3.samples.lobby.conference.FindConferenceRequest;
import org.spine3.samples.lobby.conference.PublishConferenceRequest;
import org.spine3.samples.lobby.conference.PublishConferenceResponse;
import org.spine3.samples.lobby.conference.UnpublishConferenceRequest;
import org.spine3.samples.lobby.conference.UnpublishConferenceResponse;
import org.spine3.samples.lobby.conference.UpdateConferenceResponse;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.samples.sample.lobby.conference.contracts.ConferencePublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUnpublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUpdated;
import org.spine3.samples.sample.lobby.conference.contracts.SeatTypeCreated;
import org.spine3.server.BoundedContext;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static org.spine.samples.lobby.conference.impl.EventFactory.*;
import static org.spine3.base.Events.createEvent;

/**
 * @author andrii.loboda
 */
public class ConferenceServiceImpl implements ConferenceService {
    private final BoundedContext boundedContext;
    private final ConferenceRepository conferenceRepository;


    public ConferenceServiceImpl(BoundedContext boundedContext, ConferenceRepository conferenceRepository) {
        this.boundedContext = boundedContext;
        this.conferenceRepository = conferenceRepository;
    }

    @Override
    public CreateConferenceResponse createConference(ConferenceInfo conferenceInfo) {
        final String accessCode = generateAccessCode();
        final Conference conference = asConference(conferenceInfo);
        final Conference conferenceToPersist = conference.toBuilder()
                                                         .setAccessCode(accessCode)
                                                         .build();
        conferenceRepository.store(conferenceToPersist);

        final ConferenceCreated conferenceCreatedEvent = conferenceCreated(conference);
        postEvents(conference, conferenceCreatedEvent);

        final CreateConferenceResponse response = CreateConferenceResponse.newBuilder()
                                                                          .setId(conferenceToPersist.getId())
                                                                          .setAccessCode(accessCode)
                                                                          .build();
        return response;
    }

    @Override
    public Conference findConference(FindConferenceRequest request) {
        final Conference conference = conferenceRepository.load(request.getEmailAddress(), request.getAccessCode());
        return conference;
    }

    @Override
    public UpdateConferenceResponse updateConference(EditableConferenceInfo conferenceInfo) {
        final Conference existingConference = conferenceRepository.load(conferenceInfo.getId());

        checkNotNull(existingConference, "Can't find the conference with specified id: " + conferenceInfo.getId());

        final Conference conference = updateFromConferenceInfo(existingConference, conferenceInfo);
        conferenceRepository.store(conference);

        final ConferenceUpdated conferenceUpdatedEvent = conferenceUpdated(conference);
        postEvents(conference, conferenceUpdatedEvent);

        final UpdateConferenceResponse response = UpdateConferenceResponse.newBuilder()
                                                                          .setId(conference.getId())
                                                                          .build();

        return response;
    }

    @Override
    public PublishConferenceResponse publish(PublishConferenceRequest request) {
        final Conference conference = conferenceRepository.load(request.getId());

        checkNotNull(conference, "No conference found");
        checkState(!conference.getIsPublished(), "Conference is already published");

        final Conference publishedConference = conference.toBuilder()
                                                         .setIsPublished(true)
                                                         .build();

        conferenceRepository.store(publishedConference);

        final ConferencePublished conferencePublishedEvent = conferencePublished(publishedConference);
        postEvents(publishedConference, conferencePublishedEvent);

        final PublishConferenceResponse response = PublishConferenceResponse.newBuilder()
                                                                            .setId(publishedConference.getId())
                                                                            .build();
        return response;
    }

    @Override
    public Conference findConferenceByID(ConferenceId conferenceId) {
        final Conference conference = conferenceRepository.load(conferenceId);
        return conference;
    }

    @Override
    public UnpublishConferenceResponse unPublish(UnpublishConferenceRequest request) {
        final Conference conference = conferenceRepository.load(request.getId());

        checkNotNull(conference, "No conference found with id: %s", request.getId());
        checkState(conference.getIsPublished(), "Conference is already unpublished with ID: %s", conference.getId());

        final Conference unpublishedConference = conference.toBuilder()
                                                           .setIsPublished(false)
                                                           .build();

        conferenceRepository.store(unpublishedConference);

        final ConferenceUnpublished unpublishedEvent = conferenceUnPublished(unpublishedConference);
        postEvents(unpublishedConference, unpublishedEvent);

        final UnpublishConferenceResponse response = UnpublishConferenceResponse.newBuilder()
                                                                                .setId(unpublishedConference.getId())
                                                                                .build();

        return response;
    }

    @Override
    public void createSeat(ConferenceId conferenceId, SeatType seatType) {
        final Conference conference = conferenceRepository.load(conferenceId);
        checkNotNull(conference, "There is no conference with id: %s", conferenceId);

        final Conference conferenceWithSeats = conference.toBuilder()
                                                         .addSeatType(seatType)
                                                         .build();
        conferenceRepository.store(conferenceWithSeats);

        if (conferenceWithSeats.getIsPublished()) {
            final SeatTypeCreated seatTypeCreated = seatTypeCreated(seatType);
            postEvents(conferenceWithSeats, seatTypeCreated);
        }
    }

    @Override
    public Set<SeatType> findSeatTypes(ConferenceId conferenceId) {
        final Conference conference = conferenceRepository.load(conferenceId);
        checkNotNull(conference, "There is no conference with id: %s", conferenceId);
        return newHashSet(conference.getSeatTypeList());
    }

    @Override
    public SeatType findSeatType(final SeatTypeId seatTypeId) {
        final Conference conference = conferenceRepository.load(seatTypeId);
        checkNotNull(conference, "There is no conference which contains seatType with ID: %s", seatTypeId);
        final SeatType type = Iterables.find(conference.getSeatTypeList(), new Predicate<SeatType>() {
            @Override
            public boolean apply(SeatType input) {
                return input.getId()
                            .equals(seatTypeId);
            }
        });
        return type;
    }

    private static Conference updateFromConferenceInfo(Conference target, EditableConferenceInfo info) {
        return target.toBuilder()
                     .setName(info.getName())
                     .build();
    }


    private static EventContext createConferenceEventContext(ConferenceId conferenceId) {

        final EventId eventIDMessage = EventId.newBuilder()
                                              .setUuid(Identifiers.newUuid())
                                              .build();

        final EventContext eventContext = EventContext.newBuilder()
                                                      .setEventId(eventIDMessage)
                                                      .setProducerId(AnyPacker.pack(conferenceId))
                                                      .setTimestamp(Timestamps.getCurrentTime())
                                                      .build();
        return eventContext;
    }

    private void postEvents(Conference conference, Message... messages) {

        final EventContext context = createConferenceEventContext(conference.getId());

        for (Message message : messages) {
            final Event event = createEvent(message, context);
            boundedContext.getEventBus()
                          .post(event);
        }
    }

    private static String generateAccessCode() {
        return RandomPasswordGenerator.generate(6);
    }

    private static Conference asConference(ConferenceInfo info) {
        final ConferenceId conferenceId = ConferenceId.newBuilder()
                                                      .setUuid(Identifiers.newUuid())
                                                      .build();
        return Conference.newBuilder()
                         .setId(conferenceId)
                         .setName(info.getName())
                         .setOwner(info.getOwner())
                         .build();
    }
}
