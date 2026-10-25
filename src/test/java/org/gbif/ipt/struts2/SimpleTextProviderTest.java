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
package org.gbif.ipt.struts2;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleTextProviderTest {

  private final SimpleTextProvider textProvider = new SimpleTextProvider();

  @Test
  public void testEscapedApostropheIsUnescapedOnce() {
    assertEquals("The password provided doesn't match",
        textProvider.getText(Locale.ENGLISH, "validation.password.wrong", null, new String[0]));
  }

  @Test
  public void testEscapedApostropheWithArgs() {
    assertEquals("EML file version #3 for resource Bob's data couldn't be parsed",
        textProvider.getText(Locale.ENGLISH, "portal.resource.eml.error.parse", null, new String[]{"3", "Bob's data"}));
  }

  @Test
  public void testEscapedApostropheInTranslation() {
    assertEquals("Méthodes d'échantillonnage",
        textProvider.getText(Locale.FRENCH, "submenu.methods", null, new String[0]));
  }

  @Test
  public void testMissingKey() {
    assertEquals("no.such.key", textProvider.getText(Locale.ENGLISH, "no.such.key", null, new String[0]));
    assertEquals("Bob's {0}", textProvider.getText(Locale.ENGLISH, "no.such.key", "Bob''s '{0}'", new String[0]));
  }
}
