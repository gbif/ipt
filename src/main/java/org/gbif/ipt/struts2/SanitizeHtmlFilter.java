/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import org.gbif.api.model.common.messaging.Response;
import org.gbif.ipt.utils.XSSUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlSanitizer;
import org.owasp.html.HtmlStreamEventReceiver;

import com.google.inject.Singleton;

/**
 * Filter that wraps a request and checks every parameter for potential xss attacks.
 * If found a bad request response error is returned.
 *
 * Otherwise all html tags are stripped from the content found in input parameters to prevent yet unknown xss attacks.
 */
@Singleton
public class SanitizeHtmlFilter implements Filter {
  private static final Logger LOG = LogManager.getLogger(SanitizeHtmlFilter.class);
  private static final HtmlPolicyBuilder POLICY_BUILDER = new HtmlPolicyBuilder();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      try {
        request = new XssRequestWrapper((HttpServletRequest) request);
        chain.doFilter(request, response);

      } catch (XssException e) {
        LOG.warn(e);
        HttpServletResponse resp = (HttpServletResponse) response;
        if (!resp.isCommitted()) {
          resp.sendError(Response.StatusCode.BAD_REQUEST.getCode());
        }
      }
    }
  }


  public static class XssException extends IllegalArgumentException {
    private final String parameter;

    public XssException(String parameter) {
      super("Malicious XSS content found in request parameter " + parameter);
      this.parameter = parameter;
    }

    public String getParameter() {
      return parameter;
    }
  }

  public static class XssRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, String[]> sanitized;

    /**
     * Constructor that will parse and sanitize all input parameters.
     *
     * @param request the HttpServletRequest to wrap
     */
    @SuppressWarnings("unchecked")
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


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String[]> getParameterMap() {
      return sanitized;

    }

    @Override
    public String[] getParameterValues(String name) {
      return sanitized.get(name);
    }

    private Map<String, String[]> sanitizeParamMap(Map<String, String[]> raw) {
      Map<String, String[]> res = new HashMap<String, String[]>();

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
     * Remove all tags from the content
     */
    private String sanitize(String parameter, String value) throws XssException {
      // first use old school xss checking and refuse request if found!
      if (XSSUtil.containsXSS(value)) {
        throw new XssException(parameter);
      }

      // now strip all tags to be safe in case our custom regex misses sth
      StringBuilder sb = new StringBuilder();
      HtmlSanitizer.Policy textPolicy = POLICY_BUILDER.build(new HtmlStreamEventReceiver(){
        @Override
        public void openDocument() {}
        @Override
        public void closeDocument() {}
        @Override
        public void openTag(String elementName, List<String> attribs) {
          if ("br".equals(elementName) || "p".equals(elementName)) {
            sb.append('\n');
            sb.append('\n');
          }
        }
        @Override
        public void closeTag(String elementName) {}
        @Override
        public void text(String text) {
          sb.append(text);
        }
      });
      HtmlSanitizer.sanitize(value, textPolicy);

      String cleaned = sb.toString();
      if (!cleaned.equals(value)) {
        LOG.warn("Parameter sanitization. " + parameter + " modified: " +  value + "  ==>  " + cleaned);
      }

      return cleaned;
    }
  }
}
