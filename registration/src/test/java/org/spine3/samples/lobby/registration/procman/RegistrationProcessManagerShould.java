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

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.junit.Before;
import org.junit.Test;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.order.RejectOrder;
import org.spine3.samples.lobby.registration.procman.Given.TestProcessManager;
import org.spine3.samples.lobby.registration.seat.availability.MakeSeatReservation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State.AWAITING_RESERVATION_CONFIRMATION;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State.NOT_STARTED;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State.RESERVATION_CONFIRMED;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class RegistrationProcessManagerShould {

    private Given given;
    private TestProcessManager processManager;

    @Before
    public void setUpTest() {
        this.given = new Given();
    }

    @Test
    public void handle_OrderPlaced_event_then_update_state_and_reserve_seats_if_reservation_not_expired()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final OrderPlaced event = Given.Event.orderPlaced();

        processManager.on(event, Given.Event.CONTEXT);

        assertStateUpdated(AWAITING_RESERVATION_CONFIRMATION, event);
        assertCommandSent(MakeSeatReservation.class);
        // TODO:2016-03-02:alexander.litus: check that ExpireRegistrationProcess cmd is sent
    }

    @Test
    public void handle_OrderPlaced_event_then_update_state_and_reject_order_if_reservation_expired()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final Timestamp reservationExpiration = Given.reservationExpirationTimeBeforeNow();
        final OrderPlaced event = Given.Event.orderPlaced(reservationExpiration);

        processManager.on(event, Given.Event.CONTEXT);

        assertStateUpdated(NOT_STARTED, event);
        assertCommandSent(RejectOrder.class);
    }

    @Test(expected = IllegalProcessStateFailure.class)
    public void throw_exception_if_handle_OrderPlaced_event_in_inappropriate_state() throws IllegalProcessStateFailure {
        processManager = given.processManager(AWAITING_RESERVATION_CONFIRMATION);
        final OrderPlaced event = Given.Event.orderPlaced();

        processManager.on(event, Given.Event.CONTEXT);
    }

    @Test
    public void handle_OrderUpdated_event_then_resend_reservation_cmd_if_awaiting_reservation_confirmation()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(AWAITING_RESERVATION_CONFIRMATION);
        final OrderUpdated event = Given.Event.orderUpdated();

        processManager.on(event, Given.Event.CONTEXT);

        assertCommandSent(MakeSeatReservation.class);
    }

    @Test
    public void handle_OrderUpdated_event_then_resend_reservation_cmd_if_reservation_confirmed()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(RESERVATION_CONFIRMED);
        final OrderUpdated event = Given.Event.orderUpdated();

        processManager.on(event, Given.Event.CONTEXT);

        assertCommandSent(MakeSeatReservation.class);
    }

    @Test(expected = IllegalProcessStateFailure.class)
    public void throw_exception_if_handle_OrderUpdated_event_in_inappropriate_state() throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final OrderUpdated event = Given.Event.orderUpdated();

        processManager.on(event, Given.Event.CONTEXT);
    }

    private void assertStateUpdated(RegistrationProcess.State expectedState, OrderPlaced event) {
        final RegistrationProcess actual = processManager.getState();
        assertEquals(expectedState, actual.getProcessState());
        assertEquals(event.getOrderId(), actual.getOrderId());
        assertEquals(event.getConferenceId(), actual.getConferenceId());
        assertEquals(event.getReservationAutoExpiration(), actual.getReservationAutoExpiration());
    }

    private void assertCommandSent(Class<? extends Message> commandClass) {
        final RegistrationProcessManager.CommandSender sender = given.getCommandSender();
        verify(sender, times(1)).send(isA(commandClass));
    }
}
