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

import org.spine3.base.EmailAddress;
import org.spine3.samples.lobby.common.PersonalInfo;
import org.spine3.samples.lobby.conference.ConferenceInfo;
import org.spine3.samples.lobby.conference.ConferenceServiceGrpc;
import org.spine3.server.BoundedContext;

/**
 * @author andrii.loboda
 */
public class Given {

    protected static final BoundedContext boundedContext = ConferenceBoundedContext.INSTANCE.get();
    private static final ConferenceRepository conferenceRepository = new ConferenceRepository();


    /* package */ Given() {
//        boundedContext.getEventBus().register(new StubCommandHandler());
    }


    public static ConferenceServiceGrpc.ConferenceService getConferenceService() {
        return new TestConferenceService();
    }

    public ConferenceInfo conferenceInfo() {
        final EmailAddress email = EmailAddress.newBuilder()
                                               .setValue("andrii.serebriyan@gmail.com")
                                               .build();
        final PersonalInfo owner = PersonalInfo.newBuilder()
                                               .setEmail(email)
                                               .build();
        return ConferenceInfo.newBuilder()
                             .setName("Test Conference #1")
                             .setOwner(owner)
                             .build();
    }

    public void dropData() {
        conferenceRepository.deleteAll();
    }


    private static class TestConferenceService extends ConferenceServiceImpl {
        @Override
        protected BoundedContext getBoundedContext() {
            return boundedContext;
        }

        @Override
        protected ConferenceRepository getRepository() {
            return conferenceRepository;
        }
    }
}