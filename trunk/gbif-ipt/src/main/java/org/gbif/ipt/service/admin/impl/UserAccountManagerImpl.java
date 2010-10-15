/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.FileUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
 * reads user accounts from a simple XStream managed xml file
 * 
 * @author markus
 */

@Singleton
public class UserAccountManagerImpl extends BaseManager implements UserAccountManager {
  public static final String PERSISTENCE_FILE = "users.xml";
  private SortedMap<String, User> users = new TreeMap<String, User>();
  private boolean allowSimplifiedAdminLogin = true;
  private String onlyAdminEmail;
  private final XStream xstream = new XStream();
  private ResourceManager resourceManager;

  @Inject
  public UserAccountManagerImpl(AppConfig cfg, DataDir dataDir, ResourceManager resourceManager) {
    super(cfg, dataDir);
    this.resourceManager = resourceManager;
    defineXstreamMapping();
  }

  private User addUser(User user) {
    if (user != null) {
      if (user.getRole() == Role.Admin) {
        log.debug("Adding admin " + user.getEmail());
        if (allowSimplifiedAdminLogin) {
          if (onlyAdminEmail == null) {
            // first admin - keep its email address available for simplified login
            // without email address but keyword "admin"
            onlyAdminEmail = user.getEmail();
          } else {
            // at least 2 admins exist - disable simplified admin login
            onlyAdminEmail = null;
            allowSimplifiedAdminLogin = false;
          }
        }
      } else {
        log.debug("Adding user " + user.getEmail());
      }
      users.put(user.getEmail().toLowerCase(), user);
    }
    return user;
  }

  public User authenticate(String email, String password) {
    if (allowSimplifiedAdminLogin && email != null && email.equalsIgnoreCase("admin")) {
      // lookup the admins email address#
      email = onlyAdminEmail;
    }
    User agent = get(email);
    if (agent != null && agent.getPassword() != null && agent.getPassword().equals(password)) {
      return agent;
    }
    return null;
  }

  public void create(User user) throws AlreadyExistingException, IOException {
    if (user != null) {
      if (get(user.getEmail()) != null) {
        throw new AlreadyExistingException();
      }
      addUser(user);
      save();
    }
  }

  private void defineXstreamMapping() {
    xstream.alias("user", User.class);
    xstream.useAttributeFor(User.class, "email");
    xstream.useAttributeFor(User.class, "password");
    xstream.useAttributeFor(User.class, "firstname");
    xstream.useAttributeFor(User.class, "lastname");
    xstream.useAttributeFor(User.class, "role");
    xstream.useAttributeFor(User.class, "lastLogin");
  }

  public User delete(String email) throws DeletionNotAllowedException {
    if (email != null) {
      User remUser = users.get(email.toLowerCase());
      // if admin, some other admin still existing?
      if (remUser.getRole() == Role.Admin) {
        for (User u : users.values()) {
          if (u.getRole() == Role.Admin) {
            log.warn("Last admin cannot be deleted");
            throw new DeletionNotAllowedException(Reason.LAST_ADMIN);
          }
        }
      }

      // if manager or admin, last manager of a resource?
      boolean isResourceCreator = false;
      if (remUser.hasManagerRights()) {
        for (Resource r : resourceManager.list(remUser)) {
          if (r.getCreator().equals(remUser)) {
            isResourceCreator = true;
          }
          Set<User> managers = new HashSet<User>(r.getManagers());
          managers.add(r.getCreator());
          if (managers.size() == 1) {
            String msg = "Last manager for resource " + r.getShortname() + " cannot be deleted";
            log.warn(msg);
            throw new DeletionNotAllowedException(Reason.LAST_RESOURCE_MANAGER, msg);
          }
        }
      }

      // finally remove user from internal hash or update role if its a resource creator
      if (isResourceCreator) {
        remUser.setRole(Role.User);
        log.warn("Creator of resources cannot be deleted. Changed role to a simple user instead");
      } else {
        users.remove(email.toLowerCase());
        // remove from resource managers
        for (Resource r : resourceManager.list(remUser)) {
          r.getManagers().remove(remUser);
        }
      }
      // update resource creator
      return remUser;
    }

    return null;
  }

  public User get(String email) {
    if (email == null) {
      return null;
    }
    return users.get(email.toLowerCase());
  }

  public List<User> list() {
    return new ArrayList<User>(users.values());
  }

  public List<User> list(Role role) {
    List<User> matchingUsers = new ArrayList<User>();
    for (User u : users.values()) {
      if (u.getRole() == role) {
        matchingUsers.add(u);
      }
    }
    return matchingUsers;
  }

  public void load() throws InvalidConfigException {
    Reader userReader;
    ObjectInputStream in = null;
    try {
      userReader = FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE));
      in = xstream.createObjectInputStream(userReader);
      users.clear();
      while (true) {
        try {
          User u = (User) in.readObject();
          addUser(u);
        } catch (EOFException e) {
          // end of file, expected exception!
          break;
        } catch (ClassNotFoundException e) {
          log.error(e.getMessage(), e);
        }
      }
    } catch (FileNotFoundException e) {
      log.warn("User accounts not existing, " + PERSISTENCE_FILE
          + " file missing  (This is normal when first setting up a new datadir)");
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new InvalidConfigException(TYPE.USER_CONFIG, "Couldnt read user accounts: " + e.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public void save() throws IOException {
    log.debug("Saving all " + users.size() + " user accounts...");
    Writer userWriter = FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE));
    ObjectOutputStream out = xstream.createObjectOutputStream(userWriter, "users");
    for (Entry<String, User> entry : users.entrySet()) {
      out.writeObject(entry.getValue());
    }
    out.close();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.UserAccountManager#save(org.gbif.ipt.model.User)
   */
  public void save(User user) throws IOException {
    addUser(user);
    save();
  }
}
