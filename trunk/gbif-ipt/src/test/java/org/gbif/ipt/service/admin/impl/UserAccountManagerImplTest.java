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
import org.gbif.ipt.mock.MockResourceManager;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @author hftobon
 */
public class UserAccountManagerImplTest {

  private PasswordConverter mockedPasswordConverter = mock(PasswordConverter.class);


  private UserAccountManager getUserAccountManager() {
    AppConfig mockedCfg = MockAppConfig.buildMock();
    DataDir mockedDataDir = DataDir.buildMock();
    ResourceManager mockedResourceManager = MockResourceManager.buildMock();

    UserAccountManager userManager =
      new UserAccountManagerImpl(mockedCfg, mockedDataDir, mockedResourceManager, mockedPasswordConverter);

    return userManager;
  }


  @Ignore
  @Test
  public void testCreate() {
    User admin = new User();
    admin.setEmail("admin@ipt.gbif.org");
    admin.setRole(Role.Admin);
    admin.setFirstname("Hector");
    admin.setLastname("Tobon");
    admin.setPassword("admin");

    UserAccountManager userManager = getUserAccountManager();
    try {
      userManager.create(admin);
    } catch (AlreadyExistingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // TODO To complete

  }
}
