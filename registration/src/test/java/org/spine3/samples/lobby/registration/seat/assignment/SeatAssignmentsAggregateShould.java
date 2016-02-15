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

import com.google.common.collect.FluentIterable;
import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;
import org.spine3.samples.lobby.registration.contracts.SeatAssigned;
import org.spine3.samples.lobby.registration.contracts.SeatAssignment;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsCreated;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsId;
import org.spine3.samples.lobby.registration.contracts.SeatUnassigned;
import org.spine3.samples.lobby.registration.util.Seats;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Throwables.propagate;
import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class SeatAssignmentsAggregateShould {

    private Given given;

    @Before
    public void setUpTest() {
        given = new Given();
    }

    @Test
    public void handle_CreateSeatAssignments_command_and_generate_event() {
        final TestSeatAssignmentsAggregate aggregate = given.newSeatAssignments();
        final CreateSeatAssignments cmd = Given.Command.createSeatAssignments();

        final SeatAssignmentsCreated event = aggregate.handle(cmd, Given.Command.context());

        Assert.eventIsValid(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_CreateSeatAssignments_command_is_empty() {
        final TestSeatAssignmentsAggregate aggregate = given.newSeatAssignments();
        aggregate.handle(CreateSeatAssignments.getDefaultInstance(), Given.Command.context());
    }

    @Test
    public void handle_AssignSeat_command_and_assign_seat_if_no_attendee_was_assigned_before() {
        final TestSeatAssignmentsAggregate aggregate = given.seatAssignmentsWithoutAttendees();
        final AssignSeat cmd = Given.Command.assignSeatToNewAttendee();

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());
        assertEquals(1, events.size());
        final SeatAssigned assignedEvent = (SeatAssigned) events.get(0);
        Assert.eventIsValid(assignedEvent, cmd);
    }

    @Test
    public void handle_AssignSeat_command_and_reassign_seat_if_another_attendee_was_assigned_before() {
        final TestSeatAssignmentsAggregate aggregate = given.seatAssignmentsWithAttendees();
        final AssignSeat cmd = Given.Command.assignSeatToNewAttendee();

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());
        assertEquals(2, events.size());
        final SeatUnassigned unassignedEvent = (SeatUnassigned) events.get(0);
        Assert.eventIsValid(unassignedEvent, cmd);
        final SeatAssigned assignedEvent = (SeatAssigned) events.get(1);
        Assert.eventIsValid(assignedEvent, cmd);
    }

    @Test
    public void handle_AssignSeat_command_and_update_assignment_if_this_attendee_was_assigned_before() {
        final TestSeatAssignmentsAggregate aggregate = given.seatAssignmentsWithAttendees();
        final AssignSeat cmd = Given.Command.assignSeatToUpdatedAttendee();

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());
        assertEquals(1, events.size());
        final SeatAssignmentUpdated event = (SeatAssignmentUpdated) events.get(0);
        Assert.eventIsValid(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_AssignSeat_command_is_empty() {
        final TestSeatAssignmentsAggregate aggregate = given.newSeatAssignments();
        aggregate.handle(AssignSeat.getDefaultInstance(), Given.Command.context());
    }

    @Test(expected = IllegalStateException.class)
    public void throw_exception_on_AssignSeat_command_if_no_such_seat_position() {
        final TestSeatAssignmentsAggregate aggregate = given.newSeatAssignments();
        final AssignSeat cmd = Given.Command.assignSeatToNewAttendee();

        aggregate.handle(cmd, Given.Command.context());
    }

    private static class Assert {

        private static void eventIsValid(SeatAssignmentsCreated event, CreateSeatAssignments cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());

            final List<SeatAssignment> assignments = event.getAssignmentList();

            final FluentIterable<SeatAssignment> mainAssignments = Seats.filterById(assignments, Given.MAIN_SEAT_TYPE_ID);
            assertEquals(Given.MAIN_SEAT_QUANTITY, mainAssignments.size());

            final FluentIterable<SeatAssignment> workshopAssignments = Seats.filterById(assignments, Given.WORKSHOP_SEAT_TYPE_ID);
            assertEquals(Given.WORKSHOP_SEAT_QUANTITY, workshopAssignments.size());
        }

        private static void eventIsValid(SeatUnassigned event, AssignSeat cmd) {
            assertEquals(cmd.getSeatAssignmentsId(), event.getAssignmentsId());
            assertEquals(cmd.getPosition(), event.getPosition());
        }

        private static void eventIsValid(SeatAssigned event, AssignSeat cmd) {
            assertEquals(cmd.getSeatAssignmentsId(), event.getAssignmentsId());
            final SeatAssignment assignment = event.getAssignment();
            assertEquals(Given.MAIN_SEAT_TYPE_ID, assignment.getSeatTypeId());
            assertEquals(cmd.getPosition(), assignment.getPosition());
            assertEquals(cmd.getAttendee(), assignment.getAttendee());
        }

        public static void eventIsValid(SeatAssignmentUpdated event, AssignSeat cmd) {
            assertEquals(cmd.getSeatAssignmentsId(), event.getAssignmentsId());
            assertEquals(cmd.getPosition(), event.getPosition());
            assertEquals(cmd.getAttendee(), event.getAttendee());
        }
    }

    public static class TestSeatAssignmentsAggregate extends SeatAssignmentsAggregate {

        public TestSeatAssignmentsAggregate(SeatAssignmentsId id) {
            super(id);
        }

        // Is overridden to do not throw exceptions while retrieving the default state via reflection.
        @Override
        @SuppressWarnings("RefusedBequest")
        protected SeatAssignments getDefaultState() {
            return SeatAssignments.getDefaultInstance();
        }

        // Is overridden to make accessible in tests.
        @Override
        public void incrementState(SeatAssignments newState) {
            super.incrementState(newState);
        }

        void apply(SeatAssignmentsCreated event) {
            invokeApplyMethod(event);
        }

        void apply(SeatAssigned event) {
            invokeApplyMethod(event);
        }

        void apply(SeatUnassigned event) {
            invokeApplyMethod(event);
        }

        void apply(SeatAssignmentUpdated event) {
            invokeApplyMethod(event);
        }

        private void invokeApplyMethod(Message event) {
            try {
                //noinspection DuplicateStringLiteralInspection
                final Method apply = SeatAssignmentsAggregate.class.getDeclaredMethod("apply", event.getClass());
                apply.setAccessible(true);
                apply.invoke(this, event);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                propagate(e);
            }
        }
    }
}
