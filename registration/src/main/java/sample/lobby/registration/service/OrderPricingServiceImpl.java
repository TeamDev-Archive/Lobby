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

package sample.lobby.registration.service;

import org.spine3.money.Money;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.SeatType;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.samples.lobby.registration.contracts.SeatOrderLine;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import sample.lobby.registration.projection.ConferenceProjection;
import sample.lobby.registration.projection.ConferenceProjectionRepository;

import java.util.List;

/**
 * The implementation of the service which calculates prices of order seats.
 *
 * @author Alexander Litus
 */
public class OrderPricingServiceImpl implements OrderPricingService {

    private final ConferenceProjectionRepository conferenceRepository;

    public OrderPricingServiceImpl(ConferenceProjectionRepository conferenceRepository) {
        this.conferenceRepository = conferenceRepository;
    }

    @Override
    public OrderTotal calculateTotalOrderPrice(ConferenceId conferenceId, Iterable<SeatQuantity> seats) {
        final OrderTotal.Builder result = OrderTotal.newBuilder();
        final Money.Builder totalPrice = Money.newBuilder();
        final List<SeatType> seatTypes = getSeatTypes(conferenceId);

        for (SeatQuantity seat : seats) {
            final SeatOrderLine line = buildOrderLine(seat, seatTypes);
            checkOrderLine(line, conferenceId, seat);
            result.addOrderLine(line);
            if (line.hasLineTotal()) {
                final Money lineTotal = line.getLineTotal();
                totalPrice.setAmount(totalPrice.getAmount() + lineTotal.getAmount());
                totalPrice.setCurrency(lineTotal.getCurrency());
            }
        }
        result.setTotalPrice(totalPrice);
        return result.build();
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static void checkOrderLine(SeatOrderLine orderLine, ConferenceId conferenceId, SeatQuantity seat) {
        if (orderLine.equals(SeatOrderLine.getDefaultInstance())) {
            throw new IllegalArgumentException("Unknown seat with type ID: " + seat.getSeatTypeId().getUuid() +
                    " for conference with ID: " + conferenceId.getUuid());
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static SeatOrderLine buildOrderLine(SeatQuantity seat, Iterable<SeatType> seatTypes) {
        for (SeatType seatType : seatTypes) {
            if (seat.getSeatTypeId().equals(seatType.getId())) {
                final Money unitPrice = seatType.getPrice();
                final int quantity = seat.getQuantity();
                final long totalAmount = unitPrice.getAmount() * quantity;
                final Money totalPrice = Money.newBuilder().setAmount(totalAmount).build();
                final SeatOrderLine orderLine = SeatOrderLine.newBuilder()
                        .setQuantity(quantity)
                        .setSeatTypeId(seat.getSeatTypeId())
                        .setUnitPrice(unitPrice)
                        .setLineTotal(totalPrice)
                        .build();
                return orderLine;
            }
        }
        return SeatOrderLine.getDefaultInstance();
    }

    private List<SeatType> getSeatTypes(ConferenceId conferenceId) {
        final ConferenceProjection conference = conferenceRepository.load(conferenceId);
        final List<SeatType> seatTypes = conference.getState().getSeatTypeList();
        return seatTypes;
    }
}
