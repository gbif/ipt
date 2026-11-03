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

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocBookLanguagesTest {

  private static final String TRANSLATED =
      "<section xml:lang=\"en\"><para>English</para><section><title>Methods</title><para>How</para></section></section>\n"
          + "<section xml:lang=\"es\"><para>Espa&#241;ol &amp; m&#225;s</para></section>";

  @Test
  public void splitsLanguageSections() {
    Map<String, String> sections = DocBookLanguages.split(TRANSLATED);
    assertEquals(2, sections.size());
    assertEquals("<para>English</para><section><title>Methods</title><para>How</para></section>", sections.get("en"));
    assertEquals("<para>Español &amp; más</para>", sections.get("es"));
  }

  @Test
  public void valuesWithoutLanguageSectionsAreNotTranslated() {
    assertTrue(DocBookLanguages.split(null).isEmpty());
    assertTrue(DocBookLanguages.split("").isEmpty());
    assertTrue(DocBookLanguages.split("<para>Text</para>").isEmpty());
    // a heading section, not a language section
    assertTrue(DocBookLanguages.split("<section><title>Methods</title><para>How</para></section>").isEmpty());
    // not every top-level element is a language section
    assertTrue(DocBookLanguages.split("<section xml:lang=\"en\"><para>a</para></section><para>b</para>").isEmpty());
    // text outside the sections
    assertTrue(DocBookLanguages.split("text<section xml:lang=\"en\"><para>a</para></section>").isEmpty());
  }

  @Test
  public void defaultContentIsTheMetadataLanguage() {
    assertEquals("<para>Español &amp; más</para>", DocBookLanguages.defaultContent(TRANSLATED, "spa"));
    assertEquals("<para>English</para><section><title>Methods</title><para>How</para></section>",
        DocBookLanguages.defaultContent(TRANSLATED, "eng"));
    // no section in the metadata language
    assertNull(DocBookLanguages.defaultContent(TRANSLATED, "fre"));
    // not translated: the whole value
    assertEquals("<para>Text</para>", DocBookLanguages.defaultContent("<para>Text</para>", "en"));
    assertNull(DocBookLanguages.defaultContent(null, "en"));
  }

  @Test
  public void translationsSkipTheMetadataLanguage() {
    Map<String, String> translations = DocBookLanguages.translations(TRANSLATED, "eng");
    assertEquals(1, translations.size());
    assertTrue(translations.containsKey("es"));
    assertFalse(translations.containsKey("en"));
    assertTrue(DocBookLanguages.translations("<para>Text</para>", "en").isEmpty());
  }
}
