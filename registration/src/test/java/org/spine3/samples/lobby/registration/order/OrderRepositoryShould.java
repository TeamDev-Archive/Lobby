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

package org.spine3.samples.lobby.registration.order;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.Event;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.server.BoundedContext;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newBoundedContext;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newEvent;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "ReturnOfCollectionOrArrayField"})
public class OrderRepositoryShould {

    private OrderRepository repository;

    @Before
    public void setUpTest() {
        final BoundedContext boundedContext = newBoundedContext();
        repository = new OrderRepository(boundedContext, new PricingServiceMock());
        repository.initStorage(InMemoryStorageFactory.getInstance());
        boundedContext.register(repository);
    }

    @Test
    public void store_and_load_aggregate() {
        final TestOrderAggregate expected = new TestOrderAggregate();

        repository.store(expected);

        final OrderAggregate actual = repository.load(expected.getId());
        final Order actualState = actual.getState();
        assertEquals(expected.getSeats(), actualState.getSeatList());
    }

    private static class TestOrderAggregate extends OrderAggregate {

        private final OrderPlaced event = Given.Event.orderPlaced();

        private final ImmutableList<Event> uncommittedEvents = ImmutableList.of(newEvent(event));

        private TestOrderAggregate() {
            super(Given.ORDER_ID);
        }

        @Override
        @SuppressWarnings("RefusedBequest")
        public List<Event> getStateChangingUncommittedEvents() {
            return uncommittedEvents;
        }

        public List<SeatQuantity> getSeats() {
            return event.getSeatList();
        }
    }

    private static class PricingServiceMock implements OrderPricingService {

        @Override
        public OrderTotal calculateTotalOrderPrice(ConferenceId conferenceId, Iterable<SeatQuantity> seats) {
            return OrderTotal.getDefaultInstance();
        }
    }
}
