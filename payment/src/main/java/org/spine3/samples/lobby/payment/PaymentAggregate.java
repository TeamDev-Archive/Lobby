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
import org.spine3.base.FailureThrowable;
import org.spine3.money.Money;
import org.spine3.samples.lobby.common.util.aggregate.AbstractLobbyAggregate;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.command.Assign;
import org.spine3.server.entity.Entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.spine3.samples.lobby.payment.ThirdPartyProcessorPayment.PaymentStatus;
import static org.spine3.samples.lobby.payment.ThirdPartyProcessorPayment.PaymentStatus.*;

/**
 * The payment aggregate that manages connection between the app and third-party payment processors.
 *
 * @author Dmytro Dashenkov
 * @see org.spine3.samples.lobby.payment.procman.PaymentProcessManager
 */
// TODO:04-11-16:dmytro.dashenkov: Handle wrong addressd case (i.e. ID in command/event does not match aggregate ID).
public class PaymentAggregate
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
    public PaymentAggregate(PaymentId id) {
        super(id);
    }

    @Assign
    public PaymentInstantiated handle(InitializeThirdPartyProcessorPayment command, CommandContext context)
            throws SecondInitializationAttempt {
        if (getVersion() > 0) {
            throw new SecondInitializationAttempt(getId());
        }

        // Update aggregate state
        final OrderTotal total = command.getTotal();
        final Money orderCost = total.getTotalPrice();
        final ThirdPartyProcessorPayment.Builder newState = getState().toBuilder()
                                                                      .setPrice(orderCost);
        markInitialized(newState);

        // Construct and return events
        final PaymentInstantiated resultEvent = PaymentInstantiated.newBuilder()
                                                                   .setId(getId())
                                                                   .build();
        return resultEvent;
    }

    @Assign
    public Message handle(CompleteThirdPartyProcessorPayment command, CommandContext context) throws FailureThrowable {
        final boolean successful = command.getSuccessful();
        final PaymentStatus status = successful ? SUCCEED : FAILED;
        checkResultStatus(status);

        final ThirdPartyProcessorPayment payment = getState().toBuilder()
                                                             .setStatus(status)
                                                             .build();
        incrementState(payment);
        final PaymentId id = payment.getId();
        final Message resultEvent = successful
                                    ? PaymentCompleted.newBuilder()
                                                      .setId(id)
                                                      .build()
                                    : PaymentRejected.newBuilder()
                                                     .setId(id)
                                                     .build();
        return resultEvent;
    }

    @Assign
    public PaymentCanceled handle(CancelThirdPartyProcessorPayment command, CommandContext context) throws FailureThrowable {
        final PaymentStatus status = CANCELED;
        checkResultStatus(status);

        final ThirdPartyProcessorPayment payment = getState().toBuilder()
                                                             .setStatus(status)
                                                             .build();
        incrementState(payment);
        final PaymentId id = getId();
        final PaymentCanceled resultEvent = PaymentCanceled.newBuilder()
                                                           .setId(id)
                                                           .build();
        return resultEvent;
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

    private void checkResultStatus(PaymentStatus status) throws FailureThrowable {
        checkArgument(status.getNumber() > PaymentStatus.INITIALIZED_VALUE,
                "checkResultStatus(paymentStatus) checks only result statuses.");
        final ThirdPartyProcessorPayment state = getState();
        final PaymentStatus currentStatus = state.getStatus();

        if (currentStatus.getNumber() < INITIALIZED_VALUE) {
            throw new NotInitialized(state.getId());
        }

        if (currentStatus.getNumber() > INITIALIZED_VALUE) {
            throw new AmbiguousPaymentResult(state.getId(), state.getStatus(), status);
        }
    }
}
