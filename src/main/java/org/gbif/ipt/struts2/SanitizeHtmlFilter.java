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

import org.gbif.ipt.utils.XSSUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.PolicyFactory;

import lombok.Getter;

/**
 * Filter that wraps a request and sanitizes every parameter to prevent XSS.
 * <p>
 * Most parameters are treated as plain text: all HTML tags are stripped, leaving only
 * the text content (with {@code <br>}/{@code <p>} converted to blank lines).
 * <p>
 * A small, explicit set of parameters (see {@link #RICH_TEXT_PARAMETERS}) correspond to
 * form fields that intentionally accept a limited set of HTML tags (e.g. the resource
 * description, which is later converted to DocBook in the EML). For those parameters,
 * the safe subset of tags is preserved instead of being stripped to plain text.
 * <p>
 * Rather than hard-rejecting the whole request with a generic HTTP 400 when something
 * suspicious is found, this filter sanitizes the offending value and records which
 * parameters were flagged in a request attribute ({@link #XSS_FLAGGED_FIELDS_ATTR}, a
 * {@code Map<String, String>} of parameter name to a user-facing message). A downstream
 * Struts interceptor ({@code XssFieldErrorInterceptor}) reads this attribute and turns it
 * into normal per-field validation errors on the current action, so the user sees an
 * inline message on the offending field instead of being dropped onto an error page.
 */
public class SanitizeHtmlFilter implements Filter {

  private static final Logger LOG = LogManager.getLogger(SanitizeHtmlFilter.class);

  /**
   * Request attribute key under which a {@code Map<String, String>} of
   * "flagged parameter name" -> "user-facing message" is stored, for a downstream
   * Struts interceptor to turn into field errors. Absent/empty if nothing was flagged.
   */
  public static final String XSS_FLAGGED_FIELDS_ATTR = "org.gbif.ipt.struts2.xssFlaggedFields";

  private static final String PLAIN_TEXT_FLAG_MESSAGE =
      "Potentially malicious content was removed from this field. Please review your input.";

  private static final String RICH_TEXT_FLAG_MESSAGE =
      "Some HTML in this field was not in the list of allowed tags and was removed.";

  /**
   * Names of request parameters that are allowed to contain a limited set of HTML tags,
   * matching what the corresponding form field's editor exposes to the user.
   */
  private static final Set<String> RICH_TEXT_PARAMETERS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          "eml.description",
          "eml.acknowledgements",
          "eml.abstract",
          "eml.purpose",
          "eml.introduction",
          "eml.gettingStarted",
          "eml.updateFrequencyDescription",
          "eml.additionalInfo"
      )));

  /**
   * Tags allowed in rich-text parameters, matching the "Allowed HTML tags" list shown to users
   * in the form help text: div, p, ul, ol, li, h1, pre, a, b, em, sup, sub.
   */
  private static final PolicyFactory RICH_TEXT_POLICY = new HtmlPolicyBuilder()
      .allowElements("div", "p", "ul", "ol", "li", "h1", "pre", "a", "b", "em", "sup", "sub")
      .allowAttributes("href").onElements("a")
      .allowStandardUrlProtocols()
      .requireRelNofollowOnLinks()
      .toFactory();

  /**
   * Same allow-list as {@link #RICH_TEXT_POLICY}, but WITHOUT the automatic rel="nofollow"
   * hardening on links. Used only to detect whether content was genuinely removed for being
   * disallowed (script tags, disallowed attributes, unsafe URL protocols, etc.). Comparing
   * against the hardened policy's own output would falsely flag every legitimate link, since
   * requireRelNofollowOnLinks() always adds an attribute that was never in the original input -
   * that's safety hardening, not evidence of a stripped attack attempt.
   */
  private static final PolicyFactory RICH_TEXT_DETECTION_POLICY = new HtmlPolicyBuilder()
      .allowElements("div", "p", "ul", "ol", "li", "h1", "pre", "a", "b", "em", "sup", "sub")
      .allowAttributes("href").onElements("a")
      .allowStandardUrlProtocols()
      .toFactory();

  /**
   * Policy used to strip all tags from plain-text parameters. Built once and reused -
   * HtmlPolicyBuilder/Policy instances are safe to share across threads once built.
   * <p>
   * "br" and "p" are allowed here purely so the custom {@link HtmlStreamEventReceiver}
   * below observes their open tags (to insert blank lines); the receiver never re-emits
   * the tags themselves, so no actual HTML survives in the output.
   */
  private static final HtmlPolicyBuilder PLAIN_TEXT_POLICY_BUILDER = new HtmlPolicyBuilder()
      .allowElements("br", "p");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      XssRequestWrapper wrapped = new XssRequestWrapper((HttpServletRequest) request);
      if (!wrapped.getFlaggedFields().isEmpty()) {
        wrapped.setAttribute(XSS_FLAGGED_FIELDS_ATTR, wrapped.getFlaggedFields());
      }
      chain.doFilter(wrapped, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  public static class XssRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> sanitized;
    @Getter
    private final Map<String, String> flaggedFields = new LinkedHashMap<>();

    /**
     * Constructor that will parse and sanitize all input parameters.
     *
     * @param request the HttpServletRequest to wrap
     */
    public XssRequestWrapper(HttpServletRequest request) {
      super(request);
      sanitized = sanitizeParamMap(request.getParameterMap());
    }

    @Override
    public String getParameter(String name) {
      String[] vals = getParameterValues(name);
      if (vals != null && vals.length > 0)
        return vals[0];
      else
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
      return sanitized;

    }

    @Override
    public String[] getParameterValues(String name) {
      return sanitized.get(name);
    }

    private Map<String, String[]> sanitizeParamMap(Map<String, String[]> raw) {
      Map<String, String[]> res = new HashMap<>();

      if (raw != null) {
        for (String key : raw.keySet()) {
          String[] rawVals = raw.get(key);
          String[] snzVals = new String[rawVals.length];
          for (int i = 0; i < rawVals.length; i++) {
            snzVals[i] = sanitize(key, rawVals[i]);
          }
          res.put(key, snzVals);
        }
      }

      return res;
    }

    /**
     * Sanitizes a single parameter value according to whether it is a designated
     * rich-text field or a plain-text field. Never throws - offending content is
     * sanitized and flagged via {@link #flaggedFields} instead of rejecting the request.
     */
    private String sanitize(String parameter, String value) {
      if (value == null) {
        return null;
      }

      if (RICH_TEXT_PARAMETERS.contains(parameter)) {
        return sanitizeRichText(parameter, value);
      }

      // old-school regex-based check flags the field rather than rejecting the request;
      // rich-text fields intentionally contain markup, so this check does not apply to them
      if (XSSUtils.containsXSS(value)) {
        flaggedFields.put(parameter, PLAIN_TEXT_FLAG_MESSAGE);
      }

      return sanitizePlainText(parameter, value);
    }

    /**
     * Strips all HTML tags, keeping only text content. {@code <br>} and {@code <p>} are
     * converted to blank lines rather than simply discarded, to preserve some readability
     * for values that were originally formatted (e.g. pasted from a rich text source).
     */
    private String sanitizePlainText(String parameter, String value) {
      StringBuilder sb = new StringBuilder();
      HtmlSanitizer.Policy textPolicy = PLAIN_TEXT_POLICY_BUILDER.build(new HtmlStreamEventReceiver() {
        @Override
        public void openDocument() {
        }

        @Override
        public void closeDocument() {
        }

        @Override
        public void openTag(String elementName, List<String> attribs) {
          if ("br".equals(elementName) || "p".equals(elementName)) {
            sb.append('\n');
            sb.append('\n');
          }
        }

        @Override
        public void closeTag(String elementName) {
        }

        @Override
        public void text(String text) {
          sb.append(text);
        }
      });
      HtmlSanitizer.sanitize(value, textPolicy);

      String cleaned = sb.toString();
      if (!cleaned.equals(value)) {
        LOG.warn("Parameter sanitization. {} modified: {}  ==>  {}", parameter, value, cleaned);
      }

      return cleaned;
    }

    /**
     * Sanitizes a rich-text field down to the allowed tag subset, preserving safe markup
     * instead of flattening it to plain text. Flags the field (with a milder message than
     * the plain-text case) if anything was actually removed.
     */
    private String sanitizeRichText(String parameter, String value) {
      String cleanedForOutput = RICH_TEXT_POLICY.sanitize(value);
      String cleanedForDetection = RICH_TEXT_DETECTION_POLICY.sanitize(value);

      String normalizedValue = StringEscapeUtils.unescapeHtml4(value);
      String normalizedDetection = StringEscapeUtils.unescapeHtml4(cleanedForDetection);

      if (!normalizedDetection.equals(normalizedValue)) {
        LOG.warn("Parameter sanitization (rich text). {} modified: {}  ==>  {}",
            parameter, value, cleanedForOutput);
        flaggedFields.put(parameter, RICH_TEXT_FLAG_MESSAGE);
      }

      return cleanedForOutput;
    }
  }
}
