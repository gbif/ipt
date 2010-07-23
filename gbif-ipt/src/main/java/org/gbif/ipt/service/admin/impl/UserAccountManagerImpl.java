/**
 * 
 */
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.utils.FileUtils;

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
import java.util.List;
import java.util.Map.Entry;
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

  public UserAccountManagerImpl() {
    super();
    defineXstreamMapping();
  }

  public void add(User user) {
    if (user != null) {
      addUser(user);
    }
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

  private void defineXstreamMapping() {
    xstream.alias("user", User.class);
  }

  public User delete(String email) throws DeletionNotAllowedException {
    if (email != null) {
      User remUser = users.remove(email.toLowerCase());
      // TODO: check if a resource is linked to this user and potentially throw DeletionNotAllowedException()

      // if admin, some other admin still existing?
      if (remUser.getRole() == Role.Admin) {
        boolean lastAdmin = true;
        for (User u : users.values()) {
          if (u.getRole() == Role.Admin) {
            lastAdmin = false;
            break;
          }
        }
        if (lastAdmin) {
          log.warn("Last admin cannot be deleted");
          addUser(remUser);
          throw new DeletionNotAllowedException(Reason.LAST_ADMIN);
        }
      }
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
      log.debug(e);
      throw new InvalidConfigException(TYPE.USER_CONFIG, "Couldnt read user accounts: " + e.getMessage());
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
}
