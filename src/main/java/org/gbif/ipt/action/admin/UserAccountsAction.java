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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.UserValidator;

import java.io.IOException;
import java.io.Serial;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import at.favre.lib.crypto.bcrypt.BCrypt;

import lombok.Getter;

/**
 * The Action responsible for all user input relating to the user accounts in the IPT.
 */
public class UserAccountsAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = 8892204508303815998L;

  private static final Logger LOG = LogManager.getLogger(UserAccountsAction.class);

  private static final int PASSWORD_LENGTH = 8;
  private static final String PASSWORD_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  private static final RandomStringGenerator PASSWORD_GENERATOR =
      new RandomStringGenerator.Builder()
          .selectFrom(PASSWORD_ALLOWED_CHARS.toCharArray())
          .build();
  private static final String EMAIL_NEW_ACCOUNT = "<a href=\"mailto:%s" +
      "?subject=IPT account" +
      "&amp;body=Dear %s," +
      "%%0d%%0dWe would like to inform you that we have created an IPT account for you." +
      "%%0d%%0dAccount information:" +
      "%%0d%%0dIPT: %s" +
      "%%0dEmail: %s" +
      "%%0dPassword: %s" +
      "%%0dRole: %s" +
      "%%0d%%0dThank you for your attention.\">" +
      "Click here" +
      "</a> " +
      "to share the credentials with the user";
  private static final String EMAIL_PASSWORD_CHANGE = "<a href=\"mailto:%s" +
      "?subject=IPT password change" +
      "&amp;body=Dear %s," +
      "%%0d%%0dWe would like to inform you that your IPT account's password has been successfully changed." +
      "%%0d%%0dAccount information:" +
      "%%0d%%0dIPT: %s" +
      "%%0dEmail: %s" +
      "%%0dPassword: %s" +
      "%%0d%%0dThank you for your attention.\">" +
      "Click here" +
      "</a> " +
      "to share the new password with the user";

  private final UserAccountManager userManager;
  private final ResourceManager resourceManager;
  private final UserValidator validator = new UserValidator();

  // Getters / Setters follow
  private User user;
  @Getter
  private String password2;
  private boolean resetPassword;
  private boolean newUser;
  @Getter
  private List<User> users;

  @Inject
  public UserAccountsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      UserAccountManager userManager,
      ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.userManager = userManager;
    this.resourceManager = resourceManager;
  }

  private void checkUserResourcesBeforeDeletion(String email) throws DeletionNotAllowedException {
    User remUser = userManager.get(email);

    Set<String> resourcesCreatedByUser = new HashSet<>();
    for (Resource r : resourceManager.list()) {
      User creator = userManager.get(r.getCreator().getEmail());
      if (creator != null && creator.equals(remUser)) {
        resourcesCreatedByUser.add(r.getShortname());
      }
    }

    Set<String> resourcesManagedOnlyByUser = new HashSet<>();
    for (Resource r : resourceManager.list(remUser)) {
      Set<User> managers = new HashSet<>();
      // add creator to the list of managers, but only if creator has manager rights!
      User creator = userManager.get(r.getCreator().getEmail());
      if (creator != null && creator.hasManagerRights()) {
        managers.add(creator);
      }
      for (User m : r.getManagers()) {
        User manager = userManager.get(m.getEmail());
        if (manager != null) {
          managers.add(manager);
        }
      }
      // lastly, exclude user to be deleted, then check if at least one user with manager rights remains for resource
      managers.remove(remUser);
      if (managers.isEmpty()) {
        resourcesManagedOnlyByUser.add(r.getShortname());
      }
    }

    if (!resourcesManagedOnlyByUser.isEmpty()) {
      // Check #1, is user the only manager that exists for or more resources? If yes, prevent deletion!
      throw new DeletionNotAllowedException(Reason.LAST_RESOURCE_MANAGER, resourcesManagedOnlyByUser.toString());
    } else if (!resourcesCreatedByUser.isEmpty()) {
      // Check #2, is user the creator of one or more resources? If yes, prevent deletion!
      throw new DeletionNotAllowedException(Reason.IS_RESOURCE_CREATOR, resourcesCreatedByUser.toString());
    }
  }

  private void removeUserFromResourceManagers(User user) {
    for (Resource r : resourceManager.list(user)) {
      r.getManagers().remove(user);
      resourceManager.save(r);
    }
  }

  @Override
  public String delete() {
    User currentUser = getCurrentUser();
    if (currentUser.getEmail().equalsIgnoreCase(id)) {
      // can't remove logged in user
      LOG.error("Cannot remove an account {} when logged in to it", id);
      addActionError(getText("admin.user.deleted.current"));
    } else {
      try {
        User userToBeDeleted = userManager.get(id);

        if (userToBeDeleted == null) {
          return NOT_FOUND;
        } else {
          checkUserResourcesBeforeDeletion(id);
          removeUserFromResourceManagers(userToBeDeleted);
          boolean result = userManager.delete(id);

          if (result) {
            userManager.save();
            LOG.info("User {} has been successfully deleted by {}", id, currentUser.getEmail());
            addActionMessage(getText("admin.user.deleted"));

            return SUCCESS;
          } else {
            LOG.info("Failed to delete user {}", id);
            return INPUT;
          }
        }
      } catch (DeletionNotAllowedException e) {
        if (Reason.LAST_ADMIN == e.getReason()) {
          addActionError(getText("admin.user.deleted.lastadmin"));
        } else if (Reason.LAST_RESOURCE_MANAGER == e.getReason()) {
          addActionError(getText("admin.user.deleted.lastmanager", new String[]{e.getMessage()}));
        } else if (Reason.IS_RESOURCE_CREATOR == e.getReason()) {
          addActionError(getText("admin.user.deleted.error.creator", new String[]{e.getMessage()}));
        } else {
          addActionError(getText("admin.user.deleted.error"));
        }
      } catch (IOException e) {
        addActionError(getText("admin.user.cantSave", new String[]{e.getMessage()}));
      }
    }
    return INPUT;
  }

  public String getNewUser() {
    return newUser ? "yes" : "no";
  }

  public String list() {
    users = userManager.list();
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (id == null) {
      newUser = true;
    } else {
      // modify copy of existing user - otherwise we even change the proper instances when canceling the request or
      // submitting non-validating data
      user = userManager.get(id);
    }

    // not found if id is present but user is null (invalid email was entered?)
    if (id != null && user == null) {
      notFound = true;
    }

    // if no id is submitted, we want to create a new account
    if (user == null) {
      // reset id
      id = null;
      // create new user
      user = new User();
      newUser = true;
    } else {
      try {
        user = (User) user.clone();
      } catch (CloneNotSupportedException e) {
        LOG.error("An exception occurred while retrieving user: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public String save() {
    User currentUser = getCurrentUser();

    try {
      if (id == null) {
        String passwordBeforeSaving = user.getPassword();
        userManager.create(user);

        String emailLink = String.format(EMAIL_NEW_ACCOUNT, user.getEmail(), user.getFirstname(), cfg.getBaseUrl(), user.getEmail(), passwordBeforeSaving, user.getRole());

        LOG.info("User {} has been created by {}", id, currentUser.getEmail());
        addActionMessage(getText("admin.user.added", new String[]{emailLink}));
      } else if (resetPassword) {
        String newPassword = PASSWORD_GENERATOR.generate(PASSWORD_LENGTH);
        String hash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        user.setPassword(hash);
        userManager.save(user);

        String emailLink = String.format(EMAIL_PASSWORD_CHANGE, user.getEmail(), user.getFirstname(), cfg.getBaseUrl(), user.getEmail(), newPassword);

        LOG.info("User {} password has been changed by {}", user.getEmail(), currentUser.getEmail());
        addActionMessage(getText("admin.user.passwordChanged", new String[]{user.getEmail(), newPassword, emailLink}));
      } else {
        User currentStateUser = userManager.get(user.getEmail());

        if (currentStateUser.getRole() != user.getRole()) {
          LOG.info("User {} role has been changed from {} to {} by {}",
              user.getEmail(), currentStateUser.getRole(), user.getRole(), currentUser.getEmail());
        }

        if (currentStateUser.getRole() == Role.Admin
            && user.getRole() != Role.Admin
            && userManager.list(Role.Admin).size() < 2) {
          LOG.error("Cannot change the role of the last administrator");
          addActionError(getText("admin.user.changed.current"));
          return INPUT;
        }

        if (user.getEmail().equals(getCurrentUser().getEmail())) {
          getCurrentUser().setRole(user.getRole());
        }

        userManager.save(user);
        if (getCurrentUser().getRole() != Role.Admin) {
          return HOME;
        }

        LOG.info("User {} has been successfully updated by {}", user.getEmail(), currentUser.getEmail());
        addActionMessage(getText("admin.user.changed"));
      }
      return SUCCESS;
    } catch (IOException e) {
      LOG.error("User change couldn't be saved: {}", e.getMessage(), e);
      addActionError(getText("admin.user.saveError"));
      addActionError(e.getMessage());
      return INPUT;
    } catch (AlreadyExistingException e) {
      LOG.error("User with the email address {} already exists", user.getEmail(), e);
      addActionError(getText("admin.user.exists", new String[]{user.getEmail()}));
      // resetting user
      user = new User();
      return INPUT;
    }
  }

  @StrutsParameter
  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  @StrutsParameter
  public void setResetPassword(String pass) {
    this.resetPassword = StringUtils.trimToNull(pass) != null;
  }

  @StrutsParameter(depth = 1)
  public User getUser() {
    return user;
  }

  @StrutsParameter
  public void setUsers(List<User> users) {
    this.users = users;
  }

  @Override
  public void validateHttpPostOnly() {
    // only validate on form submit ignoring list views
    // && users == null
    validator.validate(this, user);
    // check 2nd password
    if (newUser && StringUtils.trimToNull(user.getPassword()) != null && !user.getPassword().equals(password2)) {
      addFieldError("password2", getText("validation.password2.wrong"));
      password2 = null;
      user.setPassword(null);
    }
  }
}
