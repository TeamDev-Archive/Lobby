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
import org.spine3.server.CommandDispatcher;
import org.spine3.server.command.CommandBus;
import org.spine3.server.command.CommandStore;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.EventStore;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

/**
 * @author andrii.loboda
 */
public class Given {


    private final BoundedContext boundedContext = newBoundedContext();

    /* package */ Given() {
//        boundedContext.getEventBus().register(new StubCommandHandler());
    }

    /**
     * Creates a new {@link BoundedContext} instance with {@link InMemoryStorageFactory},
     * {@link CommandDispatcher} and {@link EventBus}.
     */
    public static BoundedContext newBoundedContext() {
        final InMemoryStorageFactory storageFactory = InMemoryStorageFactory.getInstance();
        final EventStore eventStore = EventStore.newBuilder()
                                                .setStreamExecutor(MoreExecutors.directExecutor())
                                                .setStorage(storageFactory.createEventStorage())
                                                .build();
        final CommandBus commandBus = CommandBus.create(new CommandStore(storageFactory.createCommandStorage()));
        final EventBus eventBus = EventBus.newInstance(eventStore);

        final BoundedContext.Builder result = BoundedContext.newBuilder()
                                                            .setName("Conference test context")
                                                            .setStorageFactory(storageFactory)
                                                            .setCommandBus(commandBus)
                                                            .setEventBus(eventBus);
        return result.build();
    }
}
