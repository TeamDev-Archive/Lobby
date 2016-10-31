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

import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatAssignmentsId;
import org.spine3.samples.lobby.registration.contracts.SeatPosition;
import org.spine3.base.FailureThrowable;

/**
 * A business failure which is thrown on attempt to unassign a seat which is not assigned to anyone.
 *
 * @author Alexander Litus
 */
public class CannotUnassignNotAssignedSeat extends FailureThrowable {

    public CannotUnassignNotAssignedSeat(SeatAssignmentsId seatAssignmentsId, SeatTypeId seatTypeId, SeatPosition position) {
        super(Failures.CannotUnassignNotAssignedSeat.newBuilder()
                                                    .setSeatAssignmentsId(seatAssignmentsId)
                                                    .setSeatTypeId(seatTypeId)
                                                    .setPosition(position)
                                                    .build());
    }

    @Override
    public Failures.CannotUnassignNotAssignedSeat getFailure() {
        return (Failures.CannotUnassignNotAssignedSeat) super.getFailure();
    }

    private static final long serialVersionUID = 8199753378L;
}
