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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * What {@link RequireManagerInterceptor} and {@link RequireAdminInterceptor} do for a request without a logged-in
 * user.
 */
public final class LoginRedirect {

  private static final Logger LOG = LogManager.getLogger(LoginRedirect.class);

  private LoginRedirect() {
  }

  /**
   * Returns the result for a request without a logged-in user, and remembers the page to return to after login.
   * <ul>
   *   <li>AJAX requests (jQuery's X-Requested-With header), e.g. the publication report polled every second, get a
   *   401: a redirect would load the login page into the calling page. They are not remembered either, as they are
   *   no page to return to.</li>
   *   <li>Other requests are redirected to the login page. Only GET requests are remembered: the URL of a POST, e.g.
   *   cancel.do, can't be opened after login (405). Without a remembered URL, LoginAction falls back to the Referer
   *   header, the page the form was on.</li>
   * </ul>
   *
   * @return {@link BaseAction#NOT_ALLOWED} for AJAX requests, {@link BaseAction#LOGIN} otherwise
   */
  public static String loginRequired(HttpServletRequest request, Map<String, Object> session) {
    if (request != null && isAjax(request)) {
      LOG.debug("AJAX request to {} without a logged-in user", request.getServletPath());
      return BaseAction.NOT_ALLOWED;
    }

    if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
      String referer = request.getServletPath();
      if (request.getQueryString() != null) {
        referer = referer + '?' + request.getQueryString();
      }
      LOG.debug("Put referer into session: {}", referer);
      session.put(Constants.SESSION_REFERER, referer);
    }
    return BaseAction.LOGIN;
  }

  /**
   * @return true if the request was sent by jQuery's AJAX functions, which set X-Requested-With
   */
  static boolean isAjax(HttpServletRequest request) {
    return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
  }
}
