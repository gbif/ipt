package org.gbif.ipt.struts2;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.StrutsStatics;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Map;

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
