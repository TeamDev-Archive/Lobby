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
import com.google.protobuf.util.TimeUtil;
import org.spine3.base.CommandContext;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.registration.contracts.OrderConfirmed;
import org.spine3.samples.lobby.registration.contracts.OrderPartiallyReserved;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderReservationCompleted;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.spine3.samples.lobby.common.util.IdFactory.newConferenceId;
import static org.spine3.samples.lobby.common.util.IdFactory.newOrderId;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newPersonalInfo;
import static org.spine3.samples.lobby.registration.util.Seats.newSeatQuantity;

/**
 * The test data factory for {@link OrderAggregate} and {@link OrderRepository} tests.
 *
 * @author Alexander Litus
 */
class Given {

    static final OrderId ORDER_ID = newOrderId();
    static final ConferenceId CONFERENCE_ID = newConferenceId();
    static final List<SeatQuantity> SEATS = ImmutableList.of(newSeatQuantity(5), newSeatQuantity(10));

    private final OrderAggregateShould.TestOrderAggregate aggregate;

    Given() {
        aggregate = new OrderAggregateShould.TestOrderAggregate(ORDER_ID);
        aggregate.setOrderPricingService(new OrderAggregateShould.PricingServiceStub());
    }

    OrderAggregateShould.TestOrderAggregate newOrder() {
        return aggregate;
    }

    OrderAggregateShould.TestOrderAggregate confirmedOrder() {
        final Order state = orderState(seats(), true);
        aggregate.incrementState(state);
        return aggregate;
    }

    OrderAggregateShould.TestOrderAggregate placedOrder() {
        final Order state = orderState(seats());
        aggregate.incrementState(state);
        return aggregate;
    }

    OrderAggregateShould.TestOrderAggregate completelyReservedOrder(Iterable<SeatQuantity> reservedSeats) {
        final Order state = orderState(reservedSeats);
        aggregate.incrementState(state);
        return aggregate;
    }

    OrderAggregateShould.TestOrderAggregate partiallyReservedOrder(Iterable<SeatQuantity> reservedSeats) {
        final List<SeatQuantity> requestedSeats = newArrayList(reservedSeats);
        //noinspection LocalVariableNamingConvention
        final int partlyReservedSeatIndex = 0;
        final SeatQuantity.Builder seat = requestedSeats.get(partlyReservedSeatIndex).toBuilder();
        seat.setQuantity(seat.getQuantity() + 5);
        requestedSeats.set(partlyReservedSeatIndex, seat.build());
        final Order state = orderState(requestedSeats);
        aggregate.incrementState(state);
        return aggregate;
    }

    static List<SeatQuantity> seats() {
        //noinspection ReturnOfCollectionOrArrayField
        return SEATS;
    }

    static Order orderState(Iterable<SeatQuantity> seats) {
        final Order.Builder order = Order.newBuilder()
                .setId(ORDER_ID)
                .setConferenceId(CONFERENCE_ID)
                .addAllSeat(ImmutableList.copyOf(seats));
        return order.build();
    }

    static Order orderState(Iterable<SeatQuantity> seats, boolean isConfirmed) {
        final Order.Builder order = orderState(seats).toBuilder();
        order.setIsConfirmed(isConfirmed);
        return order.build();
    }

    @SuppressWarnings("UtilityClass")
    static class Command {

        static final CommandContext CMD_CONTEXT = CommandContext.getDefaultInstance();

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

        static final RejectOrder REJECT_ORDER = RejectOrder.newBuilder().setOrderId(ORDER_ID).build();

        static final ConfirmOrder CONFIRM_ORDER = ConfirmOrder.newBuilder().setOrderId(ORDER_ID).build();

        static final AssignRegistrantDetails ASSIGN_REGISTRANT_DETAILS = AssignRegistrantDetails.newBuilder()
                .setOrderId(ORDER_ID)
                .setRegistrant(newPersonalInfo("John", "Black", "jblack@gmail.com"))
                .build();

        private Command() {
        }

        static CommandContext context() {
            return CMD_CONTEXT;
        }

        static RegisterToConference registerToConference() {
            return REGISTER_TO_CONFERENCE;
        }

        static MarkSeatsAsReserved markSeatsAsReserved() {
            return MARK_SEATS_AS_RESERVED;
        }

        static ConfirmOrder confirmOrder() {
            return CONFIRM_ORDER;
        }

        static RejectOrder rejectOrder() {
            return REJECT_ORDER;
        }

        static AssignRegistrantDetails assignRegistrantDetails() {
            return ASSIGN_REGISTRANT_DETAILS;
        }
    }

    @SuppressWarnings({"UtilityClass", "MagicNumber"})
    static class Event {

        static final OrderPlaced ORDER_PLACED = OrderPlaced.newBuilder()
                .setOrderId(ORDER_ID)
                .setConferenceId(CONFERENCE_ID)
                .addAllSeat(SEATS)
                .build();

        private static final OrderConfirmed ORDER_CONFIRMED = OrderConfirmed.newBuilder().setOrderId(ORDER_ID).build();

        private Event() {}

        static OrderPlaced orderPlaced() {
            return ORDER_PLACED;
        }

        static OrderUpdated orderUpdated() {
            final OrderUpdated.Builder result = OrderUpdated.newBuilder()
                    .setOrderId(ORDER_ID)
                    .addSeat(newSeatQuantity(16))
                    .addSeat(newSeatQuantity(32));
            return result.build();
        }

        static OrderPartiallyReserved orderPartiallyReserved() {
            final OrderPartiallyReserved.Builder result = OrderPartiallyReserved.newBuilder()
                    .setOrderId(ORDER_ID)
                    .addSeat(newSeatQuantity(64))
                    .addSeat(newSeatQuantity(128));
            return result.build();
        }

        static OrderReservationCompleted orderReservationCompleted() {
            final OrderReservationCompleted.Builder result = OrderReservationCompleted.newBuilder()
                    .setOrderId(ORDER_ID)
                    .addSeat(newSeatQuantity(256))
                    .addSeat(newSeatQuantity(512));
            return result.build();
        }

        static OrderConfirmed orderConfirmed() {
            return ORDER_CONFIRMED;
        }
    }
}
