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

import org.gbif.ipt.action.LoginAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.utils.CsrfTokenUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.dispatcher.Parameter;
import org.apache.struts2.interceptor.AbstractInterceptor;

/**
 * Protects logged-in users against cross-site request forgery and clickjacking.
 * <ul>
 *   <li>Every logged-in session gets a random token. It's exposed to templates as {@value #TOKEN_CONTEXT_KEY}
 *   (header.ftl and header_setup.ftl put it into a meta tag, and csrf.js adds it to same-origin POST forms and
 *   jQuery AJAX requests).</li>
 *   <li>Requests of a logged-in user with any other method than GET, HEAD, OPTIONS or TRACE must send the token as
 *   the {@value #TOKEN_PARAM} parameter or the {@value #TOKEN_HEADER} header, otherwise the result is
 *   {@value #INVALID_TOKEN} (403).</li>
 *   <li>Action methods annotated with {@link RequirePost} only accept POST, otherwise the result is
 *   {@value #METHOD_NOT_ALLOWED} (405). Together with the token check this means state can't be changed by a link
 *   or an image on another site, nor by a link inside the IPT, where SameSite cookies don't help.</li>
 *   <li>Pages of logged-in users can only be framed by the IPT itself.</li>
 * </ul>
 * Anonymous requests aren't checked: they have no session to abuse. The login form is protected by
 * {@link LoginAction}, which checks the same session token.
 */
public class CsrfInterceptor extends AbstractInterceptor {

  private static final Logger LOG = LogManager.getLogger(CsrfInterceptor.class);

  public static final String TOKEN_PARAM = "iptCsrfToken";
  public static final String TOKEN_HEADER = "X-CSRF-Token";
  public static final String TOKEN_CONTEXT_KEY = "iptCsrfToken";
  public static final String INVALID_TOKEN = "403-csrf";
  public static final String METHOD_NOT_ALLOWED = "405";

  private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    ActionContext ac = invocation.getInvocationContext();
    HttpServletRequest req = ac.getServletRequest();
    HttpServletResponse resp = ac.getServletResponse();
    Map<String, Object> session = ac.getSession();
    Object action = invocation.getAction();

    String httpMethod = req == null ? "GET" : StringUtils.upperCase(req.getMethod(), Locale.ROOT);
    User user = session == null ? null : (User) session.get(Constants.SESSION_USER);

    String token = null;
    if (user != null) {
      token = getOrCreateToken(session);
      ac.put(TOKEN_CONTEXT_KEY, token);
    }

    if (resp != null && (user != null || action instanceof LoginAction)) {
      resp.setHeader("X-Frame-Options", "SAMEORIGIN");
      resp.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
    }

    if (!"POST".equals(httpMethod) && requiresPost(invocation)) {
      LOG.warn("Rejected {} request to {}: only POST is allowed", httpMethod, actionName(invocation));
      if (resp != null) {
        resp.setHeader("Allow", "POST");
      }
      return METHOD_NOT_ALLOWED;
    }

    // a logged-in user posting a stale login form is just redirected by LoginAction, nothing changes
    if (user != null && !SAFE_METHODS.contains(httpMethod) && !isLoginForm(invocation)
        && !CsrfTokenUtils.matches(token, sentToken(ac, req))) {
      LOG.warn("Rejected {} request to {} by {}: missing or wrong CSRF token. A potential malicious attack.",
          httpMethod, actionName(invocation), user.getEmail());
      return INVALID_TOKEN;
    }

    return invocation.invoke();
  }

  /**
   * Stores a new CSRF token in the session, replacing any previous one. Login calls it (see
   * {@code BaseAction.logIn(User)}), so a new login never reuses the token of an earlier one or of the login form.
   */
  public static String resetToken(Map<String, Object> session) {
    String token = CsrfTokenUtils.newToken();
    session.put(Constants.SESSION_CSRF_TOKEN, token);
    return token;
  }

  /**
   * Returns the CSRF token of the session, creating it if needed. Anonymous sessions get one when the login form is
   * shown, and login replaces it with a new one by {@link #resetToken(Map)}. Creating it here also covers sessions
   * logged in some other way, e.g. sessions restored after an upgrade. It isn't synchronized: if two requests create
   * a token at once, one of them wins and a page holding the other gets a single rejection, which a reload fixes.
   */
  public static String getOrCreateToken(Map<String, Object> session) {
    String token = (String) session.get(Constants.SESSION_CSRF_TOKEN);
    if (token == null) {
      LOG.debug("Creating CSRF token for the session");
      token = resetToken(session);
    }
    return token;
  }

  private static String sentToken(ActionContext ac, HttpServletRequest req) {
    if (req != null && StringUtils.isNotBlank(req.getHeader(TOKEN_HEADER))) {
      return req.getHeader(TOKEN_HEADER);
    }
    // the parameters also include the fields of multipart requests
    Parameter param = ac.getParameters() == null ? null : ac.getParameters().get(TOKEN_PARAM);
    return param != null && param.isDefined() ? param.getValue() : null;
  }

  /**
   * @return true if the request submits the login form; other methods of {@link LoginAction}, like logout, are checked
   */
  private static boolean isLoginForm(ActionInvocation invocation) {
    return invocation.getAction() instanceof LoginAction
        && invocation.getProxy() != null && "login".equals(invocation.getProxy().getMethod());
  }

  /**
   * @return true if the action method invoked by the mapping is annotated with {@link RequirePost}
   */
  static boolean requiresPost(ActionInvocation invocation) {
    Object action = invocation.getAction();
    String methodName = invocation.getProxy() == null ? null : invocation.getProxy().getMethod();
    if (action == null || methodName == null) {
      return false;
    }
    Method method = MethodUtils.getMatchingAccessibleMethod(action.getClass(), methodName);
    return method != null && MethodUtils.getAnnotation(method, RequirePost.class, true, true) != null;
  }

  private static String actionName(ActionInvocation invocation) {
    if (invocation.getProxy() == null) {
      return String.valueOf(invocation.getAction());
    }
    return invocation.getProxy().getNamespace() + "/" + invocation.getProxy().getActionName()
        + " (" + invocation.getProxy().getMethod() + ")";
  }
}
