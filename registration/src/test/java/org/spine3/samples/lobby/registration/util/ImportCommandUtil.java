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

package org.spine3.samples.lobby.registration.util;

import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.base.EventContext;
import org.spine3.base.Events;
import org.spine3.samples.lobby.common.ImportEvents;
import org.spine3.samples.lobby.common.util.aggregate.LobbyAggregate;

/**
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("UtilityClass")
public class ImportCommandUtil {

    private ImportCommandUtil() {
    }

    public static void apply(LobbyAggregate<?, ?, ?> aggregate, Message event, CommandContext context) {
        final ImportEvents importCmd = ImportEvents.newBuilder()
                                                   .addEvent(Events.createEvent(event, EventContext.getDefaultInstance()))
                                                   .build();
        aggregate.dispatchForTest(importCmd, context);
    }
}
