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

package org.spine.samples.lobby.conference;

import com.google.common.util.concurrent.MoreExecutors;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;
import org.spine3.server.command.CommandStore;
import org.spine3.server.command.ExecutorCommandScheduler;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.EventStore;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

/**
 * @author andrii.loboda
 */
public enum ConferenceBoundedContext {

    INSTANCE;

    private final BoundedContext boundedContext;

    public BoundedContext get() {
        return boundedContext;
    }

    ConferenceBoundedContext() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        final EventStore eventStore = EventStore.newBuilder()
                                                .setStreamExecutor(MoreExecutors.directExecutor())
                                                .setStorage(storageFactory.createEventStorage())
                                                .build();
        final CommandBus commandBus = newCommandBus(storageFactory);
        final BoundedContext.Builder result = BoundedContext.newBuilder()
                                                            .setName("Conference tests")
                                                            .setStorageFactory(storageFactory)
                                                            .setCommandBus(commandBus)
                                                            .setEventBus(EventBus.newInstance(eventStore));
        boundedContext = result.build();
    }


    private static CommandBus newCommandBus(InMemoryStorageFactory storageFactory) {
        final CommandStore store = new CommandStore(storageFactory.createCommandStorage());
        final CommandBus.Builder builder = CommandBus.newBuilder();
        builder.setCommandStore(store);
        builder.setScheduler(new ExecutorCommandScheduler());
        return builder.build();
    }

}
