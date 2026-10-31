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

import java.security.SecureRandom;

import org.apache.commons.text.RandomStringGenerator;

/**
 * Generates passwords for accounts created or reset by an administrator.
 */
public final class PasswordUtils {

  public static final int PASSWORD_LENGTH = 12;
  private static final String PASSWORD_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final RandomStringGenerator PASSWORD_GENERATOR =
      new RandomStringGenerator.Builder()
          .selectFrom(PASSWORD_ALLOWED_CHARS.toCharArray())
          .usingRandom(RANDOM::nextInt)
          .get();

  private PasswordUtils() {
  }

  public static String generatePassword() {
    return PASSWORD_GENERATOR.generate(PASSWORD_LENGTH);
  }
}
