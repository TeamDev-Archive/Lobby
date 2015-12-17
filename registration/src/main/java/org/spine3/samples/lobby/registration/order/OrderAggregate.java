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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Duration;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.base.CommandContext;
import org.spine3.money.Money;
import org.spine3.protobuf.Durations;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.common.util.RandomPasswordGenerator;
import org.spine3.samples.lobby.registration.contracts.*;
import org.spine3.server.Assign;
import org.spine3.server.Entity;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.Apply;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.protobuf.util.TimeUtil.add;
import static com.google.protobuf.util.TimeUtil.getCurrentTime;
import static java.lang.String.format;

/**
 * The order aggregate root.
 *
 * @author Alexander Litus
 */
@SuppressWarnings({"TypeMayBeWeakened", "OverlyCoupledClass"})
public class OrderAggregate extends Aggregate<OrderId, Order> {

    /**
     * The period in minutes after which the reservation expires.
     */
    private static final int EXPIRATION_PERIOD_MINUTES = 15;

    private static final Duration RESERVATION_EXPIRATION_PERIOD = Durations.ofMinutes(EXPIRATION_PERIOD_MINUTES);

    private static final int ACCESS_CODE_LENGTH = 8;

    private OrderPricingService pricingService;

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     * @see Entity
     */
    public OrderAggregate(OrderId id) {
        super(id);
    }

    /**
     * Sets the pricing service to use to calculate a price of an order.
     *
     * @param service the pricing service implementation
     */
    public void setOrderPricingService(OrderPricingService service) {
        this.pricingService = service;
    }

    @Override
    protected Order getDefaultState() {
        return Order.getDefaultInstance();
    }

    @Assign
    public List<Message> handle(RegisterToConference command, CommandContext context) {
        Validator.checkNotConfirmed(getState(), command);
        Validator.validateCommand(command);

        final ImmutableList.Builder<Message> result = ImmutableList.builder();
        final boolean isNew = getVersion() == 0;
        if (isNew) {
            final OrderPlaced placed = EventBuilder.buildOrderPlaced(command);
            result.add(placed);
        } else {
            final OrderUpdated updated = EventBuilder.buildOrderUpdated(command);
            result.add(updated);
        }
        final OrderTotalsCalculated totalsCalculated = buildOrderTotalsCalculated(command.getOrderId(),
                command.getConferenceId(), command.getSeatList());
        result.add(totalsCalculated);
        return result.build();
    }

    @Assign
    public List<Message> handle(MarkSeatsAsReserved command, CommandContext context) {
        Validator.checkNotConfirmed(getState(), command);
        Validator.validateCommand(command);

        final ImmutableList.Builder<Message> result = ImmutableList.builder();
        final List<SeatQuantity> reservedSeats = command.getSeatList();

        if (isOrderPartiallyReserved(reservedSeats)) {
            final OrderPartiallyReserved partiallyReserved = EventBuilder.buildOrderPartiallyReserved(command);
            result.add(partiallyReserved);

            final OrderTotalsCalculated newTotalsCalculated = buildOrderTotalsCalculated(command.getOrderId(),
                    getState().getConferenceId(), command.getSeatList());
            result.add(newTotalsCalculated);
        } else {
            final OrderReservationCompleted reservationCompleted = EventBuilder.buildOrderReservationCompleted(command);
            result.add(reservationCompleted);
        }
        return result.build();
    }

    @Assign
    public OrderExpired handle(RejectOrder command, CommandContext context) {
        Validator.checkNotConfirmed(getState(), command);
        Validator.validateCommand(command);
        final OrderExpired result = OrderExpired.newBuilder()
                .setOrderId(command.getOrderId())
                .build();
        return result;
    }

    @Assign
    public OrderConfirmed handle(ConfirmOrder command, CommandContext context) {
        Validator.checkNotConfirmed(getState(), command);
        Validator.validateCommand(command);
        final OrderConfirmed result = OrderConfirmed.newBuilder()
                .setOrderId(command.getOrderId())
                .build();
        return result;
    }

    @Assign
    public OrderRegistrantAssigned handle(AssignRegistrantDetails command, CommandContext context) {
        Validator.validateCommand(command);
        final OrderRegistrantAssigned result = OrderRegistrantAssigned.newBuilder()
                .setOrderId(command.getOrderId())
                .setPersonalInfo(command.getRegistrant())
                .build();
        return result;
    }

    @Apply
    private void apply(OrderPlaced event) {
        final Order.Builder state = Order.newBuilder()
                .setId(event.getOrderId())
                .setConferenceId(event.getConferenceId())
                .addAllSeat(event.getSeatList());
        incrementState(state.build());
    }

    @Apply
    private void apply(OrderUpdated event) {
        final Order.Builder state = getState().toBuilder();
        state.addAllSeat(event.getSeatList());
        incrementState(state.build());
    }

    @Apply
    private void apply(OrderPartiallyReserved event) {
        final Order.Builder state = getState().toBuilder();
        state.addAllSeat(event.getSeatList());
        incrementState(state.build());
    }

    @Apply
    private void apply(OrderReservationCompleted event) {
        final Order.Builder state = getState().toBuilder();
        state.addAllSeat(event.getSeatList());
        incrementState(state.build());
    }

    @Apply
    private void apply(OrderTotalsCalculated event) {
        // NOP
    }

    @Apply
    private void apply(OrderExpired event) {
        // NOP
    }

    @Apply
    private void apply(OrderConfirmed event) {
        final Order.Builder state = getState().toBuilder();
        state.setIsConfirmed(true);
        incrementState(state.build());
    }

    @Apply
    private void apply(OrderRegistrantAssigned event) {
        // NOP
    }

    private boolean isOrderPartiallyReserved(final List<SeatQuantity> reservedSeats) {
        final List<SeatQuantity> requestedSeats = getState().getSeatList();
        if (reservedSeats.size() < requestedSeats.size()) {
            return true;
        }
        final Collection<SeatQuantity> partlyReservedSeats = findPartlyReservedSeats(requestedSeats, reservedSeats);
        final boolean isPartlyReserved = partlyReservedSeats.size() > 0;
        return isPartlyReserved;
    }

    private static Collection<SeatQuantity> findPartlyReservedSeats(final List<SeatQuantity> requestedSeats,
                                                                    final List<SeatQuantity> reservedSeats) {
        final Collection<SeatQuantity> result = filter(requestedSeats, new Predicate<SeatQuantity>() {
            @Override
            public boolean apply(@Nullable SeatQuantity requestedOne) {
                if (requestedOne == null) {
                    return false;
                }
                final SeatQuantity reservedOne = findById(reservedSeats, requestedOne.getSeatTypeId());
                if (reservedOne == null) {
                    return false;
                }
                final boolean isPartlyReserved = reservedOne.getQuantity() < requestedOne.getQuantity();
                return isPartlyReserved;
            }
        });
        return result;
    }

    @Nullable
    private static SeatQuantity findById(final List<SeatQuantity> seats, final SeatTypeId id) {
        final SeatQuantity defaultValue = null;
        final SeatQuantity result = find(seats, new Predicate<SeatQuantity>() {
            @Override
            public boolean apply(@Nullable SeatQuantity seat) {
                final boolean result =
                        (seat != null) &&
                        seat.hasSeatTypeId() &&
                        seat.getSeatTypeId().equals(id);
                return result;
            }
        }, defaultValue);
        return result;
    }

    @Override
    @SuppressWarnings("RefusedBequest") // method from superclass does nothing
    protected void validate(Order newState) throws IllegalStateException {
        Validator.checkNewState(newState);
    }

    private OrderTotalsCalculated buildOrderTotalsCalculated(OrderId orderId, ConferenceId conferenceId, List<SeatQuantity> seats) {
        final OrderTotal total = pricingService.calculateTotalOrderPrice(conferenceId, seats);
        return EventBuilder.buildOrderTotalsCalculated(orderId, total);
    }

    private static class EventBuilder {

        private static OrderTotalsCalculated buildOrderTotalsCalculated(OrderId orderId, OrderTotal total) {
            final OrderTotalsCalculated.Builder result = OrderTotalsCalculated.newBuilder()
                    .setOrderId(orderId)
                    .addAllOrderLine(total.getOrderLineList());

            final Money totalPrice = total.getTotalPrice();
            if (totalPrice.getAmount() > 0) {
                result.setTotal(totalPrice);
            } else {
                result.setIsFreeOfCharge(true);
            }
            return result.build();
        }

        private static OrderPlaced buildOrderPlaced(RegisterToConference command) {
            final Timestamp expirationTime = add(getCurrentTime(), RESERVATION_EXPIRATION_PERIOD);
            final String code = RandomPasswordGenerator.generate(ACCESS_CODE_LENGTH);
            final OrderAccessCode accessCode = OrderAccessCode.newBuilder().setValue(code).build();

            final OrderPlaced.Builder result = OrderPlaced.newBuilder()
                    .setOrderId(command.getOrderId())
                    .setConferenceId(command.getConferenceId())
                    .addAllSeat(command.getSeatList())
                    .setReservationAutoExpiration(expirationTime)
                    .setAccessCode(accessCode);
            return result.build();
        }

        private static OrderUpdated buildOrderUpdated(RegisterToConference command) {
            final OrderUpdated.Builder result = OrderUpdated.newBuilder()
                    .setOrderId(command.getOrderId())
                    .addAllSeat(command.getSeatList());
            return result.build();
        }

        private static OrderPartiallyReserved buildOrderPartiallyReserved(MarkSeatsAsReserved command) {
            final OrderPartiallyReserved.Builder result = OrderPartiallyReserved.newBuilder()
                    .setOrderId(command.getOrderId())
                    .setReservationExpiration(command.getReservationExpiration())
                    .addAllSeat(command.getSeatList());
            return result.build();
        }

        private static OrderReservationCompleted buildOrderReservationCompleted(MarkSeatsAsReserved command) {
            final OrderReservationCompleted.Builder result = OrderReservationCompleted.newBuilder()
                    .setOrderId(command.getOrderId())
                    .setReservationExpiration(command.getReservationExpiration())
                    .addAllSeat(command.getSeatList());
            return result.build();
        }
    }

    private static class Validator {

        private static void checkNotConfirmed(Order order, Message cmd) {
            final String message = format("Cannot modify a confirmed order with ID: %s; command: %s.",
                    order.getId().getUuid(), cmd.getClass().getName()
            );
            checkState(!order.getIsConfirmed(), message);
        }

        private static void checkNewState(Order order) {
            checkState(order.hasId(), "No order ID in a new order state.");
            final String orderId = order.getId().getUuid();
            checkState(order.hasConferenceId(), "No conference ID in a new order state, ID: " + orderId);
            if (order.getSeatList().isEmpty()) {
                throw new IllegalArgumentException("No seats in a new order state, ID: " + orderId);
            }
        }

        private static void validateCommand(RegisterToConference cmd) {
            checkOrderId(cmd.hasOrderId(), cmd);
            checkField(cmd.hasConferenceId(), "conference ID", cmd);
            checkSeats(cmd.getSeatList(), cmd);
        }

        private static void validateCommand(MarkSeatsAsReserved cmd) {
            checkOrderId(cmd.hasOrderId(), cmd);
            checkField(cmd.hasReservationExpiration(), "reservation expiration", cmd);
            checkSeats(cmd.getSeatList(), cmd);
        }

        private static void validateCommand(RejectOrder cmd) {
            checkOrderId(cmd.hasOrderId(), cmd);
        }

        private static void validateCommand(ConfirmOrder cmd) {
            checkOrderId(cmd.hasOrderId(), cmd);
        }

        private static void validateCommand(AssignRegistrantDetails cmd) {
            checkOrderId(cmd.hasOrderId(), cmd);
            checkField(cmd.hasRegistrant(), "registrant", cmd);
        }

        private static void checkOrderId(boolean hasId, Message cmd) {
            checkField(hasId, "order ID", cmd);
        }

        private static void checkSeats(List<SeatQuantity> seats, Message cmd) {
            checkField(!seats.isEmpty(), "seats", cmd);
        }

        private static void checkField(boolean hasField, String fieldName, Message cmd) {
            if (!hasField) {
                throw new IllegalArgumentException("No " + fieldName + " in the command: " + cmd.getClass().getName());
            }
        }
    }
}
