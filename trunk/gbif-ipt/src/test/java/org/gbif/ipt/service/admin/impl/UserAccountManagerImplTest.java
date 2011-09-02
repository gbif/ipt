/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockResourceManager;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * @author hftobon
 */
public class UserAccountManagerImplTest {

  private PasswordConverter mockedPasswordConverter = mock(PasswordConverter.class);
  private static File userFile;
  private User admin, manager, publisher, user;

  @BeforeClass
  public static void initialiseOnce() {
    userFile =
      new File(System.getProperty("java.io.tmpdir") + File.separatorChar + UserAccountManagerImpl.PERSISTENCE_FILE);
  }

  @After
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
    ResourceManager mockedResourceManager = MockResourceManager.buildMock();
    return new UserAccountManagerImpl(mockedCfg, mockedDataDir, mockedResourceManager, mockedPasswordConverter);
  }

  @Before
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
   * Test user creation
   * 
   * @throws IOException
   * @throws AlreadyExistingException
   */
  @Test
  public void testCreate() throws AlreadyExistingException, IOException {
    // create new instance.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    // validate if the users were added to the users list.
    Assert.assertEquals(admin, userManager.get("admin@ipt.gbif.org"));
    Assert.assertEquals(manager, userManager.get("manager@ipt.gbif.org"));
    Assert.assertEquals(publisher, userManager.get("publisher@ipt.gbif.org"));
    Assert.assertEquals(user, userManager.get("user@ipt.gbif.org"));

    // add an already existent user.
    try {
      userManager.create(admin);
      fail("An exception should be thrown if user try to add an already added user");
    } catch (AlreadyExistingException e) {
      Assert.assertTrue(true);
    }

    Assert.assertEquals(4, userManager.list().size());

  }


  @Test
  public void testDelete() throws AlreadyExistingException, IOException, DeletionNotAllowedException {
    // create a new instance only to save into the file.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    // delete any user.
    User deletedUser = userManager.delete("user@ipt.gbif.org");

    // test if user was deleted
    Assert.assertEquals(3, userManager.list().size());
    Assert.assertEquals(user, deletedUser);

    // There must always be at least one user Admin.
    try {
      userManager.delete("admin@ipt.gbif.org");
      fail("There must always be at least one user Admin");
    } catch (DeletionNotAllowedException e) {
      Assert.assertTrue(true);
    }

    // TODO test if manager or admin is going to be deleted and if is a last manager of a resource.

  }

  /**
   * Read user.xml file.
   * 
   * @throws AlreadyExistingException
   * @throws IOException
   */
  @Test
  public void testLoad() throws AlreadyExistingException, IOException {
    // create a new instance only to save into the file.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    // validate if the user.xml file was created
    Assert.assertTrue(userFile.exists());

    // create another userManager instance to test read file method.
    userManager = getUserAccountManager();

    // the new userManager should have not any user previously saved.
    Assert.assertEquals(0, userManager.list().size());

    // load information from user.xml
    userManager.load();

    // the 4 users previously added should be in the users list.
    Assert.assertEquals(4, userManager.list().size());

    // validate if the users from file were included in the list.
    Assert.assertEquals(admin, userManager.get("admin@ipt.gbif.org"));
    Assert.assertEquals(manager, userManager.get("manager@ipt.gbif.org"));
    Assert.assertEquals(publisher, userManager.get("publisher@ipt.gbif.org"));
    Assert.assertEquals(user, userManager.get("user@ipt.gbif.org"));
  }
}
