/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockResourceManager;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.PBEEncrypt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserAccountManagerImplTest {

  private ResourceManager mockedResourceManager = MockResourceManager.buildMock();
  private static File userFile;
  private User admin, manager, publisher, user;
  private PBEEncrypt encrypt;

  {
    try {
      encrypt = new PBEEncrypt(
          "Carla Maria Luise",
          new byte[]{0x00, 0x05, 0x02, 0x05, 0x04, 0x25, 0x06, 0x17},
          9);
    } catch (PBEEncrypt.EncryptionException e) {
      encrypt = null;
    }
  }

  @BeforeAll
  public static void initialiseOnce() {
    userFile =
        new File(System.getProperty("java.io.tmpdir") + File.separatorChar + UserAccountManagerImpl.PERSISTENCE_FILE);
  }

  @AfterEach
  public void deleteFile() {
    if (userFile.exists()) {
      userFile.delete();
    }
  }

  /**
   * @return a new UserAccountManager instance.
   */
  private UserAccountManager getUserAccountManager() {
    AppConfig mockedCfg = MockAppConfig.buildMock();
    DataDir mockedDataDir = MockDataDir.buildMock();
    return new UserAccountManagerImpl(mockedCfg, mockedDataDir, mockedResourceManager, encrypt);
  }

  @BeforeEach
  public void initialise() {

    // Admin user
    this.admin = new User();
    this.admin.setEmail("admin@ipt.gbif.org");
    this.admin.setRole(Role.Admin);
    this.admin.setFirstname("Hector");
    this.admin.setLastname("Tobon");
    this.admin.setPassword("admin");

    // Manager user
    this.manager = new User();
    this.manager.setEmail("manager@ipt.gbif.org");
    this.manager.setRole(Role.Manager);
    this.manager.setFirstname("Kyle");
    this.manager.setLastname("Braak");
    this.manager.setPassword("manager");

    // Publisher user
    this.publisher = new User();
    this.publisher.setEmail("publisher@ipt.gbif.org");
    this.publisher.setRole(Role.Publisher);
    this.publisher.setFirstname("Burke");
    this.publisher.setLastname("Chih-Jen");
    this.publisher.setPassword("publisher");

    // Basic user
    this.user = new User();
    this.user.setEmail("user@ipt.gbif.org");
    this.user.setRole(Role.Publisher);
    this.user.setFirstname("José");
    this.user.setLastname("Cuadra");
    this.user.setPassword("user");

  }

  /**
   * Test user authenticate.
   */
  @Test
  public void testAuthenticate() throws Exception {
    // create new instance.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);


    assertEquals(admin, userManager.authenticate("admin@ipt.gbif.org", "admin"));
    assertEquals(manager, userManager.authenticate("manager@ipt.gbif.org", "manager"));
    assertEquals(publisher, userManager.authenticate("publisher@ipt.gbif.org", "publisher"));
    assertEquals(user, userManager.authenticate("user@ipt.gbif.org", "user"));
    assertNull(userManager.authenticate("invalid-user@ipt.gbif.org", "anyPassword"));
  }

  /**
   * Test user creation.
   */
  @Test
  public void testCreate() throws Exception {
    // create new instance.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    // validate if the users were added to the users list.
    assertEquals(admin, userManager.get("admin@ipt.gbif.org"));
    assertEquals(manager, userManager.get("manager@ipt.gbif.org"));
    assertEquals(publisher, userManager.get("publisher@ipt.gbif.org"));
    assertEquals(user, userManager.get("user@ipt.gbif.org"));

    // add an already existent user.
    try {
      userManager.create(admin);
      fail("An exception should be thrown if user try to add an already added user");
    } catch (AlreadyExistingException e) {
      assertTrue(true);
    }

    assertEquals(4, userManager.list().size());

  }

  /**
   * Test user deletion with various cases.
   */
  @Test
  public void testDelete() throws Exception {
    // create a new instance only to save into the file.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    // Case #1. Ensure user with no linkages to any resource can be deleted
    User deletedUser = userManager.delete("user@ipt.gbif.org");

    // test if user was deleted
    assertEquals(3, userManager.list().size());
    assertEquals(user, deletedUser);

    // Case #2. Ensure at least one Admin always remains
    try {
      userManager.delete("admin@ipt.gbif.org");
      fail("There must always be at least one user Admin");
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.LAST_ADMIN, e.getReason());
    }

    // Case #3. Ensure the last manager of a resource cannot be deleted
    List<Resource> resources = new ArrayList<>();

    Resource res1 = new Resource();
    res1.setShortname("res1");
    res1.setCreator(manager);
    Set<User> managers1 = new HashSet<>();
    managers1.add(manager);
    res1.setManagers(managers1);
    resources.add(res1);
    when(mockedResourceManager.list(any(User.class))).thenReturn(resources);
    try {
      userManager.delete("manager@ipt.gbif.org");
      fail("Last manager for resource res1 cannot be deleted");
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.LAST_RESOURCE_MANAGER, e.getReason());
    }

    // Case #4. Ensure user CAN be deleted when they are not last manager of a resource
    res1.setCreator(publisher);
    deletedUser = userManager.delete("manager@ipt.gbif.org");
    assertEquals(2, userManager.list().size());
    assertEquals(manager, deletedUser);

    // Case #5. Ensure the creator of resources cannot be deleted
    userManager.create(manager);
    assertEquals(3, userManager.list().size());
    managers1 = new HashSet<>();
    managers1.add(manager);
    res1.setManagers(managers1);
    when(mockedResourceManager.list()).thenReturn(resources);
    try {
      userManager.delete("publisher@ipt.gbif.org");
      fail("Creator for resource res1 cannot be deleted");
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.IS_RESOURCE_CREATOR, e.getReason());
    }

    // Case #6. Ensure admin CAN be deleted when they are not last admin in IPT
    User admin2 = new User();
    admin2.setEmail("admin2@ipt.gbif.org");
    admin2.setRole(Role.Admin);
    admin2.setFirstname("Second");
    admin2.setLastname("Admin");
    admin2.setPassword("admin2");
    userManager.create(admin2);
    assertEquals(4, userManager.list().size());
    deletedUser = userManager.delete("admin@ipt.gbif.org");
    assertEquals(3, userManager.list().size());
    assertEquals(admin, deletedUser);

    // Case #7. Ensure admin cannot be deleted when they created a resource
    userManager.create(admin);
    assertEquals(4, userManager.list().size());
    res1.setCreator(admin);
    try {
      userManager.delete("admin@ipt.gbif.org");
      fail("Secondary admin cannot be deleted, because it is creator for resource res1");
    } catch (DeletionNotAllowedException e) {
      assertEquals(DeletionNotAllowedException.Reason.IS_RESOURCE_CREATOR, e.getReason());
    }
  }


  /**
   * Test get user.
   */
  @Test
  public void testGet() throws Exception {
    // create new instance.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    assertEquals(admin, userManager.get("admin@ipt.gbif.org"));
    assertEquals(publisher, userManager.get("publisher@ipt.gbif.org"));
    assertNull(userManager.get(null));
  }

  /**
   * Read from user.xml file.
   */
  @Test
  public void testLoad() throws Exception {
    // create a new instance only to save into the file.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    // validate if the user.xml file was created
    assertTrue(userFile.exists());

    // create another userManager instance to test read file method.
    userManager = getUserAccountManager();

    // the new userManager should have not any user previously saved.
    assertEquals(0, userManager.list().size());

    // load information from user.xml
    userManager.load();

    // the 4 users previously added should be in the users list.
    assertEquals(4, userManager.list().size());

    // validate if the users from file were included in the list.
    assertEquals(admin, userManager.get("admin@ipt.gbif.org"));
    assertEquals(manager, userManager.get("manager@ipt.gbif.org"));
    assertEquals(publisher, userManager.get("publisher@ipt.gbif.org"));
    assertEquals(user, userManager.get("user@ipt.gbif.org"));
  }
}
