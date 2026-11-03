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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Language codes of translated metadata, as written in xml:lang: the two-letter ISO 639-1 code where the language has
 * one (e.g. "es"), the ISO 639-2 code otherwise (e.g. "haw"). The ISO 639-2 vocabulary has the two-letter code as an
 * alternative term of each concept.
 */
public final class MetadataLanguageTags {

  /**
   * A language tag (BCP 47 shape: language, then optional subtags).
   */
  public static final Pattern TAG = Pattern.compile("^[a-zA-Z]{2,3}(-[a-zA-Z0-9]{1,8})*$");

  private MetadataLanguageTags() {
  }

  /**
   * The language tag for a code from the ISO 639-2 vocabulary, or for a two-letter code. Without the vocabulary (or for
   * a code it does not know), a three-letter code is turned into the two-letter code Java knows for it. Other codes are
   * returned as they are (lower case), if they look like a language tag; null otherwise.
   */
  public static String toTag(Vocabulary languages, String code) {
    String value = StringUtils.trimToNull(code);
    if (value == null || !TAG.matcher(value).matches()) {
      return null;
    }
    VocabularyConcept concept = languages == null ? null : languages.findConcept(value);
    if (concept != null) {
      for (VocabularyTerm term : concept.getAlternativeTerms()) {
        if (term.getTitle() != null && term.getTitle().trim().length() == 2) {
          return term.getTitle().trim().toLowerCase(Locale.ROOT);
        }
      }
      return concept.getIdentifier().toLowerCase(Locale.ROOT);
    }
    if (value.length() == 3) {
      for (String twoLetter : Locale.getISOLanguages()) {
        if (Eml.isSameLanguage(twoLetter, value)) {
          // Java lists old codes too ("ji", "iw", "in"): the current one, e.g. "yi"
          return Locale.forLanguageTag(twoLetter).getLanguage();
        }
      }
    }
    return value.toLowerCase(Locale.ROOT);
  }

  /**
   * The name of a language in the language itself, e.g. "español" for "es"; the tag if Java does not know it.
   */
  public static String endonym(String tag) {
    Locale locale = Locale.forLanguageTag(tag);
    String name = locale.getDisplayLanguage(locale);
    if (StringUtils.isBlank(name) || name.equalsIgnoreCase(tag)) {
      return tag;
    }
    return StringUtils.capitalize(name);
  }

  /**
   * The languages to offer for translations: language tag to name, in the order of the given names.
   *
   * @param languages ISO 639-2 vocabulary
   * @param names     ISO 639-2 code to name, in the user interface language
   */
  public static Map<String, String> options(Vocabulary languages, Map<String, String> names) {
    Map<String, String> options = new LinkedHashMap<>();
    names.forEach((code, name) -> {
      String tag = toTag(languages, code);
      if (tag != null) {
        options.putIfAbsent(tag, name);
      }
    });
    return options;
  }

  /**
   * The languages the metadata has been translated into (other than the metadata language), in the order they are
   * first found. Only the title and the description can be translated so far.
   */
  public static List<String> translationLanguages(Vocabulary languages, Eml eml) {
    String metadataLanguage = eml.getMetadataLanguage();
    List<String> tags = new ArrayList<>();
    addLanguages(tags, languages, eml.getTitleTranslations().keySet(), metadataLanguage);
    addDocBookLanguages(tags, languages, eml.getDescription(), metadataLanguage);
    return tags;
  }

  /**
   * Adds the languages of the translations in a DocBook field to a list of language tags, skipping the metadata
   * language and languages already in the list.
   */
  private static void addDocBookLanguages(List<String> tags, Vocabulary languages, String value,
                                          String metadataLanguage) {
    addLanguages(tags, languages, DocBookLanguages.translations(value, metadataLanguage).keySet(), metadataLanguage);
  }

  /**
   * Adds language codes to a list of language tags, skipping the metadata language and languages already in the list.
   */
  private static void addLanguages(List<String> tags, Vocabulary languages, Collection<String> codes,
                                   String metadataLanguage) {
    for (String code : new ArrayList<>(codes)) {
      String tag = toTag(languages, code);
      if (tag != null && !Eml.isSameLanguage(tag, metadataLanguage)
          && tags.stream().noneMatch(existing -> Eml.isSameLanguage(existing, tag))) {
        tags.add(tag);
      }
    }
  }
}
