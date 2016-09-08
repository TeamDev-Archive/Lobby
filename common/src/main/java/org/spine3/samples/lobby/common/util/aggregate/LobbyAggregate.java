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

package org.spine3.samples.lobby.common.util.aggregate;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import jdk.nashorn.internal.objects.NativeJavaImporter;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Event;
import org.spine3.server.aggregate.Aggregate;

/**
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class LobbyAggregate<I, S extends Message, B extends Message.Builder> extends Aggregate<I, S, B> {

    /**
     * Creates a new aggregate instance.
     *
     * @param id the ID for the new aggregate
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    protected LobbyAggregate(I id) {
        super(id);
    }

    @VisibleForTesting
    public void incrementAggregateState(S newState) {
        incrementState(newState);
    }

}