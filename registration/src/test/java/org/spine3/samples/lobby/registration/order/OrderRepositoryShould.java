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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.EventRecord;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.server.storage.AggregateStorage;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.*;
import static org.spine3.samples.lobby.registration.testdata.TestDataFactory.newEventRecord;
import static org.spine3.samples.lobby.registration.util.MessageFactory.newSeatQuantity;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"InstanceMethodNamingConvention", "ReturnOfCollectionOrArrayField", "MagicNumber"})
public class OrderRepositoryShould {

    private final OrderRepository repository = new OrderRepository(new PricingServiceMock());

    @Before
    public void setUpTest() {
        final AggregateStorage<OrderId> storage = InMemoryStorageFactory.getInstance().createAggregateStorage(null);
        repository.assignStorage(storage);
    }

    @After
    public void tearDownTest() {
        repository.assignStorage(null);
    }

    @Test
    public void store_and_load_aggregate() throws InvocationTargetException {
        final TestOrderAggregate expected = new TestOrderAggregate();

        repository.store(expected);

        final OrderAggregate actual = repository.load(expected.getId());
        final Order actualState = actual.getState();
        assertEquals(expected.getSeats(), actualState.getSeatList());
    }

    public static class TestOrderAggregate extends OrderAggregate {

        private static final OrderId ID = newOrderId();

        private final SeatTypeId seatTypeId = newSeatTypeId();

        private final List<SeatQuantity> seats = ImmutableList.of(
                newSeatQuantity(seatTypeId, 128), newSeatQuantity(seatTypeId, 256));

        private final EventRecord orderPlaced = newEventRecord(OrderPlaced.newBuilder()
                .setOrderId(ID)
                .setConferenceId(newConferenceId())
                .addAllSeat(seats)
                .build(), ID);

        private final ImmutableList<EventRecord> uncommittedEvents = ImmutableList.of(orderPlaced);

        public TestOrderAggregate() {
            super(ID);
        }

        @Override
        @SuppressWarnings("RefusedBequest")
        public List<EventRecord> getUncommittedEvents() {
            return uncommittedEvents;
        }

        public List<SeatQuantity> getSeats() {
            return seats;
        }
    }

    private static class PricingServiceMock implements OrderPricingService {

        @Override
        public OrderTotal calculateTotalOrderPrice(ConferenceId conferenceId, Iterable<SeatQuantity> seats) {
            return OrderTotal.getDefaultInstance();
        }
    }
}
