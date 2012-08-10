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

package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockExtensionManager;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
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
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwcaFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceManagerImplTest {
  // Mock classes needed to create a ResourceManager instance.
  private AppConfig mockedAppConfig = MockAppConfig.buildMock();
  private UserEmailConverter mockedUserEmailConverter = mock(UserEmailConverter.class);
  private OrganisationKeyConverter mockedOrgKeyConverter = mock(OrganisationKeyConverter.class);
  private ExtensionRowTypeConverter mockedExtensionConverter = mock(ExtensionRowTypeConverter.class);
  private JdbcInfoConverter mockedJdbcConverter = mock(JdbcInfoConverter.class);
  private SourceManager mockedSourceManager = mock(SourceManager.class);
  private ExtensionManager mockedExtensionManager = MockExtensionManager.buildMock();
  private RegistryManager mockedRegistryManager = MockRegistryManager.buildMock();
  private ConceptTermConverter mockedConceptTermConverter = mock(ConceptTermConverter.class);
  private GenerateDwcaFactory mockedDwcaFactory = mock(GenerateDwcaFactory.class);
  private PasswordConverter mockedPasswordConverter = mock(PasswordConverter.class);
  private RegistrationManager mockedRegistrationManager = mock(RegistrationManager.class);
  private Eml2Rtf mockedEml2Rtf = mock(Eml2Rtf.class);
  private VocabulariesManager mockedVocabulariesManager = mock(VocabulariesManager.class);

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
    ResourceManager resourceManager =
      new ResourceManagerImpl(mockedAppConfig, mockedDataDir, mockedUserEmailConverter, mockedOrgKeyConverter,
        mockedExtensionConverter, mockedJdbcConverter, mockedSourceManager, mockedExtensionManager,
        mockedRegistryManager, mockedConceptTermConverter, mockedDwcaFactory, mockedPasswordConverter,
        mockedRegistrationManager, mockedEml2Rtf, mockedVocabulariesManager);
    return resourceManager;
  }

  private ResourceManagerImpl getResourceManagerImpl() {

    // mock creation of datasetSubtypes Map, with 2 occurrence subtypes, and 6 checklist subtypes
    Map<String, String> datasetSubtypes = new LinkedHashMap<String, String>();
    datasetSubtypes.put("", "Select a subtype");
    datasetSubtypes.put("taxonomicAuthority", "Taxonomic Authority");
    datasetSubtypes.put("nomenclatorAuthority", "Nomenclator Authority");
    datasetSubtypes.put("inventoryThematic", "Inventory Thematic");
    datasetSubtypes.put("inventoryRegional", "Inventory Regional");
    datasetSubtypes.put("globalSpeciesDataset", "Global Species Dataset");
    datasetSubtypes.put("derivedFromOccurrence", "Derived from Occurrence");
    datasetSubtypes.put("specimen", "Specimen");
    datasetSubtypes.put("observation", "Observation");
    // mock getting the vocabulary
    when(mockedVocabulariesManager.getI18nVocab(anyString(),anyString(), anyBoolean())).thenReturn(datasetSubtypes);

    ResourceManagerImpl resourceManager =
      new ResourceManagerImpl(mockedAppConfig, mockedDataDir, mockedUserEmailConverter, mockedOrgKeyConverter,
        mockedExtensionConverter, mockedJdbcConverter, mockedSourceManager, mockedExtensionManager,
        mockedRegistryManager, mockedConceptTermConverter, mockedDwcaFactory, mockedPasswordConverter,
        mockedRegistrationManager, mockedEml2Rtf, mockedVocabulariesManager);
    return resourceManager;
  }

  /**
   * test resource creation from zipped file.
   */
  @Test
  public void testCreateFromZippedFile() {
    // TODO: write test
  }

  /**
   * Test simple resource creation
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
    assertEquals(1, resourceManager.list().size());

    // get added resource.
    Resource addedResource = resourceManager.get("math");

    // test if resource was added correctly.
    assertEquals("math", addedResource.getShortname());
    assertEquals(creator, addedResource.getCreator());

    // test if resource.xml was created.
    assertTrue(mockedDataDir.resourceFile("math", ResourceManagerImpl.PERSISTENCE_FILE).exists());

  }

  @Test
  public void testInferCoreType() {
    ResourceManagerImpl manager = getResourceManagerImpl();
    // create test resource
    Resource resource = new Resource();
    // add mapping to taxon core
    ExtensionMapping mapping = new ExtensionMapping();
    Extension ext = new Extension();
    ext.setRowType(Constants.DWC_ROWTYPE_TAXON);
    mapping.setExtension(ext);
    resource.addMapping(mapping);

    resource = manager.inferCoreType(resource);
    // assert the coreType has now been correctly inferred
    assertEquals(Resource.CoreRowType.CHECKLIST.toString().toLowerCase(), resource.getCoreType().toLowerCase());
  }

  @Test
  public void testInferSubtype() {
    ResourceManagerImpl manager = getResourceManagerImpl();
    // create test resource
    Resource resource = new Resource();
    resource.setSubtype("unknown");
    resource = manager.standardizeSubtype(resource);
    // assert the subtype has been set to null, since it doesn't correspond to a known vocab term
    assertEquals(null, resource.getSubtype());

    resource.setSubtype("specimen");
    resource = manager.standardizeSubtype(resource);
    // assert the subtype has been set to "specimen", since it does correspond to the known vocab term "specimen"
    assertEquals("specimen", resource.getSubtype());
  }
}
