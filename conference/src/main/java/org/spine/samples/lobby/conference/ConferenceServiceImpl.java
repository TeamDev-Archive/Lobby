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
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.conference.ConferenceInfo;
import org.spine3.samples.lobby.conference.ConferenceServiceGrpc.ConferenceService;
import org.spine3.samples.lobby.conference.CreateConferenceResponse;
import org.spine3.samples.lobby.conference.EditableConferenceInfo;
import org.spine3.samples.lobby.conference.FindConferenceRequest;
import org.spine3.samples.lobby.conference.UpdateConferenceResponse;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.server.BoundedContext;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author andrii.loboda
 */
public class ConferenceServiceImpl implements ConferenceService {


    private final BoundedContext boundedContext;
    private final ConferenceRepository conferenceRepository;


    public ConferenceServiceImpl() {
        boundedContext = getBoundedContext();
        conferenceRepository = getRepository();
    }


    protected BoundedContext getBoundedContext() {
        return null;
    }

    protected ConferenceRepository getRepository() {
        return new ConferenceRepository();
    }

    @Override
    public void createConference(ConferenceInfo conferenceToCreate, StreamObserver<CreateConferenceResponse> responseObserver) {

        final ImmutableList.Builder<Message> result = ImmutableList.builder();
        final ConferenceCreated conferenceCreatedEvent = EventFactory.conferenceCreated(conferenceToCreate);

        result.add(conferenceCreatedEvent);

        final ImmutableList<Message> eventsToSend = result.build();

        final String accessCode = generateAccessCode();
        final Conference conference = asConference(conferenceToCreate);
        final Conference conferenceToPersist = conference.toBuilder()
                                                         .setAccessCode(accessCode)
                                                         .build();
        conferenceRepository.store(conferenceToPersist);
        sendEvents(eventsToSend);

        final CreateConferenceResponse build = CreateConferenceResponse.newBuilder()
                                                                       .setId(conferenceToPersist.getId())
                                                                       .setAccessCode(accessCode)
                                                                       .build();

        responseObserver.onNext(build);
        responseObserver.onCompleted();


    }

    private static String generateAccessCode() {
        final SecureRandom random = new SecureRandom();
        final int encodingBase = 32;
        final int numBits = 130;
        return new BigInteger(numBits, random).toString(encodingBase)
                                              .substring(0, 6);
    }

    @Override
    public void findConference(FindConferenceRequest request, StreamObserver<Conference> responseObserver) {
        final Conference conference = conferenceRepository.loadByEmailAndAccessCode(request.getEmailAddress(), request.getAccessCode());


        responseObserver.onNext(conference);
        responseObserver.onCompleted();
    }

    @Override
    public void updateConference(EditableConferenceInfo request, StreamObserver<UpdateConferenceResponse> responseObserver) {

    }

    private static Conference asConference(ConferenceInfo info) {
        return Conference.newBuilder()
                         .setId(ConferenceId.newBuilder()
                                            .setUuid(Identifiers.newUuid()))
                         .setName(info.getName())
                         .setOwner(info.getOwner())
                         .build();
    }

    private void sendEvents(ImmutableList<Message> eventsToSend) {
        for (Message message : eventsToSend) {
            final Event event = Events.createEvent(message, EventUtils.createConferenceEventContext());
            boundedContext.getEventBus()
                          .post(event);
        }
    }

}
