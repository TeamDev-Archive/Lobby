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

package org.spine3.samples.lobby.registration.seat.availability;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spine3.base.EventRecord;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.common.SeatTypeId;
import org.spine3.samples.lobby.registration.contracts.SeatQuantity;
import org.spine3.samples.lobby.registration.testdata.TestDataFactory;
import org.spine3.server.storage.AggregateStorage;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.spine3.samples.lobby.common.util.IdFactory.*;
import static org.spine3.samples.lobby.registration.util.MessageFactory.newSeatQuantity;
import static org.spine3.samples.lobby.registration.util.MessageFactory.newSeatsAvailabilityId;

/**
 * @author Alexander Litus
 */
@SuppressWarnings({"MagicNumber", "UtilityClass", "InstanceMethodNamingConvention", "RefusedBequest",
        "ReturnOfCollectionOrArrayField", "TypeMayBeWeakened"})
public class SeatsAvailabilityRepositoryShould {

    private final SeatsAvailabilityRepository repository = new SeatsAvailabilityRepository();

    @Before
    public void setUpTest() {
        final AggregateStorage<SeatsAvailabilityId> storage =
                InMemoryStorageFactory.getInstance().createAggregateStorage(null);
        repository.assignStorage(storage);
    }

    @After
    public void tearDownTest() {
        repository.assignStorage(null);
    }

    @Test
    public void store_and_load_aggregate() throws InvocationTargetException {
        final TestSeatsAvailabilityAggregate expected = new TestSeatsAvailabilityAggregate();

        repository.store(expected);

        final SeatsAvailabilityAggregate actual = repository.load(expected.getId());
        final SeatsAvailability actualState = actual.getState();
        assertEquals(expected.getAvailableSeats(), actualState.getAvailableSeatList());

        final Map<String, SeatQuantities> pendingReservations = actualState.getPendingReservations();
        final List<SeatQuantity> reservedSeats = pendingReservations.get(expected.getReservationId()).getItemList();
        assertEquals(expected.getReservedSeats(), reservedSeats);
    }

    public static class TestSeatsAvailabilityAggregate extends SeatsAvailabilityAggregate {

        private static final SeatsAvailabilityId ID = newSeatsAvailabilityId();

        private final SeatTypeId seatTypeId = newSeatTypeId();

        private final List<SeatQuantity> availableSeats = ImmutableList.of(newSeatQuantity(seatTypeId, 256));

        private final List<SeatQuantity> reservedSeats = ImmutableList.of(newSeatQuantity(seatTypeId, 128));

        private final ReservationId reservationId = newReservationId();

        private final EventRecord seatsReservedEvent = newEventRecord(SeatsReserved.newBuilder()
                .setConferenceId(newConferenceId())
                .setReservationId(reservationId)
                .addAllReservedSeatUpdated(reservedSeats)
                .addAllAvailableSeatUpdated(availableSeats).build());

        private final ImmutableList<EventRecord> uncommittedEvents = ImmutableList.of(seatsReservedEvent);

        public TestSeatsAvailabilityAggregate() {
            super(ID);
        }

        @Override
        public List<EventRecord> getStateChangingUncommittedEvents() {
            return uncommittedEvents;
        }

        private static EventRecord newEventRecord(Message event) {
            return TestDataFactory.newEventRecord(event, ID);
        }

        public List<SeatQuantity> getAvailableSeats() {
            return availableSeats;
        }

        public List<SeatQuantity> getReservedSeats() {
            return reservedSeats;
        }

        public String getReservationId() {
            return reservationId.getUuid();
        }
    }
}
