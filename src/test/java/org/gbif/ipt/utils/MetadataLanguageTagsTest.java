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

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MetadataLanguageTagsTest {

  private static Vocabulary languages() {
    Vocabulary vocabulary = new Vocabulary();
    vocabulary.addConcept(concept("eng", "en"));
    vocabulary.addConcept(concept("fre", "fr"));
    vocabulary.addConcept(concept("spa", "es"));
    // no two-letter code
    vocabulary.addConcept(concept("haw", null));
    return vocabulary;
  }

  private static VocabularyConcept concept(String identifier, String twoLetter) {
    VocabularyConcept concept = new VocabularyConcept();
    concept.setIdentifier(identifier);
    if (twoLetter != null) {
      VocabularyTerm term = new VocabularyTerm();
      term.setTitle(twoLetter);
      concept.addAlternativeTerm(term);
    }
    return concept;
  }

  @Test
  public void toTag() {
    Vocabulary vocabulary = languages();
    assertEquals("en", MetadataLanguageTags.toTag(vocabulary, "eng"));
    assertEquals("fr", MetadataLanguageTags.toTag(vocabulary, "fre"));
    assertEquals("fr", MetadataLanguageTags.toTag(vocabulary, "FR"));
    assertEquals("haw", MetadataLanguageTags.toTag(vocabulary, "haw"));
    // unknown to the vocabulary, but shaped like a language tag
    assertEquals("pt-br", MetadataLanguageTags.toTag(vocabulary, "pt-BR"));
    assertNull(MetadataLanguageTags.toTag(vocabulary, "\"><script>"));
    assertNull(MetadataLanguageTags.toTag(vocabulary, " "));
    assertEquals("en", MetadataLanguageTags.toTag(null, "en"));
    // without the vocabulary: the two-letter code Java knows, ISO 639-2/B codes too
    assertEquals("en", MetadataLanguageTags.toTag(null, "eng"));
    assertEquals("fr", MetadataLanguageTags.toTag(null, "fre"));
    assertEquals("haw", MetadataLanguageTags.toTag(null, "haw"));
    // Java also knows the old codes "ji" and "iw"
    assertEquals("yi", MetadataLanguageTags.toTag(null, "yid"));
    assertEquals("he", MetadataLanguageTags.toTag(null, "heb"));
  }

  @Test
  public void options() {
    Map<String, String> names = new LinkedHashMap<>();
    names.put("eng", "English");
    names.put("haw", "Hawaiian");
    Map<String, String> options = MetadataLanguageTags.options(languages(), names);
    assertEquals(List.of("en", "haw"), List.copyOf(options.keySet()));
    assertEquals("English", options.get("en"));
  }

  @Test
  public void translationLanguagesOfTitleAndDescription() {
    Eml eml = new Eml();
    eml.setMetadataLanguage("eng");
    eml.getTitleTranslations().put("fr", "Titre");
    eml.setDescription("<section xml:lang=\"en\"><para>a</para></section><section xml:lang=\"spa\"><para>b</para></section>");
    // other fields can't be translated yet
    eml.setPurpose("<section xml:lang=\"en\"><para>a</para></section><section xml:lang=\"haw\"><para>c</para></section>");
    assertEquals(List.of("fr", "es"), MetadataLanguageTags.translationLanguages(languages(), eml));
  }

  @Test
  public void endonym() {
    assertEquals("Español", MetadataLanguageTags.endonym("es"));
    assertEquals("Français", MetadataLanguageTags.endonym("fr"));
  }
}
