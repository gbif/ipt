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
import org.gbif.ipt.validation.UserSupport;

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
  private UserSupport userValidation = new UserSupport();

  private User user;
  private List<User> users;

  @Override
  public String delete() {
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
      }
    } catch (IOException e) {
      addActionError("cant save user file: " + e.getMessage());
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
      // modify existing user
      user = userManager.get(id);
    }
    // if no id was submitted we wanted to create a new account
    // if an invalid email was entered, it gets stored in the id field and obviously userManager above cant find a
    // matching user.
    // in that case again provide a new, empty user instance
    if (user == null) {
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
        userManager.add(user);
        addActionMessage(getText("admin.user.added"));
      } else {
        addActionMessage(getText("admin.user.changed"));
      }
      userManager.save();
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
  public void validate() {
    // only validate on form submit ignoring list views
    // && users == null
    if (isHttpPost()) {
      userValidation.validate(this, user);
    }
  }
}
