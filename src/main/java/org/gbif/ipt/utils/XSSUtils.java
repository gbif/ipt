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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class with utility methods for XSS filtering, backed by JSoup's HTML parser/sanitizer
 * instead of regex pattern matching.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XSSUtils {

  private static final Logger LOG = LoggerFactory.getLogger(XSSUtils.class);

  private static final Safelist IPT_SAFELIST = Safelist.none()
      // Allowed tags, no attributes
      .addTags("b", "strong", "i", "em", "u", "p", "br", "ul", "ol", "li")
      // Allow <a> tags with href only
      .addTags("a")
      .addAttributes("a", "href")
      // Only allow http/https links (blocks javascript:, data:, etc.)
      .addProtocols("a", "href", "http", "https")
      // Force safe rel/target on links to prevent tabnabbing
      .addEnforcedAttribute("a", "rel", "nofollow noopener noreferrer")
      .addEnforcedAttribute("a", "target", "_blank");

  /**
   * DocBook elements produced by {@code convertToDocBook} in {@code js/docbook/docbook-v2.js}, used by the
   * description, acknowledgements, purpose, introduction and getting started fields.
   */
  private static final Safelist DOCBOOK_SAFELIST = Safelist.none()
      .addTags("section", "title", "para", "itemizedlist", "orderedlist", "listitem", "emphasis", "subscript",
          "superscript", "literalLayout", "ulink", "citetitle")
      .addAttributes("ulink", "url")
      .addProtocols("ulink", "url", "http", "https", "mailto", "ftp");

  private static final Document.OutputSettings DOCBOOK_OUTPUT_SETTINGS = new Document.OutputSettings()
      .prettyPrint(false)
      .syntax(Document.OutputSettings.Syntax.xml);

  /**
   * Tests whether a string contains anything that wouldn't survive sanitization
   * (i.e. tags/attributes not on the safelist).
   *
   * @param value decoded string to test
   * @return true if the string contains disallowed markup, false otherwise
   */
  public static boolean containsXSS(String value) {
    if (value == null) {
      return false;
    }
    boolean isSafe = Jsoup.isValid(value, IPT_SAFELIST);
    if (!isSafe) {
      LOG.warn("Potentially malicious content found: {}", value);
    }
    return !isSafe;
  }

  /**
   * Strips any markup not allowed by the safelist, returning sanitized output.
   */
  public static String stripXSS(String value) {
    if (value == null) {
      return null;
    }
    return Jsoup.clean(value, IPT_SAFELIST);
  }

  /**
   * Strips any markup not allowed in a DocBook fragment, returning sanitized output.
   * <p>
   * The XML parser is used (rather than the HTML one) to preserve element name case (e.g. {@code literalLayout})
   * and to treat {@code title} as a regular element rather than HTML's raw-text one.
   */
  public static String stripDocBookXSS(String value) {
    if (value == null) {
      return null;
    }
    Document dirty = Document.createShell("");
    dirty.body().insertChildren(0, Parser.xmlParser().parseFragmentInput(value, null, ""));
    Document clean = new Cleaner(DOCBOOK_SAFELIST).clean(dirty);
    clean.outputSettings(DOCBOOK_OUTPUT_SETTINGS);
    return clean.body().html();
  }
}
