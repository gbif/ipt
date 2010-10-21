/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.validation.UserValidator;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;

/**
 * The Action responsible for all user input relating to the user accounts in the IPT
 * 
 * @author tim
 */
public class UserAccountsAction extends POSTAction {
  private static final long serialVersionUID = 8892204508303815998L;
  @Inject
  private UserAccountManager userManager;
  private UserValidator validator = new UserValidator();

  private User user;
  private List<User> users;

  @Override
  public String delete() {
    if (getCurrentUser().getEmail().equalsIgnoreCase(id)) {
      // cant remove logged in user
      addActionError(getText("admin.user.deleted.current"));
    } else {
      try {
        User removedUser = userManager.delete(id);
        if (removedUser == null) {
          return NOT_FOUND;
        }
        userManager.save();
        addActionMessage(getText("admin.user.deleted"));
        return SUCCESS;
      } catch (DeletionNotAllowedException e) {
        if (Reason.LAST_ADMIN == e.getReason()) {
          addActionError(getText("admin.user.deleted.lastadmin"));
        } else if (Reason.LAST_RESOURCE_MANAGER == e.getReason()) {
          addActionError(getText("admin.user.deleted.lastmanager"));
        } else {
          addActionError(getText("admin.user.deleted.error"));
        }
      } catch (IOException e) {
        addActionError(getText("admin.user.cantSave") + ": " + e.getMessage());
      }
    }
    return INPUT;
  }

  // Getters / Setters follow
  public User getUser() {
    return user;
  }

  public List<User> getUsers() {
    return users;
  }

  public String list() {
    users = userManager.list();
    return SUCCESS;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (id != null) {
      // modify copy of existing user - otherwise we even change the proper instances when canceling the request or
      // submitting non validating data
      user = userManager.get(id);
    }
    // if no id was submitted we wanted to create a new account
    // if an invalid email was entered, it gets stored in the id field and obviously userManager above cant find a
    // matching user.
    // in that case again provide a new, empty user instance
    if (user != null) {
      user = (User) user.clone();
    } else {
      // reset id
      id = null;
      // create new user
      user = new User();
    }
  }

  @Override
  public String save() {
    try {
      if (id == null) {
        userManager.create(user);
        addActionMessage(getText("admin.user.added"));
      } else {
        userManager.save(user);
        addActionMessage(getText("admin.user.changed"));
      }
      return SUCCESS;
    } catch (IOException e) {
      log.error("The user change couldnt be saved: " + e.getMessage(), e);
      addActionError(getText("admin.user.saveError"));
      addActionError(e.getMessage());
      return INPUT;
    } catch (AlreadyExistingException e) {
      addActionError(getText("admin.user.exists", new String[]{id,}));
      return INPUT;
    }
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  @Override
  public void validateHttpPostOnly() {
    // only validate on form submit ignoring list views
    // && users == null
    validator.validate(this, user);
  }
}
