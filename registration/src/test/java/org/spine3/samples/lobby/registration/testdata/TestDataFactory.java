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

package org.spine3.samples.lobby.registration.testdata;

import org.spine3.eventbus.EventBus;
import org.spine3.money.Currency;
import org.spine3.money.Money;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.server.BoundedContext;
import org.spine3.server.CommandDispatcher;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.util.Identifiers;

/**
 * TODO:2015-12-08:alexander.litus: docs
 *
 * @author Alexander Litus
 */
public class TestDataFactory {

    public static BoundedContext buildBoundedContext() {
        return BoundedContext.newBuilder()
                .setStorageFactory(InMemoryStorageFactory.getInstance())
                .setCommandDispatcher(CommandDispatcher.getInstance())
                .setEventBus(EventBus.newInstance())
                .build();
    }

    public static OrderId newOrderId() {
        final String id = Identifiers.newUuid();
        return OrderId.newBuilder().setUuid(id).build();
    }

    public static ConferenceId newConferenceId() {
        final String id = Identifiers.newUuid();
        return ConferenceId.newBuilder().setUuid(id).build();
    }

    public static Money newMoney(int amount, Currency currency) {
        final Money.Builder result = Money.newBuilder()
                .setAmount(amount)
                .setCurrency(currency);
        return result.build();
    }
}
