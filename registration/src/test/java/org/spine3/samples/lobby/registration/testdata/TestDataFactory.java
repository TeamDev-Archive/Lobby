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

import org.spine3.base.EmailAddress;
import org.spine3.base.PersonName;
import org.spine3.eventbus.EventBus;
import org.spine3.samples.lobby.common.PersonalInfo;
import org.spine3.server.BoundedContext;
import org.spine3.server.CommandDispatcher;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

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
     * {@link CommandDispatcher} and {@link EventBus}.
     */
    public static BoundedContext newBoundedContext() {
        final BoundedContext.Builder result = BoundedContext.newBuilder()
                .setName("Orders & Registrations tests")
                .setStorageFactory(InMemoryStorageFactory.getInstance())
                .setCommandDispatcher(CommandDispatcher.getInstance())
                .setEventBus(EventBus.newInstance());
        return result.build();
    }

    /**
     * Creates a new {@link PersonalInfo} instance with the given {@code givenName}, {@code familyName} and {@code email}.
     */
    public static PersonalInfo newPersonalInfo(String givenName, String familyName, String email) {
        final PersonName.Builder name = PersonName.newBuilder().setGivenName(givenName).setFamilyName(familyName);
        final EmailAddress.Builder emailAddress = EmailAddress.newBuilder().setValue(email);
        final PersonalInfo.Builder result = PersonalInfo.newBuilder().setName(name).setEmail(emailAddress);
        return result.build();
    }

}
