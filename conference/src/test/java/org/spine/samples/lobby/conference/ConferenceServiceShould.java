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

import com.google.common.base.Function;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.EmailAddress;
import org.spine3.base.Event;
import org.spine3.protobuf.Messages;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.conference.EditableConferenceInfo;
import org.spine3.samples.lobby.conference.FindConferenceRequest;
import org.spine3.samples.lobby.conference.PublishConferenceRequest;
import org.spine3.samples.lobby.conference.UnpublishConferenceRequest;
import org.spine3.samples.lobby.conference.UpdateConferenceResponse;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.samples.sample.lobby.conference.contracts.ConferencePublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUnpublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUpdated;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.EventStore;
import org.spine3.server.event.EventStreamQuery;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author andrii.loboda
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class ConferenceServiceShould {

    private static final BoundedContext boundedContext = Given.BOUNDED_CONTEXT;
    private static final ConferenceService conferenceService = Given.getConferenceService();
    private static final Void NO_RESULT = null;

    private Given given;

    @Before
    public void setUp() {
        given = new Given();
    }

    @After
    public void tearDown() {
        given.dropData();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void create_conference_and_generate_ConferenceCreated_event() {

        conferenceService.createConference(given.conferenceInfo());

        final EventStore eventStore = boundedContext.getEventBus()
                                                    .getEventStore();

        final TestStreamObserver createdObserver = createCreatedObserver();

        eventStore.read(EventStreamQuery.newBuilder()
                                        .build(), createdObserver);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void load_conference_by_email_and_access_code() {

        final Conference conference = given.newConference();
        final String accessCode = conference.getAccessCode();
        final EmailAddress email = conference.getOwner()
                                             .getEmail();

        final FindConferenceRequest request = FindConferenceRequest.newBuilder()
                                                                   .setAccessCode(accessCode)
                                                                   .setEmailAddress(email)
                                                                   .build();

        final Conference conferenceFound = conferenceService.findConference(request);

        Assert.assertEquals(conferenceFound.getAccessCode(), conference.getAccessCode());
        Assert.assertEquals(conferenceFound.getOwner()
                                           .getEmail(), conference.getOwner()
                                                                  .getEmail());
        Assert.assertEquals(conferenceFound.getId(), conference.getId());

    }


    @SuppressWarnings("unchecked")
    @Test
    public void update_conference_info_and_generate_ConferenceUpdated_event() {
        final Conference conference = given.newConference();
        final String expectedConferenceName = "updated conference";

        final ConferenceId id = conference.getId();

        final EditableConferenceInfo build = EditableConferenceInfo.newBuilder()
                                                                   .setId(id)
                                                                   .setName(expectedConferenceName)
                                                                   .build();

        final UpdateConferenceResponse response = conferenceService.updateConference(build);

        Assert.assertEquals(response.getId(), id);


        final EventStore eventStore = boundedContext.getEventBus()
                                                    .getEventStore();

        final TestStreamObserver updatedObserver = createUpdatedObserver();

        eventStore.read(EventStreamQuery.newBuilder()
                                        .build(), updatedObserver);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void publish_conference_and_generate_ConferencePublished_event() {
        final Conference conference = given.newConference();

        final ConferenceId conferenceId = conference.getId();
        final PublishConferenceRequest publishConferenceRequest = PublishConferenceRequest.newBuilder()
                                                                                          .setId(conferenceId)
                                                                                          .build();

        conferenceService.publish(publishConferenceRequest);

        final Conference conferenceById = conferenceService.findConferenceByID(conferenceId);

        assertTrue("Conference should be published.", conferenceById.getIsPublished());


        final EventStore eventStore = boundedContext.getEventBus()
                                                    .getEventStore();

        final TestStreamObserver publishedObserver = createPublishedObserver();

        eventStore.read(EventStreamQuery.newBuilder()
                                        .build(), publishedObserver);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void unpublish_conference_and_generate_ConferenceUnpublished_event() {
        final Conference conference = given.newPublishedConference();

        final ConferenceId conferenceId = conference.getId();
        final UnpublishConferenceRequest unpublishRequest = UnpublishConferenceRequest.newBuilder()
                                                                                      .setId(conferenceId)
                                                                                      .build();

        conferenceService.unPublish(unpublishRequest);

        final Conference conferenceByID = conferenceService.findConferenceByID(conferenceId);
        assertTrue("Conference should be unpublished.", !conferenceByID.getIsPublished());

        final EventStore eventStore = boundedContext.getEventBus()
                                                    .getEventStore();

        final TestStreamObserver unpublishedObserver = createUnpublishedObserver();

        eventStore.read(EventStreamQuery.newBuilder()
                                        .build(), unpublishedObserver);
    }

    private static TestStreamObserver createUnpublishedObserver() {
        final List<Class> messages = new LinkedList<>();
        final Function<Event, Void> checkConferencePublished = new Function<Event, Void>() {
            @SuppressWarnings("ReturnOfNull")
            @Override
            public Void apply(Event event) {
                messages.add(Messages.fromAny(event.getMessage())
                                     .getClass());
                return NO_RESULT;
            }
        };


        final Function<Void, Void> onCompleteUpdate = new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                assertTrue(messages.contains(ConferenceUnpublished.class));
                return NO_RESULT;
            }
        };

        return TestStreamObserver.<Event>newBuilder()
                                 .setNextFunction(checkConferencePublished)
                                 .setOnCompleteFunction(onCompleteUpdate)
                                 .build();
    }


    private static TestStreamObserver createCreatedObserver() {
        final Function<Event, Void> checkConferenceCreated = new Function<Event, Void>() {
            @SuppressWarnings("ReturnOfNull")
            @Override
            public Void apply(Event event) {
                assertEquals(Messages.fromAny(event.getMessage())
                                     .getClass(), ConferenceCreated.class);
                return NO_RESULT;
            }
        };
        return TestStreamObserver.<Event>newBuilder()
                                 .setNextFunction(checkConferenceCreated)
                                 .build();
    }

    private static TestStreamObserver createUpdatedObserver() {
        final List<Class> messages = new LinkedList<>();
        final Function<Event, Void> onEventThrown = new Function<Event, Void>() {
            @SuppressWarnings("ReturnOfNull")
            @Override
            public Void apply(Event event) {
                messages.add(Messages.fromAny(event.getMessage())
                                     .getClass());
                return NO_RESULT;
            }
        };


        final Function<Void, Void> checkEventThrown = new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                assertTrue(messages.contains(ConferenceUpdated.class));
                return NO_RESULT;
            }
        };

        return TestStreamObserver.<Event>newBuilder()
                                 .setNextFunction(onEventThrown)
                                 .setOnCompleteFunction(checkEventThrown)
                                 .build();
    }


    private static TestStreamObserver createPublishedObserver() {
        final List<Class> messages = new LinkedList<>();
        final Function<Event, Void> checkConferencePublished = new Function<Event, Void>() {
            @SuppressWarnings("ReturnOfNull")
            @Override
            public Void apply(Event event) {
                messages.add(Messages.fromAny(event.getMessage())
                                     .getClass());
                return NO_RESULT;
            }
        };


        final Function<Void, Void> onCompleteUpdate = new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                assertTrue(messages.contains(ConferencePublished.class));
                return NO_RESULT;
            }
        };

        return TestStreamObserver.<Event>newBuilder()
                                 .setNextFunction(checkConferencePublished)
                                 .setOnCompleteFunction(onCompleteUpdate)
                                 .build();
    }
}
