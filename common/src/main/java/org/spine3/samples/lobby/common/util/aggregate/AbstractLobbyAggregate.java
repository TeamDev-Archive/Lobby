/*
 * Copyright 2015, TeamDev. All rights reserved.
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

package org.spine3.samples.lobby.common.util.aggregate;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.base.Event;
import org.spine3.samples.lobby.common.ImportEvents;
import org.spine3.server.aggregate.Aggregate;

import java.util.List;

/**
 * <p>Abstract type of aggregate used in the project.
 * Provides some tools for testing needs.</p>
 *
 * @param <I> the type for IDs of this class of aggregates
 * @param <S> the type of the state held by the aggregate
 * @param <B> the type of the aggregate state builder
 *
 * @see Aggregate
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class AbstractLobbyAggregate<I, S extends Message, B extends Message.Builder> extends Aggregate<I, S, B> {

    /**
     * Creates a new aggregate instance.
     *
     * @param id the ID for the new aggregate
     * @throws IllegalArgumentException if the ID is not of one of the supported types.
     *                                  Supported types are: {@code String}, {@code Long}, {@code Integer} and {@link Message}
     *
     * @see Aggregate
     * @see org.spine3.server.entity.Entity
     */
    protected AbstractLobbyAggregate(I id) {
        super(id);
    }

    /**
     * Changes the state of the aggregate.
     * Should be used for testing purposes only.
     *
     * @param newState New state to move to.
     */
    @VisibleForTesting
    public void incrementAggregateState(S newState) {
        incrementState(newState);
    }

    /**
     * Provides basic logic for handling {@code ImportEvents} command.
     * If no special logic is required, should be overridden "as is" with {@link org.spine3.server.command.Assign} annotation.
     */
    public List<Event> handle(ImportEvents command, CommandContext ctx) {
        return command.getEventList();
    }
}
