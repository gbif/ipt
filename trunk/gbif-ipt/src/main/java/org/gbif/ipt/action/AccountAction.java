package org.gbif.ipt.action;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.validation.UserSupport;

import com.google.inject.Inject;

public class AccountAction extends POSTAction {
  @Inject
  private UserAccountManager userManager;
  private UserSupport userValidation = new UserSupport();

  private String redirectUrl;
  private String email;
  private String password;
  private User user;

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public User getUser() {
    return user;
  }

  public String login() {
    if (email != null) {
      User user = userManager.authenticate(email, password);
      if (user != null) {
        log.info("User " + email + " logged in successfully");
        user.setLastLoginToNow();
        // agentService.update(user);
        session.put(Constants.SESSION_USER, user);
        // remember previous URL to redirect back to
        setRedirectUrl();
        return SUCCESS;
      } else {
        addFieldError("email", "The email - password combination does not exists");
        log.info("User " + email + " failed to log in with password " + password);
      }
    }
    return INPUT;
  }

  public String logout() {
    setRedirectUrl();
    session.clear();
    return SUCCESS;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (id != null) {
      // modify existing user
      user = userManager.get(id);
      if (user == null) {
        // set notFound flag to true so FormAction will return a NOT_FOUND 404 result name
        notFound = true;
      }
    }
  }

  public void setEmail(String email) {
    if (email != null) {
      this.email = email;
    }
  }

  public void setPassword(String password) {
    this.password = password;
  }

  private void setRedirectUrl() {
    redirectUrl = getBase() + "/";
    // if we have a request refer back to the originally requested page
    if (req != null) {
      String referer = req.getHeader("Referer");
      if (referer != null && referer.startsWith(cfg.getBaseURL()) && !(referer.endsWith("login"))) {
        redirectUrl = referer;
      }
    }
    log.info("Redirecting to " + redirectUrl);
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public void validate() {
    if (user != null) {
      userValidation.validate(this, user);
    }
  }

}
