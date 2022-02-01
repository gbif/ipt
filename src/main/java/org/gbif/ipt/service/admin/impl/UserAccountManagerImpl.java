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
import org.gbif.ipt.utils.PBEEncrypt;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Reads user accounts from a simple XStream managed xml file.
 */
@Singleton
public class UserAccountManagerImpl extends BaseManager implements UserAccountManager {

  public static final String PERSISTENCE_FILE = "users.xml";
  private final Map<String, User> users = new LinkedHashMap<>();
  private boolean allowSimplifiedAdminLogin = true;
  private String onlyAdminEmail;
  private final XStream xstream = new XStream();
  private final ResourceManager resourceManager;
  private final PBEEncrypt encrypter;

  private User setupUser;

  @Inject
  public UserAccountManagerImpl(AppConfig cfg, DataDir dataDir, ResourceManager resourceManager, PBEEncrypt encrypter) {
    super(cfg, dataDir);
    this.resourceManager = resourceManager;
    this.encrypter = encrypter;
    defineXstreamMapping();
  }

  private User addUser(User user) {
    if (user != null) {
      if (user.getRole() == Role.Admin) {
        LOG.debug("Adding admin " + user.getEmail());
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
        LOG.debug("Adding user " + user.getEmail());
      }
      users.put(user.getEmail().toLowerCase(), user);
    }
    return user;
  }

  @Override
  public User authenticate(String email, String password) {
    if (allowSimplifiedAdminLogin && email != null && email.equalsIgnoreCase("admin")) {
      // lookup the admins email address#
      email = onlyAdminEmail;
    }
    User agent = get(email);
    if (agent != null && agent.getPassword() != null
        && BCrypt.verifyer().verify(password.toCharArray(), agent.getPassword()).verified) {
      return agent;
    }
    return null;
  }

  @Override
  public void create(User user) throws AlreadyExistingException, IOException {
    if (user != null) {
      if (get(user.getEmail()) != null) {
        throw new AlreadyExistingException();
      }
      // hash password before creation
      user.setPassword(BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray()));
      addUser(user);
      save();
    }
  }

  private void defineXstreamMapping() {
    xstream.addPermission(AnyTypePermission.ANY);
    xstream.alias("user", User.class);
    xstream.useAttributeFor(User.class, "email");
    xstream.useAttributeFor(User.class, "password");
    xstream.useAttributeFor(User.class, "firstname");
    xstream.useAttributeFor(User.class, "lastname");
    xstream.useAttributeFor(User.class, "role");
    xstream.useAttributeFor(User.class, "lastLogin");
  }

  @Override
  public User delete(String email) throws DeletionNotAllowedException, IOException {
    if (email != null) {
      User remUser = get(email);
      if (remUser != null) {

        // when deleting an admin, ensure another admin still exists
        if (remUser.getRole() == Role.Admin) {
          boolean lastAdmin = true;
          for (User u : users.values()) {
            if (u.getRole() == Role.Admin && !u.equals(remUser)) {
              lastAdmin = false;
              break;
            }
          }
          if (lastAdmin) {
            LOG.warn("Last admin cannot be deleted");
            throw new DeletionNotAllowedException(Reason.LAST_ADMIN);
          }
        }

        Set<String> resourcesCreatedByUser = new HashSet<>();
        for (Resource r : resourceManager.list()) {
          User creator = get(r.getCreator().getEmail());
          if (creator != null && creator.equals(remUser)) {
            resourcesCreatedByUser.add(r.getShortname());
          }
        }

        Set<String> resourcesManagedOnlyByUser = new HashSet<>();
        for (Resource r : resourceManager.list(remUser)) {
          Set<User> managers = new HashSet<>();
          // add creator to list of managers, but only if creator has manager rights!
          User creator = get(r.getCreator().getEmail());
          if (creator != null && creator.hasManagerRights()) {
            managers.add(creator);
          }
          for (User m : r.getManagers()) {
            User manager = get(m.getEmail());
            if (manager != null && !managers.contains(manager)) {
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
        } else if (remove(email)) {
          // and remove user from each resource's list of managers
          for (Resource r : resourceManager.list(remUser)) {
            r.getManagers().remove(remUser);
            resourceManager.save(r);
          }
          save(); // persist changes to users.xml
          return remUser;
        }
      }
    }
    return null;
  }

  /**
   * Retrieve user from internal hash.
   *
   * @param email email of user account to retrieve
   *
   * @return User if found, null otherwise
   */
  @Override
  public User get(String email) {
    if (email != null && users.containsKey(email.toLowerCase())) {
      return users.get(email.toLowerCase());
    }
    return null;
  }

  /**
   * Remove user from internal hash.
   *
   * @param email email of user account to remove
   *
   * @return true if user was removed, false otherwise
   */
  public boolean remove(String email) {
    if (email != null && users.containsKey(email.toLowerCase())) {
      return users.remove(email.toLowerCase()) != null;
    }
    return false;
  }

  @Override
  public String getDefaultAdminEmail() {
    return cfg.getAdminEmail();
  }

  @Override
  public User getSetupUser() {
    return setupUser;
  }

  @Override
  public List<User> list() {
    ArrayList<User> userList = new ArrayList<>(users.values());
    userList.sort(Comparator.comparing(o -> (o.getFirstname() + " " + o.getLastname())));
    return userList;
  }

  @Override
  public List<User> list(Role role) {
    List<User> matchingUsers = new ArrayList<>();
    for (User u : users.values()) {
      if (u.getRole() == role) {
        matchingUsers.add(u);
      }
    }
    return matchingUsers;
  }

  @Override
  public void load() throws InvalidConfigException {
    try (ObjectInputStream in = xstream.createObjectInputStream(
        FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE)))) {
      users.clear();
      while (true) {
        try {
          User u = (User) in.readObject();
          addUser(u);
        } catch (EOFException e) {
          // end of file, expected exception!
          break;
        } catch (ClassNotFoundException e) {
          LOG.error(e.getMessage(), e);
        }
      }

      // first we have to check if there are users with old-fashioned passwords
      boolean isOldPasswordsPresent = users.values()
          .stream()
          .map(User::getPassword)
          .anyMatch(pass -> !StringUtils.startsWith(pass, "$2a$"));

      // if so - update all passwords
      if (isOldPasswordsPresent) {
        LOG.info("There are old-fashioned encrypted passwords, start hashing them");
        for (User user : users.values()) {
          String pass = user.getPassword();
          if (pass != null) {
            String decrypted;
            try {
              decrypted = encrypter.decrypt(pass);
              String hash = BCrypt.withDefaults().hashToString(12, decrypted.toCharArray());
              user.setPassword(hash);
            } catch (PBEEncrypt.EncryptionException e) {
              LOG.error("Cannot decrypt password for the user [{}]", user.getEmail(), e);
            }
          }
        }

        save();
      }

    } catch (FileNotFoundException e) {
      LOG.warn("User accounts not existing, " + PERSISTENCE_FILE
        + " file missing  (This is normal when first setting up a new datadir)");
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new InvalidConfigException(TYPE.USER_CONFIG, "Couldnt read user accounts: " + e.getMessage());
    }
  }

  @Override
  public synchronized void save() throws IOException {
    LOG.debug("Saving all " + users.size() + " user accounts...");
    try (Writer userWriter = FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE));
         ObjectOutputStream out = xstream.createObjectOutputStream(userWriter, "users")) {
      for (Entry<String, User> entry : users.entrySet()) {
        out.writeObject(entry.getValue());
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.UserAccountManager#save(org.gbif.ipt.model.User)
   */
  @Override
  public void save(User user) throws IOException {
    addUser(user);
    save();
  }

  @Override
  public void setSetupUser(User setupUser) {
    this.setupUser = setupUser;
  }

}
