/*
 * Copyright 2015, TeamDev. All rights reserved.
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

package org.spine3.samples.lobby.registration.order;

import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;
import org.spine3.samples.lobby.registration.contracts.OrderConfirmed;
import org.spine3.samples.lobby.registration.contracts.OrderExpired;
import org.spine3.samples.lobby.registration.contracts.OrderPartiallyReserved;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderRegistrantAssigned;
import org.spine3.samples.lobby.registration.contracts.OrderReservationCompleted;
import org.spine3.samples.lobby.registration.contracts.OrderTotalsCalculated;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.util.EventImporter;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "TypeMayBeWeakened", "OverlyCoupledClass", "ClassWithTooManyMethods"})
public class OrderAggregateShould {

    private Given given;

    @Before
    public void setUpTest() {
        given = new Given();
    }

    @Test
    public void handle_RegisterToConference_command_and_generate_correct_events_if_order_is_new() {
        final OrderAggregate aggregate = given.newOrder();
        final RegisterToConference cmd = Given.Command.registerToConference();

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(2, events.size());
        final OrderPlaced placedEvent = (OrderPlaced) events.get(0);
        Assert.eventIsValid(placedEvent, cmd);
        final OrderTotalsCalculated calculatedEvent = (OrderTotalsCalculated) events.get(1);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test
    public void handle_RegisterToConference_command_and_generate_correct_events_if_order_is_already_placed() {
        final OrderAggregate aggregate = given.placedOrder();
        final RegisterToConference cmd = Given.Command.registerToConference();

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(2, events.size());
        final OrderUpdated updated = (OrderUpdated) events.get(0);
        Assert.eventIsValid(updated, cmd);
        final OrderTotalsCalculated calculatedEvent = (OrderTotalsCalculated) events.get(1);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_RegisterToConference_command_and_throw_exception_if_order_is_confirmed() {
        final OrderAggregate aggregate = given.confirmedOrder();
        final RegisterToConference cmd = Given.Command.registerToConference();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_RegisterToConference_command_and_throw_exception_if_it_is_empty() {
        final OrderAggregate aggregate = given.newOrder();
        final RegisterToConference cmd = RegisterToConference.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_MarkSeatsAsReserved_command_and_generate_correct_events_if_order_is_completely_reserved() {
        final MarkSeatsAsReserved cmd = Given.Command.markSeatsAsReserved();
        final OrderAggregate aggregate = given.completelyReservedOrder(cmd.getSeatList());

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(1, events.size());
        final OrderReservationCompleted completedEvent = (OrderReservationCompleted) events.get(0);
        Assert.eventIsValid(completedEvent, cmd);
    }

    @Test
    public void handle_MarkSeatsAsReserved_command_and_generate_correct_events_if_order_is_partially_reserved() {
        final MarkSeatsAsReserved cmd = Given.Command.markSeatsAsReserved();
        final OrderAggregate aggregate = given.partiallyReservedOrder(cmd.getSeatList());

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(2, events.size());
        final OrderPartiallyReserved reservedEvent = (OrderPartiallyReserved) events.get(0);
        Assert.eventIsValid(reservedEvent, cmd);
        final OrderTotalsCalculated calculatedEvent = (OrderTotalsCalculated) events.get(1);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_MarkSeatsAsReserved_command_and_throw_exception_if_order_is_confirmed() {
        final OrderAggregate aggregate = given.confirmedOrder();
        final MarkSeatsAsReserved cmd = Given.Command.markSeatsAsReserved();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_MarkSeatsAsReserved_command_and_throw_exception_if_it_is_empty() {
        final OrderAggregate aggregate = given.newOrder();
        final MarkSeatsAsReserved cmd = MarkSeatsAsReserved.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_RejectOrder_command_and_generate_OrderExpired_event() {
        final OrderAggregate aggregate = given.newOrder();
        final RejectOrder cmd = Given.Command.rejectOrder();

        final OrderExpired event = aggregate.handle(cmd, Given.Command.context());

        Assert.eventIsValid(event, cmd);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_RejectOrder_command_and_throw_exception_if_order_is_confirmed() {
        final OrderAggregate aggregate = given.confirmedOrder();
        final RejectOrder cmd = Given.Command.rejectOrder();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_RejectOrder_command_and_throw_exception_if_it_is_empty() {
        final OrderAggregate aggregate = given.newOrder();
        final RejectOrder cmd = RejectOrder.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_ConfirmOrder_command_and_generate_OrderConfirmed_event() {
        final OrderAggregate aggregate = given.newOrder();
        final ConfirmOrder cmd = Given.Command.confirmOrder();

        final OrderConfirmed event = aggregate.handle(cmd, Given.Command.context());

        final List<SeatQuantity> seats = aggregate.getState()
                                                  .getSeatList();
        Assert.eventIsValid(event, cmd, seats);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_ConfirmOrder_command_and_throw_exception_if_order_is_confirmed() {
        final OrderAggregate aggregate = given.confirmedOrder();
        final ConfirmOrder cmd = Given.Command.confirmOrder();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_ConfirmOrder_command_and_throw_exception_if_it_is_empty() {
        final OrderAggregate aggregate = given.newOrder();
        final ConfirmOrder cmd = ConfirmOrder.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_AssignRegistrantDetails_command_and_generate_OrderRegistrantAssigned_event() {
        final OrderAggregate aggregate = given.newOrder();
        final AssignRegistrantDetails cmd = Given.Command.assignRegistrantDetails();

        final OrderRegistrantAssigned event = aggregate.handle(cmd, Given.Command.context());

        Assert.eventIsValid(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_AssignRegistrantDetails_command_and_throw_exception_if_it_is_empty() {
        final OrderAggregate aggregate = given.newOrder();
        final AssignRegistrantDetails cmd = AssignRegistrantDetails.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void apply_OrderPlaced_event_and_save_new_state() {
        final OrderAggregate aggregate = given.newOrder();
        final OrderPlaced event = Given.Event.orderPlaced();

        EventImporter.apply(aggregate, event, Given.Command.context());

        final Order state = aggregate.getState();
        assertEquals(event.getOrderId(), state.getId());
        assertEquals(event.getConferenceId(), state.getConferenceId());
        assertEquals(event.getSeatList(), state.getSeatList());
    }

    @Test
    public void apply_OrderUpdated_event_and_update_state() {
        final OrderAggregate aggregate = given.placedOrder();
        final OrderUpdated event = Given.Event.orderUpdated();

        EventImporter.apply(aggregate, event, Given.Command.context());

        assertEquals(event.getSeatList(), aggregate.getState()
                                                   .getSeatList());
    }

    @Test
    public void apply_OrderPartiallyReserved_event_and_update_state() {
        final OrderAggregate aggregate = given.placedOrder();
        final OrderPartiallyReserved event = Given.Event.orderPartiallyReserved();

        EventImporter.apply(aggregate, event, Given.Command.context());

        assertEquals(event.getSeatList(), aggregate.getState()
                                                   .getSeatList());
    }

    @Test
    public void apply_OrderReservationCompleted_event_and_update_state() {
        final OrderAggregate aggregate = given.placedOrder();
        final OrderReservationCompleted event = Given.Event.orderReservationCompleted();

        EventImporter.apply(aggregate, event, Given.Command.context());

        assertEquals(event.getSeatList(), aggregate.getState()
                                                   .getSeatList());
    }

    @Test
    public void apply_OrderConfirmed_event_and_update_state() {
        final OrderAggregate aggregate = given.placedOrder();
        final OrderConfirmed event = Given.Event.orderConfirmed();

        EventImporter.apply(aggregate, event, Given.Command.context());

        assertEquals(true, aggregate.getState()
                                    .getIsConfirmed());
    }

    @Test
    public void apply_OrderTotalsCalculated_event_and_update_state() {
        final OrderAggregate aggregate = given.placedOrder();
        final OrderTotalsCalculated event = Given.Event.orderTotalsCalculated();

        EventImporter.apply(aggregate, event, Given.Command.context());

        assertEquals(event.getTotal(), aggregate.getState()
                                                .getPrice());
    }

    @Test
    public void apply_OrderExpired_event_and_update_state() {
        final OrderAggregate aggregate = given.placedOrder();
        final OrderExpired event = Given.Event.orderExpired();

        EventImporter.apply(aggregate, event, Given.Command.context());

        assertTrue(aggregate.getState()
                            .getIsExpired());
    }

    @Test
    public void apply_OrderRegistrantAssigned_event_and_update_state() {
        final OrderAggregate aggregate = given.placedOrder();
        final OrderRegistrantAssigned event = Given.Event.orderRegistrantAssigned();

        EventImporter.apply(aggregate, event, Given.Command.context());

        assertEquals(event.getPersonalInfo(), aggregate.getState()
                                                       .getRegistrant());
    }

    private static class Assert {

        private static void eventIsValid(OrderPlaced event, RegisterToConference cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getConferenceId(), event.getConferenceId());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        private static void eventIsValid(OrderUpdated event, RegisterToConference cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        private static void eventIsValid(OrderTotalsCalculated event) {
            assertEquals(Given.ORDER_ID, event.getOrderId());
            assertEquals(Given.PricingServiceStub.ORDER_LINES, event.getOrderLineList());
            assertEquals(Given.PricingServiceStub.TOTAL_PRICE, event.getTotal());
        }

        private static void eventIsValid(OrderReservationCompleted event, MarkSeatsAsReserved cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getReservationExpiration(), event.getReservationExpiration());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        private static void eventIsValid(OrderPartiallyReserved event, MarkSeatsAsReserved cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getReservationExpiration(), event.getReservationExpiration());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        private static void eventIsValid(OrderExpired event, RejectOrder cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
        }

        private static void eventIsValid(OrderConfirmed event, ConfirmOrder cmd, List<SeatQuantity> seatsExpected) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(seatsExpected, event.getSeatList());
        }

        private static void eventIsValid(OrderRegistrantAssigned event, AssignRegistrantDetails cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getRegistrant(), event.getPersonalInfo());
        }
    }
}
