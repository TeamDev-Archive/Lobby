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

package org.spine3.samples.lobby.registration.order;

import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;
import org.spine3.money.Money;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.registration.contracts.OrderConfirmed;
import org.spine3.samples.lobby.registration.contracts.OrderExpired;
import org.spine3.samples.lobby.registration.contracts.OrderPartiallyReserved;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderRegistrantAssigned;
import org.spine3.samples.lobby.registration.contracts.OrderReservationCompleted;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.samples.lobby.registration.contracts.OrderTotalsCalculated;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatOrderLine;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Throwables.propagate;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.spine3.money.Currency.USD;
import static org.spine3.money.MoneyUtil.newMoney;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "TypeMayBeWeakened", "UtilityClass", "OverlyCoupledClass",
        "ClassWithTooManyMethods"})
public class OrderAggregateShould {

    private Given given;

    @Before
    public void setUpTest() {
        given = new Given();
    }

    @Test
    public void handle_RegisterToConference_command_and_generate_correct_events_if_order_is_new() {
        final TestOrderAggregate aggregate = given.newOrder();
        final RegisterToConference cmd = Given.Command.registerToConference();

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(2, events.size());
        final OrderPlaced placedEvent = findMessage(OrderPlaced.class, events);
        Assert.eventIsValid(placedEvent, cmd);
        final OrderTotalsCalculated calculatedEvent = findMessage(OrderTotalsCalculated.class, events);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test
    public void handle_RegisterToConference_command_and_generate_correct_events_if_order_is_already_placed() {
        final TestOrderAggregate aggregate = given.placedOrder();
        final RegisterToConference cmd = Given.Command.registerToConference();

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(2, events.size());
        final OrderUpdated updated = findMessage(OrderUpdated.class, events);
        Assert.eventIsValid(updated, cmd);
        final OrderTotalsCalculated calculatedEvent = findMessage(OrderTotalsCalculated.class, events);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_RegisterToConference_command_and_throw_exception_if_order_is_confirmed() {
        final TestOrderAggregate aggregate = given.confirmedOrder();
        final RegisterToConference cmd = Given.Command.registerToConference();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_RegisterToConference_command_and_throw_exception_if_it_is_empty() {
        final TestOrderAggregate aggregate = given.newOrder();
        final RegisterToConference cmd = RegisterToConference.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_MarkSeatsAsReserved_command_and_generate_correct_events_if_order_is_completely_reserved() {
        final MarkSeatsAsReserved cmd = Given.Command.markSeatsAsReserved();
        final TestOrderAggregate aggregate = given.completelyReservedOrder(cmd.getSeatList());

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(1, events.size());
        final OrderReservationCompleted completedEvent = findMessage(OrderReservationCompleted.class, events);
        Assert.eventIsValid(completedEvent, cmd);
    }

    @Test
    public void handle_MarkSeatsAsReserved_command_and_generate_correct_events_if_order_is_partially_reserved() {
        final MarkSeatsAsReserved cmd = Given.Command.markSeatsAsReserved();
        final TestOrderAggregate aggregate = given.partiallyReservedOrder(cmd.getSeatList());

        final List<Message> events = aggregate.handle(cmd, Given.Command.context());

        assertEquals(2, events.size());
        final OrderPartiallyReserved reservedEvent = findMessage(OrderPartiallyReserved.class, events);
        Assert.eventIsValid(reservedEvent, cmd);
        final OrderTotalsCalculated calculatedEvent = findMessage(OrderTotalsCalculated.class, events);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_MarkSeatsAsReserved_command_and_throw_exception_if_order_is_confirmed() {
        final TestOrderAggregate aggregate = given.confirmedOrder();
        final MarkSeatsAsReserved cmd = Given.Command.markSeatsAsReserved();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_MarkSeatsAsReserved_command_and_throw_exception_if_it_is_empty() {
        final TestOrderAggregate aggregate = given.newOrder();
        final MarkSeatsAsReserved cmd = MarkSeatsAsReserved.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_RejectOrder_command_and_generate_OrderExpired_event() {
        final TestOrderAggregate aggregate = given.newOrder();
        final RejectOrder cmd = Given.Command.rejectOrder();

        final OrderExpired event = aggregate.handle(cmd, Given.Command.context());

        Assert.eventIsValid(event, cmd);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_RejectOrder_command_and_throw_exception_if_order_is_confirmed() {
        final TestOrderAggregate aggregate = given.confirmedOrder();
        final RejectOrder cmd = Given.Command.rejectOrder();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_RejectOrder_command_and_throw_exception_if_it_is_empty() {
        final TestOrderAggregate aggregate = given.newOrder();
        final RejectOrder cmd = RejectOrder.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_ConfirmOrder_command_and_generate_OrderConfirmed_event() {
        final TestOrderAggregate aggregate = given.newOrder();
        final ConfirmOrder cmd = Given.Command.confirmOrder();

        final OrderConfirmed event = aggregate.handle(cmd, Given.Command.context());

        final List<SeatQuantity> seats = aggregate.getState().getSeatList();
        Assert.eventIsValid(event, cmd, seats);
    }

    @Test(expected = IllegalStateException.class)
    public void handle_ConfirmOrder_command_and_throw_exception_if_order_is_confirmed() {
        final TestOrderAggregate aggregate = given.confirmedOrder();
        final ConfirmOrder cmd = Given.Command.confirmOrder();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_ConfirmOrder_command_and_throw_exception_if_it_is_empty() {
        final TestOrderAggregate aggregate = given.newOrder();
        final ConfirmOrder cmd = ConfirmOrder.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void handle_AssignRegistrantDetails_command_and_generate_OrderRegistrantAssigned_event() {
        final TestOrderAggregate aggregate = given.newOrder();
        final AssignRegistrantDetails cmd = Given.Command.assignRegistrantDetails();

        final OrderRegistrantAssigned event = aggregate.handle(cmd, Given.Command.context());

        Assert.eventIsValid(event, cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handle_AssignRegistrantDetails_command_and_throw_exception_if_it_is_empty() {
        final TestOrderAggregate aggregate = given.newOrder();
        final AssignRegistrantDetails cmd = AssignRegistrantDetails.getDefaultInstance();
        aggregate.handle(cmd, Given.Command.context());
    }

    @Test
    public void apply_OrderPlaced_event_and_save_new_state() {
        final TestOrderAggregate aggregate = given.newOrder();
        final OrderPlaced event = Given.Event.orderPlaced();

        aggregate.apply(event);

        final Order state = aggregate.getState();
        assertEquals(event.getOrderId(), state.getId());
        assertEquals(event.getConferenceId(), state.getConferenceId());
        assertEquals(event.getSeatList(), state.getSeatList());
    }

    @Test
    public void apply_OrderUpdated_event_and_update_state() {
        final TestOrderAggregate aggregate = given.placedOrder();
        final OrderUpdated event = Given.Event.orderUpdated();

        aggregate.apply(event);

        assertEquals(event.getSeatList(), aggregate.getState().getSeatList());
    }

    @Test
    public void apply_OrderPartiallyReserved_event_and_update_state() {
        final TestOrderAggregate aggregate = given.placedOrder();
        final OrderPartiallyReserved event = Given.Event.orderPartiallyReserved();

        aggregate.apply(event);

        assertEquals(event.getSeatList(), aggregate.getState().getSeatList());
    }

    @Test
    public void apply_OrderReservationCompleted_event_and_update_state() {
        final TestOrderAggregate aggregate = given.placedOrder();
        final OrderReservationCompleted event = Given.Event.orderReservationCompleted();

        aggregate.apply(event);

        assertEquals(event.getSeatList(), aggregate.getState().getSeatList());
    }

    @Test
    public void apply_OrderConfirmed_event_and_update_state() {
        final TestOrderAggregate aggregate = given.placedOrder();
        final OrderConfirmed event = Given.Event.orderConfirmed();

        aggregate.apply(event);

        assertEquals(true, aggregate.getState().getIsConfirmed());
    }

    private static <E extends Message> E findMessage(Class<E> messageClass, Iterable<Message> messages) {
        for (Message message : messages) {
            if (message.getClass().equals(messageClass)) {
                @SuppressWarnings("unchecked")
                final E result = (E) message;
                return result;
            }
        }
        fail("No message found of class: " + messageClass.getName());
        throw new RuntimeException("");
    }

    private static class Assert {

        static void eventIsValid(OrderPlaced event, RegisterToConference cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getConferenceId(), event.getConferenceId());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        static void eventIsValid(OrderUpdated event, RegisterToConference cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        static void eventIsValid(OrderTotalsCalculated event) {
            assertEquals(Given.ORDER_ID, event.getOrderId());
            assertEquals(PricingServiceStub.ORDER_LINES, event.getOrderLineList());
            assertEquals(PricingServiceStub.TOTAL_PRICE, event.getTotal());
        }

        static void eventIsValid(OrderReservationCompleted event, MarkSeatsAsReserved cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getReservationExpiration(), event.getReservationExpiration());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        static void eventIsValid(OrderPartiallyReserved event, MarkSeatsAsReserved cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getReservationExpiration(), event.getReservationExpiration());
            assertEquals(cmd.getSeatList(), event.getSeatList());
        }

        static void eventIsValid(OrderExpired event, RejectOrder cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
        }

        static void eventIsValid(OrderConfirmed event, ConfirmOrder cmd, List<SeatQuantity> seatsExpected) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(seatsExpected, event.getSeatList());
        }

        static void eventIsValid(OrderRegistrantAssigned event, AssignRegistrantDetails cmd) {
            assertEquals(cmd.getOrderId(), event.getOrderId());
            assertEquals(cmd.getRegistrant(), event.getPersonalInfo());
        }
    }

    static class PricingServiceStub implements OrderPricingService {

        private static final Money TOTAL_PRICE = newMoney(100, USD);
        private static final SeatOrderLine ORDER_LINE = SeatOrderLine.newBuilder()
                .setQuantity(10)
                .setUnitPrice(newMoney(10, USD))
                .setLineTotal(TOTAL_PRICE)
                .build();
        private static final List<SeatOrderLine> ORDER_LINES = singletonList(ORDER_LINE);

        @Override
        public OrderTotal calculateTotalOrderPrice(ConferenceId conferenceId, Iterable<SeatQuantity> seats) {
            final OrderTotal.Builder result = OrderTotal.newBuilder()
                    .setTotalPrice(TOTAL_PRICE)
                    .addAllOrderLine(ORDER_LINES);
            return result.build();
        }
    }

    public static class TestOrderAggregate extends OrderAggregate {

        public TestOrderAggregate(OrderId id) {
            super(id);
        }

        // Is overridden to do not throw exceptions while retrieving the default state via reflection.
        @Override
        @SuppressWarnings("RefusedBequest")
        protected Order getDefaultState() {
            return Order.getDefaultInstance();
        }

        // Is overridden to make accessible in tests.
        @Override
        public void incrementState(Order newState) {
            super.incrementState(newState);
        }

        void apply(OrderPlaced event) {
            invokeApplyMethod(event);
        }

        void apply(OrderUpdated event) {
            invokeApplyMethod(event);
        }

        void apply(OrderPartiallyReserved event) {
            invokeApplyMethod(event);
        }

        void apply(OrderReservationCompleted event) {
            invokeApplyMethod(event);
        }

        void apply(OrderConfirmed event) {
            invokeApplyMethod(event);
        }

        private void invokeApplyMethod(Message event) {
            try {
                //noinspection DuplicateStringLiteralInspection
                final Method apply = OrderAggregate.class.getDeclaredMethod("apply", event.getClass());
                apply.setAccessible(true);
                apply.invoke(this, event);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                propagate(e);
            }
        }
    }
}
