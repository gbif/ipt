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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  public void testPlaceholdersAreFilled() {
    assertEquals("Failed to validate Description. Please see logs for more details.",
        textProvider.getText(Locale.ENGLISH, "validation.failed.see.logs", null, new String[]{"Description"}));
    assertEquals("Failed to analyze the source: No such file",
        textProvider.getText(Locale.ENGLISH, "manage.source.analyzed.problem", null, new String[]{"No such file"}));
  }

  /**
   * Every English message must be a valid {@link MessageFormat} pattern, e.g. use {0} and not {}, otherwise its
   * arguments are never filled in.
   */
  @Test
  public void testEnglishMessagesAreValidPatterns() {
    ResourceBundle bundle = textProvider.getTexts(Locale.ENGLISH);
    List<String> invalid = new ArrayList<>();
    for (String key : bundle.keySet()) {
      try {
        new MessageFormat(bundle.getString(key));
      } catch (IllegalArgumentException e) {
        invalid.add(key);
      }
    }
    assertTrue(invalid.isEmpty(), "Invalid message patterns: " + invalid);
  }

  /**
   * Apostrophes in English messages must be escaped as '', a single ' is dropped by {@link MessageFormat}.
   */
  @Test
  public void testEnglishApostrophesAreEscaped() {
    ResourceBundle bundle = textProvider.getTexts(Locale.ENGLISH);
    List<String> unescaped = new ArrayList<>();
    for (String key : bundle.keySet()) {
      if (bundle.getString(key).replace("''", "").contains("'")) {
        unescaped.add(key);
      }
    }
    assertTrue(unescaped.isEmpty(), "Unescaped apostrophes: " + unescaped);
  }

  @Test
  public void testMissingKey() {
    assertEquals("no.such.key", textProvider.getText(Locale.ENGLISH, "no.such.key", null, new String[0]));
    assertEquals("Bob's {0}", textProvider.getText(Locale.ENGLISH, "no.such.key", "Bob''s '{0}'", new String[0]));
  }
}
