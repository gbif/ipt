/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates and compares the random tokens used for CSRF protection, both for logged-in sessions and for the login
 * form.
 */
public final class CsrfTokenUtils {

  private static final int TOKEN_BYTES = 32;
  private static final SecureRandom RANDOM = new SecureRandom();

  private CsrfTokenUtils() {
  }

  /**
   * @return a new random token: 32 bytes, URL-safe Base64 encoded without padding (43 characters), so it can be used
   * in forms, headers and URLs without escaping
   */
  public static String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /**
   * Compares tokens in constant time, so the response time doesn't reveal how much of a guess was right.
   *
   * @param expected the token the server issued
   * @param sent the token the request sent
   * @return true only if both tokens are present and equal; a missing token never matches
   */
  public static boolean matches(String expected, String sent) {
    return expected != null && sent != null
        && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8));
  }
}
