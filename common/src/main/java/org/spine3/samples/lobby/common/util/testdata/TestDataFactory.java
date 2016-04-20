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

package org.spine3.samples.lobby.common.util.testdata;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.base.CommandContext;
import org.spine3.base.CommandId;
import org.spine3.base.Commands;
import org.spine3.base.EmailAddress;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.base.EventId;
import org.spine3.base.Events;
import org.spine3.base.PersonName;
import org.spine3.samples.lobby.common.PersonalInfo;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;
import org.spine3.server.command.CommandStore;
import org.spine3.server.command.ExecutorCommandScheduler;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.EventStore;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import static com.google.protobuf.util.TimeUtil.getCurrentTime;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.protobuf.Messages.toAny;

/**
 * The utility class which is used for creating objects needed in tests.
 *
 * @author Alexander Litus
 */
@SuppressWarnings("UtilityClass")
@VisibleForTesting
public class TestDataFactory {

    private TestDataFactory() {}

    /**
     * Creates a new {@link BoundedContext} instance with {@link InMemoryStorageFactory},
     * {@link CommandBus} and {@link EventBus}.
     */
    @VisibleForTesting
    public static BoundedContext newBoundedContext() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        final EventStore eventStore = EventStore.newBuilder()
                .setStreamExecutor(MoreExecutors.directExecutor())
                .setStorage(storageFactory.createEventStorage())
                .build();
        final CommandBus commandBus = newCommandBus(storageFactory);
        final String name = "BC-" + newUuid();
        final BoundedContext.Builder result = BoundedContext.newBuilder()
                .setName(name)
                .setStorageFactory(storageFactory)
                .setCommandBus(commandBus)
                .setEventBus(EventBus.newInstance(eventStore));
        return result.build();
    }

    /**
     * Creates a new command bus with {@link InMemoryStorageFactory}.
     */
    @VisibleForTesting
    public static CommandBus newCommandBus() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        return newCommandBus(storageFactory);
    }

    private static CommandBus newCommandBus(StorageFactory storageFactory) {
        final CommandStore store = new CommandStore(storageFactory.createCommandStorage());
        final CommandBus.Builder builder = CommandBus.newBuilder();
        builder.setCommandStore(store);
        // TODO:2016-04-08:alexander.litus: change to App Engine-compatible scheduler
        builder.setScheduler(new ExecutorCommandScheduler());
        return builder.build();
    }

    /**
     * Creates a new {@link PersonalInfo} instance with the given {@code givenName}, {@code familyName} and {@code email}.
     */
    @VisibleForTesting
    public static PersonalInfo newPersonalInfo(String givenName, String familyName, String email) {
        final PersonName.Builder name = PersonName.newBuilder().setGivenName(givenName).setFamilyName(familyName);
        final EmailAddress.Builder emailAddress = EmailAddress.newBuilder().setValue(email);
        final PersonalInfo.Builder result = PersonalInfo.newBuilder().setName(name).setEmail(emailAddress);
        return result.build();
    }

    /**
     * Creates a new {@link Event} instance with the given {@code event}, and {@code aggregateId}.
     */
    @VisibleForTesting
    public static Event newEvent(Message event) {
        final CommandId commandId = Commands.generateId();
        final EventId eventId = Events.generateId();
        final CommandContext commandContext = CommandContext.newBuilder().setCommandId(commandId).build();
        final Timestamp currentTime = getCurrentTime();
        final EventContext eventContext = EventContext.newBuilder()
                .setCommandContext(commandContext)
                .setEventId(eventId)
                .setTimestamp(currentTime)
                .build();
        final Event.Builder result = Event.newBuilder()
                .setContext(eventContext)
                .setMessage(toAny(event));
        return result.build();
    }
}
