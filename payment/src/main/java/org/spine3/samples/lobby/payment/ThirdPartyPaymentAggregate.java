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
import org.spine3.money.Money;
import org.spine3.samples.lobby.common.util.aggregate.AbstractLobbyAggregate;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.command.Assign;
import org.spine3.server.entity.Entity;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.spine3.samples.lobby.payment.ThirdPartyProcessorPayment.PaymentStatus;
import static org.spine3.samples.lobby.payment.ThirdPartyProcessorPayment.PaymentStatus.INITIALIZED;
import static org.spine3.samples.lobby.payment.ThirdPartyProcessorPayment.PaymentStatus.INITIALIZED_VALUE;

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
    public List<Message> handle(InitializeThirdPartyProcessorPayment command, CommandContext context)
            throws SecondInitializationAttempt {
        if (getVersion() > 0) {
            throw new SecondInitializationAttempt(getId());
        }

        final OrderTotal total = command.getTotal();
        final Money orderCost = total.getTotalPrice();
        final ThirdPartyProcessorPayment.Builder newState = getState().toBuilder()
                                                                   .setPrice(orderCost);
        final Message resultEvent = PaymentInstantiated.newBuilder()
                                                       .setId(getId())
                                                       .build();
        markInitialized(newState);
        return Collections.singletonList(resultEvent);
    }

    /**
     * Is the aggregate initialized?
     *
     * @return {@code true} if {@link ThirdPartyProcessorPayment#status_ status} is greater or equal to
     * {@link PaymentStatus#INITIALIZED INITIALIZED}.
     */
    public boolean isInitialized() {
        final ThirdPartyProcessorPayment state = getState();
        return state.getStatus()
                    .getNumber() >= INITIALIZED_VALUE;
    }

    private void markInitialized(ThirdPartyProcessorPayment.Builder stateBuilder) {
        final ThirdPartyProcessorPayment state = getState();
        final PaymentStatus currentStatus = state.getStatus();
        checkState(
                currentStatus.getNumber() <= INITIALIZED_VALUE,
                "Can't return aggregate from \""
                        + currentStatus.name()
                        + "\" to \"INITIALIZED\".");
        stateBuilder.setStatus(INITIALIZED)
               .build();
        incrementState(stateBuilder.build());
    }
}
