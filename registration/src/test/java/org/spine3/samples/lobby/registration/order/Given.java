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
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.TimeUtil;
import org.spine3.base.CommandContext;
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

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.spine3.money.Currency.USD;
import static org.spine3.money.MoneyUtil.newMoney;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.IdFactory.newOrderId;
import static org.spine3.samples.lobby.common.util.testdata.TestDataFactory.newPersonalInfo;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * The test data factory for {@link OrderAggregate} and {@link OrderRepository} tests.
 *
 * @author Alexander Litus
 */
/* package */ class Given {

    /* package */ static final OrderId ORDER_ID = newOrderId();
    private static final ConferenceId CONFERENCE_ID = newConferenceId();
    private static final List<SeatQuantity> SEATS = ImmutableList.of(newSeatQuantity(5), newSeatQuantity(10));

    private final OrderAggregate aggregate;

    /* package */ Given() {
        aggregate = new OrderAggregate(ORDER_ID);
        aggregate.setOrderPricingService(new PricingServiceStub());
    }

    /* package */ OrderAggregate newOrder() {
        return aggregate;
    }

    /* package */ OrderAggregate confirmedOrder() {
        final Order state = orderState(SEATS, true);
        final ConfirmOrder command = ConfirmOrder.newBuilder().setOrderId(ORDER_ID).build();
        aggregate.handle(command, CommandContext.getDefaultInstance());
        aggregate.incrementAggregateState(state);
        return aggregate;
    }

    /* package */ OrderAggregate placedOrder() {
        final Order state = orderState(SEATS);
        aggregate.incrementAggregateState(state);
        return aggregate;
    }

    /* package */ OrderAggregate completelyReservedOrder(Iterable<SeatQuantity> reservedSeats) {
        final List<SeatQuantity> requestedSeats = newArrayList(reservedSeats);
        final Order state = orderState(reservedSeats);
        aggregate.handle(MarkSeatsAsReserved.newBuilder()
                                            .setOrderId(ORDER_ID)
                                            .setReservationExpiration(Timestamp.getDefaultInstance())
                                            .addAllSeat(requestedSeats)
                                            .build(), CommandContext.getDefaultInstance());
        aggregate.incrementAggregateState(state);
        return aggregate;
    }

    /* package */ OrderAggregate partiallyReservedOrder(Iterable<SeatQuantity> reservedSeats) {
        final List<SeatQuantity> requestedSeats = newArrayList(reservedSeats);
        final int partlyReservedSeatIndex = 0;
        final SeatQuantity.Builder seat = requestedSeats.get(partlyReservedSeatIndex)
                                                        .toBuilder();
        seat.setQuantity(seat.getQuantity() + 5);
        requestedSeats.set(partlyReservedSeatIndex, seat.build());
        final Order state = orderState(requestedSeats);
        aggregate.incrementAggregateState(state);
        return aggregate;
    }

    private static Order orderState(Iterable<SeatQuantity> seats) {
        final Order.Builder order = Order.newBuilder()
                                         .setId(ORDER_ID)
                                         .setConferenceId(CONFERENCE_ID)
                                         .addAllSeat(ImmutableList.copyOf(seats));
        return order.build();
    }

    private static Order orderState(Iterable<SeatQuantity> seats, boolean isConfirmed) {
        final Order.Builder order = orderState(seats).toBuilder();
        order.setIsConfirmed(isConfirmed);
        return order.build();
    }

    /* package */ static class PricingServiceStub implements OrderPricingService {

        /* package */ static final Money TOTAL_PRICE = newMoney(100, USD);
        private static final SeatOrderLine ORDER_LINE = SeatOrderLine.newBuilder()
                                                                     .setQuantity(10)
                                                                     .setUnitPrice(newMoney(10, USD))
                                                                     .setLineTotal(TOTAL_PRICE)
                                                                     .build();
        /* package */ static final List<SeatOrderLine> ORDER_LINES = singletonList(ORDER_LINE);

        @Override
        public OrderTotal calculateTotalOrderPrice(ConferenceId conferenceId, Iterable<SeatQuantity> seats) {
            final OrderTotal.Builder result = OrderTotal.newBuilder()
                                                        .setTotalPrice(TOTAL_PRICE)
                                                        .addAllOrderLine(ORDER_LINES);
            return result.build();
        }
    }

    /**
     * A test utility class providing commands.
     */
    @SuppressWarnings("UtilityClass")
    /* package */ static class Command {

        private static final CommandContext CMD_CONTEXT = CommandContext.getDefaultInstance();

        private static final RegisterToConference REGISTER_TO_CONFERENCE
                = RegisterToConference.newBuilder()
                                      .setOrderId(ORDER_ID)
                                      .setConferenceId(CONFERENCE_ID)
                                      .addAllSeat(SEATS)
                                      .build();

        private static final MarkSeatsAsReserved MARK_SEATS_AS_RESERVED
                = MarkSeatsAsReserved.newBuilder()
                                     .setOrderId(ORDER_ID)
                                     .setReservationExpiration(TimeUtil.getCurrentTime())
                                     .addAllSeat(SEATS)
                                     .build();

        private static final RejectOrder REJECT_ORDER = RejectOrder.newBuilder()
                                                                   .setOrderId(ORDER_ID)
                                                                   .build();

        private static final ConfirmOrder CONFIRM_ORDER = ConfirmOrder.newBuilder()
                                                                      .setOrderId(ORDER_ID)
                                                                      .build();

        private static final AssignRegistrantDetails ASSIGN_REGISTRANT_DETAILS
                = AssignRegistrantDetails.newBuilder()
                                         .setOrderId(ORDER_ID)
                                         .setRegistrant(newPersonalInfo("John", "Black", "jblack@gmail.com"))
                                         .build();

        private Command() {
        }

        /* package */ static CommandContext context() {
            return CMD_CONTEXT;
        }

        /* package */ static RegisterToConference registerToConference() {
            return REGISTER_TO_CONFERENCE;
        }

        /* package */ static MarkSeatsAsReserved markSeatsAsReserved() {
            return MARK_SEATS_AS_RESERVED;
        }

        /* package */ static ConfirmOrder confirmOrder() {
            return CONFIRM_ORDER;
        }

        /* package */ static RejectOrder rejectOrder() {
            return REJECT_ORDER;
        }

        /* package */ static AssignRegistrantDetails assignRegistrantDetails() {
            return ASSIGN_REGISTRANT_DETAILS;
        }
    }

    /**
     * A test utility class providing events.
     */
    @SuppressWarnings({"UtilityClass", "MagicNumber"})
    /* package */ static class Event {

        private static final OrderPlaced ORDER_PLACED = OrderPlaced.newBuilder()
                                                                   .setOrderId(ORDER_ID)
                                                                   .setConferenceId(CONFERENCE_ID)
                                                                   .addAllSeat(SEATS)
                                                                   .build();

        private static final OrderConfirmed ORDER_CONFIRMED = OrderConfirmed.newBuilder()
                                                                            .setOrderId(ORDER_ID)
                                                                            .build();

        private Event() {
        }

        /* package */ static OrderPlaced orderPlaced() {
            return ORDER_PLACED;
        }

        /* package */ static OrderUpdated orderUpdated() {
            final OrderUpdated.Builder result = OrderUpdated.newBuilder()
                                                            .setOrderId(ORDER_ID)
                                                            .addSeat(newSeatQuantity(16))
                                                            .addSeat(newSeatQuantity(32));
            return result.build();
        }

        /* package */ static OrderPartiallyReserved orderPartiallyReserved() {
            final OrderPartiallyReserved.Builder result = OrderPartiallyReserved.newBuilder()
                                                                                .setOrderId(ORDER_ID)
                                                                                .addSeat(newSeatQuantity(64))
                                                                                .addSeat(newSeatQuantity(128));
            return result.build();
        }

        /* package */ static OrderReservationCompleted orderReservationCompleted() {
            final OrderReservationCompleted.Builder result = OrderReservationCompleted.newBuilder()
                                                                                      .setOrderId(ORDER_ID)
                                                                                      .addSeat(newSeatQuantity(256))
                                                                                      .addSeat(newSeatQuantity(512));
            return result.build();
        }

        /* package */ static OrderConfirmed orderConfirmed() {
            return ORDER_CONFIRMED;
        }

        /* package */ static OrderTotalsCalculated orderTotalsCalculated() {
            final OrderTotalsCalculated.Builder builder = OrderTotalsCalculated.newBuilder()
                                                                               .setOrderId(ORDER_ID)
                                                                               .setTotal(newMoney(100, USD));
            return builder.build();
        }

        /* package */ static OrderExpired orderExpired() {
            final OrderExpired.Builder builder = OrderExpired.newBuilder()
                                                             .setOrderId(ORDER_ID);
            return builder.build();
        }

        /* package */ static OrderRegistrantAssigned orderRegistrantAssigned() {
            final OrderRegistrantAssigned.Builder builder
                    = OrderRegistrantAssigned.newBuilder()
                                             .setOrderId(ORDER_ID)
                                             .setPersonalInfo(
                                                     newPersonalInfo("Albert", "Einstein", "einstein@fu-berlin.de"));
            return builder.build();
        }
    }
}
