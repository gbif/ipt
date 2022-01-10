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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * A Java port of password encoding as done natively by Drupal 7.
 *
 * <p>A password is structured as:
 *
 * <pre>
 *   $S$&lt;iterations&gt;&lt;salt&gt;&lt;encoded&gt;
 * </pre>
 *
 * Where:
 *
 * <ul>
 *   <li>iterations is a based 64 encoded number of loops to apply the hashing algorithm
 *   <li>salt is an 8 character random string
 *   <li>encoded is the the final encoded hash of the password using SHA-512 encoding applied
 *       iterations times and with the salt key. The final encoded is truncated to the length
 *       provided in the constructor
 * </ul>
 *
 * Mostly this code is copied from the registry project.
 */
@Singleton
public class RegistryPasswordEncoder {

  private static final Logger LOG = LoggerFactory.getLogger(RegistryPasswordEncoder.class);
  private static final String ALGORITHM = "SHA-512";
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final String PASSWORD_ITOA64 =
      "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  private final int encodedHashLength;

  public RegistryPasswordEncoder() {
    encodedHashLength = 55;
  }

  public RegistryPasswordEncoder(int hashLength) {
    encodedHashLength = hashLength;
  }

  /**
   * Reads the iteration count out of the encoded settings.
   */
  private static int passwordGetCountLog2(String settings) {
    return PASSWORD_ITOA64.indexOf(settings.charAt(3));
  }

  /**
   * Encode using the algorithm.
   */
  private static byte[] sha512(String input) {
    return sha512(input.getBytes());
  }

  /**
   * Encode using the algorithm.
   */
  private static byte[] sha512(byte[] input) {
    try {
      return MessageDigest.getInstance(ALGORITHM).digest(input);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Missing required message digest algorithm " + ALGORITHM);
    }
  }

  /**
   * Encodes the password using a random salt.
   *
   * @param password to encode
   * @return the encoded password which will have a random salt
   */
  public String encode(CharSequence password) {
    String settingsHash = randomSalt();
    return encode(password.toString(), settingsHash);
  }

  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return encode(rawPassword.toString(), encodedPassword).equalsIgnoreCase(encodedPassword);
  }

  /**
   * Encodes password using the settings and salt from the provided encoded password.
   *
   * @param preEncoded the pre-encoded version storing individual hashing settings in its first 12
   *     chars.
   * @return the encoded password using the existing hash settings or null on error
   */
  public String encode(final String password, String preEncoded) {
    // The first 12 characters of an existing hash are its setting string.
    preEncoded = preEncoded.substring(0, 12);
    int countLog2 = passwordGetCountLog2(preEncoded);
    String salt = preEncoded.substring(4, 12);

    int count = 1 << countLog2;

    byte[] hash;
    try {
      hash = sha512(salt.concat(password));

      do {
        hash = sha512(joinBytes(hash, password.getBytes(StandardCharsets.UTF_8)));
      } while (--count > 0);
    } catch (Exception e) {
      LOG.error("Unable to encode the password", e);
      return null;
    }

    String output = preEncoded + base64Encode(hash, hash.length);
    return output.substring(0, encodedHashLength);
  }

  /** Joins the byte arrays into a new array, sized to fit. */
  private static byte[] joinBytes(byte[] a, byte[] b) {
    byte[] combined = new byte[a.length + b.length];

    System.arraycopy(a, 0, combined, 0, a.length);
    System.arraycopy(b, 0, combined, a.length, b.length);
    return combined;
  }

  /**
   * Encodes the input using some smarts. Understanding those smarts is an exercise left to the
   * reader.
   *
   * @see <a href="http://stackoverflow.com/questions/11736555/java-autentication-of-drupal-passwords">here</a>
   */
  private static String base64Encode(byte[] input, int count) {

    StringBuilder output = new StringBuilder();
    int i = 0;
    CharSequence itoa64 = PASSWORD_ITOA64;
    do {
      long value = signedByteToUnsignedLong(input[i++]);

      output.append(itoa64.charAt((int) value & 0x3f));
      if (i < count) {
        value |= signedByteToUnsignedLong(input[i]) << 8;
      }
      output.append(itoa64.charAt((int) (value >> 6) & 0x3f));
      if (i++ >= count) {
        break;
      }
      if (i < count) {
        value |= signedByteToUnsignedLong(input[i]) << 16;
      }

      output.append(itoa64.charAt((int) (value >> 12) & 0x3f));
      if (i++ >= count) {
        break;
      }
      output.append(itoa64.charAt((int) (value >> 18) & 0x3f));
    } while (i < count);

    return output.toString();
  }

  /**
   * Clears any sign bit on the given byte.
   */
  private static long signedByteToUnsignedLong(byte b) {
    return b & 0xFF;
  }

  /**
   * Returns a random 8 character salt prefixed with "$S$D" (which is what Drupal 7 did).
   */
  private static String randomSalt() {
    // drupal uses 8 character salts, prefixed with $S$D, so we copy that
    StringBuilder sb = new StringBuilder(11);
    sb.append("$S$D");
    for (int i = 0; i < 8; i++) {
      sb.append(PASSWORD_ITOA64.charAt(RANDOM.nextInt(PASSWORD_ITOA64.length())));
    }
    return sb.toString();
  }
}
