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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.StringValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.ConferenceSlug;
import org.spine3.samples.lobby.common.SeatType;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.conference.contracts.Conference;
import org.spine3.samples.lobby.registration.seat.availability.AddSeats;
import org.spine3.samples.lobby.registration.seat.availability.RemoveSeats;
import org.spine3.samples.sample.lobby.conference.contracts.*;
import org.spine3.server.Assign;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.AggregateRepositoryBase;
import org.spine3.server.aggregate.Apply;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.spine3.samples.lobby.common.util.CommonMessageFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.CommonMessageFactory.newSeatTypeId;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newBoundedContext;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened", "InstanceMethodNamingConvention", "ClassWithTooManyMethods", "UtilityClass"})
public class ConferenceProjectionShould {

    private final BoundedContext boundedContext = newBoundedContext();
    private final TestConferenceProjection projection = new TestConferenceProjection(Given.CONFERENCE_ID);

    @Before
    public void setUpTest() {
        boundedContext.register(new TestCommandHandlerRepository());
        projection.setBoundedContext(boundedContext);
    }

    @After
    public void tearDownTest() throws IOException {
        boundedContext.close();
    }

    @Test
    public void handle_ConferenceCreated_event_and_update_state() {
        final ConferenceCreated event = Given.conferenceCreated();

        projection.on(event);

        assertEquals(event.getConference(), projection.getState());
    }

    @Test
    public void handle_ConferenceUpdated_event_and_update_state() {
        final ConferenceUpdated event = Given.conferenceUpdated();

        projection.on(event);

        assertEquals(event.getConference(), projection.getState());
    }

    @Test
    public void handle_ConferencePublished_event_and_update_state() {
        final ConferencePublished event = Given.conferencePublished();

        projection.on(event);

        assertTrue(projection.getState().getIsPublished());
    }

    @Test
    public void handle_ConferenceUnpublished_event_and_update_state() {
        final ConferenceUnpublished event = Given.conferenceUnpublished();

        projection.on(event);

        assertFalse(projection.getState().getIsPublished());
    }

    @Test
    public void handle_SeatTypeCreated_event_and_update_state() {
        projection.incrementState(Given.conference());
        final SeatTypeCreated event = Given.seatTypeCreated(5);

        projection.on(event);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void not_add_seat_type_with_the_same_id_on_SeatTypeCreated_event() {
        projection.incrementState(Given.conference());
        final SeatTypeCreated event = Given.seatTypeCreated(5);

        projection.on(event);
        projection.on(event);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_update_state() {
        projection.incrementState(Given.conference());
        projection.on(Given.seatTypeCreated("old-description", 5));

        final SeatTypeUpdated updatedEvent = Given.seatTypeUpdated("new-description", 7);
        projection.on(updatedEvent);

        assertSeatTypesConsistOf(updatedEvent.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_added() {
        projection.incrementState(Given.conference());

        projection.on(Given.seatTypeCreated(3));
        projection.on(Given.seatTypeUpdated(7));

        assertTrue(TestCommandHandler.isAddSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_removed() {
        projection.incrementState(Given.conference());

        projection.on(Given.seatTypeCreated(7));
        projection.on(Given.seatTypeUpdated(3));

        assertTrue(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_do_not_send_commands_when_seat_quantity_not_changed() {
        projection.incrementState(Given.conference());
        final int seatQuantity = 5;

        projection.on(Given.seatTypeCreated(seatQuantity));
        TestCommandHandler.setIsAddSeatsCommandHandled(false);

        projection.on(Given.seatTypeUpdated(seatQuantity));

        assertFalse(TestCommandHandler.isAddSeatsCommandHandled());
        assertFalse(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    private void assertSeatTypesConsistOf(SeatType... expectedSeatTypes) {
        final ImmutableList<SeatType> expectedTypes = ImmutableList.copyOf(expectedSeatTypes);
        final List<SeatType> actualTypes = projection.getState().getSeatTypeList();
        assertEquals(expectedTypes.size(), actualTypes.size());
        assertTrue(actualTypes.containsAll(expectedTypes));
    }

    static class Given {

        static final ConferenceId CONFERENCE_ID = newConferenceId();
        private static final ConferenceUnpublished CONFERENCE_UNPUBLISHED = ConferenceUnpublished.newBuilder().setConferenceId(CONFERENCE_ID).build();
        private static final ConferencePublished CONFERENCE_PUBLISHED = ConferencePublished.newBuilder().setConferenceId(CONFERENCE_ID).build();
        static final SeatTypeId SEAT_TYPE_ID = newSeatTypeId();
        private static final String CONFERENCE_NAME = "Test Conference";
        private static final String TWITTER_SEARCH = CONFERENCE_NAME + " twitter";
        private static final String TAGLINE = CONFERENCE_NAME + " tagline";
        private static final String LOCATION = CONFERENCE_NAME + " location";
        private static final String DESCRIPTION = CONFERENCE_NAME + " description";
        private static final ConferenceSlug.Builder SLUG = ConferenceSlug.newBuilder().setValue("slug");
        private static final Conference CONFERENCE = Conference.newBuilder()
                .setId(CONFERENCE_ID)
                .setSlug(SLUG)
                .setName(CONFERENCE_NAME)
                .setDescription(DESCRIPTION)
                .setLocation(LOCATION)
                .setTagline(TAGLINE)
                .setTwitterSearch(TWITTER_SEARCH).build();

        private Given() {}

        static ConferenceCreated conferenceCreated() {
            final Conference conference = conference();
            return ConferenceCreated.newBuilder().setConference(conference).build();
        }

        static ConferenceUpdated conferenceUpdated() {
            final Conference conference = conference();
            return ConferenceUpdated.newBuilder().setConference(conference).build();
        }

        static ConferencePublished conferencePublished() {
            return CONFERENCE_PUBLISHED;
        }

        static ConferenceUnpublished conferenceUnpublished() {
            return CONFERENCE_UNPUBLISHED;
        }

        static SeatTypeCreated seatTypeCreated(int seatQuantity) {
            final SeatType seatType = newSeatType("descriptionForSeatTypeCreatedEvent", seatQuantity);
            return SeatTypeCreated.newBuilder().setSeatType(seatType).build();
        }

        static SeatTypeUpdated seatTypeUpdated(int seatQuantity) {
            final SeatType seatType = newSeatType("descriptionForSeatTypeUpdatedEvent", seatQuantity);
            return SeatTypeUpdated.newBuilder().setSeatType(seatType).build();
        }

        static SeatTypeCreated seatTypeCreated(String description, int seatQuantity) {
            final SeatType seatType = newSeatType(description, seatQuantity);
            return SeatTypeCreated.newBuilder().setSeatType(seatType).build();
        }

        static SeatTypeUpdated seatTypeUpdated(String description, int seatQuantity) {
            final SeatType seatType = newSeatType(description, seatQuantity);
            return SeatTypeUpdated.newBuilder().setSeatType(seatType).build();
        }

        static Conference conference() {
            return CONFERENCE;
        }

        static SeatType newSeatType(String description, int seatQuantity) {
            final SeatType.Builder result = SeatType.newBuilder()
                    .setConferenceId(CONFERENCE_ID)
                    .setId(SEAT_TYPE_ID)
                    .setDescription(description)
                    .setQuantityTotal(seatQuantity);
            return result.build();
        }
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
