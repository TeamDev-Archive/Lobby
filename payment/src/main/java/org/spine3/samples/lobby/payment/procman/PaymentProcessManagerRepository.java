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

import com.google.protobuf.Message;
import org.spine3.base.EventContext;
import org.spine3.server.BoundedContext;
import org.spine3.server.entity.IdFunction;
import org.spine3.server.procman.ProcessManagerRepository;
import org.spine3.server.type.EventClass;

import javax.annotation.Nonnull;

/**
 * @author Dmytro Dashenkov
 */
public class PaymentProcessManagerRepository extends ProcessManagerRepository<
        PaymentProcessManagerId,
        PaymentProcessManager,
        PaymentProcess> {


    protected PaymentProcessManagerRepository(BoundedContext boundedContext) {
        super(boundedContext);
    }

    @Override
    public IdFunction<PaymentProcessManagerId, ? extends Message, EventContext> getIdFunction(@Nonnull EventClass eventClass) {
        return new IdFunction<PaymentProcessManagerId, Message, EventContext>() {
            @SuppressWarnings("OverlyStrongTypeCast")
            @Override
            public PaymentProcessManagerId getId(@Nonnull Message message, @Nonnull EventContext context) {
                //// TODO:12-11-16:dmytro.dashenkov: Report hardly understandable behavior.
                return ((PaymentProcess) message).getId();
            }
        };
    }
}
