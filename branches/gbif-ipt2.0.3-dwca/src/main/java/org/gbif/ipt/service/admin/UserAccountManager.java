/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.UserAccountManagerImpl;

import com.google.inject.ImplementedBy;

import java.io.IOException;
import java.util.List;

/**
 * This interface details ALL methods associated with the User accounts within the IPT.
 * 
 * @author tim
 */
@ImplementedBy(UserAccountManagerImpl.class)
public interface UserAccountManager {

  /**
   * Authenticate a user checking his password & email
   * 
   * @param email the unique email address that works as the identifier for a user
   * @param password the users password in clear text
   * @return the user instance if exists or null if user doesnt exist or password doesnt match
   */
  public User authenticate(String email, String password);

  /**
   * Adds a new user account and persists the change.
   * 
   * @throws AlreadyExistingException
   */
  public void create(User user) throws AlreadyExistingException, IOException;

  /**
   * Removes the specified user from the in memory list of users.
   * See save() to persist this change to files.
   * Managers linked to resources, e.g. resource creators, need to delete the resource or assign another manager first
   * before their account can be removed.
   * 
   * @return user that has been removed or null if not existing
   * @throws DeletionNotAllowedException if its the last admin or a manager linked to a resource
   */
  public User delete(String email) throws DeletionNotAllowedException;

  /**
   * Get a user object by its unique, case insensitive email.
   * The returned user instance is the master in memory copy - any change will globally modify the instance
   * but not persist any change to disk. Call save() to do so.
   * 
   * @param email
   * @return the user object or null if no user with the given email is known
   */
  public User get(String email);

  /**
   * User objects are references to the persistent instances, so changes have global impact
   * 
   * @return list of all users.
   */
  public List<User> list();

  /**
   * User objects are references to the persistent instances, so changes have global impact
   * 
   * @return list of all users with the given role.
   */
  public List<User> list(Role role);

  /**
   * Loads all user accounts from file into the manager
   */
  public void load() throws InvalidConfigException;

  /**
   * Saves all user accounts from manager to file.
   * Needs to be manually called if user properties have been modified or if users have been added or removed.
   * 
   * @throws IOException
   */
  public void save() throws IOException;

  /**
   * Updates the internal cache with this user and persists the change
   * 
   * @param user
   * @throws IOException
   */
  public void save(User user) throws IOException;
  
  public User getSetupUser();

  public void setSetupUser(User setupLogin);
}
