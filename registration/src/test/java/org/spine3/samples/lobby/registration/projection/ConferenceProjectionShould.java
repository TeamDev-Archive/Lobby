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

package org.spine3.samples.lobby.registration.projection;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.StringValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.eventbus.EventBus;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.ConferenceSlug;
import org.spine3.samples.lobby.common.SeatType;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.Conference;
import org.spine3.samples.lobby.registration.seat.availability.AddSeats;
import org.spine3.samples.lobby.registration.seat.availability.RemoveSeats;
import org.spine3.samples.sample.lobby.conference.contracts.*;
import org.spine3.server.Assign;
import org.spine3.server.BoundedContext;
import org.spine3.server.CommandDispatcher;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.AggregateRepositoryBase;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.time.LocalDate;
import org.spine3.util.Identifiers;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.spine3.samples.lobby.registration.Conference.PublishingStatus.NOT_PUBLISHED;
import static org.spine3.samples.lobby.registration.Conference.PublishingStatus.PUBLISHED;
import static org.spine3.samples.lobby.registration.projection.ConferenceProjection.convert;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened", "InstanceMethodNamingConvention", "ClassWithTooManyMethods"})
public class ConferenceProjectionShould {

    private final ConferenceId id = ConferenceId.newBuilder().setUuid(Identifiers.newUuid()).build();
    private final TestConferenceProjection projection = new TestConferenceProjection(id);
    private final BoundedContext boundedContext = buildBoundedContext();

    @Before
    public void setUpTest() {
        boundedContext.register(new TestCommandHandlerRepository());
        projection.setBoundedContext(boundedContext);
    }

    static BoundedContext buildBoundedContext() {
        return BoundedContext.newBuilder()
                .setStorageFactory(InMemoryStorageFactory.getInstance())
                .setCommandDispatcher(CommandDispatcher.getInstance())
                .setEventBus(EventBus.newInstance())
                .build();
    }

    @After
    public void tearDownTest() throws IOException {
        boundedContext.close();
    }

    @Test
    public void handle_ConferenceCreated_event_and_update_state() {
        final ConferenceCreated event = conferenceCreated();

        projection.on(event);

        final Conference expectedState = convert(event.getConference());
        final Conference actualState = projection.getState();
        assertEquals(expectedState, actualState);
    }

    @Test
    public void handle_ConferenceUpdated_event_and_update_state() {
        final ConferenceUpdated event = conferenceUpdated();

        projection.on(event);

        final Conference expectedState = convert(event.getConference());
        final Conference actualState = projection.getState();
        assertEquals(expectedState, actualState);
    }

    @Test
    public void handle_ConferencePublished_event_and_update_state() {
        final ConferencePublished event = conferencePublished();

        projection.on(event);

        final Conference.PublishingStatus status = projection.getState().getPublishingStatus();
        assertEquals(PUBLISHED, status);
    }

    @Test
    public void handle_ConferenceUnpublished_event_and_update_state() {
        final ConferenceUnpublished event = conferenceUnpublished();

        projection.on(event);

        final Conference.PublishingStatus status = projection.getState().getPublishingStatus();
        assertEquals(NOT_PUBLISHED, status);
    }

    @Test
    public void handle_SeatTypeCreated_event_and_update_state() {
        projection.incrementState(convert(buildConferenceForEvent()));
        final SeatTypeCreated event = seatTypeCreated(5);

        projection.on(event);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void not_add_seat_type_with_the_same_id_on_SeatTypeCreated_event() {
        projection.incrementState(convert(buildConferenceForEvent()));
        final SeatTypeCreated event = seatTypeCreated(5);

        projection.on(event);
        projection.on(event);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_update_state() {
        projection.incrementState(convert(buildConferenceForEvent()));
        projection.on(seatTypeCreated("old-description", 5));

        final SeatTypeUpdated updatedEvent = seatTypeUpdated("new-description", 7);
        projection.on(updatedEvent);

        assertSeatTypesConsistOf(updatedEvent.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_added() {
        projection.incrementState(convert(buildConferenceForEvent()));

        projection.on(seatTypeCreated(3));
        projection.on(seatTypeUpdated(7));

        assertTrue(TestCommandHandler.isAddSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_removed() {
        projection.incrementState(convert(buildConferenceForEvent()));

        projection.on(seatTypeCreated(7));
        projection.on(seatTypeUpdated(3));

        assertTrue(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_do_not_send_commands_when_seat_quantity_not_changed() {
        projection.incrementState(convert(buildConferenceForEvent()));
        final int seatQuantity = 5;

        projection.on(seatTypeCreated(seatQuantity));
        TestCommandHandler.setIsAddSeatsCommandHandled(false);

        projection.on(seatTypeUpdated(seatQuantity));

        assertFalse(TestCommandHandler.isAddSeatsCommandHandled());
        assertFalse(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    private void assertSeatTypesConsistOf(SeatType... expectedSeatTypes) {
        final ImmutableList<SeatType> expectedTypes = ImmutableList.copyOf(expectedSeatTypes);
        final List<SeatType> actualTypes = projection.getState().getSeatTypeList();
        assertEquals(expectedTypes.size(), actualTypes.size());
        assertTrue(actualTypes.containsAll(expectedTypes));
    }

    private ConferenceCreated conferenceCreated() {
        final org.spine3.samples.lobby.conference.contracts.Conference conference = buildConferenceForEvent();
        return ConferenceCreated.newBuilder().setConference(conference).build();
    }

    private ConferenceUpdated conferenceUpdated() {
        final org.spine3.samples.lobby.conference.contracts.Conference conference = buildConferenceForEvent();
        return ConferenceUpdated.newBuilder().setConference(conference).build();
    }

    private ConferencePublished conferencePublished() {
        return ConferencePublished.newBuilder().setConferenceId(id).build();
    }

    private ConferenceUnpublished conferenceUnpublished() {
        return ConferenceUnpublished.newBuilder().setConferenceId(id).build();
    }

    private SeatTypeCreated seatTypeCreated(int seatQuantity) {
        final SeatType seatType = buildSeatType("description-256", seatQuantity);
        return SeatTypeCreated.newBuilder().setSeatType(seatType).build();
    }

    private SeatTypeUpdated seatTypeUpdated(int seatQuantity) {
        final SeatType seatType = buildSeatType("description-512", seatQuantity);
        return SeatTypeUpdated.newBuilder().setSeatType(seatType).build();
    }

    private SeatTypeCreated seatTypeCreated(String description, int seatQuantity) {
        final SeatType seatType = buildSeatType(description, seatQuantity);
        return SeatTypeCreated.newBuilder().setSeatType(seatType).build();
    }

    private SeatTypeUpdated seatTypeUpdated(String description, int seatQuantity) {
        final SeatType seatType = buildSeatType(description, seatQuantity);
        return SeatTypeUpdated.newBuilder().setSeatType(seatType).build();
    }

    private org.spine3.samples.lobby.conference.contracts.Conference buildConferenceForEvent() {
        return org.spine3.samples.lobby.conference.contracts.Conference.newBuilder()
                .setId(id)
                .setSlug(ConferenceSlug.newBuilder().setValue("slug"))
                .setName("Test Conference")
                .setDescription("Test Conference description")
                .setLocation("Test Conference location")
                .setTagline("Test Conference tagline")
                .setTwitterSearch("Test Conference twitter")
                .setStartDate(LocalDate.getDefaultInstance())
                .build();
    }

    private SeatType buildSeatType(String description, int seatQuantity) {
        return SeatType.newBuilder()
                .setConferenceId(id)
                .setId(SeatTypeId.newBuilder().setUuid("testSeatType"))
                .setDescription(description)
                .setQuantityTotal(seatQuantity).build();
    }

    public static class TestConferenceProjection extends ConferenceProjection {

        public TestConferenceProjection(ConferenceId id) {
            super(id);
        }

        @Override
        public void incrementState(Conference newState) {
            super.incrementState(newState);
        }
    }

    /**
     * Handles commands sent by ConferenceProjection.
     */
    public static class TestCommandHandlerRepository extends AggregateRepositoryBase<ConferenceId, TestCommandHandler> {
    }

    @SuppressWarnings({"StaticNonFinalField", "AssignmentToStaticFieldFromInstanceMethod"})
    public static class TestCommandHandler extends Aggregate<ConferenceId, StringValue> {

        private static boolean isAddSeatsCommandHandled = false;
        private static boolean isRemoveSeatsCommandHandled = false;

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

        @Override
        protected StringValue getDefaultState() {
            return StringValue.getDefaultInstance();
        }
    }
}
