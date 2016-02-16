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

package org.spine3.samples.lobby.registration.seat.assignment;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.Event;
import org.spine3.samples.lobby.registration.contracts.SeatAssignment;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsCreated;
import org.spine3.server.BoundedContext;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newBoundedContext;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newEvent;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatAssignmentsId;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class SeatAssignmentsRepositoryShould {

    private SeatAssignmentsRepository repository;

    @Before
    public void setUpTest() {
        final BoundedContext boundedContext = newBoundedContext();
        repository = new SeatAssignmentsRepository(boundedContext);
        repository.initStorage(InMemoryStorageFactory.getInstance());
        boundedContext.register(repository);
    }

    @Test
    public void store_and_load_aggregate() {
        final TestSeatAssignmentsAggregate expectedAggregate = new TestSeatAssignmentsAggregate();
        final List<SeatAssignment> expectedAssignments = expectedAggregate.getAssignments();

        repository.store(expectedAggregate);

        final SeatAssignmentsAggregate actualAggregate = repository.load(expectedAggregate.getId());
        final SeatAssignments actualState = actualAggregate.getState();
        final Collection<SeatAssignment> actualAssignments = actualState.getAssignments().values();
        assertEquals(expectedAssignments.size(), actualAssignments.size());
        assertTrue(expectedAssignments.containsAll(actualAssignments));
    }

    private static class TestSeatAssignmentsAggregate extends SeatAssignmentsAggregate {

        private final SeatAssignmentsCreated event = Given.Event.seatAssignmentsCreated();

        private TestSeatAssignmentsAggregate() {
            super(newSeatAssignmentsId());
        }

        @Override
        @SuppressWarnings("RefusedBequest")
        public List<Event> getStateChangingUncommittedEvents() {
            return ImmutableList.of(newEvent(event));
        }

        public List<SeatAssignment> getAssignments() {
            return event.getAssignmentList();
        }
    }
}
