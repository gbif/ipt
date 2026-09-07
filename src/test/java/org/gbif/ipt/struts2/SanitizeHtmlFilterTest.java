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

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanitizeHtmlFilterTest {

  private static final String RICH_TEXT_PARAM = "eml.description";
  private static final String PLAIN_PARAM = "eml.title";

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain chain;

  private final SanitizeHtmlFilter filter = new SanitizeHtmlFilter();

  private void givenParams(Map<String, String[]> params) {
    when(request.getParameterMap()).thenReturn(params);
  }

  private SanitizeHtmlFilter.XssRequestWrapper runFilterAndCaptureWrappedRequest() throws Exception {
    filter.doFilter(request, response, chain);
    ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
    verify(chain).doFilter(captor.capture(), eq((ServletResponse) response));
    return (SanitizeHtmlFilter.XssRequestWrapper) captor.getValue();
  }

  @Test
  void scriptTagInPlainFieldIsFlaggedAndCleaned() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(PLAIN_PARAM, new String[]{"Title<embed src=\\\"javascript:alert(1)\\\">"});

    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertFalse(wrapped.getFlaggedFields().isEmpty());
    assertTrue(wrapped.getFlaggedFields().containsKey(PLAIN_PARAM));
    assertEquals("Title", wrapped.getParameter(PLAIN_PARAM));
  }

  @Test
  void imgTagInPlainFieldIsFlaggedAndCleaned() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(PLAIN_PARAM, new String[]{"<img src=x onerror=alert(1)>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertFalse(wrapped.getFlaggedFields().isEmpty());
    assertTrue(wrapped.getFlaggedFields().containsKey(PLAIN_PARAM));
    assertEquals("", wrapped.getParameter(PLAIN_PARAM));
  }

  @Test
  void javascriptProtocolInPlainFieldIsFlaggedAndCleaned() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(PLAIN_PARAM, new String[]{"<a href=\"javascript:alert(1)\">click</a>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertFalse(wrapped.getFlaggedFields().isEmpty());
    assertTrue(wrapped.getFlaggedFields().containsKey(PLAIN_PARAM));
    assertEquals("click", wrapped.getParameter(PLAIN_PARAM));
  }

  @Test
  void anyMaliciousValueAmongMultipleValuesIsFlagged() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    // second value in a multi-value parameter is malicious
    params.put("eml.keyword", new String[]{"harmless", "<script>alert(1)</script>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertFalse(wrapped.getFlaggedFields().isEmpty());
    assertTrue(wrapped.getFlaggedFields().containsKey("eml.keyword"));
    assertEquals("harmless", wrapped.getParameter("eml.keyword"));
  }

  @Test
  void plainFieldWithNoMarkupIsUnchanged() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(PLAIN_PARAM, new String[]{"Entomological Collections (NHRS)"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertTrue(wrapped.getFlaggedFields().isEmpty());
    assertEquals("Entomological Collections (NHRS)", wrapped.getParameter(PLAIN_PARAM));
  }

  @Test
  void plainFieldStripsBenignTagsButKeepsText() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    // <b> is not on the XSSUtils pattern list and is not an allowed element in the plain
    // policy either, so the tag itself should disappear while its text content remains.
    params.put(PLAIN_PARAM, new String[]{"Hello <b>World</b>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertTrue(wrapped.getFlaggedFields().isEmpty());
    assertEquals("Hello World", wrapped.getParameter(PLAIN_PARAM));
  }

  @Test
  void plainFieldConvertsBrAndPToBlankLines() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(PLAIN_PARAM, new String[]{"Para one<br>Para two<p>Para three</p>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertTrue(wrapped.getFlaggedFields().isEmpty());
    assertEquals("Para one\n\nPara two\n\nPara three", wrapped.getParameter(PLAIN_PARAM));
  }

  @Test
  void nullParameterValueIsHandledWithoutError() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(PLAIN_PARAM, new String[]{null});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertTrue(wrapped.getFlaggedFields().isEmpty());
    assertNull(wrapped.getParameter(PLAIN_PARAM));
  }

  @Test
  void richTextFieldPreservesAllowedTags() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(RICH_TEXT_PARAM, new String[]{"<p>Hello <b>World</b></p>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertTrue(wrapped.getFlaggedFields().isEmpty());
    assertEquals("<p>Hello <b>World</b></p>", wrapped.getParameter(RICH_TEXT_PARAM));
  }

  @Test
  void richTextFieldStripsDisallowedElementsLikeScript() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(RICH_TEXT_PARAM, new String[]{"<p>Hello</p><script>alert(1)</script>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertFalse(wrapped.getFlaggedFields().isEmpty());
    assertTrue(wrapped.getFlaggedFields().containsKey(RICH_TEXT_PARAM));
    assertEquals("<p>Hello</p>", wrapped.getParameter(RICH_TEXT_PARAM));
  }

  @Test
  void richTextFieldStripsJavascriptHrefButKeepsLinkText() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(RICH_TEXT_PARAM, new String[]{"<a href=\"javascript:alert(1)\">click me</a>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertFalse(wrapped.getFlaggedFields().isEmpty());
    assertTrue(wrapped.getFlaggedFields().containsKey(RICH_TEXT_PARAM));
    String sanitized = wrapped.getParameter(RICH_TEXT_PARAM);
    // href must not survive with a JavaScript: URL, regardless of exact surrounding markup
    assertFalse(sanitized.contains("javascript:"),
        "javascript: URL must be stripped from rich-text href, but was: " + sanitized);
  }

  @Test
  void richTextFieldStripsDisallowedAttributesLikeOnclick() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    // onclick= would normally be caught by XSSUtils, but rich-text fields skip that check
    // deliberately - this must be caught by the OWASP policy instead, since only "href" is
    // an allowed attribute on <a>.
    params.put(RICH_TEXT_PARAM, new String[]{"<a href=\"https://example.org\" onclick=\"evil()\">link</a>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertFalse(wrapped.getFlaggedFields().isEmpty());
    assertTrue(wrapped.getFlaggedFields().containsKey(RICH_TEXT_PARAM));
    String sanitized = wrapped.getParameter(RICH_TEXT_PARAM);
    assertFalse(sanitized.contains("onclick"),
        "onclick attribute must be stripped from rich-text field, but was: " + sanitized);
  }

  @Test
  void richTextFieldAllowsStandardHttpLink() throws Exception {
    Map<String, String[]> params = new HashMap<>();
    params.put(RICH_TEXT_PARAM,
        new String[]{"<a href=\"https://creativecommons.org/licenses/by/4.0/legalcode\">CC-BY 4.0</a>"});
    givenParams(params);

    SanitizeHtmlFilter.XssRequestWrapper wrapped = runFilterAndCaptureWrappedRequest();

    assertTrue(wrapped.getFlaggedFields().isEmpty());
    String sanitized = wrapped.getParameter(RICH_TEXT_PARAM);
    assertTrue(
        sanitized.contains("href=\"https://creativecommons.org/licenses/by/4.0/legalcode\""),
        "standard https href should be preserved, but was: " + sanitized);
    assertTrue(sanitized.contains("CC-BY 4.0"));
  }
}
