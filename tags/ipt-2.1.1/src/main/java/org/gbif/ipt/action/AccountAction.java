package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.UserValidator;

import java.io.IOException;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class AccountAction extends POSTAction {

  // logging
  private static final Logger LOG = Logger.getLogger(AccountAction.class);

  private final UserAccountManager userManager;
  private final UserValidator userValidation = new UserValidator();

  private String redirectUrl;
  private String email;
  private String password;
  private String password2;
  private User user;
  // to show admin contact
  private User admin;
  private String lostPswdEmailSubject;
  private String lostPswdEmailBody;

  @Inject
  public AccountAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    UserAccountManager userManager) {
    super(textProvider, cfg, registrationManager);
    this.userManager = userManager;
  }

  @Override
  public String execute() throws Exception {
    // check if any user is logged in right now - otherwise redirect to login page
    if (user == null) {
      return LOGIN;
    }
    return super.execute();
  }

  public User getAdmin() {
    return admin;
  }

  public String getEmail() {
    return email;
  }

  public String getLostPswdEmailBody() {
    return lostPswdEmailBody;
  }

  public String getLostPswdEmailSubject() {
    return lostPswdEmailSubject;
  }

  public String getPassword() {
    return password;
  }

  public String getPassword2() {
    return password2;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public User getUser() {
    return user;
  }

  public String login() throws IOException {
    // login
    if (email != null) {
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
    }
    return INPUT;
  }

  public String logout() {
    redirectUrl = getBase() + "/";
    session.clear();
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    // populate admin user
    admin = userManager.list(Role.Admin).get(0);
    lostPswdEmailSubject = getText("login.forgottenpassword.mail.subject");
    lostPswdEmailBody = getTextWithDynamicArgs("login.forgottenpassword.mail.body", admin.getName(), "",
      cfg.getBaseUrl() + "/admin/users.do");
    if (getCurrentUser() != null) {
      // modify existing user in session
      user = getCurrentUser();
    }
  }

  @Override
  public String save() {
    try {
      // update passwords?
      if (password != null) {
        user.setPassword(password);
      }
      addActionMessage(getText("admin.user.changed"));
      userManager.save();
      return SUCCESS;
    } catch (IOException e) {
      addActionError(getText("admin.user.saveError"));
      addActionError(e.getMessage());
      return INPUT;
    }
  }

  public void setEmail(String email) {
    this.email = StringUtils.trimToNull(email);
    if (email != null) {
      lostPswdEmailBody = getTextWithDynamicArgs("login.forgottenpassword.mail.body", admin.getName(), this.email,
        cfg.getBaseUrl() + "/admin/user.do?id=" + this.email);
    }
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  private void setRedirectUrl() {
    redirectUrl = getBase() + "/";
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

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public void validateHttpPostOnly() {
    if (user != null) {
      userValidation.validate(this, user);
      // update passwords?
      if (password != null && !password.equals(password2)) {
        addFieldError("password2", getText("validation.password2.wrong"));
        password2 = null;
      }
    }
  }

}
