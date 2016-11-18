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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class PaymentAggregateShould {

    private static final String WRONG_RESULT_EVENT_TYPE_ERR_TEMPLATE = "Wrong result event type: ";
    private static final String SECOND_RESOLUTION_HANDLED = "Should not handle second payment resolution";

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

    @Test
    public void have_state_with_sensible_id_after_init() throws SecondInitializationAttempt {
        final String stringId = Identifiers.newUuid();
        final PaymentAggregate aggregate = Given.aggregate(stringId);
        final InitializeThirdPartyProcessorPayment command = Given.initCommand(aggregate);
        aggregate.handle(command, CommandContext.getDefaultInstance());
        assertEquals(aggregate.getId(), aggregate.getState()
                                                 .getId());
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
        final PaymentAggregate aggregate = checkProcessorResponseCommand(PaymentCompleted.class, true);
        assertStatus(aggregate, PaymentStatus.SUCCEED);
    }


    @Test
    public void handle_reject_payment_command() throws FailureThrowable {
        final PaymentAggregate aggregate = checkProcessorResponseCommand(PaymentRejected.class, false);
        assertStatus(aggregate, PaymentStatus.FAILED);
    }

    @Test
    public void handle_cancel_payment_command() throws FailureThrowable {
        final PaymentAggregate aggregate = Given.initializedAggregate();
        final CancelThirdPartyProcessorPayment command = Given.cencelCommand(aggregate);
        final PaymentCanceled event = aggregate.handle(command, CommandContext.getDefaultInstance());
        assertEquals(aggregate.getId(), event.getId());
        final PaymentStatus status = aggregate.getState()
                                              .getStatus();
        assertEquals(PaymentStatus.CANCELED, status);
    }

    @Test
    public void fail_to_handle_any_command_after_payment_resolved() throws FailureThrowable {
        final PaymentAggregate aggregate = Given.initializedAggregate(); // Failure handled
        assertEquals(aggregate.getVersion(), 1);

        final CompleteThirdPartyProcessorPayment successCommand = Given.completeCommand(aggregate, true);
        final CompleteThirdPartyProcessorPayment rejectCommand = Given.completeCommand(aggregate, false);
        final CancelThirdPartyProcessorPayment cancelCommand = Given.cencelCommand(aggregate);

        aggregate.handle(successCommand, CommandContext.getDefaultInstance()); // Failure handled
        assertStatus(aggregate, PaymentStatus.SUCCEED);
        assertEquals(aggregate.getVersion(), 2);

        // Try to apply reject payment command
        try {
            aggregate.handle(rejectCommand, CommandContext.getDefaultInstance());
            fail(SECOND_RESOLUTION_HANDLED);
        } catch (AmbiguousPaymentResult ignored) {
        }
        // Try to apply successful payment command one more time
        try {
            aggregate.handle(successCommand, CommandContext.getDefaultInstance());
            fail(SECOND_RESOLUTION_HANDLED);
        } catch (AmbiguousPaymentResult ignored) {
        }
        // Try to apply cancel payment command
        try {
            aggregate.handle(cancelCommand, CommandContext.getDefaultInstance());
            fail(SECOND_RESOLUTION_HANDLED);
        } catch (AmbiguousPaymentResult ignored) {
        }

        // Same status and version an before the failed applications
        assertStatus(aggregate, PaymentStatus.SUCCEED);
        assertEquals(aggregate.getVersion(), 2);
    }

    private static <E extends Message> PaymentAggregate checkProcessorResponseCommand(Class<E> responceEventClass,
                                                                                      boolean wasSuccessful)
            throws FailureThrowable {
        final PaymentAggregate aggregate = Given.initializedAggregate();
        final CompleteThirdPartyProcessorPayment command = Given.completeCommand(aggregate, wasSuccessful);
        final Message event = aggregate.handle(command, CommandContext.getDefaultInstance());
        assertNotNull(event);
        assertTrue(
                WRONG_RESULT_EVENT_TYPE_ERR_TEMPLATE + event.getClass()
                                                            .toString(),
                responceEventClass.isInstance(event));
        @SuppressWarnings("unchecked") // Checked in previous line
        final E actualEvent = (E) event;
        try {
            final Method idGetter = responceEventClass.getDeclaredMethod("getId");
            assertEquals(aggregate.getId(), idGetter.invoke(actualEvent));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            fail(e.getMessage());
        }

        return aggregate;
    }

    private static void assertStatus(PaymentAggregate aggregate, PaymentStatus status) {
        final PaymentStatus actualStatus = aggregate.getState()
                                                    .getStatus();
        assertEquals(status, actualStatus);
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

        private static CancelThirdPartyProcessorPayment cencelCommand(PaymentAggregate aggregate) {
            final CancelThirdPartyProcessorPayment command = CancelThirdPartyProcessorPayment.newBuilder()
                                                                                             .setId(aggregate.getId())
                                                                                             .build();
            return command;
        }
    }
}
