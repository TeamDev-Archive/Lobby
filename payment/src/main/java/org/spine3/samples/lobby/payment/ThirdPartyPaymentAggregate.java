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

package org.spine3.samples.lobby.payment;

import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.base.Identifiers;
import org.spine3.money.Money;
import org.spine3.samples.lobby.common.util.aggregate.AbstractLobbyAggregate;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.command.Assign;
import org.spine3.server.entity.Entity;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmytro Dashenkov
 */
public class ThirdPartyPaymentAggregate
        extends AbstractLobbyAggregate<PaymentId, ThirdPartyProcessorPayment, ThirdPartyProcessorPayment.Builder> {

    /**
     * Creates a new aggregate instance.
     *
     * @param id the ID for the new aggregate
     *
     * @throws IllegalArgumentException if the ID is not of one of the supported types.
     *                                  Supported types are: {@code String}, {@code Long}, {@code Integer} and {@link Message}
     * @see Aggregate
     * @see Entity
     */
    public ThirdPartyPaymentAggregate(PaymentId id) {
        super(id);
    }

    @Assign
    public List<Message> handle(InstantiateThirdPartyProcessorPayment command, CommandContext context) {
        final OrderTotal total = command.getTotal();
        final Money orderCost = total.getTotalPrice();

        // TODO:01-11-16:dmytro.dashenkov: Implement.

        final PaymentId id = PaymentId.newBuilder()
                                      .setValue(Identifiers.newUuid())
                                      .build();
        final Message resultEvent = PaymentInstantiated.newBuilder()
                                                     .setId(id)
                                                     .build();
        return Collections.singletonList(resultEvent);
    }
}
