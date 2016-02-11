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

package org.spine3.samples.lobby.registration.conference;

import org.junit.Before;
import org.junit.Test;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.server.BoundedContext;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newBoundedContext;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class ConferenceProjectionRepositoryShould {

    private ConferenceProjectionRepository repository;
    private final ConferenceId id = newConferenceId();

    @Before
    public void setUpTest() {
        final BoundedContext boundedContext = newBoundedContext();
        repository = new ConferenceProjectionRepository(boundedContext);
        repository.initStorage(InMemoryStorageFactory.getInstance());
        boundedContext.register(repository);
    }

    @Test
    public void store_and_load_projection() {
        final ConferenceProjection expected = givenConferenceProjection();

        repository.store(expected);

        final ConferenceProjection actual = repository.load(id);
        assertEquals(expected.getState(), actual.getState());
    }

    private ConferenceProjection givenConferenceProjection() {
        final TestConferenceProjection projection = new TestConferenceProjection(id);
        final Conference conference = Given.conference();
        projection.incrementState(conference);
        return projection;
    }

    public static class TestConferenceProjection extends ConferenceProjection {

        public TestConferenceProjection(ConferenceId id) {
            super(id);
        }

        // Is overridden to make it accessible in tests.
        @Override
        public void incrementState(Conference newState) {
            super.incrementState(newState);
        }
    }
}
