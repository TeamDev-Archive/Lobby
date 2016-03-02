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

package org.spine3.samples.lobby.registration.procman;

import com.google.protobuf.Timestamp;
import org.junit.Before;
import org.junit.Test;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.server.BoundedContext;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import static com.google.protobuf.util.TimeUtil.getCurrentTime;
import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.IdFactory.newOrderId;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State.PAYMENT_RECEIVED;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newBoundedContext;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newProcessManagerId;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class RegistrationProcessManagerRepositoryShould {

    private RegistrationProcessManagerRepository repository;

    @Before
    public void setUpTest() {
        final BoundedContext boundedContext = newBoundedContext();
        repository = new RegistrationProcessManagerRepository(boundedContext);
        repository.initStorage(InMemoryStorageFactory.getInstance());
        boundedContext.register(repository);
    }

    @Test
    public void store_and_load_process_manager() {
        final TestProcessManager expected = new TestProcessManager();

        repository.store(expected);

        final RegistrationProcessManager actual = repository.load(expected.getId());
        assertEquals(expected.getState(), actual.getState());
    }

    private static class TestProcessManager extends RegistrationProcessManager {

        private static final ProcessManagerId ID = newProcessManagerId();
        private static final OrderId ORDER_ID = newOrderId();
        private static final ConferenceId CONFERENCE_ID = newConferenceId();

        private TestProcessManager() {
            super(ID);
            final Timestamp currentTime = getCurrentTime();
            final RegistrationProcess newState = getState().toBuilder()
                    .setProcessState(PAYMENT_RECEIVED)
                    .setOrderId(ORDER_ID)
                    .setConferenceId(CONFERENCE_ID)
                    .setReservationAutoExpiration(currentTime)
                    .build();
            incrementState(newState);
        }

        @Override
        @SuppressWarnings("RefusedBequest") // is overridden to do not throw exceptions on default state obtaining
        protected RegistrationProcess getDefaultState() {
            return RegistrationProcess.getDefaultInstance();
        }
    }
}
