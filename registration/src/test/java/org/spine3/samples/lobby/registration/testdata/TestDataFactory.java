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

package org.spine3.samples.lobby.registration.testdata;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.base.CommandContext;
import org.spine3.base.CommandId;
import org.spine3.base.Commands;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.base.EventId;
import org.spine3.base.Events;
import org.spine3.net.EmailAddress;
import org.spine3.people.PersonName;
import org.spine3.samples.lobby.common.PersonalInfo;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;
import org.spine3.server.command.CommandStore;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.EventStore;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import static com.google.protobuf.util.TimeUtil.getCurrentTime;

/**
 * The utility class which is used for creating objects needed in tests.
 *
 * @author Alexander Litus
 */
@SuppressWarnings("UtilityClass")
public class TestDataFactory {

    private TestDataFactory() {
    }

    /**
     * Creates a new {@link BoundedContext} instance with {@link InMemoryStorageFactory},
     * {@link CommandBus} and {@link EventBus}.
     */
    public static BoundedContext newBoundedContext() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        final EventStore eventStore = EventStore.newBuilder()
                                                .setStreamExecutor(MoreExecutors.directExecutor())
                                                .setStorage(storageFactory.createEventStorage())
                                                .build();
        final CommandBus commandBus = newCommandBus(storageFactory);
        final BoundedContext.Builder result = BoundedContext.newBuilder()
                                                            .setName("Orders & Registrations tests")
                                                            .setStorageFactory(storageFactory)
                                                            .setCommandBus(commandBus)
                                                            .setEventBus(EventBus.newBuilder()
                                                                                 .setEventStore(eventStore)
                                                                                 .build());
        return result.build();
    }

    /**
     * Creates a new command bus with {@link InMemoryStorageFactory}.
     */
    public static CommandBus newCommandBus() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        return newCommandBus(storageFactory);
    }

    private static CommandBus newCommandBus(StorageFactory storageFactory) {
        final CommandStore store = new CommandStore(storageFactory.createCommandStorage());
        return CommandBus.newInstance(store);
    }

    /**
     * Creates a new {@link PersonalInfo} instance with the given {@code givenName}, {@code familyName} and {@code email}.
     */
    public static PersonalInfo newPersonalInfo(String givenName, String familyName, String email) {
        final PersonName.Builder name = PersonName.newBuilder()
                                                  .setGivenName(givenName)
                                                  .setFamilyName(familyName);
        final EmailAddress.Builder emailAddress = EmailAddress.newBuilder()
                                                              .setValue(email);
        final PersonalInfo.Builder result = PersonalInfo.newBuilder()
                                                        .setName(name)
                                                        .setEmail(emailAddress);
        return result.build();
    }

    /**
     * Creates a new {@link Event} instance with the given {@code event}, and {@code aggregateId}.
     */
    public static Event newEvent(Message event) {
        final CommandId commandId = Commands.generateId();
        final EventId eventId = Events.generateId();
        final CommandContext commandContext = CommandContext.newBuilder()
                                                            .setCommandId(commandId)
                                                            .build();
        final Timestamp currentTime = getCurrentTime();
        final EventContext eventContext = EventContext.newBuilder()
                                                      .setCommandContext(commandContext)
                                                      .setEventId(eventId)
                                                      .setTimestamp(currentTime)
                                                      .build();
        final Event.Builder result = Event.newBuilder()
                                          .setContext(eventContext)
                                          .setMessage(Any.pack(event));
        return result.build();
    }
}
