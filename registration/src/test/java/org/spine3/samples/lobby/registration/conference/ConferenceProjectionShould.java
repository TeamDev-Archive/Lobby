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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.StringValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.base.EventContext;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.SeatType;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.samples.lobby.registration.seat.availability.AddSeats;
import org.spine3.samples.lobby.registration.seat.availability.RemoveSeats;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceCreated;
import org.spine3.samples.sample.lobby.conference.contracts.ConferencePublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUnpublished;
import org.spine3.samples.sample.lobby.conference.contracts.ConferenceUpdated;
import org.spine3.samples.sample.lobby.conference.contracts.SeatTypeCreated;
import org.spine3.samples.sample.lobby.conference.contracts.SeatTypeUpdated;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.AggregateRepository;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.util.List;

import static org.junit.Assert.*;
import static org.spine3.samples.lobby.common.util.testdata.TestDataFactory.newBoundedContext;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class ConferenceProjectionShould {

    private static final EventContext CONTEXT = EventContext.getDefaultInstance();
    private final BoundedContext boundedContext = newBoundedContext();
    private final TestConferenceProjection projection = new TestConferenceProjection(Given.CONFERENCE_ID);

    @Before
    public void setUpTest() {
        final TestCommandHandlerRepository repository = new TestCommandHandlerRepository(boundedContext);
        repository.initStorage(InMemoryStorageFactory.getInstance());
        boundedContext.register(repository);
        projection.setCommandBus(boundedContext.getCommandBus());
    }

    @After
    public void tearDownTest() throws Exception {
        boundedContext.close();
    }

    @Test
    public void handle_ConferenceCreated_event_and_update_state() {
        final ConferenceCreated event = Given.conferenceCreated();

        projection.on(event, CONTEXT);

        assertEquals(event.getConference(), projection.getState());
    }

    @Test
    public void handle_ConferenceUpdated_event_and_update_state() {
        final ConferenceUpdated event = Given.conferenceUpdated();

        projection.on(event, CONTEXT);

        assertEquals(event.getConference(), projection.getState());
    }

    @Test
    public void handle_ConferencePublished_event_and_update_state() {
        final ConferencePublished event = Given.conferencePublished();

        projection.on(event, CONTEXT);

        assertTrue(projection.getState().getIsPublished());
    }

    @Test
    public void handle_ConferenceUnpublished_event_and_update_state() {
        final ConferenceUnpublished event = Given.conferenceUnpublished();

        projection.on(event, CONTEXT);

        assertFalse(projection.getState().getIsPublished());
    }

    @Test
    public void handle_SeatTypeCreated_event_and_update_state() {
        projection.incrementState(Given.conference());
        final SeatTypeCreated event = Given.seatTypeCreated(5);

        projection.on(event, CONTEXT);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void not_add_seat_type_with_the_same_id_on_SeatTypeCreated_event() {
        projection.incrementState(Given.conference());
        final SeatTypeCreated event = Given.seatTypeCreated(5);

        projection.on(event, CONTEXT);
        projection.on(event, CONTEXT);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_update_state() {
        projection.incrementState(Given.conference());
        projection.on(Given.seatTypeCreated("old-description", 5), CONTEXT);

        final SeatTypeUpdated updatedEvent = Given.seatTypeUpdated("new-description", 7);
        projection.on(updatedEvent, CONTEXT);

        assertSeatTypesConsistOf(updatedEvent.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_added() {
        projection.incrementState(Given.conference());

        projection.on(Given.seatTypeCreated(3), CONTEXT);
        projection.on(Given.seatTypeUpdated(7), CONTEXT);

        assertTrue(TestCommandHandler.isAddSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_removed() {
        projection.incrementState(Given.conference());

        projection.on(Given.seatTypeCreated(7), CONTEXT);
        projection.on(Given.seatTypeUpdated(3), CONTEXT);

        assertTrue(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_do_not_send_commands_when_seat_quantity_not_changed() {
        projection.incrementState(Given.conference());
        final int seatQuantity = 5;

        projection.on(Given.seatTypeCreated(seatQuantity), CONTEXT);
        TestCommandHandler.setIsAddSeatsCommandHandled(false);

        projection.on(Given.seatTypeUpdated(seatQuantity), CONTEXT);

        assertFalse(TestCommandHandler.isAddSeatsCommandHandled());
        assertFalse(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    private void assertSeatTypesConsistOf(SeatType... expectedSeatTypes) {
        final ImmutableList<SeatType> expectedTypes = ImmutableList.copyOf(expectedSeatTypes);
        final List<SeatType> actualTypes = projection.getState().getSeatTypeList();
        assertEquals(expectedTypes.size(), actualTypes.size());
        assertTrue(actualTypes.containsAll(expectedTypes));
    }

    private static class TestConferenceProjection extends ConferenceProjection {

        private TestConferenceProjection(ConferenceId id) {
            super(id);
        }

        // Is overridden to do not throw exceptions while retrieving the default state via reflection.
        @Override
        @SuppressWarnings("RefusedBequest")
        protected Conference getDefaultState() {
            return Conference.getDefaultInstance();
        }

        @VisibleForTesting
        @Override
        public void incrementState(Conference newState) {
            super.incrementState(newState);
        }
    }

    /**
     * Handles commands sent by ConferenceProjection.
     */
    private static class TestCommandHandlerRepository extends AggregateRepository<ConferenceId, TestCommandHandler> {

        private TestCommandHandlerRepository(BoundedContext boundedContext) {
            super(boundedContext);
        }
    }

    @SuppressWarnings({"StaticNonFinalField", "AssignmentToStaticFieldFromInstanceMethod"})
    private static class TestCommandHandler extends Aggregate<ConferenceId, StringValue, StringValue.Builder> {

        private static boolean isAddSeatsCommandHandled = false;
        private static boolean isRemoveSeatsCommandHandled = false;

        @SuppressWarnings("PublicConstructorInNonPublicClass") // it is required
        public TestCommandHandler(ConferenceId id) {
            super(id);
            isAddSeatsCommandHandled = false;
            isRemoveSeatsCommandHandled = false;
        }

        @Assign
        public StringValue handle(AddSeats cmd, CommandContext ctx) {
            isAddSeatsCommandHandled = true;
            return StringValue.getDefaultInstance();
        }

        @Assign
        public StringValue handle(RemoveSeats cmd, CommandContext ctx) {
            isRemoveSeatsCommandHandled = true;
            return StringValue.getDefaultInstance();
        }

        @Apply
        private void event(StringValue event) {
        }

        public static boolean isAddSeatsCommandHandled() {
            return isAddSeatsCommandHandled;
        }

        public static void setIsAddSeatsCommandHandled(boolean value) {
            isAddSeatsCommandHandled = value;
        }

        public static boolean isRemoveSeatsCommandHandled() {
            return isRemoveSeatsCommandHandled;
        }
    }
}
