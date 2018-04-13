package org.gbif.ipt.action;


import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.CsrfLoginInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;

import javax.servlet.http.Cookie;
import java.io.IOException;

/**
 * Action handling login/logout only. Login can happen both from small login box on every page, or dedicated login
 * page that provides the IPT administrator contact in case of problems or to create a new account.
 */
public class LoginAction extends POSTAction {

  // logging
  private static final Logger LOG = Logger.getLogger(AccountAction.class);

  private final UserAccountManager userManager;

  private String redirectUrl;
  private String email;
  private String password;
  // to show admin contact
  private User admin;
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
    // populate admin user
    admin = userManager.list(User.Role.Admin).get(0);
  }

  public String login() throws IOException {
    // login
    Cookie csrfCookie = getCookie(CsrfLoginInterceptor.CSRFtoken);
    if (email != null && !StringUtils.isBlank(csrfToken) && csrfCookie != null) {
      // prevent login CSRF, see https://support.detectify.com/customer/portal/articles/1969819-login-csrf
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
    if (req != null) {
      String referer = req.getHeader("Referer");
      if (referer != null && referer.startsWith(cfg.getBaseUrl()) && !(referer.endsWith("login.do") || referer
        .endsWith("login"))) {
        redirectUrl = referer;
      }
    }
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

  public User getAdmin() {
    return admin;
  }

  public void setAdmin(User admin) {
    this.admin = admin;
  }

  public void setCsrfToken(String csrfToken) {
    this.csrfToken = csrfToken;
  }

}
