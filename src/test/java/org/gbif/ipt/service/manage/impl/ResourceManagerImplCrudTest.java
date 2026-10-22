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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.ResourceSummaryView;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceImportService;
import org.gbif.ipt.service.manage.ResourceLoadCallbacks;
import org.gbif.ipt.service.manage.ResourceLoader;
import org.gbif.ipt.service.manage.ResourcePersister;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.ipt.model.KeywordSet;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ResourceManagerImpl} with its collaborators mocked: the in-memory index, persistence and delegation to the
 * import service and loader.
 */
public class ResourceManagerImplCrudTest {

  private static final String SHORTNAME = "bees";
  private static final String ORGANISATION_KEY = "f9b67ad0-9c9b-11d9-b9db-b8a03c50a862";

  @TempDir
  Path tempDir;

  private File resourcesDir;
  private AppConfig cfg;
  private DataDir dataDir;
  private RegistryManager registryManager;
  private MetadataReader metadataReader;
  private ResourceImportService importService;
  private ResourceLoader loader;
  private ResourcePersister persister;
  private BaseAction action;
  private ResourceManagerImpl manager;

  @BeforeEach
  public void setUp() throws IOException {
    resourcesDir = Files.createDirectory(tempDir.resolve("resources")).toFile();
    cfg = mock(AppConfig.class);
    dataDir = mock(DataDir.class);
    when(cfg.getDataDir()).thenReturn(dataDir);
    when(cfg.getResourceGuid(anyString())).thenAnswer(invocation ->
        "http://localhost:7001/ipt/resource?r=" + invocation.getArgument(0, String.class));
    when(dataDir.resourceFile(any(Resource.class))).thenAnswer(invocation ->
        new File(resourcesDir, invocation.getArgument(0, Resource.class).getShortname() + "/resource.xml"));
    when(dataDir.resourceFile(any(Resource.class), eq(""))).thenAnswer(invocation ->
        new File(resourcesDir, invocation.getArgument(0, Resource.class).getShortname()));
    when(dataDir.resourceInferredMetadataFile(anyString())).thenAnswer(invocation ->
        new File(resourcesDir, invocation.getArgument(0, String.class) + "/inferredMetadata.xml"));
    when(dataDir.resourceEmlFile(anyString())).thenAnswer(invocation ->
        new File(resourcesDir, invocation.getArgument(0, String.class) + "/eml.xml"));
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        new File(resourcesDir, invocation.getArgument(0, String.class) + "/eml-"
            + invocation.getArgument(1, BigDecimal.class).toPlainString() + ".xml"));
    when(dataDir.resourceDatapackageMetadataFile(anyString(), any())).thenAnswer(invocation ->
        new File(resourcesDir, invocation.getArgument(0, String.class) + "/datapackage.json"));
    when(dataDir.tmpDir()).thenAnswer(invocation -> Files.createTempDirectory(tempDir, "tmp").toFile());

    registryManager = mock(RegistryManager.class);
    metadataReader = mock(MetadataReader.class);
    importService = mock(ResourceImportService.class);
    loader = mock(ResourceLoader.class);
    persister = mock(ResourcePersister.class);
    action = mock(BaseAction.class);
    manager = new ResourceManagerImpl(cfg, dataDir, registryManager, metadataReader, importService, loader, persister);
  }

  @Test
  public void testSaveWritesResourceAndAddsItToIndex() throws Exception {
    Resource resource = resource(SHORTNAME);
    doAnswer(invocation -> {
      invocation.getArgument(1, Writer.class).write("<resource/>");
      return null;
    }).when(persister).save(eq(resource), any(Writer.class));

    manager.save(resource);

    assertEquals("<resource/>", Files.readString(resourceFile(SHORTNAME).toPath()));
    assertSame(resource, manager.get("BEES"));
    assertEquals(List.of(resource), manager.list());
    // never published
    assertTrue(manager.listPublishedPublicResourceSummaries().isEmpty());
  }

  @Test
  public void testSaveFailure() {
    Resource resource = resource(SHORTNAME);
    doThrow(new IllegalArgumentException("cannot serialize")).when(persister).save(eq(resource), any(Writer.class));

    InvalidConfigException e = assertThrows(InvalidConfigException.class, () -> manager.save(resource));
    assertEquals(InvalidConfigException.TYPE.CONFIG_WRITE, e.getType());
    assertEquals("cannot serialize", e.getMessage());
    assertNull(manager.get(SHORTNAME));
  }

  @Test
  public void testSavePublishedPublicResourceAddsSummaryFromPublishedVersion() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    // the title has changed since the last publication
    resource.setTitle("Changed title");

    manager.save(resource);

    List<ResourceSummaryView> summaries = manager.listPublishedPublicResourceSummaries();
    assertEquals(1, summaries.size());
    ResourceSummaryView summary = summaries.get(0);
    assertEquals(SHORTNAME, summary.getShortname());
    assertEquals("WII Herbarium Dataset", summary.getTitle());
    assertEquals(UUID.fromString(ORGANISATION_KEY), summary.getOrganisationKey());
    assertEquals("Academy of Natural Sciences", summary.getOrganisationName());
    assertTrue(summary.isPublished());
  }

  @Test
  public void testSavePrivatePublishedResourceDoesNotAddSummary() throws Exception {
    manager.save(publishedResource(PublicationStatus.PRIVATE));

    assertTrue(manager.listPublishedPublicResourceSummaries().isEmpty());
    assertNotNull(manager.get(SHORTNAME));
  }

  @Test
  public void testSaveDeletedPublishedResourceDoesNotAddSummary() throws Exception {
    manager.save(publishedResource(PublicationStatus.DELETED));

    assertTrue(manager.listPublishedPublicResourceSummaries().isEmpty());
  }

  @Test
  public void testSaveKeepsResourceInIndexWhenSummaryCannotBeReconstructed() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    // the versioned EML is missing
    Files.delete(new File(resourcesDir, SHORTNAME + "/eml-1.0.xml").toPath());

    manager.save(resource);

    assertSame(resource, manager.get(SHORTNAME));
    assertTrue(manager.listPublishedPublicResourceSummaries().isEmpty());
  }

  @Test
  public void testSummaryNextPublishedIsHiddenWhenAutoPublishingIsDisabled() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    Date nextPublished = new Date(System.currentTimeMillis() + 86_400_000L);
    resource.setNextPublished(nextPublished);

    when(cfg.isAutoPublishingEnabled()).thenReturn(true);
    assertEquals(nextPublished, manager.toResourceSummaryViewReconstructed(resource).getNextPublished());

    when(cfg.isAutoPublishingEnabled()).thenReturn(false);
    assertNull(manager.toResourceSummaryViewReconstructed(resource).getNextPublished());
  }

  @Test
  public void testSummaryOfVersionRegisteredAfterPublication() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    resource.setKey(UUID.randomUUID());
    resource.setStatus(PublicationStatus.REGISTERED);

    ResourceSummaryView summary = manager.toResourceSummaryViewReconstructed(resource);

    assertEquals(PublicationStatus.REGISTERED, summary.getStatus());
  }

  @Test
  public void testUpdateStoredResources() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    manager.save(resource);
    resource.getOrganisation().setAlias("ANSP");

    manager.updateStoredResources(resource);

    ResourceSummaryView summary = manager.listPublishedPublicResourceSummaries().get(0);
    assertEquals(PublicationStatus.REGISTERED, summary.getStatus());
    assertEquals("ANSP", summary.getOrganisationAlias());
  }

  @Test
  public void testRemovePublishedPublicVersion() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    manager.save(resource);

    manager.removePublishedPublicVersion(SHORTNAME);

    assertTrue(manager.listPublishedPublicResourceSummaries().isEmpty());
    assertSame(resource, manager.get(SHORTNAME));
  }

  @Test
  public void testUpdateOrganisationNameForResources() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    manager.save(resource);
    Organisation renamed = new Organisation();
    renamed.setKey(ORGANISATION_KEY);
    renamed.setName("Academy of Natural Sciences of Drexel University");
    renamed.setAlias("ANSP");

    manager.updateOrganisationNameForResources(renamed);

    assertEquals("Academy of Natural Sciences of Drexel University", resource.getOrganisation().getName());
    ResourceSummaryView summary = manager.listPublishedPublicResourceSummaries().get(0);
    assertEquals("Academy of Natural Sciences of Drexel University", summary.getOrganisationName());
    assertEquals("ANSP", summary.getOrganisationAlias());
  }

  @Test
  public void testDeleteAndRemove() throws Exception {
    Resource resource = publishedResource(PublicationStatus.PUBLIC);
    manager.save(resource);

    manager.delete(resource, true);

    assertNull(manager.get(SHORTNAME));
    assertTrue(manager.listPublishedPublicResourceSummaries().isEmpty());
    assertFalse(new File(resourcesDir, SHORTNAME).exists());
    verify(registryManager, never()).deregister(any());
  }

  @Test
  public void testDeleteWithoutRemoving() throws Exception {
    Resource resource = resource(SHORTNAME);
    manager.save(resource);

    manager.delete(resource, false);

    assertSame(resource, manager.get(SHORTNAME));
    assertTrue(new File(resourcesDir, SHORTNAME).exists());
  }

  @Test
  public void testDeleteRegisteredResourceDeregistersIt() throws Exception {
    Resource resource = registeredResource();
    manager.save(resource);

    manager.delete(resource, true);

    verify(registryManager).deregister(resource);
    assertNull(manager.get(SHORTNAME));
  }

  @Test
  public void testDeleteRegisteredResourceFailsWhenDeregistrationFails() throws Exception {
    Resource resource = registeredResource();
    manager.save(resource);
    doThrow(new RegistryException(RegistryException.Type.BAD_RESPONSE, "http://api.gbif.org", "nope"))
        .when(registryManager).deregister(resource);

    DeletionNotAllowedException e =
        assertThrows(DeletionNotAllowedException.class, () -> manager.delete(resource, true));
    assertEquals(DeletionNotAllowedException.Reason.REGISTRY_ERROR, e.getReason());
    assertSame(resource, manager.get(SHORTNAME));
    assertTrue(new File(resourcesDir, SHORTNAME).exists());
  }

  @Test
  public void testDeleteResourceFromIpt() throws Exception {
    Resource resource = registeredResource();
    manager.save(resource);

    manager.deleteResourceFromIpt(resource);

    // deleted locally only
    verify(registryManager, never()).deregister(any());
    assertNull(manager.get(SHORTNAME));
    assertFalse(new File(resourcesDir, SHORTNAME).exists());
  }

  @Test
  public void testIsEmlExisting() throws Exception {
    assertFalse(manager.isEmlExisting(SHORTNAME));

    File emlFile = new File(resourcesDir, SHORTNAME + "/eml.xml");
    Files.createDirectories(emlFile.getParentFile().toPath());
    Files.createFile(emlFile.toPath());

    assertTrue(manager.isEmlExisting(SHORTNAME));
  }

  @Test
  public void testSyncEmlWithUnregisteredResource() {
    Resource resource = resource(SHORTNAME);
    resource.setMetadataVersion(new BigDecimal("3.0"));
    resource.setCoreType("occurrence");
    resource.setSubtype("specimen");

    manager.syncEmlWithResource(resource);

    assertEquals("http://localhost:7001/ipt/resource?r=bees", resource.getEml().getGuid());
    assertEquals(new BigDecimal("3.0"), resource.getEml().getEmlVersion());
    assertTrue(hasKeyword(resource.getEml(), Constants.THESAURUS_DATASET_TYPE, "Occurrence"));
    assertTrue(hasKeyword(resource.getEml(), Constants.THESAURUS_DATASET_SUBTYPE, "Specimen"));
  }

  @Test
  public void testSyncEmlWithRegisteredResource() {
    Resource resource = resource(SHORTNAME);
    UUID key = UUID.randomUUID();
    resource.setKey(key);

    manager.syncEmlWithResource(resource);

    assertEquals(key.toString(), resource.getEml().getGuid());
  }

  @Test
  public void testSyncEmlRemovesKeywordsOfMissingTypeAndSubtype() {
    Resource resource = resource(SHORTNAME);
    resource.setCoreType("occurrence");
    resource.setSubtype("specimen");
    manager.syncEmlWithResource(resource);
    resource.setCoreType(null);
    resource.setSubtype(null);

    manager.syncEmlWithResource(resource);

    assertFalse(hasThesaurus(resource.getEml(), Constants.THESAURUS_DATASET_TYPE));
    assertFalse(hasThesaurus(resource.getEml(), Constants.THESAURUS_DATASET_SUBTYPE));
  }

  @Test
  public void testSyncEmlPreservingKeywords() {
    Resource resource = resource(SHORTNAME);
    resource.setCoreType("occurrence");

    manager.syncEmlWithResource(resource, true);

    assertFalse(hasThesaurus(resource.getEml(), Constants.THESAURUS_DATASET_TYPE));
  }

  @Test
  public void testSyncEmlOfDataPackageResourceDoesNotAddKeywords() {
    Resource resource = resource(SHORTNAME);
    resource.setCoreType("dwc-dp");
    resource.setDataPackageIdentifier("http://rs.tdwg.org/dwc/dwc-dp");

    manager.syncEmlWithResource(resource);

    assertFalse(hasThesaurus(resource.getEml(), Constants.THESAURUS_DATASET_TYPE));
  }

  @Test
  public void testSaveEml() throws Exception {
    Resource resource = resource(SHORTNAME);
    resource.setTitle("Bees");
    Files.createDirectories(new File(resourcesDir, SHORTNAME).toPath());

    manager.saveEml(resource);

    assertNotNull(resource.getModified());
    assertEquals("http://localhost:7001/ipt/resource?r=bees", resource.getEml().getGuid());
    String eml = Files.readString(new File(resourcesDir, SHORTNAME + "/eml.xml").toPath());
    assertTrue(eml.contains("Bees"), eml);
  }

  @Test
  public void testSaveDatapackageMetadata() throws Exception {
    Resource resource = resource(SHORTNAME);
    FrictionlessMetadata<?, ?, ?> metadata = new FrictionlessMetadata<>();
    resource.setDataPackageMetadata(metadata);

    manager.saveDatapackageMetadata(resource);

    verify(metadataReader).writeValue(new File(resourcesDir, SHORTNAME + "/datapackage.json"), metadata);
    assertNotNull(resource.getModified());
  }

  @Test
  public void testSaveDatapackageMetadataFailure() throws Exception {
    Resource resource = resource(SHORTNAME);
    doThrow(new IOException("disk full")).when(metadataReader).writeValue(any(File.class), any());

    assertThrows(RuntimeException.class, () -> manager.saveDatapackageMetadata(resource));
  }

  @Test
  public void testSaveInferredMetadata() throws Exception {
    Resource resource = resource(SHORTNAME);
    InferredEmlMetadata inferred = new InferredEmlMetadata();
    resource.setInferredMetadata(inferred);

    manager.saveInferredMetadata(resource);

    verify(persister).saveInferredMetadata(eq(inferred), any(Writer.class));
    assertTrue(new File(resourcesDir, SHORTNAME + "/inferredMetadata.xml").exists());
  }

  @Test
  public void testCreateDelegatesToImportService() throws Exception {
    User creator = new User();
    Resource created = resource(SHORTNAME);
    when(importService.createNew(SHORTNAME, "occurrence", creator)).thenReturn(created);

    assertSame(created, manager.create(SHORTNAME, "occurrence", creator));
  }

  @Test
  public void testCreateFromFileWithExistingShortname() throws Exception {
    manager.save(resource(SHORTNAME));
    File emlFile = FileUtils.getClasspathFile("data/eml.xml");

    assertThrows(AlreadyExistingException.class,
        () -> manager.create(SHORTNAME, null, emlFile, new User(), action));
    verify(importService, never()).createFromEml(any(), any(), any(), any());
  }

  @Test
  public void testCreateFromSingleEmlFile() throws Exception {
    File emlFile = FileUtils.getClasspathFile("data/eml.xml");
    User creator = new User();
    Resource created = resource(SHORTNAME);
    when(importService.createFromEml(eq(SHORTNAME), eq(emlFile), eq(creator), any(ActionLogger.class)))
        .thenReturn(created);

    Resource resource = manager.create(SHORTNAME, null, emlFile, creator, action);

    assertSame(created, resource);
  }

  @Test
  public void testCreateFromSingleDataPackageDescriptor() throws Exception {
    File descriptor = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    Resource created = resource(SHORTNAME);
    when(importService.createFromPackageDescriptor(eq(SHORTNAME), eq("dwc-dp"), eq(descriptor), any(), any()))
        .thenReturn(created);

    Resource resource = manager.create(SHORTNAME, "dwc-dp", descriptor, new User(), action);

    assertSame(created, resource);
  }

  @Test
  public void testCreateFromSingleColDpMetadataFile() throws Exception {
    File metadata = Files.writeString(tempDir.resolve("metadata.yml"), "title: x").toFile();
    Resource created = resource(SHORTNAME);
    created.setCoreType(Constants.COL_DP);
    when(importService.createFromColDpMetadata(eq(SHORTNAME), eq(metadata), any(), any())).thenReturn(created);

    Resource resource = manager.create(SHORTNAME, Constants.COL_DP, metadata, new User(), action);

    assertSame(created, resource);
  }

  @Test
  public void testCreateSetsMissingType() throws Exception {
    File descriptor = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    Resource created = resource(SHORTNAME);
    when(importService.createFromPackageDescriptor(any(), any(), any(), any(), any())).thenReturn(created);

    Resource resource = manager.create(SHORTNAME, "dwc-dp", descriptor, new User(), action);

    assertEquals("dwc-dp", resource.getCoreType());
  }

  @Test
  public void testCreateFromFileWithUnsupportedExtension() throws Exception {
    File file = Files.writeString(tempDir.resolve("metadata.txt"), "text").toFile();

    ImportException e = assertThrows(ImportException.class,
        () -> manager.create(SHORTNAME, null, file, new User(), action));
    assertTrue(e.getMessage().contains("txt"), e.getMessage());
  }

  @Test
  public void testReplaceEml() throws Exception {
    Resource resource = resource(SHORTNAME);
    Files.createDirectories(new File(resourcesDir, SHORTNAME).toPath());
    File emlFile = FileUtils.getClasspathFile("data/eml.xml");
    Eml eml = new Eml();
    eml.setTitle("Replaced");
    when(importService.copyMetadata(SHORTNAME, emlFile)).thenReturn(eml);

    manager.replaceEml(resource, emlFile, true);

    verify(importService).validateEmlFile(emlFile);
    assertSame(eml, resource.getEml());
    assertNotNull(resource.getMetadataModified());
    assertSame(resource, manager.get(SHORTNAME));
    assertTrue(new File(resourcesDir, SHORTNAME + "/eml.xml").exists());
  }

  @Test
  public void testReplaceEmlWithoutValidation() throws Exception {
    Resource resource = resource(SHORTNAME);
    Files.createDirectories(new File(resourcesDir, SHORTNAME).toPath());
    File emlFile = FileUtils.getClasspathFile("data/eml.xml");
    when(importService.copyMetadata(SHORTNAME, emlFile)).thenReturn(new Eml());

    manager.replaceEml(resource, emlFile, false);

    verify(importService, never()).validateEmlFile(any());
  }

  @Test
  public void testReplaceFrictionlessDatapackageMetadata() throws Exception {
    Resource resource = resource(SHORTNAME);
    resource.setCoreType("dwc-dp");
    resource.setDataPackageIdentifier("http://rs.tdwg.org/dwc/dwc-dp");
    FrictionlessMetadata<?, ?, ?> current = new FrictionlessMetadata<>();
    current.setVersion("4");
    resource.setDataPackageMetadata(current);
    resource.setInferGeocoverageAutomatically(true);
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    FrictionlessMetadata<?, ?, ?> replacement = new FrictionlessMetadata<>();
    replacement.setName("other");
    replacement.setId("id");
    replacement.setVersion("1");
    replacement.getAdditionalProperties().put("custom", "value");
    when(importService.copyDatapackageMetadata(SHORTNAME, metadataFile, "dwc-dp")).thenReturn(replacement);

    manager.replaceDatapackageMetadata(action, resource, metadataFile, true);

    verify(importService).validateDatapackageMetadataFile(action, metadataFile, FrictionlessMetadata.class);
    assertSame(replacement, resource.getDataPackageMetadata());
    assertEquals(SHORTNAME, replacement.getName());
    assertNull(replacement.getId());
    // the version is kept
    assertEquals("4", replacement.getVersion());
    assertTrue(replacement.getAdditionalProperties().isEmpty());
    assertFalse(resource.isInferGeocoverageAutomatically());
    assertFalse(resource.isInferTaxonomicCoverageAutomatically());
    assertFalse(resource.isInferTemporalCoverageAutomatically());
    verify(metadataReader).writeValue(new File(resourcesDir, SHORTNAME + "/datapackage.json"), replacement);
  }

  @Test
  public void testLoad() throws Exception {
    File bees = Files.createDirectories(resourcesDir.toPath().resolve("bees")).toFile();
    Files.createFile(bees.toPath().resolve("resource.xml"));
    File ants = Files.createDirectories(resourcesDir.toPath().resolve("ants")).toFile();
    Files.createFile(ants.toPath().resolve("resource.xml"));
    File broken = Files.createDirectories(resourcesDir.toPath().resolve("broken")).toFile();
    Files.createFile(broken.toPath().resolve("resource.xml"));
    File empty = Files.createDirectories(resourcesDir.toPath().resolve("empty")).toFile();
    Files.createFile(resourcesDir.toPath().resolve("not-a-directory.txt"));
    // previously loaded resources are dropped
    manager.save(resource("old"));
    org.apache.commons.io.FileUtils.deleteDirectory(new File(resourcesDir, "old"));
    User creator = new User();

    Resource loadedBees = resource("bees");
    Resource loadedAnts = resource("ants");
    when(loader.load(any(File.class), eq(creator), any(ResourceLoadCallbacks.class))).thenAnswer(invocation -> {
      File dir = invocation.getArgument(0, File.class);
      if (dir.equals(broken)) {
        throw new InvalidConfigException(InvalidConfigException.TYPE.RESOURCE_CONFIG, "broken");
      }
      return dir.equals(bees) ? loadedBees : loadedAnts;
    });

    int loaded = manager.load(resourcesDir, creator);

    assertEquals(2, loaded);
    assertEquals(2, manager.list().size());
    assertSame(loadedBees, manager.get("bees"));
    assertSame(loadedAnts, manager.get("ants"));
    assertNull(manager.get("old"));
    assertNull(manager.get("broken"));
    // empty resource directories are cleaned up
    assertFalse(empty.exists());
    verify(loader, never()).load(eq(empty), any(), any(ResourceLoadCallbacks.class));
  }

  @Test
  public void testLoadMissingResourcesDirectory() {
    assertEquals(0, manager.load(new File(tempDir.toFile(), "missing"), null));
  }

  @Test
  public void testLoadCallbacksSyncEmlAndSave() throws Exception {
    File bees = Files.createDirectories(resourcesDir.toPath().resolve("bees")).toFile();
    Files.createFile(bees.toPath().resolve("resource.xml"));
    Resource loadedBees = resource("bees");
    ArgumentCaptor<ResourceLoadCallbacks> callbacks = ArgumentCaptor.forClass(ResourceLoadCallbacks.class);
    when(loader.load(eq(bees), any(), callbacks.capture())).thenReturn(loadedBees);
    manager.load(resourcesDir, null);

    Resource other = resource("ants");
    callbacks.getValue().syncEml(other);
    assertEquals("http://localhost:7001/ipt/resource?r=ants", other.getEml().getGuid());

    callbacks.getValue().save(other);
    assertSame(other, manager.get("ants"));
    verify(persister, times(1)).save(eq(other), any(Writer.class));
  }

  private File resourceFile(String shortname) {
    return new File(resourcesDir, shortname + "/resource.xml");
  }

  private static Resource resource(String shortname) {
    Resource resource = new Resource();
    resource.setShortname(shortname);
    return resource;
  }

  /**
   * Resource published once, with the EML of the published version in the resource directory.
   */
  private Resource publishedResource(PublicationStatus status) throws IOException {
    Resource resource = resource(SHORTNAME);
    resource.setCoreType("occurrence");
    Organisation organisation = new Organisation();
    organisation.setKey(ORGANISATION_KEY);
    organisation.setName("Academy of Natural Sciences");
    resource.setOrganisation(organisation);
    resource.setStatus(status);
    resource.setMetadataVersion(new BigDecimal("1.0"));
    resource.setLastPublished(new Date());
    resource.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), new Date(), status));
    File versionedEml = new File(resourcesDir, SHORTNAME + "/eml-1.0.xml");
    Files.createDirectories(versionedEml.getParentFile().toPath());
    Files.copy(FileUtils.getClasspathFile("data/eml.xml").toPath(), versionedEml.toPath());
    return resource;
  }

  private static Resource registeredResource() {
    Resource resource = resource(SHORTNAME);
    resource.setKey(UUID.randomUUID());
    resource.setStatus(PublicationStatus.REGISTERED);
    return resource;
  }

  private static boolean hasThesaurus(Eml eml, String thesaurus) {
    return eml.getKeywords().stream().anyMatch(k -> thesaurus.equals(k.getKeywordThesaurus()));
  }

  private static boolean hasKeyword(Eml eml, String thesaurus, String keyword) {
    for (KeywordSet keywordSet : eml.getKeywords()) {
      if (thesaurus.equals(keywordSet.getKeywordThesaurus()) && keywordSet.getKeywordsString().contains(keyword)) {
        return true;
      }
    }
    return false;
  }
}
