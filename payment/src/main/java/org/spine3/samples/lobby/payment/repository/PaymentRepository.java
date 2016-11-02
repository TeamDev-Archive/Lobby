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

package org.spine3.samples.lobby.payment.repository;

import org.spine3.base.Identifiers;
import org.spine3.samples.lobby.payment.PaymentId;
import org.spine3.samples.lobby.payment.ThirdPartyPaymentAggregate;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.AggregateRepository;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

/**
 * @author Dmytro Dashenkov
 */
public class PaymentRepository extends AggregateRepository<PaymentId, ThirdPartyPaymentAggregate> {

    @SuppressWarnings("StaticNonFinalField") // Singleton
    private static PaymentRepository defaultInstance = null;

    @SuppressWarnings("WeakerAccess") // May be used in a Controller component later
    public static synchronized PaymentRepository getInstance(BoundedContext bc, StorageFactory factory) {
        if (defaultInstance == null) {
            defaultInstance = new PaymentRepository(bc);
            defaultInstance.initStorage(factory);
        }
        return defaultInstance;
    }

    public static synchronized PaymentRepository getInstance(BoundedContext bc) {
        return getInstance(bc, InMemoryStorageFactory.getInstance());
    }

    /**
     * Creates a new repository instance.
     *
     * @param boundedContext the bounded context to which this repository belongs
     */
    private PaymentRepository(BoundedContext boundedContext) {
        super(boundedContext);
    }

    /**
     * Creates new instance of {@link ThirdPartyPaymentAggregate} and stores it in the storage.
     *
     * @return new instance of the aggregate.
     */
    public ThirdPartyPaymentAggregate createNewAggregate() {
        final PaymentId paymentId = PaymentId.newBuilder()
                                             .setValue(Identifiers.newUuid())
                                             .build();
        final ThirdPartyPaymentAggregate aggregate = new ThirdPartyPaymentAggregate(paymentId);
        store(aggregate);
        return aggregate;
    }
}
