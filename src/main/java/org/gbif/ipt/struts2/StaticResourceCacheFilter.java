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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Pattern;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sets Cache-Control on static files, so browsers don't keep old copies after an IPT upgrade.
 * <ul>
 *   <li>Files with a version in their name, e.g. jquery-3.7.1.min.js, never change: cached for a year.</li>
 *   <li>Other files may change in any release: browsers revalidate them on every use (304 if unchanged).</li>
 * </ul>
 */
public class StaticResourceCacheFilter implements Filter {

  static final String IMMUTABLE = "max-age=31536000, immutable";
  static final String REVALIDATE = "no-cache";

  // a version such as -3.7.1. or -0.1- in the file name
  private static final Pattern VERSIONED = Pattern.compile("-\\d+(\\.\\d+)+[.-][^/]*$");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String path = req.getRequestURI().substring(req.getContextPath().length());
    ((HttpServletResponse) response).setHeader("Cache-Control", cacheControl(req, path));
    chain.doFilter(request, response);
  }

  // A missing file gets REVALIDATE too, so its 404 isn't cached for a year
  private static String cacheControl(HttpServletRequest req, String path) {
    try {
      if (VERSIONED.matcher(path).find() && req.getServletContext().getResource(path) != null) {
        return IMMUTABLE;
      }
    } catch (MalformedURLException e) {
      // not a valid resource path
    }
    return REVALIDATE;
  }
}
