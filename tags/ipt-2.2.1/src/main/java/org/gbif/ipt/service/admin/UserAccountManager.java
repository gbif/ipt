package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.UserAccountManagerImpl;

import java.io.IOException;
import java.util.List;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the User accounts within the IPT.
 */
@ImplementedBy(UserAccountManagerImpl.class)
public interface UserAccountManager {

  /**
   * Authenticate a user checking his password & email.
   *
   * @param email    the unique email address that works as the identifier for a user
   * @param password the users password in clear text
   *
   * @return the user instance if exists or null if user doesnt exist or password doesnt match
   */
  User authenticate(String email, String password);

  /**
   * Adds a new user account and persists the change.
   */
  void create(User user) throws AlreadyExistingException, IOException;

  /**
   * Removes the specified user from the in memory list of users.
   * See save() to persist this change to files.
   * Managers linked to resources, e.g. resource creators, need to delete the resource or assign another manager first
   * before their account can be removed.
   *
   * @return user that has been removed or null if not existing
   *
   * @throws DeletionNotAllowedException if its the last admin or a manager linked to a resource
   */
  User delete(String email) throws DeletionNotAllowedException;

  /**
   * Get a user object by its unique, case insensitive email.
   * The returned user instance is the master in memory copy - any change will globally modify the instance
   * but not persist any change to disk. Call save() to do so.
   *
   * @return the user object or null if no user with the given email is known
   */
  User get(String email);

  /**
   * User objects are references to the persistent instances, so changes have global impact.
   *
   * @return list of all users.
   */
  List<User> list();

  /**
   * User objects are references to the persistent instances, so changes have global impact.
   *
   * @return list of all users with the given role.
   */
  List<User> list(Role role);

  /**
   * Loads all user accounts from file into the manager.
   */
  void load() throws InvalidConfigException;

  /**
   * Saves all user accounts from manager to file.
   * Needs to be manually called if user properties have been modified or if users have been added or removed.
   */
  void save() throws IOException;

  /**
   * Updates the internal cache with this user and persists the change.
   */
  void save(User user) throws IOException;

  User getSetupUser();

  void setSetupUser(User setupLogin);
}
