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
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceLoadCallbacks;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;
import org.gbif.ipt.service.manage.ResourcePersister;
import org.gbif.ipt.service.manage.ResourceTypeService;
import org.gbif.ipt.service.manage.ResourceVersioningService;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.metadata.eml.ipt.model.Eml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceLoaderImplTest {

  private static final String SHORTNAME = "bees";
  private static final String DWC_DP_IDENTIFIER = "http://rs.tdwg.org/dwc/dwc-dp";
  private static final String CAMTRAP_DP_IDENTIFIER = "https://rs.gbif.org/sandbox/data-packages/camtrap-dp";

  @TempDir
  Path tempDir;

  private File resourceDir;
  private File inferredMetadataFile;
  private DataDir dataDir;
  private ExtensionManager extensionManager;
  private DataPackageSchemaManager schemaManager;
  private ResourceTypeService resourceTypeService;
  private ResourceVersioningService resourceVersioningService;
  private ResourceMetadataLoader resourceMetadataLoader;
  private ResourcePersister resourcePersister;
  private ResourceLoadCallbacks callbacks;
  private BaseAction action;
  private ActionLogger alog;
  private ResourceLoaderImpl loader;

  @BeforeEach
  public void setUp() throws IOException {
    resourceDir = Files.createDirectory(tempDir.resolve(SHORTNAME)).toFile();
    File resourceFile = Files.createFile(resourceDir.toPath().resolve("resource.xml")).toFile();
    inferredMetadataFile = resourceDir.toPath().resolve("inferredMetadata.xml").toFile();

    dataDir = mock(DataDir.class);
    when(dataDir.resourceFile(SHORTNAME)).thenReturn(resourceFile);
    when(dataDir.resourceInferredMetadataFile(SHORTNAME)).thenReturn(inferredMetadataFile);
    when(dataDir.resourceEmlFile(SHORTNAME)).thenReturn(resourceDir.toPath().resolve("eml.xml").toFile());
    when(dataDir.sourceFile(any(Resource.class), any(FileSource.class))).thenAnswer(invocation ->
        new File(resourceDir, "sources/" + invocation.getArgument(1, FileSource.class).getName() + ".txt"));

    extensionManager = mock(ExtensionManager.class);
    schemaManager = mock(DataPackageSchemaManager.class);
    resourceTypeService = mock(ResourceTypeService.class);
    resourceVersioningService = mock(ResourceVersioningService.class);
    resourceMetadataLoader = mock(ResourceMetadataLoader.class);
    resourcePersister = mock(ResourcePersister.class);
    callbacks = mock(ResourceLoadCallbacks.class);
    action = mock(BaseAction.class);
    alog = new ActionLogger(LogManager.getLogger(ResourceLoaderImplTest.class), action);

    loader = new ResourceLoaderImpl(mock(AppConfig.class), dataDir, extensionManager, schemaManager,
        resourceTypeService, resourceVersioningService, resourceMetadataLoader, mock(SimpleTextProvider.class),
        mock(RegistrationManager.class), resourcePersister);
  }

  @Test
  public void testLoadNonExistentDirectory() {
    assertNull(loader.load(new File(tempDir.toFile(), "missing"), null, alog, callbacks));
    verify(resourcePersister, never()).load(any());
  }

  @Test
  public void testLoadSetsShortnameFromDirectoryName() {
    Resource stored = new Resource();
    stored.setShortname("something-else");
    stored.setCreator(user("creator@example.org"));
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertSame(stored, loaded);
    assertEquals(SHORTNAME, loaded.getShortname());
  }

  @Test
  public void testLoadWithoutActionLogger() {
    Resource stored = new Resource();
    stored.setCreator(user("creator@example.org"));
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    assertSame(stored, loader.load(resourceDir, null, callbacks));
  }

  @Test
  public void testLoadPopulatesMissingCreator() {
    User creator = user("creator@example.org");
    Resource stored = new Resource();
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, creator, alog, callbacks);

    assertSame(creator, loaded.getCreator());
  }

  @Test
  public void testLoadKeepsExistingCreator() {
    User creator = user("creator@example.org");
    Resource stored = new Resource();
    stored.setCreator(creator);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, user("other@example.org"), alog, callbacks);

    assertSame(creator, loaded.getCreator());
  }

  @Test
  public void testLoadRemovesNonExistentManagers() {
    Resource stored = new Resource();
    User manager = user("manager@example.org");
    stored.getManagers().add(manager);
    stored.getManagers().add(null);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertEquals(1, loaded.getManagers().size());
    assertTrue(loaded.getManagers().contains(manager));
  }

  @Test
  public void testLoadFailsForNonExistentExtension() {
    Resource stored = new Resource();
    // the extension couldn't be resolved
    stored.setMappings(new ArrayList<>(List.of(new ExtensionMapping())));
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    assertThrows(InvalidConfigException.class, () -> loader.load(resourceDir, null, alog, callbacks));
    verify(action).getText(eq("manage.resource.create.extension.null"), any(String[].class));
    verify(callbacks, never()).save(any());
  }

  @Test
  public void testLoadFailsForNonInstalledExtension() {
    Extension extension = new Extension();
    extension.setRowType("http://rs.gbif.org/terms/1.0/NotInstalled");
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(extension);
    Resource stored = new Resource();
    stored.setMappings(new ArrayList<>(List.of(mapping)));
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    assertThrows(InvalidConfigException.class, () -> loader.load(resourceDir, null, alog, callbacks));
    verify(action).getText(eq("manage.resource.create.rowType.null"), any(String[].class));
  }

  @Test
  public void testLoadFailsWhenResourceCannotBeRead() {
    when(resourcePersister.load(any(InputStream.class))).thenThrow(new RuntimeException("broken XML"));

    InvalidConfigException e =
        assertThrows(InvalidConfigException.class, () -> loader.load(resourceDir, null, alog, callbacks));
    assertEquals(InvalidConfigException.TYPE.RESOURCE_CONFIG, e.getType());
  }

  @Test
  public void testLoadInfersMissingCoreType() {
    Resource stored = new Resource();
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    loader.load(resourceDir, null, alog, callbacks);

    verify(resourceTypeService).inferCoreType(stored);
  }

  @Test
  public void testLoadKeepsExistingCoreType() {
    Resource stored = new Resource();
    stored.setCoreType("occurrence");
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    loader.load(resourceDir, null, alog, callbacks);

    verify(resourceTypeService, never()).inferCoreType(any());
  }

  @Test
  public void testLoadStandardizesSubtype() {
    Resource stored = new Resource();
    stored.setSubtype("specimen");
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    loader.load(resourceDir, null, alog, callbacks);

    verify(resourceTypeService).standardizeSubtype(stored);
  }

  @Test
  public void testLoadWithoutSubtype() {
    Resource stored = new Resource();
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    loader.load(resourceDir, null, alog, callbacks);

    verify(resourceTypeService, never()).standardizeSubtype(any());
  }

  @Test
  public void testLoadSetsUpSources() throws Exception {
    Resource stored = new Resource();
    TextFileSource fileSource = new TextFileSource();
    fileSource.setName("occurrences");
    fileSource.setProcessing(true);
    SqlSource sqlSource = new SqlSource();
    sqlSource.setName("db");
    sqlSource.setProcessing(true);
    stored.addSource(fileSource, false);
    stored.addSource(sqlSource, false);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertSame(loaded, fileSource.getResource());
    assertSame(loaded, sqlSource.getResource());
    assertFalse(fileSource.isProcessing());
    assertFalse(sqlSource.isProcessing());
    assertEquals(new File(resourceDir, "sources/occurrences.txt"), fileSource.getFile());
  }

  @Test
  public void testLoadSetsMissingIdentifierStatus() {
    Resource stored = new Resource();
    stored.setIdentifierStatus(null);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertEquals(IdentifierStatus.UNRESERVED, loaded.getIdentifierStatus());
  }

  @Test
  public void testLoadDarwinCoreArchiveResource() {
    Resource stored = new Resource();
    stored.setCoreType("occurrence");
    stored.setMetadataVersion(new BigDecimal("3"));
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);
    when(resourceVersioningService.convertVersion(stored)).thenReturn(new BigDecimal("3.0"));

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    verify(resourceMetadataLoader).loadMetadata(loaded);
    verify(resourceVersioningService).updateResourceVersion(loaded, new BigDecimal("3"), new BigDecimal("3.0"));
    verify(callbacks).syncEml(loaded);
    verify(callbacks, never()).save(any());
    verify(resourceVersioningService, never()).restoreDwcDpVersionedEmlFiles(any());
    // not published yet
    verify(resourceVersioningService, never()).renameDwcaToIncludeVersion(any(), any());
  }

  @Test
  public void testLoadDarwinCoreArchiveResourceWithoutVersionConversion() {
    Resource stored = new Resource();
    stored.setMetadataVersion(new BigDecimal("3.0"));
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);
    when(resourceVersioningService.convertVersion(stored)).thenReturn(null);

    loader.load(resourceDir, null, alog, callbacks);

    verify(resourceVersioningService, never()).updateResourceVersion(any(), any(), any());
  }

  @Test
  public void testLoadPublishedDarwinCoreArchiveResourceRenamesDwca() {
    Resource stored = new Resource();
    stored.addVersionHistory(new VersionHistory(new BigDecimal("2.0"), new Date(), PublicationStatus.PUBLIC));
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    verify(resourceVersioningService).renameDwcaToIncludeVersion(loaded, new BigDecimal("2.0"));
  }

  @Test
  public void testLoadAddsVersionHistoryForPre22Resources() {
    Resource stored = new Resource();
    VersionHistory history = new VersionHistory(new BigDecimal("4.0"), new Date(), PublicationStatus.PUBLIC);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);
    when(resourceVersioningService.constructVersionHistoryForLastPublishedVersion(stored)).thenReturn(history);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertEquals(1, loaded.getVersionHistory().size());
    assertSame(history, loaded.getVersionHistory().get(0));
  }

  @Test
  public void testLoadRestoresStrippedLicenseLink() throws Exception {
    Resource stored = new Resource();
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);
    doLoadMetadata(resource -> resource.getEml().setIntellectualRights(
        "This work is licensed under a Creative Commons Attribution (CC-BY 4.0) License."));

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertEquals("http://creativecommons.org/licenses/by/4.0/legalcode", loaded.getEml().parseLicenseUrl());
    assertTrue(dataDir.resourceEmlFile(SHORTNAME).exists());
  }

  @Test
  public void testLoadDataPackageResource() {
    Resource stored = new Resource();
    stored.setCoreType("camtrap-dp");
    stored.setDataPackageIdentifier(CAMTRAP_DP_IDENTIFIER);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    verify(resourceMetadataLoader).loadMetadata(loaded);
    verify(resourceVersioningService, never()).convertVersion(any());
    verify(resourceVersioningService, never()).renameDwcaToIncludeVersion(any(), any());
    verify(resourceVersioningService, never()).restoreDwcDpVersionedEmlFiles(any());
    verify(callbacks, never()).syncEml(any());
    // nothing to fix
    verify(callbacks, never()).save(any());
  }

  @Test
  public void testLoadDataPackageResourceSavesNormalizedIdentifier() {
    DataPackageSchema installed = DataPackageSchema.builder().identifier(DWC_DP_IDENTIFIER).build();
    when(schemaManager.get("http://rs.gbif.org/data-packages/dwc-dp")).thenReturn(installed);
    Resource stored = new Resource();
    stored.setDataPackageIdentifier("http://rs.gbif.org/data-packages/dwc-dp");
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertEquals(DWC_DP_IDENTIFIER, loaded.getDataPackageIdentifier());
    verify(callbacks).save(loaded);
  }

  @Test
  public void testLoadDataPackageResourceSavesBackfilledMappingVersions() {
    when(schemaManager.getVersion(CAMTRAP_DP_IDENTIFIER)).thenReturn("1.0");
    Resource stored = new Resource();
    stored.setDataPackageIdentifier(CAMTRAP_DP_IDENTIFIER);
    DataPackageMapping mapping = new DataPackageMapping();
    mapping.setDataPackageSchema(DataPackageSchema.builder().identifier(CAMTRAP_DP_IDENTIFIER).build());
    stored.addDataPackageMapping(mapping);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    assertEquals("1.0", mapping.getVersion());
    verify(callbacks).save(loaded);
  }

  @Test
  public void testLoadDwcDpResourceRestoresVersionedEmlFiles() {
    Resource stored = new Resource();
    stored.setDataPackageIdentifier(DWC_DP_IDENTIFIER);
    when(resourcePersister.load(any(InputStream.class))).thenReturn(stored);

    Resource loaded = loader.load(resourceDir, null, alog, callbacks);

    verify(resourceVersioningService).restoreDwcDpVersionedEmlFiles(loaded);
  }

  @Test
  public void testLoadInferredEmlMetadata() throws Exception {
    Files.createFile(inferredMetadataFile.toPath());
    InferredEmlMetadata inferred = new InferredEmlMetadata();
    when(resourcePersister.loadInferredEmlMetadata(any(InputStream.class))).thenReturn(inferred);
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);

    loader.loadInferredMetadata(resource);

    assertSame(inferred, resource.getInferredMetadata());
  }

  @Test
  public void testLoadMissingInferredEmlMetadata() {
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setInferredMetadata(null);

    loader.loadInferredMetadata(resource);

    assertInstanceOf(InferredEmlMetadata.class, resource.getInferredMetadata());
    verify(resourcePersister, never()).loadInferredEmlMetadata(any());
  }

  @Test
  public void testLoadUnreadableInferredEmlMetadata() throws Exception {
    Files.createFile(inferredMetadataFile.toPath());
    when(resourcePersister.loadInferredEmlMetadata(any(InputStream.class))).thenThrow(new RuntimeException("broken"));
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setInferredMetadata(null);

    loader.loadInferredMetadata(resource);

    assertInstanceOf(InferredEmlMetadata.class, resource.getInferredMetadata());
  }

  @Test
  public void testLoadInferredCamtrapMetadata() throws Exception {
    Files.createFile(inferredMetadataFile.toPath());
    InferredCamtrapMetadata inferred = new InferredCamtrapMetadata();
    when(resourcePersister.loadInferredCamtrapMetadata(any(InputStream.class))).thenReturn(inferred);
    Resource resource = camtrapResource();

    loader.loadInferredMetadata(resource);

    assertSame(inferred, resource.getInferredMetadata());
    verify(resourcePersister, never()).loadInferredEmlMetadata(any());
  }

  @Test
  public void testLoadMissingInferredCamtrapMetadata() {
    Resource resource = camtrapResource();

    loader.loadInferredMetadata(resource);

    assertInstanceOf(InferredCamtrapMetadata.class, resource.getInferredMetadata());
    verify(resourcePersister, never()).loadInferredCamtrapMetadata(any());
  }

  @Test
  public void testLoadUnreadableInferredCamtrapMetadata() throws Exception {
    Files.createFile(inferredMetadataFile.toPath());
    when(resourcePersister.loadInferredCamtrapMetadata(any(InputStream.class)))
        .thenThrow(new ClassCastException("not Camtrap"));
    Resource resource = camtrapResource();

    loader.loadInferredMetadata(resource);

    assertInstanceOf(InferredCamtrapMetadata.class, resource.getInferredMetadata());
  }

  /**
   * DwC-DP metadata is EML, so is its inferred metadata.
   */
  @Test
  public void testLoadInferredDwcDpMetadata() throws Exception {
    Files.createFile(inferredMetadataFile.toPath());
    InferredEmlMetadata inferred = new InferredEmlMetadata();
    when(resourcePersister.loadInferredEmlMetadata(any(InputStream.class))).thenReturn(inferred);
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCoreType("dwc-dp");
    resource.setDataPackageIdentifier(DWC_DP_IDENTIFIER);

    loader.loadInferredMetadata(resource);

    assertSame(inferred, resource.getInferredMetadata());
    verify(resourcePersister, never()).loadInferredCamtrapMetadata(any());
  }

  /**
   * Metadata is inferred only for Camtrap DP and DwC-DP, so other data packages have no inferred metadata to load.
   */
  @Test
  public void testLoadInferredMetadataSkipsOtherDataPackages() throws Exception {
    Files.createFile(inferredMetadataFile.toPath());
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCoreType(Constants.COL_DP);
    resource.setDataPackageIdentifier(Constants.DATA_PACKAGE_COLDP_IDENTIFIER);
    InferredMetadata before = resource.getInferredMetadata();

    loader.loadInferredMetadata(resource);

    assertSame(before, resource.getInferredMetadata());
    verify(resourcePersister, never()).loadInferredCamtrapMetadata(any());
    verify(resourcePersister, never()).loadInferredEmlMetadata(any());
  }

  private static Resource camtrapResource() {
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCoreType("camtrap-dp");
    resource.setDataPackageIdentifier(CAMTRAP_DP_IDENTIFIER);
    return resource;
  }

  private void doLoadMetadata(Consumer<Resource> metadataLoader) {
    doAnswer(invocation -> {
      metadataLoader.accept(invocation.getArgument(0, Resource.class));
      return null;
    }).when(resourceMetadataLoader).loadMetadata(any(Resource.class));
  }

  private static User user(String email) {
    User user = new User();
    user.setEmail(email);
    return user;
  }

  @Test
  public void testRestoreStrippedLicenseLink() {
    String ccBy = "This work is licensed under a <a href=\"http://creativecommons.org/licenses/by/4.0/legalcode\">"
        + "Creative Commons Attribution (CC-BY 4.0) License</a>.";

    // license anchor stripped by the sanitizer (IPT 3.3.5)
    Eml eml = new Eml();
    eml.setIntellectualRights("This work is licensed under a Creative Commons Attribution (CC-BY 4.0) License.");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals(ccBy, eml.getIntellectualRights());
    assertEquals("http://creativecommons.org/licenses/by/4.0/legalcode", eml.parseLicenseUrl());

    // old-fashioned license name, with the URL as text
    eml.setIntellectualRights("This work is licensed under a Creative Commons Attribution (CC-BY) 4.0 License: "
        + "http://creativecommons.org/licenses/by/4.0\".");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals(ccBy, eml.getIntellectualRights());

    // URL only
    eml.setIntellectualRights("Licensed under https://creativecommons.org/licenses/by-nc/4.0/legalcode");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals("http://creativecommons.org/licenses/by-nc/4.0/legalcode", eml.parseLicenseUrl());

    // CC0, name only
    eml.setIntellectualRights("To the extent possible under law, the publisher has waived all rights to these data "
        + "and has dedicated them to the Public Domain (CC0 1.0).");
    assertTrue(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode", eml.parseLicenseUrl());

    // intact license is left alone
    eml.setIntellectualRights(ccBy);
    assertFalse(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals(ccBy, eml.getIntellectualRights());

    // custom rights statement is left alone
    eml.setIntellectualRights("All rights reserved.");
    assertFalse(ResourceLoaderImpl.restoreStrippedLicenseLink(eml));
    assertEquals("All rights reserved.", eml.getIntellectualRights());

    // missing rights
    assertFalse(ResourceLoaderImpl.restoreStrippedLicenseLink(new Eml()));
  }

  /**
   * A resource with another form of the installed schema identifier gets the installed one, so its mappings, which refer
   * to the installed schema, are used.
   */
  @Test
  public void testNormalizeDataPackageIdentifier() {
    DataPackageSchemaManager schemaManager = mock(DataPackageSchemaManager.class);
    DataPackageSchema installed = DataPackageSchema.builder().identifier("http://rs.tdwg.org/dwc/dwc-dp").build();
    when(schemaManager.get("http://rs.gbif.org/data-packages/dwc-dp")).thenReturn(installed);
    when(schemaManager.get("http://rs.tdwg.org/dwc/dwc-dp")).thenReturn(installed);

    Resource alternative = resource("http://rs.gbif.org/data-packages/dwc-dp");
    assertTrue(ResourceLoaderImpl.normalizeDataPackageIdentifier(alternative, schemaManager));
    assertEquals("http://rs.tdwg.org/dwc/dwc-dp", alternative.getDataPackageIdentifier());

    Resource same = resource("http://rs.tdwg.org/dwc/dwc-dp");
    assertFalse(ResourceLoaderImpl.normalizeDataPackageIdentifier(same, schemaManager));

    Resource notInstalled = resource("http://example.org/unknown");
    assertFalse(ResourceLoaderImpl.normalizeDataPackageIdentifier(notInstalled, schemaManager));
    assertEquals("http://example.org/unknown", notInstalled.getDataPackageIdentifier());
  }

  private static Resource resource(String dataPackageIdentifier) {
    Resource resource = new Resource();
    resource.setShortname("res");
    resource.setDataPackageIdentifier(dataPackageIdentifier);
    return resource;
  }
}
