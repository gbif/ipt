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
import org.gbif.ipt.mock.MockExtensionManager;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.impl.ResourceManagerImpl;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwcaFactory;

import java.io.File;
import java.io.FileFilter;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author hftobon
 */
public class ResourceManagerImplTest {

  private DataDir mockedDataDir = MockDataDir.buildMock();

  @After
  public void deleteTempResourceFiles() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));

    // get possible temporal files
    File[] files = tempDir.listFiles(new FileFilter() {

      public boolean accept(File f) {
        return f.getName().endsWith("-" + ResourceManagerImpl.PERSISTENCE_FILE);
      }
    });
    // delete temporal files created in this test
    if (files != null) {
      for (File f : files) {
        f.delete();
      }
    }
  }

  private ResourceManager getResourceManager() {
    // Mock classes needed to create a ResourceManager instance.
    AppConfig mockedAppConfig = MockAppConfig.buildMock();
    UserEmailConverter mockedUserEmailConverter = mock(UserEmailConverter.class);
    OrganisationKeyConverter mockedOrgKeyConverter = mock(OrganisationKeyConverter.class);
    ExtensionRowTypeConverter mockedExtensionConverter = mock(ExtensionRowTypeConverter.class);
    JdbcInfoConverter mockedJdbcConverter = mock(JdbcInfoConverter.class);
    SourceManager mockedSourceManager = mock(SourceManager.class);
    ExtensionManager mockedExtensionManager = MockExtensionManager.buildMock();
    RegistryManager mockedRegistryManager = MockRegistryManager.buildMock();
    ConceptTermConverter mockedConceptTermConverter = mock(ConceptTermConverter.class);
    GenerateDwcaFactory mockedDwcaFactory = mock(GenerateDwcaFactory.class);
    PasswordConverter mockedPasswordConverter = mock(PasswordConverter.class);
    RegistrationManager mockedRegistrationManager = mock(RegistrationManager.class);
    Eml2Rtf mockedEml2Rtf = mock(Eml2Rtf.class);

    ResourceManager resourceManager =
      new ResourceManagerImpl(mockedAppConfig, mockedDataDir, mockedUserEmailConverter, mockedOrgKeyConverter,
        mockedExtensionConverter, mockedJdbcConverter, mockedSourceManager, mockedExtensionManager,
        mockedRegistryManager, mockedConceptTermConverter, mockedDwcaFactory, mockedPasswordConverter,
        mockedRegistrationManager, mockedEml2Rtf);
    return resourceManager;
  }

  /**
   * test resource creation from zipped file.
   */
  @Test
  public void testCreateFromZippedFile() {
    // TODO: write test
    assertTrue(true);
  }

  /**
   * Test simple resource creation
   * 
   * @throws AlreadyExistingException
   */
  @Test
  public void testSimpleCreate() throws AlreadyExistingException {
    ResourceManager resourceManager = getResourceManager();

    // create user.
    User creator = new User();
    creator.setFirstname("Leonardo");
    creator.setLastname("Pisano");
    creator.setEmail("fi@liberabaci.com");
    creator.setLastLoginToNow();
    creator.setRole(Role.Manager);
    creator.setPassword("011235813");

    // create a new resource.
    resourceManager.create("math", creator);

    // test if new resource was added to the resources list.
    Assert.assertEquals(1, resourceManager.list().size());

    // get added resource.
    Resource addedResource = resourceManager.get("math");

    // test if resource was added correctly.
    Assert.assertEquals("math", addedResource.getShortname());
    Assert.assertEquals(creator, addedResource.getCreator());

    // test if resource.xml was created.
    Assert.assertTrue(mockedDataDir.resourceFile("math", ResourceManagerImpl.PERSISTENCE_FILE).exists());

  }
}
