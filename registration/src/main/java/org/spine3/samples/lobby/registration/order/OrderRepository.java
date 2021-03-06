/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import org.spine3.samples.lobby.common.OrderId;
import org.spine3.server.BoundedContext;
import org.spine3.server.aggregate.AggregateRepository;

import javax.annotation.Nonnull;

/**
 * The repository for order aggregates.
 *
 * @author Alexander Litus
 * @see OrderAggregate
 */
public class OrderRepository extends AggregateRepository<OrderId, OrderAggregate> {

    private final OrderPricingService orderPricingService;

    /**
     * Creates a new repository instance.
     *
     * @param boundedContext      the bounded context to which this repository belongs
     * @param orderPricingService the pricing service to inject to order aggregates
     */
    public OrderRepository(BoundedContext boundedContext, OrderPricingService orderPricingService) {
        super(boundedContext);
        this.orderPricingService = orderPricingService;
    }

    /**
     * Loads an aggregate by an ID and injects required services to it.
     *
     * @param id id of the aggregate to load
     * @return the loaded object
     * @throws IllegalStateException if the repository wasn't configured prior to calling this method
     */
    @Nonnull
    @Override
    public OrderAggregate load(OrderId id) throws IllegalStateException {
        final OrderAggregate order = super.load(id);
        order.setOrderPricingService(orderPricingService);
        return order;
    }
}
