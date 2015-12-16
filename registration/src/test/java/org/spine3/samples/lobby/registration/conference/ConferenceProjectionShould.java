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
import org.spine3.samples.lobby.registration.testdata.TestDataFactory;
import org.spine3.samples.sample.lobby.conference.contracts.*;
import org.spine3.server.Assign;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.AggregateRepositoryBase;
import org.spine3.server.aggregate.Apply;
import org.spine3.time.LocalDate;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened", "InstanceMethodNamingConvention", "ClassWithTooManyMethods"})
public class ConferenceProjectionShould {

    private static final ConferenceId ID = TestDataFactory.newConferenceId();
    private final BoundedContext boundedContext = TestDataFactory.buildBoundedContext();

    private final TestConferenceProjection projection = new TestConferenceProjection(ID);

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
        final ConferenceCreated event = BuildHelper.conferenceCreated();

        projection.on(event);

        assertEquals(event.getConference(), projection.getState());
    }

    @Test
    public void handle_ConferenceUpdated_event_and_update_state() {
        final ConferenceUpdated event = BuildHelper.conferenceUpdated();

        projection.on(event);

        assertEquals(event.getConference(), projection.getState());
    }

    @Test
    public void handle_ConferencePublished_event_and_update_state() {
        final ConferencePublished event = BuildHelper.conferencePublished();

        projection.on(event);

        assertTrue(projection.getState().getIsPublished());
    }

    @Test
    public void handle_ConferenceUnpublished_event_and_update_state() {
        final ConferenceUnpublished event = BuildHelper.conferenceUnpublished();

        projection.on(event);

        assertFalse(projection.getState().getIsPublished());
    }

    @Test
    public void handle_SeatTypeCreated_event_and_update_state() {
        projection.incrementState(BuildHelper.newConference());
        final SeatTypeCreated event = BuildHelper.seatTypeCreated(5);

        projection.on(event);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void not_add_seat_type_with_the_same_id_on_SeatTypeCreated_event() {
        projection.incrementState(BuildHelper.newConference());
        final SeatTypeCreated event = BuildHelper.seatTypeCreated(5);

        projection.on(event);
        projection.on(event);

        assertSeatTypesConsistOf(event.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_update_state() {
        projection.incrementState(BuildHelper.newConference());
        projection.on(BuildHelper.seatTypeCreated("old-description", 5));

        final SeatTypeUpdated updatedEvent = BuildHelper.seatTypeUpdated("new-description", 7);
        projection.on(updatedEvent);

        assertSeatTypesConsistOf(updatedEvent.getSeatType());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_added() {
        projection.incrementState(BuildHelper.newConference());

        projection.on(BuildHelper.seatTypeCreated(3));
        projection.on(BuildHelper.seatTypeUpdated(7));

        assertTrue(TestCommandHandler.isAddSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_send_AddSeats_command_when_seats_removed() {
        projection.incrementState(BuildHelper.newConference());

        projection.on(BuildHelper.seatTypeCreated(7));
        projection.on(BuildHelper.seatTypeUpdated(3));

        assertTrue(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    @Test
    public void handle_SeatTypeUpdated_event_and_do_not_send_commands_when_seat_quantity_not_changed() {
        projection.incrementState(BuildHelper.newConference());
        final int seatQuantity = 5;

        projection.on(BuildHelper.seatTypeCreated(seatQuantity));
        TestCommandHandler.setIsAddSeatsCommandHandled(false);

        projection.on(BuildHelper.seatTypeUpdated(seatQuantity));

        assertFalse(TestCommandHandler.isAddSeatsCommandHandled());
        assertFalse(TestCommandHandler.isRemoveSeatsCommandHandled());
    }

    private void assertSeatTypesConsistOf(SeatType... expectedSeatTypes) {
        final ImmutableList<SeatType> expectedTypes = ImmutableList.copyOf(expectedSeatTypes);
        final List<SeatType> actualTypes = projection.getState().getSeatTypeList();
        assertEquals(expectedTypes.size(), actualTypes.size());
        assertTrue(actualTypes.containsAll(expectedTypes));
    }

    private static class BuildHelper {

        private static ConferenceCreated conferenceCreated() {
            final Conference conference = newConference();
            return ConferenceCreated.newBuilder().setConference(conference).build();
        }

        private static ConferenceUpdated conferenceUpdated() {
            final Conference conference = newConference();
            return ConferenceUpdated.newBuilder().setConference(conference).build();
        }

        private static ConferencePublished conferencePublished() {
            return ConferencePublished.newBuilder().setConferenceId(ID).build();
        }

        private static ConferenceUnpublished conferenceUnpublished() {
            return ConferenceUnpublished.newBuilder().setConferenceId(ID).build();
        }

        private static SeatTypeCreated seatTypeCreated(int seatQuantity) {
            final SeatType seatType = newSeatType("description-256", seatQuantity);
            return SeatTypeCreated.newBuilder().setSeatType(seatType).build();
        }

        private static SeatTypeUpdated seatTypeUpdated(int seatQuantity) {
            final SeatType seatType = newSeatType("description-512", seatQuantity);
            return SeatTypeUpdated.newBuilder().setSeatType(seatType).build();
        }

        private static SeatTypeCreated seatTypeCreated(String description, int seatQuantity) {
            final SeatType seatType = newSeatType(description, seatQuantity);
            return SeatTypeCreated.newBuilder().setSeatType(seatType).build();
        }

        private static SeatTypeUpdated seatTypeUpdated(String description, int seatQuantity) {
            final SeatType seatType = newSeatType(description, seatQuantity);
            return SeatTypeUpdated.newBuilder().setSeatType(seatType).build();
        }

        private static Conference newConference() {
            return Conference.newBuilder()
                    .setId(ID)
                    .setSlug(ConferenceSlug.newBuilder().setValue("slug"))
                    .setName("Test Conference")
                    .setDescription("Test Conference description")
                    .setLocation("Test Conference location")
                    .setTagline("Test Conference tagline")
                    .setTwitterSearch("Test Conference twitter")
                    .setStartDate(LocalDate.getDefaultInstance())
                    .build();
        }

        private static SeatType newSeatType(String description, int seatQuantity) {
            return SeatType.newBuilder()
                    .setConferenceId(ID)
                    .setId(SeatTypeId.newBuilder().setUuid("testSeatType"))
                    .setDescription(description)
                    .setQuantityTotal(seatQuantity).build();
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
