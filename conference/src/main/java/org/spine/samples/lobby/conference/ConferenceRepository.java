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

import org.spine3.base.EmailAddress;
import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.conference.contracts.Conference;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author andrii.loboda
 */
public class ConferenceRepository {

    // TODO:2016-04-20:alexander.litus: what storage to use?
    private final Map<ConferenceId, Conference> store = newHashMap();

    public void store(Conference entity) {
        store.put(entity.getId(), entity);
    }

    @Nullable
    public Conference load(ConferenceId id) {
        return store.get(id);
    }

    @Nullable
    public Conference load(EmailAddress emailAddress, String accessCode) {
        for (Conference conference : store.values()) {
            final boolean emailMatches = conference.getOwner()
                                                   .getEmail()
                                                   .equals(emailAddress);
            final boolean accessCodeMatches = conference.getAccessCode()
                                                        .equals(accessCode);
            if (accessCodeMatches && emailMatches) {
                return conference;
            }
        }
        return null;
    }

    public void deleteAll() {
        store.clear();
    }
}

