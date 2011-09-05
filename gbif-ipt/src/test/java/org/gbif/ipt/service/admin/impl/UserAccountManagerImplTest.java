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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author hftobon
 */
public class UserAccountManagerImplTest {

  private PasswordConverter mockedPasswordConverter = mock(PasswordConverter.class);
  private ResourceManager mockedResourceManager = MockResourceManager.buildMock();
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
   * Test user authenticate.
   * 
   * @throws AlreadyExistingException
   * @throws IOException
   */
  @Test
  public void testAuthenticate() throws AlreadyExistingException, IOException {
    // create new instance.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);


    Assert.assertEquals(admin, userManager.authenticate("admin@ipt.gbif.org", "admin"));
    Assert.assertEquals(manager, userManager.authenticate("manager@ipt.gbif.org", "manager"));
    Assert.assertEquals(publisher, userManager.authenticate("publisher@ipt.gbif.org", "publisher"));
    Assert.assertEquals(user, userManager.authenticate("user@ipt.gbif.org", "user"));
    Assert.assertNull(userManager.authenticate("invalid-user@ipt.gbif.org", "anyPassword"));
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

  /**
   * Test user deletion.
   * 
   * @throws AlreadyExistingException
   * @throws IOException
   * @throws DeletionNotAllowedException
   */
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

    // test if the manager or admin that is going to be deleted, is the last manager of a resource.
    List<Resource> resources = new ArrayList<Resource>();

    Resource res1 = new Resource();
    res1.setCreator(manager);
    Set<User> managers1 = new HashSet<User>();
    managers1.add(manager);
    res1.setManagers(managers1);
    res1.setShortname("res1");

    resources.add(res1);

    when(mockedResourceManager.list(any(User.class))).thenReturn(resources);
    try {
      userManager.delete("manager@ipt.gbif.org");
      fail("Last manager for resource res1 cannot be deleted");
    } catch (DeletionNotAllowedException e) {
      Assert.assertTrue(true);
    }

    // Creator of resources cannot be deleted. Test if user role is changed to a simple user.
    resources.remove(res1); // 
    Resource res2 = new Resource();
    Set<User> managers2 = new HashSet<User>();
    managers2.add(publisher);
    res2.setCreator(manager);
    res2.setManagers(managers2);
    res2.setShortname("res2");
    resources.add(res2);

    userManager.delete("manager@ipt.gbif.org");
    Assert.assertEquals(Role.User, manager.getRole());

  }


  /**
   * Test get user.
   * 
   * @throws AlreadyExistingException
   * @throws IOException
   */
  @Test
  public void testGet() throws AlreadyExistingException, IOException {
    // create new instance.
    UserAccountManager userManager = getUserAccountManager();

    // add users
    userManager.create(admin);
    userManager.create(manager);
    userManager.create(publisher);
    userManager.create(user);

    Assert.assertEquals(admin, userManager.get("admin@ipt.gbif.org"));
    Assert.assertEquals(publisher, userManager.get("publisher@ipt.gbif.org"));
    Assert.assertNull(userManager.get(null));
  }

  /**
   * Read from user.xml file.
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
