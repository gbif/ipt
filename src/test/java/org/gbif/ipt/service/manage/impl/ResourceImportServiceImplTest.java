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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidMetadataException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.metadata.eml.InvalidEmlException;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonMappingException;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceImportServiceImplTest {

  private static final String SHORTNAME = "bees";

  @TempDir
  Path tempDir;

  private File resourceDir;
  private DataDir dataDir;
  private DataPackageSchemaManager schemaManager;
  private MetadataReader metadataReader;
  private ResourceManager resourceManager;
  private Provider<ResourceManager> resourceManagerProvider;
  private BaseAction action;
  private ActionLogger alog;
  private ResourceImportServiceImpl service;
  private User creator;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() throws IOException {
    resourceDir = Files.createDirectory(tempDir.resolve(SHORTNAME)).toFile();
    dataDir = mock(DataDir.class);
    when(dataDir.resourceEmlFile(anyString())).thenAnswer(invocation ->
        tempDir.resolve(invocation.getArgument(0, String.class)).resolve("eml.xml").toFile());
    when(dataDir.resourceDatapackageMetadataFile(anyString(), any())).thenAnswer(invocation ->
        tempDir.resolve(invocation.getArgument(0, String.class)).resolve("datapackage.json").toFile());

    schemaManager = mock(DataPackageSchemaManager.class);
    metadataReader = mock(MetadataReader.class);
    resourceManager = mock(ResourceManager.class);
    resourceManagerProvider = mock(Provider.class);
    when(resourceManagerProvider.get()).thenReturn(resourceManager);
    action = mock(BaseAction.class);
    alog = new ActionLogger(LogManager.getLogger(ResourceImportServiceImplTest.class), action);

    service = new ResourceImportServiceImpl(dataDir, mock(SourceManager.class), mock(ExtensionManager.class),
        schemaManager, metadataReader, resourceManagerProvider);

    creator = new User();
    creator.setEmail("creator@example.org");
  }

  /**
   * The resource manager depends on the import service, so it must only be resolved when used.
   */
  @Test
  public void testResourceManagerIsResolvedLazily() {
    verify(resourceManagerProvider, never()).get();
  }

  @Test
  public void testCreateNew() throws Exception {
    Resource resource = service.createNew("Bees", "occurrence", creator);

    assertEquals("bees", resource.getShortname());
    assertEquals("occurrence", resource.getCoreType());
    assertSame(creator, resource.getCreator());
    assertNotNull(resource.getCreated());
    assertNull(resource.getEml().getDateStamp());
    assertNull(resource.getEml().getPubDate());
    assertNull(resource.getDataPackageIdentifier());
    verify(resourceManager).save(resource);
  }

  @Test
  public void testCreateNewWithExistingShortname() {
    when(resourceManager.get(SHORTNAME)).thenReturn(new Resource());

    assertThrows(AlreadyExistingException.class, () -> service.createNew(SHORTNAME, "occurrence", creator));
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testCreateNewCamtrapDp() throws Exception {
    when(schemaManager.getSchemaIdentifier(CAMTRAP_DP)).thenReturn("https://rs.gbif.org/sandbox/data-packages/camtrap-dp");

    Resource resource = service.createNew(SHORTNAME, CAMTRAP_DP, creator);

    assertInstanceOf(CamtrapMetadata.class, resource.getDataPackageMetadata());
    assertEquals("https://rs.gbif.org/sandbox/data-packages/camtrap-dp", resource.getDataPackageIdentifier());
    assertTrue(resource.isInferGeocoverageAutomatically());
    assertTrue(resource.isInferTaxonomicCoverageAutomatically());
    assertTrue(resource.isInferTemporalCoverageAutomatically());
  }

  @Test
  public void testCreateNewColDp() throws Exception {
    when(schemaManager.getSchemaIdentifier(COL_DP)).thenReturn("https://rs.gbif.org/data-packages/coldp");

    Resource resource = service.createNew(SHORTNAME, COL_DP, creator);

    assertInstanceOf(FrictionlessColMetadata.class, resource.getDataPackageMetadata());
    assertEquals("https://rs.gbif.org/data-packages/coldp", resource.getDataPackageIdentifier());
  }

  @Test
  public void testCreateNewReturnsNullWhenSaveFails() throws Exception {
    doThrow(new InvalidConfigException(InvalidConfigException.TYPE.CONFIG_WRITE, "disk full"))
        .when(resourceManager).save(any());

    assertNull(service.createNew(SHORTNAME, "occurrence", creator));
  }

  @Test
  public void testCreateFromEml() throws Exception {
    File emlFile = FileUtils.getClasspathFile("data/eml.xml");

    Resource resource = service.createFromEml(SHORTNAME, emlFile, creator, alog);

    assertEquals(Constants.DATASET_TYPE_METADATA_IDENTIFIER, resource.getCoreType());
    assertEquals("WII Herbarium Dataset", resource.getEml().getTitle());
    assertNotNull(resource.getMetadataModified());
    // copied into the resource directory
    assertTrue(new File(resourceDir, "eml.xml").exists());
    verify(resourceManager).save(resource);
  }

  @Test
  public void testCreateFromEmlWithExistingShortname() throws Exception {
    when(resourceManager.get(SHORTNAME)).thenReturn(new Resource());
    File emlFile = FileUtils.getClasspathFile("data/eml.xml");

    assertThrows(AlreadyExistingException.class, () -> service.createFromEml(SHORTNAME, emlFile, creator, alog));
    assertFalse(new File(resourceDir, "eml.xml").exists());
  }

  @Test
  public void testCreateFromInvalidEml() throws Exception {
    File emlFile = Files.writeString(tempDir.resolve("invalid.xml"), "<eml><not-closed>", StandardCharsets.UTF_8).toFile();

    assertThrows(ImportException.class, () -> service.createFromEml(SHORTNAME, emlFile, creator, alog));
    verify(action, atLeastOnce()).getText("manage.resource.create.failed");
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testCopyMetadata() throws Exception {
    Eml eml = service.copyMetadata(SHORTNAME, FileUtils.getClasspathFile("data/eml.xml"));

    assertEquals("WII Herbarium Dataset", eml.getTitle());
    assertTrue(new File(resourceDir, "eml.xml").exists());
  }

  @Test
  public void testCopyMissingMetadata() throws Exception {
    Eml eml = service.copyMetadata(SHORTNAME, tempDir.resolve("missing.xml").toFile());

    assertNotNull(eml);
    assertNull(eml.getTitle());
  }

  @Test
  public void testCopyInvalidMetadataCleansUp() throws Exception {
    File emlFile = Files.writeString(tempDir.resolve("invalid.xml"), "<eml><not-closed>", StandardCharsets.UTF_8).toFile();

    assertThrows(ImportException.class, () -> service.copyMetadata(SHORTNAME, emlFile));
    // the directory only contained the invalid EML file
    assertFalse(resourceDir.exists());
  }

  @Test
  public void testCreateFromPackageDescriptor() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    FrictionlessMetadata<?, ?, ?> metadata = new FrictionlessMetadata<>();
    metadata.setName("original-name");
    metadata.setId("original-id");
    metadata.getAdditionalProperties().put("custom", "value");
    when(metadataReader.readValue(any(File.class), any())).thenReturn(metadata);

    Resource resource = service.createFromPackageDescriptor(SHORTNAME, "dwc-dp", metadataFile, creator, alog);

    assertSame(metadata, resource.getDataPackageMetadata());
    assertEquals(SHORTNAME, metadata.getName());
    assertNull(metadata.getId());
    assertNull(metadata.getCreated());
    assertTrue(metadata.getAdditionalProperties().isEmpty());
    assertEquals("dwc-dp", resource.getCoreType());
    assertNotNull(resource.getMetadataModified());
    assertTrue(new File(resourceDir, "datapackage.json").exists());
  }

  @Test
  public void testCreateFromInvalidPackageDescriptor() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{").toFile();
    when(metadataReader.readValue(any(File.class), any())).thenThrow(new IOException("broken"));

    assertThrows(ImportException.class,
        () -> service.createFromPackageDescriptor(SHORTNAME, "dwc-dp", metadataFile, creator, alog));
    verify(action, atLeastOnce()).getText("manage.resource.create.failed");
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testCreateFromColDpMetadata() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("metadata.yaml"), "title: x").toFile();
    ColMetadata metadata = new ColMetadata();
    when(metadataReader.readValue(any(File.class), any())).thenReturn(metadata);

    Resource resource = service.createFromColDpMetadata(SHORTNAME, metadataFile, creator, alog);

    assertSame(metadata, resource.getDataPackageMetadata());
    assertEquals(COL_DP, resource.getCoreType());
    verify(metadataReader).readValue(any(File.class), any());
  }

  @Test
  public void testCopyDatapackageMetadataWithInvalidContent() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    when(metadataReader.readValue(any(File.class), any()))
        .thenThrow(JsonMappingException.fromUnexpectedIOE(new IOException("unexpected")));

    ImportException e = assertThrows(ImportException.class,
        () -> service.copyDatapackageMetadata(SHORTNAME, metadataFile, CAMTRAP_DP));
    assertTrue(e.getMessage().startsWith("Invalid metadata document"), e.getMessage());
  }

  @Test
  public void testCopyUnreadableDatapackageMetadataCleansUp() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{").toFile();
    when(metadataReader.readValue(any(File.class), any())).thenThrow(new IOException("broken"));

    assertThrows(ImportException.class, () -> service.copyDatapackageMetadata(SHORTNAME, metadataFile, CAMTRAP_DP));
    assertFalse(resourceDir.exists());
  }

  @Test
  public void testCopyDatapackageMetadataReadsClassForType() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    CamtrapMetadata metadata = new CamtrapMetadata();
    when(metadataReader.readValue(new File(resourceDir, "datapackage.json"), CamtrapMetadata.class)).thenReturn(metadata);

    DataPackageMetadata copied = service.copyDatapackageMetadata(SHORTNAME, metadataFile, CAMTRAP_DP);

    assertSame(metadata, copied);
  }

  @Test
  public void testValidateEmlFileWithUnsupportedNamespace() throws Exception {
    File emlFile = Files.writeString(tempDir.resolve("eml.xml"),
        "<eml:eml xmlns:eml=\"https://example.org/unknown\"/>", StandardCharsets.UTF_8).toFile();

    assertThrows(InvalidEmlException.class, () -> service.validateEmlFile(emlFile));
  }

  @Test
  public void testValidateDatapackageMetadataFileWithInvalidMetadata() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    when(metadataReader.readValue(metadataFile, CamtrapMetadata.class)).thenReturn(new CamtrapMetadata());
    // the validator reports problems as action errors
    when(action.hasActionErrors()).thenReturn(true);

    assertThrows(InvalidMetadataException.class,
        () -> service.validateDatapackageMetadataFile(action, metadataFile, CamtrapMetadata.class));
  }

  @Test
  public void testValidateDatapackageMetadataFileWithValidMetadata() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    when(metadataReader.readValue(metadataFile, CamtrapMetadata.class)).thenReturn(new CamtrapMetadata());

    service.validateDatapackageMetadataFile(action, metadataFile, CamtrapMetadata.class);

    verify(metadataReader).readValue(metadataFile, CamtrapMetadata.class);
    verify(metadataReader, never()).readValue(metadataFile, ColMetadata.class);
  }

  @Test
  public void testValidateFrictionlessColDpMetadataFileAlsoValidatesColMetadata() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("metadata.yaml"), "title: x").toFile();
    when(metadataReader.readValue(metadataFile, FrictionlessColMetadata.class)).thenReturn(new FrictionlessColMetadata());
    when(metadataReader.readValue(metadataFile, ColMetadata.class)).thenReturn(new ColMetadata());

    service.validateDatapackageMetadataFile(action, metadataFile, FrictionlessColMetadata.class);

    verify(metadataReader).readValue(metadataFile, ColMetadata.class);
  }

  @Test
  public void testInferNameFieldsForCamtrapContributor() {
    CamtrapContributor fullName = contributor("Jane Mary Doe");
    service.inferNameFieldsForCamtrapContributor(fullName);
    assertEquals("Jane", fullName.getFirstName());
    assertEquals("Mary Doe", fullName.getLastName());

    CamtrapContributor singleName = contributor("  Plato ");
    service.inferNameFieldsForCamtrapContributor(singleName);
    assertNull(singleName.getFirstName());
    assertEquals("Plato", singleName.getLastName());

    CamtrapContributor blank = contributor("   ");
    service.inferNameFieldsForCamtrapContributor(blank);
    assertNull(blank.getFirstName());
    assertNull(blank.getLastName());
  }

  private static CamtrapContributor contributor(String title) {
    CamtrapContributor contributor = new CamtrapContributor();
    contributor.setTitle(title);
    return contributor;
  }
}
