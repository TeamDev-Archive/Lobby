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

import org.spine.samples.lobby.conference.impl.ConferenceRepository;
import org.spine.samples.lobby.conference.impl.ConferenceServiceImpl;
import org.spine3.base.EmailAddress;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.PersonalInfo;
import org.spine3.samples.lobby.common.util.RandomPasswordGenerator;
import org.spine3.samples.lobby.conference.ConferenceInfo;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.server.BoundedContext;

import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.testdata.TestDataFactory.newBoundedContext;

/**
 * @author andrii.loboda
 */
public class Given {

    /* package */  static final BoundedContext BOUNDED_CONTEXT = newBoundedContext();
    private static final ConferenceRepository CONFERENCE_REPOSITORY = new ConferenceRepository();
    private static final ConferenceId CONFERENCE_ID = newConferenceId();
    private static final ConferenceId PUBLISHED_CONFERENCE_ID = newConferenceId();
    public static final String TEST_EMAIL_ADDRESS = "test@mail.com";


    /* package */ Given() {
        createSampleData();
//        BOUNDED_CONTEXT.getEventBus().register(new StubCommandHandler());
    }


    /* package */
    static ConferenceService getConferenceService() {
        return new TestConferenceService(BOUNDED_CONTEXT, CONFERENCE_REPOSITORY);
    }

    /* package */ ConferenceInfo conferenceInfo() {
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

    /* package */  void dropData() {
        CONFERENCE_REPOSITORY.deleteAll();
    }

    /* package */ Conference newConference() {
        return CONFERENCE_REPOSITORY.load(CONFERENCE_ID);
    }

    /* package */ Conference newPublishedConference() {
        return CONFERENCE_REPOSITORY.load(PUBLISHED_CONFERENCE_ID);
    }


    private static void createSampleData() {
        createUnpublishedConference();
        createPublishedConference();
    }

    private static void createUnpublishedConference() {

        final EmailAddress email = EmailAddress.newBuilder()
                                               .setValue(TEST_EMAIL_ADDRESS)
                                               .build();

        final PersonalInfo owner = PersonalInfo.newBuilder()
                                               .setEmail(email)
                                               .build();

        final String accessCode = RandomPasswordGenerator.generate(6);

        final Conference conference = Conference.newBuilder()
                                                .setId(CONFERENCE_ID)
                                                .setName("Test conference #1")
                                                .setOwner(owner)
                                                .setAccessCode(accessCode)
                                                .build();
        CONFERENCE_REPOSITORY.store(conference);
    }

    private static void createPublishedConference() {
        final Conference conference = Conference.newBuilder()
                                                .setId(PUBLISHED_CONFERENCE_ID)
                                                .setName("Test Published conference #2")
                                                .setIsPublished(true)
                                                .build();
        CONFERENCE_REPOSITORY.store(conference);
    }


    private static class TestConferenceService extends ConferenceServiceImpl {

        private TestConferenceService(BoundedContext boundedContext, ConferenceRepository conferenceRepository) {
            super(boundedContext, conferenceRepository);
        }
    }
}
