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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncodingDamageTest {

  @Test
  public void testLooksDamaged() {
    assertTrue(EncodingDamage.looksDamaged("DragÃ¸r", false));
    assertTrue(EncodingDamage.looksDamaged("Skakbr?t", false));
    assertTrue(EncodingDamage.looksDamaged("bad �", false));
    // already reported as characters that aren't valid in the encoding
    assertFalse(EncodingDamage.looksDamaged("bad �", true));
    assertFalse(EncodingDamage.looksDamaged("Dragør", false));
    assertFalse(EncodingDamage.looksDamaged("SÃO PAULO", false));
    assertFalse(EncodingDamage.looksDamaged("what?", false));
    assertFalse(EncodingDamage.looksDamaged("??", false));
    assertFalse(EncodingDamage.looksDamaged("https://example.org/a?b=c", false));
  }

  @Test
  public void testMayLookDamaged() {
    assertTrue(EncodingDamage.mayLookDamaged(new String[] {"Oslo", "DragÃ¸r"}));
    assertTrue(EncodingDamage.mayLookDamaged(new String[] {"Skakbr?t"}));
    assertTrue(EncodingDamage.mayLookDamaged(new String[] {"bad �"}));
    assertFalse(EncodingDamage.mayLookDamaged(new String[] {"Oslo", "Dragør", "what?"}));
    // a question mark at the end of a value and a letter at the start of the next aren't in one word
    assertFalse(EncodingDamage.mayLookDamaged(new String[] {"what?", "yes"}));
  }

  @Test
  public void testMarkHtml() {
    assertEquals("Drag<mark>&Atilde;&cedil;</mark>r &lt;b&gt;", EncodingDamage.markHtml("DragÃ¸r <b>"));
    assertEquals("Skakbr<mark>?</mark>t", EncodingDamage.markHtml("Skakbr?t"));
    assertEquals("a<mark>��</mark>b", EncodingDamage.markHtml("a��b"));
    assertEquals("https://example.org/a?b=c", EncodingDamage.markHtml("https://example.org/a?b=c"));
    assertEquals("", EncodingDamage.markHtml(null));
  }
}
