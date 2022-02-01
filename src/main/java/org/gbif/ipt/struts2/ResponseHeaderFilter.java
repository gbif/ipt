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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;


/**
 * Class filters response, setting headers.
 * </br>
 * A set of cache control headers are added to the response in order to reduce the likelihood of browser caches and
 * proxies disclosing any sensitive data through caching.
 * </br>
 * See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4">HTTP/1.1 header
 * definitions</a>}.
 * See {@link <a href="https://www.mnot.net/cache_docs/">Caching Tutorial</a>}.
 * See {@link <a http://stackoverflow.com/a/2068407">Caching Header FAQ</a>}.
 */
public class ResponseHeaderFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  /**
   * Sets the following headers on response:
   * </br>
   * Cache-Control: HTTP/1.1 spec for clients with the following directives:
   * - max-age: indicates client is willing to accept a response whose age is no greater than the time in seconds
   * - no-store: indicates a cache MUST NOT store any part of either this response or the request that elicited it
   * - must-revalidate: indicates caches must obey information you give them about a response's age
   * </br>
   * Pragma: Defined for backward compatibility with HTTP/1.0. HTTP/1.1 caches SHOULD treat "Pragma: no-cache" as if
   * the client had sent "Cache-Control: no-cache".
   * </br>
   * Expires: HTTP/1.1 spec for clients and proxies gives the date/time after which the response is considered stale.
   * HTTP/1.1 clients and caches MUST treat invalid date formats (e.g.  "0") as in the past (i.e., "already expired").
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletResponse res = ((HttpServletResponse) response);
    res.setHeader("Cache-Control", "max-age=0, no-store, must-revalidate");
    res.setHeader("Pragma", "no-cache");
    res.setHeader("Expires", "0");
    chain.doFilter(request, res);
  }

  @Override
  public void destroy() {

  }
}
