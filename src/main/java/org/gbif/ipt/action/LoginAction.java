package org.gbif.ipt.action;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.CsrfLoginInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.HashMap;

/**
 * Action handling login/logout only. Login can happen both from small login box on every page, or dedicated login
 * page that provides the IPT administrator contact in case of problems or to create a new account.
 */
public class LoginAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(AccountAction.class);

  private final UserAccountManager userManager;

  private String redirectUrl;
  private String email;
  private String password;
  private String adminEmail;
  private String csrfToken;

  @Inject
  public LoginAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    UserAccountManager userManager) {
    super(textProvider, cfg, registrationManager);
    this.userManager = userManager;
  }

  @Override
  public void prepare() {
    super.prepare();
    adminEmail = userManager.getDefaultAdminEmail();
    if (Strings.isNullOrEmpty(adminEmail)) {
      adminEmail = userManager.list(User.Role.Admin).get(0).getEmail();
    }
  }

  public String login() throws IOException {
    // login
    Cookie csrfCookie = getCookie(CsrfLoginInterceptor.CSRFtoken);

    // user already logged in, return
    if (session.get(Constants.SESSION_USER) != null) {
      return SUCCESS;
    }

    if (email != null && !StringUtils.isBlank(csrfToken) && csrfCookie != null) {
      // prevent login CSRF
      // Make sure the token from the login form is the same as in the cookie
        if (csrfToken.equals(csrfCookie.getValue())){
          User authUser = userManager.authenticate(email, password);
          if (authUser == null) {
            addActionError(getText("admin.user.wrong.email.password.combination"));
            LOG.info("User " + email + " failed to log in");
          } else {
            LOG.info("User " + email + " logged in successfully");
            authUser.setLastLoginToNow();
            userManager.save();
            session.put(Constants.SESSION_USER, authUser);
            // remember previous URL to redirect back to
            setRedirectUrl();
            return SUCCESS;
          }
        } else {
          LOG.warn("CSRF login token wrong! A potential malicious attack.");
        }
    }
    return INPUT;
  }

  public String logout() {
    redirectUrl = getBase() + "/";
    session.clear();
    return SUCCESS;
  }

  private void setRedirectUrl() {
    redirectUrl = "/";
    // if we have a request refer back to the originally requested page
    String referer = (String) session.get(Constants.SESSION_REFERER);
    LOG.debug("Session's referer: {}", referer);

    if (referer != null && !(referer.endsWith("login.do") || referer.endsWith("login"))) {
      if (cfg.getBaseUrl() != null) {
        redirectUrl = cfg.getBaseUrl() + referer;
      } else {
        redirectUrl = referer;
      }
    }

    // remove referer from session
    session.remove(Constants.SESSION_REFERER);

    LOG.info("Redirecting to " + redirectUrl);
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAdminEmail() {
    return adminEmail;
  }

  public void setAdminEmail(String adminEmail) {
    this.adminEmail = adminEmail;
  }

  public void setCsrfToken(String csrfToken) {
    this.csrfToken = csrfToken;
  }

}
