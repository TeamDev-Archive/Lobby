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
import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.base.EmailAddress;
import org.spine3.base.PersonName;
import org.spine3.samples.lobby.common.PersonalInfo;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatAssigned;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsId;
import org.spine3.samples.lobby.registration.contracts.SeatPosition;
import org.spine3.samples.lobby.registration.contracts.SeatUnassigned;
import org.spine3.server.Assign;
import org.spine3.server.aggregate.Aggregate;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.spine3.samples.lobby.registration.util.ValidationUtils.checkMessageField;

/**
 * The aggregate which manages assignments of conference seats to attendees.
 *
 * @author Alexander Litus
 */
public class SeatAssignmentsAggregate extends Aggregate<SeatAssignmentsId, SeatAssignments> {

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID type is not supported
     */
    public SeatAssignmentsAggregate(SeatAssignmentsId id) {
        super(id);
    }

    @Assign
    public List<Message> handle(AssignSeat cmd, CommandContext context) {
        Validator.validateCommand(cmd);
        final SeatAssignments state = getState();
        Validator.checkContainsPosition(cmd.getPosition(), state);

        final ImmutableList.Builder<Message> result = ImmutableList.builder();
        final SeatAssignment primaryAssignment = getAssignment(cmd.getPosition());
        final PersonalInfo primaryAttendee = primaryAssignment.getAttendee();
        if (isAttendeeChanged(primaryAttendee, cmd.getAttendee())) {
            if (primaryAssignment.hasAttendee()) {
                result.add(newSeatUnassignedEvent(cmd.getPosition()));
            }
            result.add(newSeatAssignedEvent(cmd, primaryAssignment.getSeatTypeId()));
        } else if (isAttendeeNameChanged(primaryAttendee, cmd.getAttendee())) {
            result.add(newSeatAssignmentUpdatedEvent(cmd));
        }
        return result.build();
    }

    @Assign
    public SeatUnassigned handle(UnassignSeat cmd, CommandContext context) throws CannotUnassignNotAssignedSeat{
        Validator.validateCommand(cmd);
        final SeatAssignments state = getState();
        Validator.checkContainsPosition(cmd.getPosition(), state);

        final SeatAssignment primaryAssignment = getAssignment(cmd.getPosition());
        if (primaryAssignment.hasAttendee()) {
            final SeatUnassigned event = newSeatUnassignedEvent(cmd.getPosition());
            return event;
        } else {
            throw new CannotUnassignNotAssignedSeat(getId(), primaryAssignment.getSeatTypeId(), cmd.getPosition());
        }
    }

    private SeatAssignment getAssignment(SeatPosition seatPosition) {
        final SeatAssignments state = getState();
        final Map<Integer, SeatAssignment> assignments = state.getAssignments();
        final SeatAssignment assignment = assignments.get(seatPosition.getValue());
        return assignment;
    }

    private static boolean isAttendeeChanged(PersonalInfo primaryAttendee, PersonalInfo newAttendee) {
        final EmailAddress primaryEmail = primaryAttendee.getEmail();
        final EmailAddress newEmail = newAttendee.getEmail();
        return !primaryEmail.equals(newEmail);
    }

    private static boolean isAttendeeNameChanged(PersonalInfo primaryAttendee, PersonalInfo newAttendee) {
        final PersonName newName = newAttendee.getName();
        final PersonName primaryName = primaryAttendee.getName();
        return !primaryName.equals(newName);
    }

    private SeatAssigned newSeatAssignedEvent(AssignSeat cmd, SeatTypeId seatTypeId) {
        final SeatAssigned.Builder builder = SeatAssigned.newBuilder()
                .setAssignmentsId(getId())
                .setPosition(cmd.getPosition())
                .setSeatTypeId(seatTypeId)
                .setAttendee(cmd.getAttendee());
        return builder.build();
    }

    private SeatAssignmentUpdated newSeatAssignmentUpdatedEvent(AssignSeat cmd) {
        final SeatAssignmentUpdated.Builder builder = SeatAssignmentUpdated.newBuilder()
                .setAssignmentsId(getId())
                .setPosition(cmd.getPosition())
                .setAttendee(cmd.getAttendee());
        return builder.build();
    }

    public SeatUnassigned newSeatUnassignedEvent(SeatPosition position) {
        final SeatUnassigned.Builder builder = SeatUnassigned.newBuilder()
                .setAssignmentsId(getId())
                .setPosition(position);
        return builder.build();
    }

    private static class Validator {

        private static final String SEAT_ASSIGNMENTS_ID = "seat assignments ID";
        private static final String SEAT_POSITION = "seat position";
        private static final String ATTENDEE = "attendee";
        private static final String ATTENDEE_EMAIL = "attendee email";

        private static void validateCommand(AssignSeat cmd) {
            checkMessageField(cmd.hasSeatAssignmentsId(), SEAT_ASSIGNMENTS_ID, cmd);
            checkMessageField(cmd.hasAttendee(), ATTENDEE, cmd);
            final PersonalInfo attendee = cmd.getAttendee();
            final boolean hasValidEmail = attendee.hasEmail() && !isNullOrEmpty(attendee.getEmail().getValue());
            checkMessageField(hasValidEmail, ATTENDEE_EMAIL, attendee);
            checkMessageField(cmd.hasPosition(), SEAT_POSITION, cmd);
        }

        public static void checkContainsPosition(SeatPosition seatPosition, SeatAssignments state) {
            final Map<Integer, SeatAssignment> assignments = state.getAssignments();
            final int value = seatPosition.getValue();
            checkState(assignments.containsKey(value), "No such position: " + value);
        }

        private static void validateCommand(UnassignSeat cmd) {
            checkMessageField(cmd.hasSeatAssignmentsId(), SEAT_ASSIGNMENTS_ID, cmd);
            checkMessageField(cmd.hasPosition(), SEAT_POSITION, cmd);
        }
    }
}
