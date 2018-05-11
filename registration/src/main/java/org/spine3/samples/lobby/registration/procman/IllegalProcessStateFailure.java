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

package org.spine3.samples.lobby.registration.procman;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.spine3.base.FailureThrowable;

/**
 * A business failure which is thrown if a registration process is in inappropriate state for the requested operation.
 *
 * @author Alexander Litus
 */
public class IllegalProcessStateFailure extends FailureThrowable {

    /**
     * Creates a new failure instance.
     *
     * @param processManagerId an ID of the current process manager
     * @param processState     a current state of the process
     * @param messageHandled   a message which is handled but cannot be processed
     */
    public IllegalProcessStateFailure(ProcessManagerId processManagerId,
                                      RegistrationProcess.State processState,
                                      Message messageHandled) {
        super(Failures.IllegalProcessStateFailure.newBuilder()
                                                 .setProcessManagerId(processManagerId)
                                                 .setProcessState(processState)
                                                 .setMessage(Any.pack(messageHandled))
                                                 .build());
    }

    @Override
    public Failures.IllegalProcessStateFailure getFailure() {
        return (Failures.IllegalProcessStateFailure) super.getFailure();
    }

    private static final long serialVersionUID = 12365498497L;
}
