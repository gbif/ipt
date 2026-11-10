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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsrfTokenUtilsTest {

  @Test
  public void tokensAreRandomAndUrlSafe() {
    String token = CsrfTokenUtils.newToken();
    assertTrue(token.matches("[A-Za-z0-9_-]{43}"), token);
    assertNotEquals(token, CsrfTokenUtils.newToken());
  }

  @Test
  public void matchesOnlyEqualPresentTokens() {
    String token = CsrfTokenUtils.newToken();
    assertTrue(CsrfTokenUtils.matches(token, new String(token)));
    assertFalse(CsrfTokenUtils.matches(token, CsrfTokenUtils.newToken()));
    assertFalse(CsrfTokenUtils.matches(token, token.substring(1)));
    assertFalse(CsrfTokenUtils.matches(token, ""));
    assertFalse(CsrfTokenUtils.matches(token, null));
    assertFalse(CsrfTokenUtils.matches(null, token));
    assertFalse(CsrfTokenUtils.matches(null, null));
  }
}
