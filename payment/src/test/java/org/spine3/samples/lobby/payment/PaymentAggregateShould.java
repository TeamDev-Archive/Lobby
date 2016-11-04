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
import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.base.FailureThrowable;
import org.spine3.base.Identifiers;
import org.spine3.samples.lobby.payment.ThirdPartyProcessorPayment.PaymentStatus;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class PaymentAggregateShould {

    //
    // Initialization
    // --------------

    @Test
    public void initialize_with_NOT_STARTED_status_value() {
        final PaymentAggregate aggregate = Given.aggregate("some-val");
        final ThirdPartyProcessorPayment aggregateState = aggregate.getState();
        assertEquals(aggregateState.getStatus(), PaymentStatus.NOT_STARTED);
        assertFalse(aggregate.isInitialized());
    }

    @Test
    public void handle_initialization_commands() throws SecondInitializationAttempt {
        final PaymentAggregate aggregate = Given.aggregate(Identifiers.newUuid());
        final InitializeThirdPartyProcessorPayment command = Given.initCommand(aggregate);
        final PaymentInstantiated event = aggregate.handle(command, CommandContext.getDefaultInstance());
        assertEquals(command.getId(), event.getId());
        assertTrue(aggregate.isInitialized());
    }

    @Test(expected = SecondInitializationAttempt.class)
    public void fail_to_initialize_twice() throws SecondInitializationAttempt {
        final PaymentAggregate aggregate = Given.aggregate(Identifiers.newUuid());
        final InitializeThirdPartyProcessorPayment firstCommand = Given.initCommand(aggregate);
        final InitializeThirdPartyProcessorPayment secondCommand = Given.initCommand(aggregate);
        aggregate.handle(firstCommand, CommandContext.getDefaultInstance());
        aggregate.handle(secondCommand, CommandContext.getDefaultInstance());
    }

    //
    // Payment process result handling
    // -------------------------------

    @Test
    public void handle_successful_completion_command() throws FailureThrowable {
        final PaymentAggregate aggregate = Given.initializedAggregate();
        final CompleteThirdPartyProcessorPayment completeCommand = Given.completeCommand(aggregate, true);
        final List<Message> events = aggregate.handle(completeCommand, CommandContext.getDefaultInstance());
        assertEquals(events.size(), 1);
        final Message actualEvent = events.get(0);
        assertNotNull(actualEvent);
        assertTrue("Wrong result event type: " + actualEvent.getClass()
                                                            .toString(), actualEvent instanceof PaymentCompleted);
        final PaymentCompleted event = (PaymentCompleted) actualEvent;
        assertEquals(aggregate.getId(), event.getId());
    }

    private static class Given {
        private static PaymentAggregate aggregate(String stringId) {
            final PaymentId id = PaymentId.newBuilder()
                                          .setValue(stringId)
                                          .build();
            final PaymentAggregate aggregate = new PaymentAggregate(id);
            return aggregate;
        }

        private static PaymentAggregate initializedAggregate() throws SecondInitializationAttempt {
            final PaymentAggregate aggregate = aggregate(Identifiers.newUuid());
            final InitializeThirdPartyProcessorPayment command = initCommand(aggregate);
            aggregate.handle(command, CommandContext.getDefaultInstance());
            return aggregate;
        }

        private static InitializeThirdPartyProcessorPayment initCommand(PaymentAggregate aggregate) {
            final InitializeThirdPartyProcessorPayment command
                    = InitializeThirdPartyProcessorPayment.newBuilder()
                                                          .setId(aggregate.getId())
                                                          .build();
            return command;
        }

        private static CompleteThirdPartyProcessorPayment completeCommand(PaymentAggregate aggregate, boolean success) {
            final CompleteThirdPartyProcessorPayment command
                    = CompleteThirdPartyProcessorPayment.newBuilder()
                                                        .setId(aggregate.getId())
                                                        .setSuccessful(success)
                                                        .build();
            return command;
        }
    }
}
