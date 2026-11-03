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

import org.gbif.metadata.eml.ipt.model.Eml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

/**
 * Translations of EML DocBook text fields. Only the description can be translated so far.
 * <p>
 * A field in more than one language holds one top-level {@code <section xml:lang="..">} per language, the metadata
 * language first:
 * <pre>{@code
 * <section xml:lang="en"><para>..</para><section><title>Methods</title>..</section></section>
 * <section xml:lang="es"><para>..</para></section>
 * }</pre>
 * A field in a single language has no such sections. Sections with a heading never carry {@code xml:lang}, so a
 * value is taken as multilingual only when every top-level element is a section with {@code xml:lang}.
 */
public final class DocBookLanguages {

  public static final String LANG_ATTRIBUTE = "xml:lang";

  private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings()
      .prettyPrint(false)
      .syntax(Document.OutputSettings.Syntax.xml);

  private DocBookLanguages() {
  }

  /**
   * The language sections of a value, in document order: language (as written in xml:lang) to the content of its
   * section. Empty if the value is not multilingual. Only the first section of a language is kept.
   */
  public static Map<String, String> split(String value) {
    if (StringUtils.isBlank(value)) {
      return Collections.emptyMap();
    }
    Document document = Document.createShell("");
    document.outputSettings(OUTPUT_SETTINGS);
    List<Node> nodes = Parser.xmlParser().parseFragmentInput(value, null, "");
    document.body().insertChildren(0, nodes);

    Map<String, String> sections = new LinkedHashMap<>();
    for (Node node : document.body().childNodes()) {
      if (node instanceof TextNode text) {
        if (!text.isBlank()) {
          return Collections.emptyMap();
        }
      } else if (node instanceof Element element) {
        String language = StringUtils.trimToNull(element.attr(LANG_ATTRIBUTE));
        if (!"section".equals(element.tagName()) || language == null) {
          return Collections.emptyMap();
        }
        sections.putIfAbsent(language, element.html());
      }
      // comments and the like carry no content
    }
    return sections;
  }

  public static boolean isMultilingual(String value) {
    return !split(value).isEmpty();
  }

  /**
   * The content in a language: the section in that language for a multilingual value (null if there is none), the
   * whole value otherwise.
   *
   * @param value    DocBook value
   * @param language metadata language of the resource, e.g. "en" or "eng"
   */
  public static String defaultContent(String value, String language) {
    Map<String, String> sections = split(value);
    if (sections.isEmpty()) {
      return value;
    }
    return content(sections, language);
  }

  /**
   * The content of the section in a language, null if there is none. Codes of the same language match, e.g. "en"
   * and "eng".
   */
  public static String content(Map<String, String> sections, String language) {
    for (Map.Entry<String, String> section : sections.entrySet()) {
      if (Eml.isSameLanguage(section.getKey(), language)) {
        return section.getValue();
      }
    }
    return null;
  }

  /**
   * The translations of a value: every language section except the one in the metadata language.
   */
  public static Map<String, String> translations(String value, String language) {
    Map<String, String> translations = new LinkedHashMap<>(split(value));
    translations.keySet().removeIf(key -> Eml.isSameLanguage(key, language));
    return translations;
  }
}
