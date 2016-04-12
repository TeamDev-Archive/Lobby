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
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.Event;
import org.spine3.protobuf.Messages;
import org.spine3.samples.lobby.conference.ConferenceServiceGrpc;
import org.spine3.samples.lobby.conference.CreateConferenceResponse;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.EventStore;
import org.spine3.server.event.EventStreamQuery;

import static org.junit.Assert.assertEquals;

/**
 * @author andrii.loboda
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class ConferenceServiceShould {

    private final BoundedContext boundedContext = Given.boundedContext;
    private final ConferenceServiceGrpc.ConferenceService conferenceService = Given.getConferenceService();

    private Given given;

    @Before
    public void setUpTest() {
        given = new Given();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void create_conference_and_generate_ConferenceCreated_event() {
        conferenceService.createConference(given.conferenceInfo(), TestStreamObserver.<CreateConferenceResponse>newBuilder()
                                                                                     .build());

        final EventStore eventStore = boundedContext.getEventBus()
                                                    .getEventStore();

        final TestStreamObserver conferenceCreatedObserver = TestStreamObserver.<Event>newBuilder()
                                                                                    .setNextFunction(new Function<Event, Void>() {
                                                                                        @SuppressWarnings("ReturnOfNull")
                                                                                        @Override
                                                                                        public Void apply(Event event) {
                                                                                            assertEquals(Messages.fromAny(event.getMessage())
                                                                                                                 .getClass(), ConferenceCreated.class);
                                                                                            return null;
                                                                                        }
                                                                                    })
                                                                                    .build();
        eventStore.read(EventStreamQuery.newBuilder()
                                        .build(), conferenceCreatedObserver);
    }


}
