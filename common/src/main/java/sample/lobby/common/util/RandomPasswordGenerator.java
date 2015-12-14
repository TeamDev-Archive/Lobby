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

import java.security.SecureRandom;

/**
 * The generator of random password strings.
 *
 * @author Alexander Litus
 */
@SuppressWarnings({"UtilityClass", "TypeMayBeWeakened"})
public class RandomPasswordGenerator {

    // The characters allowed in passwords.
    private static final String ALLOWED_CHARS = "AaBbCcDdEeFfGgHhiJjKkLMmNnPpQqRrSsTtUuVvWwXxYyZz123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomPasswordGenerator() {
    }

    /**
     * Generates a random password of the given length.
     *
     * @param stringLength the length of the password
     * @return a random password string
     */
    public static String generate(int stringLength) {
        final StringBuilder code = new StringBuilder(stringLength);
        for (int i = 0; i < stringLength; i++) {
            final char randomChar = getRandomChar(ALLOWED_CHARS);
            code.append(randomChar);
        }
        return code.toString();
    }

    private static char getRandomChar(String allowedChars) {
        final int randomIndex = RANDOM.nextInt(allowedChars.length());
        final char result = allowedChars.charAt(randomIndex);
        return result;
    }
}
