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

package org.spine3.samples.lobby.payment.procman;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.base.CommandId;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.base.EventId;
import org.spine3.base.Events;
import org.spine3.base.Identifiers;
import org.spine3.protobuf.AnyPacker;
import org.spine3.protobuf.Timestamps;
import org.spine3.samples.lobby.payment.InitializeThirdPartyProcessorPayment;
import org.spine3.samples.lobby.payment.InstantiateThirdPartyProcessorPayment;
import org.spine3.samples.lobby.payment.PaymentCompleted;
import org.spine3.samples.lobby.payment.PaymentId;
import org.spine3.samples.lobby.payment.ThirdPartyPaymentAggregate;
import org.spine3.samples.lobby.payment.procman.PaymentProcess.PaymentState;
import org.spine3.samples.lobby.payment.repository.PaymentRepository;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.Assign;
import org.spine3.server.event.Subscribe;
import org.spine3.server.procman.CommandRouted;
import org.spine3.server.procman.ProcessManager;

import static org.spine3.samples.lobby.payment.procman.PaymentProcess.PaymentState.NOT_STARTED;

/**
 * @author Dmytro Dashenkov
 */
public class PaymentProcessManager extends ProcessManager<PaymentProcessManagerId, PaymentProcess> {

    private final BoundedContext boundedContext;

    /**
     * Creates a new instance.
     *
     * @param id an ID for the new instance
     *
     * @throws IllegalArgumentException if the ID type is unsupported
     */
    public PaymentProcessManager(PaymentProcessManagerId id, BoundedContext boundedContext) {
        super(id);
        this.boundedContext = boundedContext;
    }

    @Assign
    public CommandRouted handle(InstantiateThirdPartyProcessorPayment command, CommandContext context)
            throws SecondInstantiationAttempt {
        checkNotStarted();
        // Create new Aggregate Repository
        final PaymentRepository repo = PaymentRepository.getInstance(boundedContext);
        final ThirdPartyPaymentAggregate aggregate = repo.createNewAggregate();
        // Create InitializeThirdPartyProcessorPayment command from InstantiateThirdPartyProcessorPayment
        final PaymentId id = aggregate.getId();
        final OrderTotal total = command.getTotal();
        final InitializeThirdPartyProcessorPayment initCommand = InitializeThirdPartyProcessorPayment.newBuilder()
                                                                                                     .setId(id)
                                                                                                     .setTotal(total)
                                                                                                     .build();
        final CommandId commandId = CommandId.newBuilder()
                                             .setUuid(Identifiers.newUuid())
                                             .build();
        final CommandContext commandContext = CommandContext.newBuilder(context)
                                                            .setCommandId(commandId)
                                                            .setTimestamp(Timestamps.getCurrentTime())
                                                            .build();
        // Route the init command to further handlers (e.g. Aggregate)
        final CommandRouted routed = newRouter().of(initCommand, commandContext)
                                                .route();
        return routed;
    }

    @Subscribe
    public void on(PaymentCompleted event, EventContext context) {
        final PaymentProcess state = getState();

        // Check whether the event is addressed to current instance of the procman
        final Message aggregateId = AnyPacker.unpack(context.getProducerId());
        final boolean belongsToThisInstance = aggregateId.equals(state.getAggregateId());
        if (!belongsToThisInstance) {
            return;
        }

        final org.spine3.samples.lobby.payment.contracts.PaymentCompleted externalEventMessage
                = org.spine3.samples.lobby.payment.contracts.PaymentCompleted.newBuilder()
                                                                             .setOrderId(state.getOrderId())
                                                                             .build();
        final Any wrappedEventMessage = AnyPacker.pack(externalEventMessage);
        final EventId externalEventId = Events.generateId();
        final EventContext externalEventContext = context.toBuilder()
                                                         .setEventId(externalEventId)
                                                         .setTimestamp(Timestamps.getCurrentTime())
                                                         .build();
        final Event externalEvent = Event.newBuilder()
                                         .setMessage(wrappedEventMessage)
                                         .setContext(externalEventContext)
                                         .build();
        boundedContext.getEventBus().post(externalEvent);
    }

    private void checkNotStarted() throws SecondInstantiationAttempt {
        final PaymentProcess process = getState();
        final PaymentState state = process.getState();

        if (state != NOT_STARTED) {
            throw new SecondInstantiationAttempt(process.getId());
        }
    }
}
