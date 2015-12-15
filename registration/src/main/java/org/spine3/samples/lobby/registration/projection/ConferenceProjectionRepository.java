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

package org.spine3.samples.lobby.registration.projection;

import org.spine3.samples.lobby.common.ConferenceId;
import org.spine3.samples.lobby.registration.Conference;
import org.spine3.server.BoundedContext;
import org.spine3.server.projection.ProjectionRepository;

import javax.annotation.Nonnull;

/**
 * The repository which manages conference projections.
 *
 * @see ConferenceProjection
 * @author Alexander Litus
 */
public class ConferenceProjectionRepository extends ProjectionRepository<ConferenceId, ConferenceProjection, Conference> {

    private final BoundedContext context;

    /**
     * Creates a new repository instance.
     *
     * @param context current bounded context
     */
    public ConferenceProjectionRepository(BoundedContext context) {
        super();
        this.context = context;
    }

    @Nonnull
    @Override
    public ConferenceProjection load(ConferenceId id) throws IllegalStateException {
        final ConferenceProjection conference = super.load(id);
        conference.setBoundedContext(context);
        return conference;
    }
}
