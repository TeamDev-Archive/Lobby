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
import com.google.common.collect.ImmutableMap;
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.PersonalInfo;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatAssigned;
import org.spine3.samples.lobby.registration.contracts.SeatAssignment;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsCreated;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsId;
import org.spine3.samples.lobby.registration.contracts.SeatPosition;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.contracts.SeatUnassigned;

import java.util.List;
import java.util.Map;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.samples.lobby.common.util.IdFactory.newOrderId;
import static org.spine3.samples.lobby.common.util.IdFactory.newSeatTypeId;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newPersonalInfo;
import static org.spine3.samples.lobby.registration.util.Seats.*;

/**
 * A test data factory for {@link SeatAssignmentsAggregate} and {@link SeatAssignmentsRepository} tests.
 *
 * @author Alexander Litus
 */
@SuppressWarnings("UtilityClass")
/* package */ class Given {

    private static final SeatAssignmentsId ASSIGNMENTS_ID = newSeatAssignmentsId();
    private static final OrderId ORDER_ID = newOrderId();

    /* package */ static final SeatTypeId MAIN_SEAT_TYPE_ID = newSeatTypeId("main_" + newUuid());
    /* package */ static final int MAIN_SEAT_QUANTITY = 5;
    /* package */ static final SeatTypeId WORKSHOP_SEAT_TYPE_ID = newSeatTypeId("workshop_" + newUuid());
    /* package */ static final int WORKSHOP_SEAT_QUANTITY = 10;

    private static class Assignments {

        private static final SeatPosition MAIN_SEAT_POSITION = newSeatPosition(0);
        private static final SeatPosition WORKSHOP_SEAT_POSITION = newSeatPosition(1);

        private static final PersonalInfo MAIN_SEAT_ATTENDEE = newPersonalInfo("J", "Doe", "j@mail.com");
        private static final String MAIN_SEAT_ATTENDEE_EMAIL = MAIN_SEAT_ATTENDEE.getEmail().getValue();

        private static final Map<Integer, SeatAssignment> MAP_WITH_ATTENDEES = ImmutableMap.<Integer, SeatAssignment>builder()
                .put(
                    MAIN_SEAT_POSITION.getValue(),
                    newSeatAssignment(MAIN_SEAT_TYPE_ID, MAIN_SEAT_POSITION, MAIN_SEAT_ATTENDEE))
                .put(
                    WORKSHOP_SEAT_POSITION.getValue(),
                    newSeatAssignment(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_POSITION, newPersonalInfo("K", "White", "k@mail.com")))
                .build();

        private static final Map<Integer, SeatAssignment> MAP_WITHOUT_ATTENDEES = ImmutableMap.<Integer, SeatAssignment>builder()
                .put(MAIN_SEAT_POSITION.getValue(), newSeatAssignment(MAIN_SEAT_TYPE_ID, MAIN_SEAT_POSITION))
                .put(WORKSHOP_SEAT_POSITION.getValue(), newSeatAssignment(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_POSITION))
                .build();
    }


    private final SeatAssignmentsAggregate aggregate;

    /* package */ Given() {
        aggregate = new SeatAssignmentsAggregate(ASSIGNMENTS_ID);
    }

    /* package */ SeatAssignmentsAggregate emptySeatAssignments() {
        return aggregate;
    }

    /* package */ SeatAssignmentsAggregate seatAssignmentsWithAttendees() {
        final SeatAssignments state = aggregate.getState().toBuilder()
                .putAllAssignments(Assignments.MAP_WITH_ATTENDEES)
                .build();
        aggregate.incrementStateForTest(state);
        return aggregate;
    }

    /* package */ SeatAssignmentsAggregate seatAssignmentsWithoutAttendees() {
        final SeatAssignments state = aggregate.getState().toBuilder()
                .putAllAssignments(Assignments.MAP_WITHOUT_ATTENDEES)
                .build();
        aggregate.incrementStateForTest(state);
        return aggregate;
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private static SeatAssignment newSeatAssignment(SeatTypeId seatTypeId, SeatPosition position) {
        final SeatAssignment.Builder builder = SeatAssignment.newBuilder()
                .setSeatTypeId(seatTypeId)
                .setPosition(position);
        return builder.build();
    }

    /* package */ static SeatAssignment newSeatAssignment(SeatTypeId seatTypeId, SeatPosition position, PersonalInfo attendee) {
        final SeatAssignment.Builder builder = SeatAssignment.newBuilder()
                .setSeatTypeId(seatTypeId)
                .setPosition(position)
                .setAttendee(attendee);
        return builder.build();
    }

    /**
     * A test utility class providing commands.
     */
    /* package */ static class Command {

        private static final CommandContext CMD_CONTEXT = CommandContext.getDefaultInstance();

        private static final List<SeatQuantity> SEATS = ImmutableList.of(
                newSeatQuantity(MAIN_SEAT_TYPE_ID, MAIN_SEAT_QUANTITY),
                newSeatQuantity(WORKSHOP_SEAT_TYPE_ID, WORKSHOP_SEAT_QUANTITY));

        private static final CreateSeatAssignments CREATE_SEAT_ASSIGNMENTS = CreateSeatAssignments.newBuilder()
                .setOrderId(ORDER_ID)
                .addAllSeat(SEATS)
                .build();

        private static final AssignSeat ASSIGN_SEAT_TO_NEW_ATTENDEE = AssignSeat.newBuilder()
                .setSeatAssignmentsId(ASSIGNMENTS_ID)
                .setAttendee(newPersonalInfo("Man", "Newman", "new@mail.com"))
                .setPosition(Assignments.MAIN_SEAT_POSITION)
                .build();

        private static final AssignSeat ASSIGN_SEAT_TO_UPDATED_ATTENDEE = AssignSeat.newBuilder()
                .setSeatAssignmentsId(ASSIGNMENTS_ID)
                .setAttendee(newPersonalInfo("NewGivenName", "NewFamilyName", Assignments.MAIN_SEAT_ATTENDEE_EMAIL))
                .setPosition(Assignments.MAIN_SEAT_POSITION)
                .build();

        private static final UnassignSeat UNASSIGN_SEAT = UnassignSeat.newBuilder()
                .setSeatAssignmentsId(ASSIGNMENTS_ID)
                .setPosition(Assignments.MAIN_SEAT_POSITION)
                .build();

        private Command() {}

        /* package */ static CommandContext context() {
            return CMD_CONTEXT;
        }

        /* package */ static CreateSeatAssignments createSeatAssignments() {
            return CREATE_SEAT_ASSIGNMENTS;
        }

        /* package */ static AssignSeat assignSeatToNewAttendee() {
            return ASSIGN_SEAT_TO_NEW_ATTENDEE;
        }

        /* package */ static AssignSeat assignSeatToUpdatedAttendee() {
            return ASSIGN_SEAT_TO_UPDATED_ATTENDEE;
        }

        /* package */ static UnassignSeat unassignSeat() {
            return UNASSIGN_SEAT;
        }
    }

    /**
     * A test utility class providing events.
     */
    /* package */ static class Event {

        private static final SeatAssignmentsCreated SEAT_ASSIGNMENTS_CREATED = SeatAssignmentsCreated.newBuilder()
                .setAssignmentsId(ASSIGNMENTS_ID)
                .setOrderId(ORDER_ID)
                .addAllAssignment(Assignments.MAP_WITH_ATTENDEES.values())
                .build();

        private static final SeatAssigned SEAT_ASSIGNED = SeatAssigned.newBuilder()
                .setAssignmentsId(ASSIGNMENTS_ID)
                .setAssignment(
                        newSeatAssignment(
                                MAIN_SEAT_TYPE_ID,
                                Assignments.MAIN_SEAT_POSITION,
                                newPersonalInfo("NewName", "NewSurname", "n@mail.com")
                        )
                ).build();

        private static final SeatUnassigned SEAT_UNASSIGNED = SeatUnassigned.newBuilder()
                .setAssignmentsId(ASSIGNMENTS_ID)
                .setPosition(Assignments.MAIN_SEAT_POSITION).build();

        private static final SeatAssignmentUpdated SEAT_ASSIGNMENT_UPDATED = SeatAssignmentUpdated.newBuilder()
                .setAssignmentsId(ASSIGNMENTS_ID)
                .setPosition(Assignments.MAIN_SEAT_POSITION)
                .setAttendee(newPersonalInfo("UpdatedGivenName", "UpdatedFamilyName", Assignments.MAIN_SEAT_ATTENDEE_EMAIL))
                .build();

        private Event() {}

        /* package */ static SeatAssignmentsCreated seatAssignmentsCreated() {
            return SEAT_ASSIGNMENTS_CREATED;
        }

        /* package */ static SeatAssigned seatAssigned() {
            return SEAT_ASSIGNED;
        }

        /* package */ static SeatUnassigned seatUnassigned() {
            return SEAT_UNASSIGNED;
        }

        /* package */ static SeatAssignmentUpdated seatAssignmentUpdated() {
            return SEAT_ASSIGNMENT_UPDATED;
        }
    }
}
