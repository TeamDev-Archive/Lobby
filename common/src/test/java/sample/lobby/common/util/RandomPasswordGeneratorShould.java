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

package sample.lobby.common.util;

import org.junit.Test;

import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Alexander Litus
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class RandomPasswordGeneratorShould {

    private static final int PASSWORD_LENGTH = 7;

    @Test
    public void generate_non_empty_password() {
        final String password = RandomPasswordGenerator.generate(PASSWORD_LENGTH);
        assertFalse(isNullOrEmpty(password));
    }

    @Test
    public void generate_password_of_given_length() {
        final String password = RandomPasswordGenerator.generate(PASSWORD_LENGTH);
        assertEquals(PASSWORD_LENGTH, password.length());
    }

    @Test
    public void generate_random_passwords() {
        final Set<Object> passwords = newHashSet();
        final int generationsCount = 30;
        for (int i = 0; i < generationsCount; i++) {
            final String password = RandomPasswordGenerator.generate(PASSWORD_LENGTH);
            passwords.add(password);
        }
        assertEquals(generationsCount, passwords.size());
    }
}
