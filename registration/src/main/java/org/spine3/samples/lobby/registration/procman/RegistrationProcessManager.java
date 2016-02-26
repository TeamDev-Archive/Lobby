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
import org.spine3.base.EventContext;
import org.spine3.protobuf.Timestamps;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.server.BoundedContext;
import org.spine3.server.Subscribe;
import org.spine3.server.procman.ProcessManager;

import static com.google.protobuf.util.TimeUtil.getCurrentTime;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State.NOT_STARTED;

/**
 * A process manager for registration to conference process.
 *
 * @author Alexander Litus
 */
public class RegistrationProcessManager extends ProcessManager<ProcessManagerId, RegistrationProcess> {

    private BoundedContext boundedContext;

    /**
     * Creates a new instance.
     *
     * @param id an ID for the new instance
     * @throws IllegalArgumentException if the ID type is unsupported
     */
    public RegistrationProcessManager(ProcessManagerId id) {
        super(id);
    }

    /*package*/ void setBoundedContext(BoundedContext boundedContext) {
        this.boundedContext = boundedContext;
    }

    @Subscribe
    public void on(OrderPlaced event, EventContext context) {
        final State processState = getState().getState();
        if (processState != NOT_STARTED) {
            // TODO:2016-02-23:alexander.litus: throw smth?
        }
        final Timestamp currentTime = getCurrentTime();
        final Timestamp thanReservationExpiration = event.getReservationAutoExpiration();
        final boolean isReservationExpired = Timestamps.isAfter(currentTime, thanReservationExpiration);
        if (isReservationExpired) {
            rejectOrder(event.getOrderId());
        } else {
            reserveSeats(event);
        }
    }

    private void reserveSeats(OrderPlaced event) {

    }

    private void rejectOrder(OrderId orderId) {

    }
}
