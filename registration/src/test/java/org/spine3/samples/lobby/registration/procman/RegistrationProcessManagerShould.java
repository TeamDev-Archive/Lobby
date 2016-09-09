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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.Command;
import org.spine3.protobuf.AnyPacker;
import org.spine3.samples.lobby.payment.contracts.PaymentCompleted;
import org.spine3.samples.lobby.registration.contracts.OrderConfirmed;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.order.ConfirmOrder;
import org.spine3.samples.lobby.registration.order.MarkSeatsAsReserved;
import org.spine3.samples.lobby.registration.order.RejectOrder;
import org.spine3.samples.lobby.registration.procman.Given.TestProcessManager;
import org.spine3.samples.lobby.registration.seat.availability.CancelSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.CommitSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.MakeSeatReservation;
import org.spine3.samples.lobby.registration.seat.availability.SeatsReserved;
import org.spine3.samples.lobby.registration.util.MessagePacker;
import org.spine3.server.procman.CommandRouted;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.spine3.samples.lobby.registration.procman.RegistrationProcess.State.*;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "ClassWithTooManyMethods", "OverlyCoupledClass", "TypeMayBeWeakened"})
public class RegistrationProcessManagerShould {

    private Given given;
    private TestProcessManager processManager;

    @Before
    public void setUpTest() {
        this.given = new Given();
    }

    @After
    public void tearDown() throws Exception {
        processManager.getCommandBus()
                      .close();
    }

    @Test
    public void handle_OrderPlaced_event_then_update_state_and_reserve_seats_if_reservation_not_expired()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final OrderPlaced event = Given.Event.orderPlaced();

        processManager.on(event, Given.Event.CONTEXT);

        assertStateUpdated(AWAITING_RESERVATION_CONFIRMATION, event);
        final List<Message> commandsSent = processManager.getCommandsSent();
        assertEquals(2, commandsSent.size());

        final Message command = MessagePacker.unpackAny(commandsSent.get(0));
        final MakeSeatReservation reserveSeats = (MakeSeatReservation) command;
        assertEquals(event.getConferenceId(), reserveSeats.getConferenceId());
        assertEquals(event.getOrderId()
                          .getUuid(), reserveSeats.getReservationId()
                                                  .getUuid());
        assertEquals(event.getSeatList(), reserveSeats.getSeatList());

        final ExpireRegistrationProcess expireProcess =
                (ExpireRegistrationProcess) MessagePacker.unpackAny(commandsSent.get(1));
        assertEquals(processManager.getId(), expireProcess.getProcessManagerId());
    }

    @Test
    public void handle_OrderPlaced_event_then_update_state_and_reject_order_if_reservation_expired()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final Timestamp reservationExpiration = Given.reservationExpirationTimeBeforeNow();
        final OrderPlaced event = Given.Event.orderPlaced(reservationExpiration);

        processManager.on(event, Given.Event.CONTEXT);

        assertStateUpdated(NOT_STARTED, event);
        final RejectOrder cmd = assertCommandSent(RejectOrder.class);
        assertEquals(event.getOrderId(), cmd.getOrderId());
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

        orderUpdatedEventHandlingTest();
    }

    @Test
    public void handle_OrderUpdated_event_then_resend_reservation_cmd_if_reservation_confirmed()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(RESERVATION_CONFIRMED);

        orderUpdatedEventHandlingTest();
    }

    private void orderUpdatedEventHandlingTest() throws IllegalProcessStateFailure {
        final OrderUpdated event = Given.Event.orderUpdated();

        processManager.on(event, Given.Event.CONTEXT);

        final MakeSeatReservation cmd = assertCommandSent(MakeSeatReservation.class);
        assertEquals(processManager.getState()
                                   .getConferenceId(), cmd.getConferenceId());
        assertEquals(event.getOrderId()
                          .getUuid(), cmd.getReservationId()
                                         .getUuid());
        assertEquals(event.getSeatList(), cmd.getSeatList());
    }

    @Test(expected = IllegalProcessStateFailure.class)
    public void throw_exception_if_handle_OrderUpdated_event_in_inappropriate_state() throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final OrderUpdated event = Given.Event.orderUpdated();

        processManager.on(event, Given.Event.CONTEXT);
    }

    @Test
    public void handle_SeatsReserved_event_then_update_state_and_mark_seats_as_reserved()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(AWAITING_RESERVATION_CONFIRMATION);
        final SeatsReserved event = Given.Event.seatsReserved();

        processManager.on(event, Given.Event.CONTEXT);

        assertStateUpdated(RESERVATION_CONFIRMED);

        final MarkSeatsAsReserved cmd = assertCommandSent(MarkSeatsAsReserved.class);
        final RegistrationProcess state = processManager.getState();
        assertEquals(state.getOrderId(), cmd.getOrderId());
        assertEquals(state.getReservationAutoExpiration(), cmd.getReservationExpiration());
        assertEquals(event.getReservedSeatUpdatedList(), cmd.getSeatList());
    }

    @Test(expected = IllegalProcessStateFailure.class)
    public void throw_exception_if_handle_SeatsReserved_event_in_inappropriate_state() throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final SeatsReserved event = Given.Event.seatsReserved();

        processManager.on(event, Given.Event.CONTEXT);
    }

    @Test
    public void handle_PaymentCompleted_event_then_update_state_and_confirm_order() throws IllegalProcessStateFailure {
        processManager = given.processManager(RESERVATION_CONFIRMED);
        final PaymentCompleted event = Given.Event.paymentCompleted();

        processManager.on(event, Given.Event.CONTEXT);

        assertStateUpdated(PAYMENT_RECEIVED);

        final ConfirmOrder cmd = assertCommandSent(ConfirmOrder.class);
        assertEquals(event.getOrderId(), cmd.getOrderId());
    }

    @Test(expected = IllegalProcessStateFailure.class)
    public void throw_exception_if_handle_PaymentCompleted_event_in_inappropriate_state()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final PaymentCompleted event = Given.Event.paymentCompleted();

        processManager.on(event, Given.Event.CONTEXT);
    }

    @Test
    public void handle_OrderConfirmed_event_then_update_state_and_commit_seat_reservation_if_reservation_confirmed()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(RESERVATION_CONFIRMED);
        orderConfirmedEventHandlingTest();
    }

    @Test
    public void handle_OrderConfirmed_event_then_update_state_and_commit_seat_reservation_if_payment_received()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(PAYMENT_RECEIVED);
        orderConfirmedEventHandlingTest();
    }

    private void orderConfirmedEventHandlingTest() throws IllegalProcessStateFailure {
        final OrderConfirmed event = Given.Event.orderConfirmed();

        processManager.on(event, Given.Event.CONTEXT);

        assertProcessIsCompleted();
        final CommitSeatReservation cmd = assertCommandSent(CommitSeatReservation.class);
        assertEquals(event.getOrderId()
                          .getUuid(), cmd.getReservationId()
                                         .getUuid());
    }

    @Test(expected = IllegalProcessStateFailure.class)
    public void throw_exception_if_handle_OrderConfirmed_event_in_inappropriate_state()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(NOT_STARTED);
        final OrderConfirmed event = Given.Event.orderConfirmed();

        processManager.on(event, Given.Event.CONTEXT);
    }

    @Test
    public void handle_ExpireRegistrationProcess_command_then_update_state_and_send_commands_if_process_not_completed()
            throws IllegalProcessStateFailure, InvalidProtocolBufferException {
        processManager = given.processManager(PAYMENT_RECEIVED, /*isCompleted=*/false);
        final ExpireRegistrationProcess expireProcessCmd = Given.Command.expireRegistrationProcess();

        final CommandRouted event = processManager.handle(expireProcessCmd, Given.Command.CONTEXT);

        assertProcessIsCompleted();
        final List<Command> commandsSent = event.getProducedList();
        assertEquals(2, commandsSent.size());
        final RejectOrder rejectCmd = findCommandMessage(RejectOrder.class, commandsSent);
        assertEquals(Given.ORDER_ID, rejectCmd.getOrderId());
        final CancelSeatReservation cancelCmd = findCommandMessage(CancelSeatReservation.class, commandsSent);
        assertEquals(Given.RESERVATION_ID, cancelCmd.getReservationId());
        assertEquals(Given.CONFERENCE_ID, cancelCmd.getConferenceId());
    }

    @Test
    public void handle_ExpireRegistrationProcess_command_and_send_no_commands_if_process_is_completed()
            throws IllegalProcessStateFailure {
        processManager = given.processManager(PAYMENT_RECEIVED, /*isCompleted=*/true);
        final ExpireRegistrationProcess cmd = Given.Command.expireRegistrationProcess();

        final CommandRouted event = processManager.handle(cmd, Given.Command.CONTEXT);

        assertEquals(0, event.getProducedCount());
    }

    private <M extends Message> M assertCommandSent(Class<M> commandClass) {
        final List<Message> commandsSent = processManager.getCommandsSent();
        assertEquals(1, commandsSent.size());

        Message message = commandsSent.get(0);

        if (message instanceof Any) {
            message = AnyPacker.unpack((Any) message);
        }

        assertEquals(commandClass, message.getClass());
        @SuppressWarnings("unchecked")
        final M result = (M) message;
        return result;
    }

    private void assertProcessIsCompleted() {
        final RegistrationProcess actualState = processManager.getState();
        assertTrue(actualState.getIsCompleted());
    }

    private void assertStateUpdated(RegistrationProcess.State expectedState) {
        final RegistrationProcess actual = processManager.getState();
        assertEquals(expectedState, actual.getProcessState());
    }

    private void assertStateUpdated(RegistrationProcess.State expectedState, OrderPlaced event) {
        final RegistrationProcess actual = processManager.getState();
        assertEquals(expectedState, actual.getProcessState());
        assertEquals(event.getOrderId(), actual.getOrderId());
        assertEquals(event.getConferenceId(), actual.getConferenceId());
        assertEquals(event.getReservationAutoExpiration(), actual.getReservationAutoExpiration());
    }

    private static <M extends Message> M findCommandMessage(Class<M> cmdMessageClass, Iterable<Command> commands) throws InvalidProtocolBufferException {
        for (Command command : commands) {
            final Any any = command.getMessage();

            @SuppressWarnings("unchecked")
            final M message = (M) MessagePacker.unpackAny(any);

            if (message.getClass()
                       .equals(cmdMessageClass)) {
                return message;
            }
        }
        throw new IllegalArgumentException("No command found of class: {}" + cmdMessageClass.getName());
    }
}
