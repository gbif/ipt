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

import java.util.stream.Stream;

import org.gbif.ipt.IptBaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XSSUtilsTest extends IptBaseTest {

  /**
   * Payloads that previously bypassed the old regex-based XSSUtil.
   * All must now be flagged by containsXSS,
   * and stripXSS must remove the dangerous markup entirely.
   */
  public static Stream<Arguments> legacyBypassPayloads() {
    return Stream.of(
        // Event handlers outside the old hardcoded list
        // (load|error|mouseover|submit|reset|focus|click)
        Arguments.of("<svg onanimationstart=alert(1)>", "onanimationstart"),
        Arguments.of("<body onpageshow=alert(1)>", "onpageshow"),
        Arguments.of("<details open ontoggle=alert(1)>", "ontoggle"),
        Arguments.of("<div onwheel=\"alert(1)\">text</div>", "onwheel"),
        Arguments.of("<input autofocus onfocus=alert(1)>", "onfocus"),

        // whitespace defeating the eval(/expression( literal patterns,
        // combined with an unlisted event handler
        Arguments.of("<svg onwheel=\"eval (alert(1))\">", "eval"),
        Arguments.of("<div style=\"width:expression (alert(1))\">", "expression"),

        // tags never covered by the old list at all (script/iframe/img only)
        Arguments.of("<math href=\"javascript:alert(1)\">click</math>", "javascript:"),
        Arguments.of("<object data=\"javascript:alert(1)\"></object>", "javascript:"),
        Arguments.of("<embed src=\"javascript:alert(1)\">", "javascript:"),
        Arguments.of("<link rel=stylesheet href=\"javascript:alert(1)\">", "javascript:"),

        // nested tag reassembly - old code stripped in a single non-recursive
        // pass, so removing the inner <script> left a working outer <script>
        Arguments.of("<scr<script>ipt>alert(1)</scr</script>ipt>", "<script")
    );
  }

  public static Stream<Arguments> legacyBypassLinkPayloads() {
    return Stream.of(
        // "javascript:" broken up with control chars / numeric char refs
        Arguments.of("<a href=\"java\tscript:alert(1)\">click</a>", "script:"),
        Arguments.of("<a href=\"&#106;avascript:alert(1)\">click</a>", "script:"),

        // data: URI carrying an encoded payload
        Arguments.of(
            "<a href=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\">click</a>",
            "data:")
    );
  }

  @ParameterizedTest
  @MethodSource("legacyBypassPayloads")
  void legacyBypassIsNowFlaggedAndFullyStripped(String payload, String dangerousToken) {
    assertTrue(XSSUtils.containsXSS(payload),
        "Expected payload to be flagged as XSS: " + payload);

    String cleaned = XSSUtils.stripXSS(payload);
    assertFalse(cleaned.toLowerCase().contains(dangerousToken.toLowerCase()),
        "Cleaned output still contains dangerous token '" + dangerousToken + "': " + cleaned);
    // No tag structure of any kind should survive Safelist filtering
    assertFalse(cleaned.contains("<"), "Cleaned output still contains a tag: " + cleaned);
  }

  @ParameterizedTest
  @MethodSource("legacyBypassLinkPayloads")
  void legacyBypassLinkIsNowFlaggedAndProperlyStripped(String payload, String dangerousToken) {
    assertTrue(XSSUtils.containsXSS(payload),
        "Expected payload to be flagged as XSS: " + payload);

    String cleaned = XSSUtils.stripXSS(payload);
    assertFalse(cleaned.toLowerCase().contains(dangerousToken.toLowerCase()),
        "Cleaned output still contains dangerous token '" + dangerousToken + "': " + cleaned);
    // Link should survive
    assertTrue(cleaned.contains("<"), "Cleaned output should contain a tag: " + cleaned);
    assertEquals("<a rel=\"nofollow noopener noreferrer\" target=\"_blank\">click</a>", cleaned);
  }

  /**
   * Same categories of payload as the URL-embedded cases in the legacy test,
   * kept in the original "full URL" shape for direct before/after comparison.
   */
  public static Stream<Arguments> legacyBypassPayloadsAsUrls() {
    return Stream.of(
        Arguments.of("http://gbif.org/search?q=<svg onanimationstart=alert(1)>"),
        Arguments.of("http://gbif.org/search?q=<body onpageshow=alert(1)>"),
        Arguments.of("http://gbif.org/search?redirect=<a href=\"java\tscript:alert(1)\">click</a>"),
        Arguments.of("http://gbif.org/search?redirect=<a href=\"&#106;avascript:alert(1)\">click</a>"),
        Arguments.of("http://gbif.org/search?q=<svg onwheel=\"eval (alert(1))\">")
    );
  }

  @ParameterizedTest
  @MethodSource("legacyBypassPayloadsAsUrls")
  void legacyBypassUrlIsFlagged(String value) {
    assertTrue(XSSUtils.containsXSS(value));
  }

  /**
   * Payloads the OLD implementation already caught. Kept here as a regression
   * guard: the new implementation must not become weaker on these.
   */
  public static Stream<Arguments> previouslyCaughtPayloads() {
    return Stream.of(
        Arguments.of("<script>alert(6227)</script>", "script"),
        Arguments.of("<iframe>frame it</iframe>", "iframe"),
        Arguments.of("<img src=x onerror=prompt(/XSS/)>", "img"),
        Arguments.of("<img src=x onmouseover=prompt(/XSS/)>", "img")
    );
  }

  @ParameterizedTest
  @MethodSource("previouslyCaughtPayloads")
  void previouslyCaughtPayloadStillFlaggedAndStripped(String payload, String dangerousToken) {
    assertTrue(XSSUtils.containsXSS(payload));
    String cleaned = XSSUtils.stripXSS(payload);
    assertFalse(cleaned.toLowerCase().contains(dangerousToken.toLowerCase()));
  }

  public static Stream<Arguments> legitimateValues() {
    return Stream.of(
        Arguments.of("sven@gbif.org"),
        Arguments.of("http://gbif.org/login.do?email=sven@gbif.org&password=xyz"),
        Arguments.of("http://www.gbif.org/ipt"),
        Arguments.of("http://api.gbif.org/v1/occurrence/search?year=1800"),
        Arguments.of("O'Brien & Sons"),
        Arguments.of("Report_2024-Q1_final(v2).csv"),
        Arguments.of("Species name: Panthera onca (Linnaeus, 1758)"),
        Arguments.of("100% coverage; error margin under 5 percent"),

        // HTML-entity-encoded text is inert as far as an HTML parser is
        // concerned - JSoup correctly sees literal text, not a <script> tag,
        // so this is NOT XSS at this layer.
        Arguments.of("&lt;script&gt;alert(1)&lt;/script&gt;")
    );
  }

  @ParameterizedTest
  @MethodSource("legitimateValues")
  void legitimateValueIsNotFlagged(String value) {
    assertFalse(XSSUtils.containsXSS(value),
        "Legitimate value was incorrectly flagged as XSS: " + value);
  }

  @Test
  void nullValueIsNotFlaggedAndStripReturnsNull() {
    assertFalse(XSSUtils.containsXSS(null));
    assertNull(XSSUtils.stripXSS(null));
  }

  @Test
  void emptyAndBlankValuesAreNotFlagged() {
    assertFalse(XSSUtils.containsXSS(""));
    assertFalse(XSSUtils.containsXSS("   "));
    assertEquals("", XSSUtils.stripXSS(""));
  }

  @Test
  void plainTextWithoutMarkupIsUnchangedBySripXSS() {
    String plain = "Just a normal title with no markup";
    assertFalse(XSSUtils.containsXSS(plain));
    assertEquals(plain, XSSUtils.stripXSS(plain));
  }

  /**
   * JSoup's parser is more conservative than the plain HTML5-spec description
   * of "a bare '&lt;' not followed by a letter/!/// is just literal text".
   * In practice, standalone '&lt;' or '&gt;' characters in free text get
   * flagged by {@code isValid}, even when they don't form a complete,
   * well-formed tag.
   *
   * <p>This is intentionally treated as correct/expected here, not a bug to
   * work around: any raw '&lt;' or '&gt;' reaching an HTML rendering context
   * is inherently ambiguous and needs output-encoding at render time
   * regardless of what this filter does. Fields where users legitimately
   * need to type comparison operators or angle brackets should rely on the
   * view layer's HTML auto-escaping, not on this filter passing them through
   * unmodified.
   */
  @Test
  void standaloneAngleBracketsInPlainTextAreConservativelyFlagged() {
    assertTrue(XSSUtils.containsXSS("5 < 10 and 10 > 5"));
    assertTrue(XSSUtils.containsXSS("margin of error < 5%"));
  }

  @Test
  void nullCharacterIsRemoved() {
    String withNull = "Title\0Name";
    String cleaned = XSSUtils.stripXSS(withNull);
    assertFalse(cleaned.contains("\0"));
  }

  @Test
  void mixedLegitimateAndMaliciousContentKeepsSafePartRemovesUnsafePart() {
    String value = "Great dataset<script>alert(1)</script> from 2024";
    assertTrue(XSSUtils.containsXSS(value));
    String cleaned = XSSUtils.stripXSS(value);
    assertFalse(cleaned.toLowerCase().contains("script"));
    assertTrue(cleaned.contains("Great dataset"));
    assertTrue(cleaned.contains("2024"));
  }
}
