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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import io.grpc.stub.StreamObserver;
import org.spine3.base.Event;
import org.spine3.base.Events;
import org.spine3.base.Identifiers;
import org.spine3.samples.lobby.conference.ConferenceInfo;
import org.spine3.samples.lobby.conference.ConferenceServiceGrpc.ConferenceService;
import org.spine3.samples.lobby.conference.CreateConferenceResponse;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.server.BoundedContext;

import java.util.UUID;

/**
 * @author andrii.loboda
 */
public class ConferenceServiceImpl implements ConferenceService {

    private final BoundedContext boundedContext;


    public ConferenceServiceImpl() {
        boundedContext = getBoundedContext();
    }

    protected BoundedContext getBoundedContext() {
        return null;
    }

    @Override
    public void createConference(ConferenceInfo conferenceToCreate, StreamObserver<CreateConferenceResponse> responseObserver) {

        final ImmutableList.Builder<Message> result = ImmutableList.builder();
        final ConferenceCreated conferenceCreatedEvent = EventFactory.conferenceCreated(conferenceToCreate);

        result.add(conferenceCreatedEvent);

        final ImmutableList<Message> eventsToSend = result.build();

        sendEvents(eventsToSend);

        final CreateConferenceResponse build = CreateConferenceResponse.newBuilder()
                                                                       .setId(Identifiers.newUuid())
                                                                       .build();

        responseObserver.onNext(build);
        responseObserver.onCompleted();
    }

    private void sendEvents(ImmutableList<Message> eventsToSend) {
        for (Message message : eventsToSend) {
            final Event event = Events.createEvent(message, EventUtils.createConferenceEventContext());
            boundedContext.getEventBus()
                          .post(event);
        }
    }

}
