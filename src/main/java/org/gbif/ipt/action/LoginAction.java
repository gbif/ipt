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
package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.CsrfLoginInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.PBEEncrypt;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

/**
 * Action handling login/logout only. Login can happen both from small login box on every page, or dedicated login
 * page that provides the IPT administrator contact in case of problems or to create a new account.
 */
public class LoginAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(LoginAction.class);

  private static final long serialVersionUID = -863287752175768744L;

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
    if (StringUtils.isBlank(adminEmail)) {
      List<User> users = userManager.list(User.Role.Admin);

      if (!users.isEmpty()) {
        adminEmail = users.get(0).getEmail();
      } else {
        LOG.error("Failed to load the default admin email");
      }
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
          User authUser = userManager.authenticate(email.trim(), password.trim());
          if (authUser == null) {
            addActionError(getText("admin.user.wrong.email.password.combination"));
            LOG.info("User {} failed to log in", email);
          } else {
            LOG.info("User {} logged in successfully", email);
            authUser.setLastLoginToNow();
            userManager.save();
            session.put(Constants.SESSION_USER, authUser);

            int sessionTimeout = cfg.getSessionTimeout();
            LOG.debug("Setting session timeout to {} seconds", sessionTimeout);
            req.getSession().setMaxInactiveInterval(sessionTimeout);

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
    redirectUrl = getBase() + "/";
    // if we have a request refer back to the originally requested page
    String referer = (String) session.get(Constants.SESSION_REFERER);
    LOG.debug("Session's referer: {}", referer);

    if (StringUtils.isNotEmpty(referer) && !(referer.endsWith("login.do") || referer.endsWith("login"))) {
      redirectUrl = getBase() + referer;
    }

    // remove referer from session
    session.remove(Constants.SESSION_REFERER);

    LOG.info("Redirecting to {}", redirectUrl);
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
