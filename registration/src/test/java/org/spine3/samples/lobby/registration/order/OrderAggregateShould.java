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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import com.google.protobuf.util.TimeUtil;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.money.Money;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.*;
import org.spine3.util.Identifiers;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.spine3.money.Currency.USD;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.*;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "TypeMayBeWeakened"})
public class OrderAggregateShould {

    private static final OrderId ORDER_ID = newOrderId();
    private static final ConferenceId CONFERENCE_ID = newConferenceId();
    private static final CommandContext CMD_CONTEXT = CommandContext.getDefaultInstance();
    private Given given;

    @Before
    public void setUpTest() {
        given = new Given();
    }

    @Test
    public void handle_RegisterToConference_command_and_generate_correct_events_if_order_is_new() {
        final TestOrderAggregate aggregate = given.newOrder();
        final RegisterToConference cmd = Given.registerToConferenceCommand();

        final List<Message> events = aggregate.handle(cmd, CMD_CONTEXT);

        assertEquals(2, events.size());
        final OrderPlaced placedEvent = findMessage(OrderPlaced.class, events);
        Assert.eventIsValid(placedEvent, cmd);
        final OrderTotalsCalculated calculatedEvent = findMessage(OrderTotalsCalculated.class, events);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test
    public void handle_RegisterToConference_command_and_generate_correct_events_if_order_is_already_created() {
        final TestOrderAggregate aggregate = given.alreadyPlacedOrder();
        final RegisterToConference cmd = Given.registerToConferenceCommand();

        final List<Message> events = aggregate.handle(cmd, CMD_CONTEXT);

        assertEquals(2, events.size());
        final OrderUpdated updated = findMessage(OrderUpdated.class, events);
        Assert.eventIsValid(updated, cmd);
        final OrderTotalsCalculated calculatedEvent = findMessage(OrderTotalsCalculated.class, events);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test(expected = IllegalStateException.class)
    public void throw_exception_if_handle_RegisterToConference_command_and_order_is_confirmed() {
        final TestOrderAggregate aggregate = given.confirmedOrder();
        final RegisterToConference cmd = Given.registerToConferenceCommand();
        aggregate.handle(cmd, CMD_CONTEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_handle_empty_RegisterToConference_command() {
        final TestOrderAggregate aggregate = given.newOrder();
        final RegisterToConference cmd = RegisterToConference.getDefaultInstance();
        aggregate.handle(cmd, CMD_CONTEXT);
    }

    @Test
    public void handle_MarkSeatsAsReserved_command_and_generate_correct_events_if_order_is_completely_reserved() {
        final MarkSeatsAsReserved cmd = Given.markSeatsAsReservedCommand();
        final TestOrderAggregate aggregate = given.completelyReservedOrder(cmd.getSeatList());

        final List<Message> events = aggregate.handle(cmd, CMD_CONTEXT);

        assertEquals(1, events.size());
        final OrderReservationCompleted completedEvent = findMessage(OrderReservationCompleted.class, events);
        Assert.eventIsValid(completedEvent, cmd);
    }

    @Test
    public void handle_MarkSeatsAsReserved_command_and_generate_correct_events_if_order_is_partially_reserved() {
        final MarkSeatsAsReserved cmd = Given.markSeatsAsReservedCommand();
        final TestOrderAggregate aggregate = given.partiallyReservedOrder(cmd.getSeatList());

        final List<Message> events = aggregate.handle(cmd, CMD_CONTEXT);

        assertEquals(2, events.size());
        final OrderPartiallyReserved reservedEvent = findMessage(OrderPartiallyReserved.class, events);
        Assert.eventIsValid(reservedEvent, cmd);
        final OrderTotalsCalculated calculatedEvent = findMessage(OrderTotalsCalculated.class, events);
        Assert.eventIsValid(calculatedEvent);
    }

    @Test(expected = IllegalStateException.class)
    public void throw_exception_if_handle_MarkSeatsAsReserved_command_and_order_is_confirmed() {
        final TestOrderAggregate aggregate = given.confirmedOrder();
        final MarkSeatsAsReserved cmd = Given.markSeatsAsReservedCommand();
        aggregate.handle(cmd, CMD_CONTEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_if_handle_empty_MarkSeatsAsReserved_command() {
        final TestOrderAggregate aggregate = given.newOrder();
        final MarkSeatsAsReserved cmd = MarkSeatsAsReserved.getDefaultInstance();
        aggregate.handle(cmd, CMD_CONTEXT);
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

        private static void stateIsValid(Order actualState, OrderId orderId, ConferenceId conferenceId, List<SeatQuantity> seats) {
            assertEquals(orderId, actualState.getId());
            assertEquals(conferenceId, actualState.getConferenceId());
            assertEquals(seats, actualState.getSeatList());
        }

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
            assertEquals(ORDER_ID, event.getOrderId());
            assertEquals(PricingServiceStub.ORDER_LINES, event.getOrderLineList());
            assertEquals(PricingServiceStub.TOTAL_PRICE, event.getTotal());
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
    }

    private static class Given {

        static final List<SeatQuantity> SEATS = ImmutableList.of(newSeatQuantity(5), newSeatQuantity(10));

        static final RegisterToConference REGISTER_TO_CONFERENCE = RegisterToConference.newBuilder()
                .setOrderId(ORDER_ID)
                .setConferenceId(CONFERENCE_ID)
                .addAllSeat(SEATS)
                .build();

        static final MarkSeatsAsReserved MARK_SEATS_AS_RESERVED = MarkSeatsAsReserved.newBuilder()
                .setOrderId(ORDER_ID)
                .setReservationExpiration(TimeUtil.getCurrentTime())
                .addAllSeat(SEATS)
                .build();

        private final TestOrderAggregate aggregate;

        private Given() {
            this.aggregate = new TestOrderAggregate(ORDER_ID);
            aggregate.setOrderPricingService(new PricingServiceStub());
        }

        TestOrderAggregate newOrder() {
            return aggregate;
        }

        TestOrderAggregate confirmedOrder() {
            final Order state = orderState(seats(), true);
            aggregate.incrementState(state);
            return aggregate;
        }

        TestOrderAggregate alreadyPlacedOrder() {
            final Order state = orderState(seats());
            aggregate.incrementState(state);
            return aggregate;
        }

        TestOrderAggregate completelyReservedOrder(List<SeatQuantity> reservedSeats) {
            final Order state = orderState(reservedSeats);
            aggregate.incrementState(state);
            return aggregate;
        }

        TestOrderAggregate partiallyReservedOrder(List<SeatQuantity> reservedSeats) {
            final List<SeatQuantity> requestedSeats = newArrayList(reservedSeats);
            //noinspection LocalVariableNamingConvention
            final int partlyReservedSeatIndex = 0;
            final SeatQuantity.Builder seat = requestedSeats.get(partlyReservedSeatIndex).toBuilder();
            seat.setQuantity(seat.getQuantity() + 5);
            requestedSeats.set(partlyReservedSeatIndex, seat.build());
            final Order state = orderState(ImmutableList.<SeatQuantity>builder().addAll(requestedSeats).build());
            aggregate.incrementState(state);
            return aggregate;
        }

        static RegisterToConference registerToConferenceCommand() {
            return REGISTER_TO_CONFERENCE;
        }

        static MarkSeatsAsReserved markSeatsAsReservedCommand() {
            return MARK_SEATS_AS_RESERVED;
        }

        static List<SeatQuantity> seats() {
            //noinspection ReturnOfCollectionOrArrayField
            return SEATS;
        }

        static Order orderState(Iterable<SeatQuantity> seats) {
            final Order.Builder order = Order.newBuilder()
                    .setId(ORDER_ID)
                    .setConferenceId(CONFERENCE_ID)
                    .addAllSeat(seats);
            return order.build();
        }

        static Order orderState(Iterable<SeatQuantity> seats, boolean isConfirmed) {
            final Order.Builder order = orderState(seats).toBuilder();
            order.setIsConfirmed(isConfirmed);
            return order.build();
        }

        static SeatQuantity newSeatQuantity(int quantity) {
            final String id = Identifiers.newUuid();
            final SeatQuantity.Builder result = SeatQuantity.newBuilder()
                    .setSeatTypeId(SeatTypeId.newBuilder().setUuid(id))
                    .setQuantity(quantity);
            return result.build();
        }
    }

    private static class PricingServiceStub implements OrderPricingService {

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

        // Is overridden to make accessible in tests.
        @Override
        public void incrementState(Order newState) {
            super.incrementState(newState);
        }

        public void setIsConfirmed(boolean isConfirmed) {
            final Order.Builder order = getState().toBuilder();
            order.setIsConfirmed(isConfirmed);
            incrementState(order.build());
        }
    }
}
