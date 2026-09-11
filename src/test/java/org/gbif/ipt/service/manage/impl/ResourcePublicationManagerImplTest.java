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
package org.gbif.ipt.service.manage.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.config.TestBeanProvider;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.mock.MockRegistryManager;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.DataPackageFieldConverter;
import org.gbif.ipt.model.converter.DataPackageIdentifierConverter;
import org.gbif.ipt.model.converter.ExtensionMappingConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.TableSchemaNameConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.factory.ExtensionFactory;
import org.gbif.ipt.model.factory.ThesaurusHandlingRule;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.ExtensionsHolder;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDarwinCoreDataPackageFactory;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.utils.DOIUtils;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.utils.HttpClient;
import org.gbif.utils.file.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ResourcePublicationManagerImplTest {

  private static final String DATASET_TYPE_OCCURRENCE_IDENTIFIER = "occurrence";
  private static final String DATASET_SUBTYPE_SPECIMEN_IDENTIFIER = "specimen";
  private static final String RESOURCE_SHORTNAME = "res2";

  // Mock classes
  private final AppConfig mockAppConfig = MockAppConfig.buildMock();
  private final DataDir mockedDataDir = MockDataDir.buildMock();
  private final RegistryManager mockRegistryManager = MockRegistryManager.buildMock();
  private final RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
  private final GenerateDwcaFactory mockDwcaFactory = mock(GenerateDwcaFactory.class);
  private final SimpleTextProvider mockSimpleTextProvider = mock(SimpleTextProvider.class);
  private final Eml2Rtf mockEml2Rtf = mock(Eml2Rtf.class);
  private final BaseAction baseAction = new BaseAction(mockSimpleTextProvider, mockAppConfig, mockRegistrationManager);

  private User creator;
  private Resource resource;
  private Ipt ipt;
  private Organisation organisation;
  private JdbcSupport support;

  @TempDir
  File resourceDir;
  @TempDir
  File tmpDataDir;

  @BeforeEach
  public void setup() throws Exception {
    // create user.
    creator = new User();
    creator.setFirstname("Leonardo");
    creator.setLastname("Pisano");
    creator.setEmail("fi@liberabaci.com");
    creator.setLastLoginToNow();
    creator.setRole(User.Role.Manager);
    creator.setPassword("011235813");

    resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);

    // tmp directory
    when(mockedDataDir.tmpDir()).thenReturn(tmpDataDir);

    organisation = new Organisation();
    organisation.setKey("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");
    organisation.setName("Academy of Natural Sciences");

    ipt = new Ipt();
    ipt.setKey("27c24cba-13c5-47d1-96a1-16abd8f11437");
    ipt.setName("Test IPT");
  }

  @Test
  public void testUpdateAlternateIdentifierForIPTURLToResource() throws Exception {
    ResourceManager mockResourceManger = mock(ResourceManager.class);
    ResourcePublicationManagerImpl manager = getResourcePublicationManagerImpl(mockResourceManger);

    // mock finding eml.xml file
    when(mockedDataDir.resourceEmlFile(anyString()))
        .thenReturn(File.createTempFile("eml", "xml"));

    // create PRIVATE test resource
    Resource resource = new Resource();
    resource.setShortname("bees");
    Eml eml = new Eml();
    eml.setTitle("Bees of Kansas");
    eml.setAlternateIdentifiers(new LinkedList<>());
    resource.setEml(eml);
    resource.setStatus(PublicationStatus.PRIVATE);

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);

    // update the alt. id - it should not have been set, since the resource is Private
    assertEquals(0, resource.getEml().getAlternateIdentifiers().size());

    // change resource to PUBLIC
    resource.setStatus(PublicationStatus.PUBLIC);

    // mock returning the public resource URL
    when(mockAppConfig.getResourceUrl("bees")).thenReturn("http://localhost:7001/ipt/resource?r=bees");

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);

    // assert it has been set
    assertEquals("http://localhost:7001/ipt/resource?r=bees", resource.getEml().getAlternateIdentifiers().get(0));

    // mock changing the the baseURL now (returning a different public resource URL)
    when(mockAppConfig.getResourceUrl("bees")).thenReturn("http://192.38.28.24:7001/ipt/resource?r=bees");

    manager = new ResourcePublicationManagerImpl(
        mockAppConfig,
        mockedDataDir,
        mockResourceManger,
        mockRegistryManager,
        mockRegistrationManager,
        mockEml2Rtf,
        mockDwcaFactory,
        mock(GenerateDataPackageFactory.class),
        mock(GenerateDarwinCoreDataPackageFactory.class),
        mockSimpleTextProvider,
        mock(ResourceMetadataInferringService.class));

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);
    // assert it has been set
    assertEquals("http://192.38.28.24:7001/ipt/resource?r=bees", resource.getEml().getAlternateIdentifiers().get(0));

    // create PRIVATE test resource, with existing alt id
    resource.setStatus(PublicationStatus.PRIVATE);

    // update alt. id
    manager.updateAlternateIdentifierForIPTURLToResource(resource);

    // update the alt. id - it should disapear since the resource is Private now
    assertEquals(0, resource.getEml().getAlternateIdentifiers().size());
  }

  @Test
  public void testUpdateAlternateIdentifierForRegistry() throws Exception {
    ResourceManager mockResourceManger = mock(ResourceManager.class);
    ResourcePublicationManagerImpl manager = getResourcePublicationManagerImpl(mockResourceManger);

    // mock finding eml.xml file
    when(mockedDataDir.resourceEmlFile(anyString()))
        .thenReturn(File.createTempFile("eml", "xml"));

    // create PRIVATE test resource
    Resource resource = new Resource();
    resource.setShortname("bees");
    Eml eml = new Eml();
    eml.setTitle("Bees of Kansas");
    eml.setAlternateIdentifiers(new LinkedList<>());
    resource.setEml(eml);
    resource.setStatus(PublicationStatus.PRIVATE);

    // update alt. id
    manager.updateAlternateIdentifierForRegistry(resource);
    // update the alt. id - it should not have been set, since the resource isn't registered yet
    assertEquals(0, resource.getEml().getAlternateIdentifiers().size());

    // change resource to PUBLIC
    resource.setStatus(PublicationStatus.PUBLIC);
    // update alt. id
    manager.updateAlternateIdentifierForRegistry(resource);
    // update the alt. id - it should not have been set, since the resource isn't registered yet
    assertEquals(0, resource.getEml().getAlternateIdentifiers().size());

    // change resource to Registered and give it a Registry UUID
    UUID key = UUID.randomUUID();
    resource.setKey(key);
    resource.setStatus(PublicationStatus.REGISTERED);

    // update alt. id
    manager.updateAlternateIdentifierForRegistry(resource);
    // assert it has been set
    assertEquals(key.toString(), resource.getEml().getAlternateIdentifiers().get(0));

    // try to update alt. id again
    manager.updateAlternateIdentifierForRegistry(resource);
    // there should still only be 1
    assertEquals(1, resource.getEml().getAlternateIdentifiers().size());
  }

  @Test
  public void testRegisterMigratedResource() throws Exception {
    ResourceManager resourceManager = getResourceManagerImpl();
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(resourceManager);

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    when(mockRegistryManager.isResourceBelongsToOrganisation(anyString(), anyString())).thenReturn(true);

    manager.register(resource, organisation, ipt, baseAction);

    // get registered resource.
    Resource registered = resourceManager.get(resource.getShortname());

    assertEquals(PublicationStatus.REGISTERED, registered.getStatus());
    assertEquals(registeredDigirResourceUUID, registered.getKey().toString());
    assertEquals(organisation, registered.getOrganisation());
  }

  @Test
  public void testRegisterMigratedResourceTooManyUUID() throws Exception {
    ResourceManager mockResourceManger = mock(ResourceManager.class);
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(mockResourceManger);

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";
    String extraUUID = "7615e6d1-9ebd-4302-9a7e-4913ca8b2bb4";

    resource.getEml().getAlternateIdentifiers().clear();
    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // add the extra (unwanted) UUID to list of alternate identifiers - at most there should be 1 only before reg.
    resource.getEml().getAlternateIdentifiers().add(extraUUID);

    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    assertThrows(InvalidConfigException.class, () -> manager.register(resource, organisation, ipt, baseAction));
  }

  @Test
  public void testRegisterMigratedResourceWithBadUUID() throws Exception {
    ResourceManager mockResourceManger = mock(ResourceManager.class);
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(mockResourceManger);

    // supply random UUID in the resource's eml.alternateIdentifiers that won't match one of organisation's resources
    resource.getEml().getAlternateIdentifiers().clear();
    resource.getEml().getAlternateIdentifiers().add(UUID.randomUUID().toString());
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    // mock returning list of resources that are associated to the Academy of Natural Sciences organization
    when(mockRegistryManager.isResourceBelongsToOrganisation(anyString(), anyString())).thenReturn(false);

    assertThrows(InvalidConfigException.class, () -> manager.register(resource, organisation, ipt, baseAction));
  }

  @Test
  public void testRegisterMigratedResourceWithDuplicateUUIDCase1() throws Exception {
    ResourceManager resourceManager = getResourceManagerImpl();
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(resourceManager);

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    // ensure there is at least one public resource already having an alternate identifier with this UUID
    resourceManager.create("res1", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    resourceManager.get("res1").getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    resourceManager.get("res1").setStatus(PublicationStatus.PUBLIC);

    // should throw InvalidConfigException
    assertThrows(InvalidConfigException.class, () -> manager.register(resource, organisation, ipt, baseAction));
  }

  @Test
  public void testRegisterMigratedResourceWithDuplicateUUIDCase2() throws Exception {
    ResourceManager resourceManager = getResourceManagerImpl();
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(resourceManager);

    String registeredDigirResourceUUID = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

    // indicate resource is migrated from DiGIR, by supplying the Registry UUID for the existing resource in the
    // resource's eml.alternateIdentifiers
    resource.getEml().getAlternateIdentifiers().add(registeredDigirResourceUUID);
    // indicate resource is ready to be published, by setting its status to Public
    resource.setStatus(PublicationStatus.PUBLIC);

    // ensure there is at least one registered resource already having this UUID
    resourceManager.create("res1", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    resourceManager.get("res1").setKey(UUID.fromString(registeredDigirResourceUUID));
    resourceManager.get("res1").setStatus(PublicationStatus.REGISTERED);

    // should throw InvalidConfigException
    assertThrows(InvalidConfigException.class, () -> manager.register(resource, organisation, ipt, baseAction));
  }

  @Test
  public void testDetectDuplicateUsesOfUUID() throws Exception {
    ResourceManager resourceManager = getResourceManagerImpl();
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(resourceManager);

    UUID candidate = UUID.fromString("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");

    // ensure there is at least one public resource already having an alternate identifier with this UUID
    resourceManager.create("res1", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    resourceManager.get("res1").getEml().getAlternateIdentifiers().add(candidate.toString());
    resourceManager.get("res1").setStatus(PublicationStatus.PUBLIC);

    // ensure there is at least one registered resource already having this UUID
    resourceManager.create("res2", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    resourceManager.get("res2").setKey(UUID.fromString(candidate.toString()));
    resourceManager.get("res2").setStatus(PublicationStatus.REGISTERED);

    // create the resource that is to be registered
    resourceManager.create("res3", Constants.DATASET_TYPE_METADATA_IDENTIFIER, creator);
    resourceManager.get("res3").setKey(UUID.fromString(candidate.toString()));
    resourceManager.get("res3").setStatus(PublicationStatus.PUBLIC);

    // detect the number of duplicate usages of the UUID assigned to the resource about to get registered
    List<String> names = manager.detectDuplicateUsesOfUUID(candidate, "res3");

    assertEquals(2, names.size());
  }

  /**
   * Test trying to restore last published version works.
   * Test starts by configuring a resource that is in middle of publishing version 3.1: the eml-3.1.xml,
   * and shortname-3.1.rtf have been written.
   * TODO: test resource with persisted dwca files
   */
  @Test
  public void testRestoreVersion() throws Exception {
    // create instance of manager
    ResourceManager resourceManager = mock(ResourceManager.class);
    ResourcePublicationManager manager = spy(getResourcePublicationManagerImpl(resourceManager));
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();

    doReturn(new ResourceSummaryView()).when(resourceManager).toResourceSummaryViewReconstructed(any());

    // add versionHistory for version 2.0
    Date released20 = new Date();
    VersionHistory history20 =
        new VersionHistory(new BigDecimal("2.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history20.setModifiedBy(resource.getModifier());
    history20.setDoi(null);
    history20.setStatus(IdentifierStatus.UNAVAILABLE);
    history20.setReleased(released20);
    history20.setRecordsPublished(100);
    resource.addVersionHistory(history20);

    // add versionHistory for version 3.0
    Date released30 = new Date();
    VersionHistory history30 =
        new VersionHistory(new BigDecimal("3.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history30.setModifiedBy(resource.getModifier());
    history30.setDoi(null);
    history30.setStatus(IdentifierStatus.UNAVAILABLE);
    history30.setReleased(released30);
    history30.setRecordsPublished(200);
    resource.addVersionHistory(history30);

    // add versionHistory for version 3.1
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    VersionHistory history31 =
        new VersionHistory(new BigDecimal("3.1"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history31.setModifiedBy(resource.getModifier());
    history31.setDoi(doi);
    history31.setStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    history31.setRecordsPublished(400);
    resource.addVersionHistory(history31);

    // configure resource to reflect it is in middle of publishing version 3.1
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.setDoi(doi);
    resource.setStatus(PublicationStatus.PUBLIC);
    resource.setMetadataVersion(new BigDecimal("3.1"));
    resource.setLastPublished(released30);
    resource.setRecordsPublished(400);

    // make some assertions
    assertEquals(new BigDecimal("3.1"), resource.getEml().getEmlVersion());
    assertEquals(new BigDecimal("3.0"), resource.getLastPublishedVersionsVersion());
    assertEquals(released30, resource.getLastPublished());
    assertEquals(400, resource.getRecordsPublished());

    File emlFile31 = mockedDataDir.resourceEmlFile(resource.getShortname(), new BigDecimal("3.1"));
    assertTrue(emlFile31.exists());
    File rtfFile31 = mockedDataDir.resourceRtfFile(resource.getShortname(), new BigDecimal("3.1"));
    assertTrue(rtfFile31.exists());

    // publish, will try to update DOI, triggering exception
    manager.restoreVersion(resource, new BigDecimal("3.1"), baseAction);

    // make some assertions
    assertFalse(emlFile31.exists());
    assertFalse(rtfFile31.exists());
    assertEquals(200, resource.getRecordsPublished());
    assertEquals(new BigDecimal("3.0"), resource.getEmlVersion());
    assertEquals(released30, resource.getLastPublished());
    assertEquals(new BigDecimal("2.0"), resource.getReplacedMetadataVersion());
  }

  @Test
  public void testPublishNonRegisteredMetadataOnlyResource() throws Exception {
    // create instance of manager
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ResourcePublicationManager resourceManager = getResourcePublicationManagerImpl(mockResourceManager);
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure turning auto-publishing daily
    resource.setUpdateFrequency("daily");
    resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);

    // make a few pre-publication assertions
    assertEquals(BigDecimal.valueOf(3.0), resource.getEml().getEmlVersion());
    Date created = resource.getCreated();
    assertNotNull(created);
    Date pubDate = resource.getEml().getPubDate();
    assertNotNull(pubDate);
    Date lastPublished = resource.getLastPublished();
    assertNull(lastPublished);
    assertNull(resource.getNextPublished());
    assertEquals(Constants.DATASET_TYPE_METADATA_IDENTIFIER, resource.getCoreType());

    // publish
    resourceManager.publish(resource, BigDecimal.valueOf(3.1), baseAction);

    // make some post-publication assertions
    assertEquals(BigDecimal.valueOf(3.1), resource.getEml().getEmlVersion());
    assertNotNull(resource.getNextPublished());
    assertEquals(created.toString(), resource.getCreated().toString());
    assertNotEquals(pubDate.toString(), resource.getEml().getPubDate());
    assertNotNull(resource.getLastPublished().toString());
    assertTrue(new File(resourceDir, DataDir.EML_XML_FILENAME).exists());
    assertTrue(new File(resourceDir, "eml-3.1.xml").exists());
    assertTrue(new File(resourceDir, "rtf-res2.rtf").exists());
    assertTrue(new File(resourceDir, "rtf-res2-3.1.rtf").exists());
  }

  /**
   * Do publish, test trying to update DOI assigned to resource.
   * </br>
   * Publishes non-registered metadata-only resource that has been assigned a DOI. When trying to publish a new
   * minor version an exception is thrown because the DataCite metadata is invalid (missing publisher).
   */
  @Test
  public void testPublishResourceWithDOIAssignedButInvalidDOIMetadata() throws Exception {
    // create instance of manager
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ResourcePublicationManager resourceManager = getResourcePublicationManagerImpl(mockResourceManager);
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure reserved DOI
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);
    resource.setStatus(PublicationStatus.PUBLIC);
    Date released = new Date();
    resource.setLastPublished(released);
    // versionHistory
    VersionHistory history =
        new VersionHistory(new BigDecimal("3.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history.setModifiedBy(resource.getModifier());
    history.setDoi(doi);
    history.setStatus(IdentifierStatus.PUBLIC);
    history.setReleased(released);
    resource.addVersionHistory(history);

    // make a few pre-publication assertions
    assertEquals(new BigDecimal("3.0"), resource.getEml().getEmlVersion());
    Date created = resource.getCreated();
    assertNotNull(created);
    Date pubDate = resource.getEml().getPubDate();
    assertNotNull(pubDate);
    assertEquals(Constants.DATASET_TYPE_METADATA_IDENTIFIER, resource.getCoreType());
    assertTrue(resource.isAlreadyAssignedDoi());
    assertNotNull(resource.getAssignedDoi());
    assertEquals(new BigDecimal("3.0"), resource.getLastPublishedVersionsVersion());
    assertEquals(PublicationStatus.PUBLIC, resource.getLastPublishedVersionsPublicationStatus());
    assertEquals(new BigDecimal("3.1"), resource.getNextVersion());

    // publish, will try to update DOI, triggering exception
    assertThrows(PublicationException.class,
        () -> resourceManager.publish(resource, resource.getNextVersion(), baseAction));
  }

  /**
   * Do publish, test trying to register DOI (first DOI assigned to resource).
   * </br>
   * Publishes non-registered metadata-only resource that has a DOI reserved, but no DOI assigned.
   * When trying to publish a new major version an exception is thrown because the DataCite metadata is invalid
   * (missing publisher).
   */
  @Test
  public void testPublishPublicResourceWithDOIReservedButInvalidDOIMetadata() throws Exception {
    // create instance of manager
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ResourcePublicationManager resourceManager = getResourcePublicationManagerImpl(mockResourceManager);
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure reserved DOI
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.setStatus(PublicationStatus.PUBLIC);
    Date released = new Date();
    resource.setLastPublished(released);
    // versionHistory - no DOI
    VersionHistory history =
        new VersionHistory(new BigDecimal("3.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history.setModifiedBy(resource.getModifier());
    history.setReleased(released);
    resource.addVersionHistory(history);

    // make a few pre-publication assertions
    assertFalse(resource.isAlreadyAssignedDoi());
    assertNull(resource.getAssignedDoi());
    assertEquals(new BigDecimal("3.0"), resource.getLastPublishedVersionsVersion());
    assertEquals(PublicationStatus.PUBLIC, resource.getLastPublishedVersionsPublicationStatus());
    // next published version is a major version change
    assertEquals(new BigDecimal("4.0"), resource.getNextVersion());

    // publish, will try to register DOI, triggering exception
    assertThrows(PublicationException.class,
        () -> resourceManager.publish(resource, resource.getNextVersion(), baseAction));
  }

  /**
   * Do publish, test trying to replace current assigned DOI with new DOI that has been reserved.
   * </br>
   * Publishes non-registered metadata-only resource that has a new DOI reserved, and existing DOI assigned.
   * When trying to publish a new major version an exception is thrown because the DataCite metadata is invalid
   * (missing publisher).
   */
  @Test
  public void testPublishPublicResourceWithDOIAssignedAndReservedButInvalidDOIMetadata() throws Exception {
    // create instance of manager
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ResourcePublicationManager resourceManager = getResourcePublicationManagerImpl(mockResourceManager);
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // configure reserved DOI
    DOI doi = DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX);
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC_PENDING_PUBLICATION);
    resource.setStatus(PublicationStatus.PUBLIC);
    Date released = new Date();
    resource.setLastPublished(released);
    // versionHistory - no DOI
    VersionHistory history =
        new VersionHistory(new BigDecimal("3.0"), resource.getLastPublished(), PublicationStatus.PUBLIC);
    history.setModifiedBy(resource.getModifier());
    history.setDoi(DOIUtils.mintDOI(DOIRegistrationAgency.DATACITE, Constants.TEST_DOI_PREFIX));
    history.setStatus(IdentifierStatus.PUBLIC);
    history.setReleased(released);
    resource.addVersionHistory(history);

    // make a few pre-publication assertions
    assertTrue(resource.isAlreadyAssignedDoi());
    assertNotNull(resource.getAssignedDoi());
    assertEquals(new BigDecimal("3.0"), resource.getLastPublishedVersionsVersion());
    assertEquals(PublicationStatus.PUBLIC, resource.getLastPublishedVersionsPublicationStatus());
    assertEquals(IdentifierStatus.PUBLIC_PENDING_PUBLICATION, resource.getIdentifierStatus());
    // next published version is a major version change
    assertEquals(new BigDecimal("4.0"), resource.getNextVersion());

    // publish, will try to replace DOI with new reserved DOI, triggering exception
    assertThrows(PublicationException.class,
        () -> resourceManager.publish(resource, resource.getNextVersion(), baseAction));
  }

  @Test
  public void testHasMaxProcessFailures() throws Exception {
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ResourcePublicationManager resourceManager = getResourcePublicationManagerImpl(mockResourceManager);

    ListValuedMap<String, Date> processFailures = new ArrayListValuedHashMap<>();
    processFailures.put("res1", new Date());
    processFailures.put("res1", new Date());
    processFailures.put("res2", new Date());
    processFailures.put("res2", new Date());
    processFailures.put("res2", new Date());
    resourceManager.getProcessFailures().putAll(processFailures);

    Resource resource = new Resource();
    resource.setShortname("res1");
    resource.setTitle("Mammals");
    assertFalse(resourceManager.hasMaxProcessFailures(resource));
    resource.setShortname("res2");
    assertTrue(resourceManager.hasMaxProcessFailures(resource));
  }

  @Test
  public void testPublishNonRegisteredMetadataOnlyResourceFailure() throws Exception {
    // create instance of manager
    ResourceManager resourceManager = getResourceManagerImpl();
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(resourceManager);
    // prepare resource
    Resource resource = getNonRegisteredMetadataOnlyResource();
    // save resource
    resourceManager.save(resource);

    // make pre-publication assertions
    assertEquals(BigDecimal.valueOf(3.0), resource.getEml().getEmlVersion());

    // to trigger PublicationException, indicate publication already in progress (add Future to processFutures)
    // noinspection unchecked
    manager.getProcessFutures().put(resource.getShortname(), mock(Future.class));

    // publish, catching expected Exception
    assertThrows(PublicationException.class,
        () -> manager.publish(resource, BigDecimal.valueOf(4.0), baseAction));
  }

  @Test
  public void testRemoveArchiveVersion() throws Exception {
    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ResourcePublicationManager manager = getResourcePublicationManagerImpl(mockResourceManager);

    File dwca60 = new File(resourceDir, resource.getShortname() + "/" + "dwca-60.0.zip");
    assertFalse(dwca60.exists());

    File zippedResourceFolder = FileUtils.getClasspathFile("resources/res1.zip");
    org.apache.commons.io.FileUtils.copyFile(zippedResourceFolder, dwca60);
    assertTrue(dwca60.exists());

    when(mockedDataDir.resourceDwcaFile("res2", new BigDecimal("60.0"))).thenReturn(dwca60);

    manager.removeArchiveVersion(resource.getShortname(), new BigDecimal("60.0"));
  }

  public ResourcePublicationManagerImpl getResourcePublicationManagerImpl(ResourceManager resourceManager) throws Exception {
    // mock the cfg
    when(mockAppConfig.getBaseUrl()).thenReturn("http://localhost:7001/ipt");
    // mock resource link used as EML GUID
    when(mockAppConfig.getResourceGuid("bees")).thenReturn("http://localhost:7001/ipt/resource?id=bees");
    when(mockAppConfig.getResourceGuid("res2")).thenReturn("http://localhost:7001/ipt/resource?id=res2");

    // construct ExtensionFactory using injected parameters
    HttpClient httpClient = TestBeanProvider.provideHttpClient();
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = TestBeanProvider.provideNsAwareSaxParserFactory();
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);
    support = TestBeanProvider.provideJdbcSupport();

    // construct occurrence core Extension
    InputStream occurrenceCoreIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/dwc_occurrence.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);

    // construct occurrence core Extension
    InputStream eventCoreIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/dwc_event_2015-04-24.xml");
    Extension eventCore = extensionFactory.build(eventCoreIs);

    // construct simple images extension
    InputStream simpleImageIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/simple_image.xml");
    Extension simpleImage = extensionFactory.build(simpleImageIs);

    ExtensionManager extensionManager = mock(ExtensionManager.class);
    ExtensionsHolder extensionsHolder = mock(ExtensionsHolder.class);
    DataPackageSchemaManager mockSchemaManager = mock(DataPackageSchemaManager.class);

    // mock ExtensionManager returning different Extensions
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence"))
        .thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Event"))
        .thenReturn(eventCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord"))
        .thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.gbif.org/terms/1.0/Image"))
        .thenReturn(simpleImage);
    when(extensionManager.list())
        .thenReturn(List.of(occurrenceCore, eventCore, simpleImage));

    when(extensionsHolder.getExtensionsByRowtype()).thenReturn(
        Map.ofEntries(
            Map.entry("http://rs.tdwg.org/dwc/terms/Occurrence", occurrenceCore),
            Map.entry("http://rs.tdwg.org/dwc/terms/Event", eventCore),
            Map.entry("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord", occurrenceCore),
            Map.entry("http://rs.gbif.org/terms/1.0/Image", simpleImage)));

    // mock finding dwca.zip file that does not exist
    when(mockedDataDir.resourceDwcaFile(anyString())).thenReturn(new File("dwca.zip"));

    return new ResourcePublicationManagerImpl(
        mockAppConfig,
        mockedDataDir,
        resourceManager,
        mockRegistryManager,
        mockRegistrationManager,
        mockEml2Rtf,
        mockDwcaFactory,
        mock(GenerateDataPackageFactory.class),
        mock(GenerateDarwinCoreDataPackageFactory.class),
        mockSimpleTextProvider,
        mock(ResourceMetadataInferringService.class)
    );
  }

  public ResourceManagerImpl getResourceManagerImpl() throws Exception {
    // mock the cfg
    when(mockAppConfig.getBaseUrl()).thenReturn("http://localhost:7001/ipt");
    // mock resource link used as EML GUID
    when(mockAppConfig.getResourceGuid("bees")).thenReturn("http://localhost:7001/ipt/resource?id=bees");
    when(mockAppConfig.getResourceGuid("res2")).thenReturn("http://localhost:7001/ipt/resource?id=res2");

    // construct ExtensionFactory using injected parameters
    HttpClient httpClient = TestBeanProvider.provideHttpClient();
    ThesaurusHandlingRule thesaurusRule = new ThesaurusHandlingRule(mock(VocabulariesManagerImpl.class));
    SAXParserFactory saxf = TestBeanProvider.provideNsAwareSaxParserFactory();
    ExtensionFactory extensionFactory = new ExtensionFactory(thesaurusRule, saxf, httpClient);
    support = TestBeanProvider.provideJdbcSupport();
    PasswordEncrypter passwordEncrypter = new PasswordEncrypter(TestBeanProvider.providePasswordEncryption());
    JdbcInfoConverter jdbcConverter = new JdbcInfoConverter(support);

    // construct occurrence core Extension
    InputStream occurrenceCoreIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/dwc_occurrence.xml");
    Extension occurrenceCore = extensionFactory.build(occurrenceCoreIs);

    // construct occurrence core Extension
    InputStream eventCoreIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/dwc_event_2015-04-24.xml");
    Extension eventCore = extensionFactory.build(eventCoreIs);

    // construct simple images extension
    InputStream simpleImageIs = ResourceManagerImplTest.class.getResourceAsStream("/extensions/simple_image.xml");
    Extension simpleImage = extensionFactory.build(simpleImageIs);

    ExtensionManager extensionManager = mock(ExtensionManager.class);
    ExtensionsHolder extensionsHolder = mock(ExtensionsHolder.class);
    DataPackageSchemaManager mockSchemaManager = mock(DataPackageSchemaManager.class);

    // mock ExtensionManager returning different Extensions
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Occurrence"))
        .thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/terms/Event"))
        .thenReturn(eventCore);
    when(extensionManager.get("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord"))
        .thenReturn(occurrenceCore);
    when(extensionManager.get("http://rs.gbif.org/terms/1.0/Image"))
        .thenReturn(simpleImage);
    when(extensionManager.list())
        .thenReturn(List.of(occurrenceCore, eventCore, simpleImage));

    when(extensionsHolder.getExtensionsByRowtype()).thenReturn(
        Map.ofEntries(
            Map.entry("http://rs.tdwg.org/dwc/terms/Occurrence", occurrenceCore),
            Map.entry("http://rs.tdwg.org/dwc/terms/Event", eventCore),
            Map.entry("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord", occurrenceCore),
            Map.entry("http://rs.gbif.org/terms/1.0/Image", simpleImage)));

    ExtensionRowTypeConverter extensionRowTypeConverter = new ExtensionRowTypeConverter(extensionsHolder);
    ConceptTermConverter conceptTermConverter = new ConceptTermConverter(extensionRowTypeConverter);

    ResourceConvertersManager mockResourceConvertersManager = new ResourceConvertersManager(
        mock(UserEmailConverter.class), mock(OrganisationKeyConverter.class), mock(ExtensionMappingConverter.class), extensionRowTypeConverter,
        conceptTermConverter, mock(DataPackageIdentifierConverter.class),
        mock(TableSchemaNameConverter.class), mock(DataPackageFieldConverter.class), jdbcConverter);

    // mock finding dwca.zip file that does not exist
    when(mockedDataDir.resourceDwcaFile(anyString())).thenReturn(new File("dwca.zip"));

    return new ResourceManagerImpl(
        mockAppConfig,
        mockedDataDir,
        mockResourceConvertersManager,
        mock(SourceManager.class),
        extensionManager,
        mockSchemaManager,
        mockRegistryManager,
        passwordEncrypter,
        mock(VocabulariesManager.class),
        mockSimpleTextProvider,
        mockRegistrationManager,
        mock(MetadataReader.class));
  }

  /**
   * Return a Non Registered Metadata Only Resource used for testing.
   *
   * @return a Non Registered Metadata Only Resource used for testing
   */
  public Resource getNonRegisteredMetadataOnlyResource() throws Exception {
    // retrieve resource configuration file
    File resourceXML = FileUtils.getClasspathFile("resources/res1/resource.xml");
    // copy to resource folder
    File copiedResourceXML = new File(resourceDir, DataDir.PERSISTENCE_FILENAME);
    org.apache.commons.io.FileUtils.copyFile(resourceXML, copiedResourceXML);
    // mock finding resource.xml file from resource directory
    when(mockedDataDir.resourceFile(anyString())).thenReturn(copiedResourceXML);

    // retrieve sample eml.xml
    File emlXML = FileUtils.getClasspathFile("resources/res1/eml.xml");
    // copy to resource folder
    File copiedEmlXML = new File(resourceDir, DataDir.EML_XML_FILENAME);
    org.apache.commons.io.FileUtils.copyFile(emlXML, copiedEmlXML);

    // mock new saved eml-3.1.xml file being versioned (represents new minor version)
    File versionThreeEmlXML = new File(resourceDir, "eml-3.1.xml");
    org.apache.commons.io.FileUtils.copyFile(emlXML, versionThreeEmlXML);
    // mock finding versioned eml-3.1.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), eq(BigDecimal.valueOf(3.1)))).thenReturn(versionThreeEmlXML);

    // mock new saved eml-4.0.xml file being versioned (represents new major version)
    File versionFourEmlXML = new File(resourceDir, "eml-4.0.xml");
    org.apache.commons.io.FileUtils.copyFile(emlXML, versionFourEmlXML);
    // mock finding versioned eml-4.0.xml file
    when(mockedDataDir.resourceEmlFile(anyString(), eq(BigDecimal.valueOf(4.0)))).thenReturn(versionFourEmlXML);

    // mock finding eml.xml file
    when(mockedDataDir.resourceEmlFile(anyString())).thenReturn(copiedEmlXML);
    // mock finding versioned dwca file
    when(mockedDataDir.resourceDwcaFile(anyString(), eq(BigDecimal.valueOf(3.1))))
        .thenReturn(File.createTempFile("dwca-4.0", "zip"));
    // mock finding previous versioned dwca file
    when(mockedDataDir.resourceDwcaFile(anyString(), eq(BigDecimal.valueOf(3.0))))
        .thenReturn(File.createTempFile("dwca-3.0", "zip"));

    // retrieve sample rtf.xml
    File rtfXML = FileUtils.getClasspathFile("resources/res1/rtf-res1.rtf");
    // copy to resource folder
    File copiedRtfXML = new File(resourceDir, "rtf-res2.rtf");
    org.apache.commons.io.FileUtils.copyFile(rtfXML, copiedRtfXML);

    // mock new saved rtf-res2-3.1.xml file being versioned (new minor version)
    File versionThreeRtfXML = new File(resourceDir, "rtf-res2-3.1.rtf");
    org.apache.commons.io.FileUtils.copyFile(rtfXML, versionThreeRtfXML);
    // mock finding versioned rtf-res2-3.1.xml file
    when(mockedDataDir.resourceRtfFile(anyString(), eq(BigDecimal.valueOf(3.1)))).thenReturn(versionThreeRtfXML);

    // mock new saved rtf-res2-4.0.xml file being versioned (new major version)
    File versionFourRtfXML = new File(resourceDir, "rtf-res2-4.0.rtf");
    org.apache.commons.io.FileUtils.copyFile(rtfXML, versionFourRtfXML);
    // mock finding versioned rtf-res2-4.0.xml file
    when(mockedDataDir.resourceRtfFile(anyString(), eq(BigDecimal.valueOf(4.0)))).thenReturn(versionFourRtfXML);

    // create ResourceManagerImpl
    ResourceManagerImpl resourceManager = getResourceManagerImpl();

    // create a new resource.
    Resource resource = resourceManager.create(RESOURCE_SHORTNAME, null, copiedEmlXML, creator, baseAction);
    resource.setMetadataVersion(BigDecimal.valueOf(3.0));
    return resource;
  }
}
