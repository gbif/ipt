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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.StrutsStatics;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * An Interceptor that makes sure an admin user is currently logged in and returns a notAllowed otherwise.
 */
public class CsrfLoginInterceptor extends AbstractInterceptor {

  private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  public final static String CSRFtoken = "CSRFtoken";
  private final static int TOKEN_LENGTH = 32;
  private static SecureRandom rnd = new SecureRandom();

  @Inject
  private AppConfig cfg;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    ActionContext ac = invocation.getInvocationContext();
    HttpServletResponse resp = (HttpServletResponse) ac.get(StrutsStatics.HTTP_RESPONSE);
    Map<String, Object> session = ac.getSession();

    Cookie csrfCookie = new Cookie(CSRFtoken, null);
    if (session.containsKey(Constants.SESSION_USER)) {
      // logged in already, remove cookie
      csrfCookie.setMaxAge(0);

    } else {
      // create new CSRF login token and store it as a cookie
      StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
      for( int i = 0; i < TOKEN_LENGTH; i++ ) {
        sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
      }
      String token = sb.toString();
      // add token to cookie
      csrfCookie.setValue(token);
      csrfCookie.setMaxAge(cfg.CSRF_TOKEN_EXPIRATION);
      csrfCookie.setHttpOnly(true);

      try {
        URI iptUri = URI.create(cfg.getBaseUrl());
        csrfCookie.setPath(iptUri.getPath());
        csrfCookie.setDomain(iptUri.getHost());
        csrfCookie.setSecure(iptUri.getScheme().equalsIgnoreCase("https"));
      } catch (Exception e) {
        // ignore
      }

      // add token to newCsrfToken stack value to populate form
      ac.put("newCsrfToken", token);
    }
    resp.addCookie(csrfCookie);

    return invocation.invoke();
  }
}
