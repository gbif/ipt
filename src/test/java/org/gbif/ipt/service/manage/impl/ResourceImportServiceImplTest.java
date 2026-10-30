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
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapContributor;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.Agent;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceImportServiceImplTest {

  private static final String SHORTNAME = "bees";
  private static final String COL_DP_IDENTIFIER = "http://rs.gbif.org/data-packages/coldp";

  @TempDir
  Path tempDir;

  private File resourceDir;
  private DataDir dataDir;
  private DataPackageSchemaManager schemaManager;
  private SourceManager sourceManager;
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

    sourceManager = mock(SourceManager.class);
    service = new ResourceImportServiceImpl(dataDir, sourceManager, mock(ExtensionManager.class),
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
    assertInstanceOf(InferredCamtrapMetadata.class, resource.getInferredMetadata());
    assertEquals("https://rs.gbif.org/sandbox/data-packages/camtrap-dp", resource.getDataPackageIdentifier());
    assertTrue(resource.isInferGeocoverageAutomatically());
    assertTrue(resource.isInferTaxonomicCoverageAutomatically());
    assertTrue(resource.isInferTemporalCoverageAutomatically());
  }

  @Test
  public void testCreateNewColDp() throws Exception {
    when(schemaManager.getSchemaIdentifier(COL_DP)).thenReturn("https://rs.gbif.org/data-packages/coldp");

    Resource resource = service.createNew(SHORTNAME, COL_DP, creator);

    assertInstanceOf(ColMetadata.class, resource.getDataPackageMetadata());
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

  /**
   * A ColDP's metadata can be metadata.json (e.g. from Plazi), it takes precedence over a datapackage.json.
   */
  @Test
  public void testCreateFromColDpArchiveWithMetadataJson() throws Exception {
    File archiveDir = Files.createDirectory(tempDir.resolve("archive")).toFile();
    File metadataJson = Files.writeString(archiveDir.toPath().resolve("Metadata.json"), "{\"title\": \"coldp\"}").toFile();
    File datapackageJson = Files.writeString(archiveDir.toPath().resolve("datapackage.json"), "{}").toFile();
    File dataDirMetadata = new File(resourceDir, "datapackage.json");
    ColMetadata metadata = new ColMetadata();
    when(metadataReader.readValue(dataDirMetadata, ColMetadata.class)).thenReturn(metadata);

    Resource resource = service.createFromFrictionlessDataPackage(
        SHORTNAME, archiveDir, COL_DP, List.of(datapackageJson, metadataJson), creator, alog);

    assertSame(metadata, resource.getDataPackageMetadata());
    assertEquals("{\"title\": \"coldp\"}", Files.readString(dataDirMetadata.toPath()));
  }

  /**
   * ColDP metadata file names aren't looked for in other data packages.
   */
  @Test
  public void testCreateFromCamtrapArchiveIgnoresColDpMetadata() throws Exception {
    File archiveDir = Files.createDirectory(tempDir.resolve("archive")).toFile();
    File metadataJson = Files.writeString(archiveDir.toPath().resolve("metadata.json"), "{\"title\": \"coldp\"}").toFile();
    File datapackageJson = Files.writeString(archiveDir.toPath().resolve("datapackage.json"), "{}").toFile();
    File dataDirMetadata = new File(resourceDir, "datapackage.json");
    CamtrapMetadata metadata = new CamtrapMetadata();
    when(metadataReader.readValue(dataDirMetadata, CamtrapMetadata.class)).thenReturn(metadata);

    service.createFromFrictionlessDataPackage(
        SHORTNAME, archiveDir, CAMTRAP_DP, List.of(metadataJson, datapackageJson), creator, alog);

    assertEquals("{}", Files.readString(dataDirMetadata.toPath()));
  }

  /**
   * A byte order mark at the start of a data file isn't read as part of its first column's name, so the column is
   * mapped (#2303: the observations.csv of the Camtrap DP 1.0 example starts with one).
   */
  @Test
  public void testCreateFromCamtrapArchiveMapsFirstColumnAfterByteOrderMark() throws Exception {
    when(schemaManager.get(CAMTRAP_DP)).thenReturn(DataPackageSchema.builder()
        .identifier("https://rs.gbif.org/sandbox/data-packages/camtrap-dp")
        .name(CAMTRAP_DP)
        .version("1.0")
        .tableSchemas(new LinkedHashSet<>(List.of(table("observations", "observationID", "deploymentID"))))
        .build());
    when(schemaManager.getSchemaIdentifier(CAMTRAP_DP)).thenReturn("https://rs.gbif.org/sandbox/data-packages/camtrap-dp");
    when(sourceManager.add(any(Resource.class), any(File.class), anyString())).thenAnswer(invocation -> {
      TextFileSource source = new TextFileSource();
      source.setName(invocation.getArgument(2, String.class));
      source.setFile(invocation.getArgument(1, File.class));
      source.setFieldsTerminatedBy(",");
      source.setIgnoreHeaderLines(1);
      return source;
    });
    CamtrapMetadata metadata = new CamtrapMetadata();
    metadata.getAdditionalProperties().put("resources",
        List.of(Map.of("name", "observations", "path", "observations.csv")));
    when(metadataReader.readValue(any(File.class), eq(CamtrapMetadata.class))).thenReturn(metadata);

    Path archive = Files.createDirectory(tempDir.resolve("archive"));
    File datapackageJson = Files.writeString(archive.resolve("datapackage.json"), "{}").toFile();
    File observations = Files.write(archive.resolve("observations.csv"),
        "\uFEFFobservationID,deploymentID\no1,d1\n".getBytes(StandardCharsets.UTF_8)).toFile();

    Resource resource = service.createFromFrictionlessDataPackage(SHORTNAME, archive.toFile(), CAMTRAP_DP,
        List.of(datapackageJson, observations), creator, alog);

    List<String> fields = resource.getDataPackageMappings().get(0).getFields().stream()
        .map(f -> f.getIndex() + ":" + f.getField().getName())
        .toList();
    assertEquals(List.of("0:observationID", "1:deploymentID"), fields);
  }

  /**
   * ColDP data files are matched to tables by name in any case and with or without separators, columns ignoring a
   * col: prefix and case. Files in folders and that aren't tables are skipped (like Plazi's).
   */
  @Test
  public void testCreateFromColDpArchiveMatchesFilesAndColumnsByName() throws Exception {
    when(schemaManager.get(COL_DP)).thenReturn(colDpSchema());
    when(schemaManager.getSchemaIdentifier(COL_DP)).thenReturn(COL_DP_IDENTIFIER);
    when(sourceManager.add(any(Resource.class), any(File.class), anyString())).thenAnswer(invocation -> {
      TextFileSource source = new TextFileSource();
      source.setName(invocation.getArgument(2, String.class));
      source.setFile(invocation.getArgument(1, File.class));
      source.setIgnoreHeaderLines(1);
      return source;
    });
    when(metadataReader.readValue(any(File.class), any())).thenReturn(new ColMetadata());

    Path archive = Files.createDirectory(tempDir.resolve("archive"));
    File metadataJson = Files.writeString(archive.resolve("metadata.json"), "{}").toFile();
    File nameUsage = Files.writeString(archive.resolve("name-usage.tsv"),
        "col:ID\tcol:scientificName\tcol_kingdom\nt1\tAus bus\tAnimalia\n").toFile();
    File reference = Files.writeString(archive.resolve("Reference.TSV"), "ID\tCITATION\nr1\tA ref\n").toFile();
    File notATable = Files.writeString(archive.resolve("notes.tsv"), "ID\nn1\n").toFile();
    File treatments = Files.createDirectory(archive.resolve("treatments")).toFile();
    Files.writeString(treatments.toPath().resolve("t1.taxon.html"), "<html/>");

    Resource resource = service.createFromFrictionlessDataPackage(SHORTNAME, archive.toFile(), COL_DP,
        List.of(metadataJson, nameUsage, reference, notATable, treatments), creator, alog);

    Map<String, DataPackageMapping> mappings = resource.getDataPackageMappings().stream()
        .collect(Collectors.toMap(m -> m.getDataPackageTableSchemaName().getName(), m -> m));
    assertEquals(Set.of("name-usage", "reference"), mappings.keySet());

    List<String> nameUsageFields = mappings.get("name-usage").getFields().stream()
        .map(f -> f.getIndex() + ":" + f.getField().getName())
        .toList();
    assertEquals(List.of("0:ID", "1:scientificName"), nameUsageFields);

    List<String> referenceFields = mappings.get("reference").getFields().stream()
        .map(f -> f.getIndex() + ":" + f.getField().getName())
        .toList();
    assertEquals(List.of("0:ID", "1:citation"), referenceFields);

    // tab separated without quoting, as its extension says, whatever its content looks like
    TextFileSource nameUsageSource = (TextFileSource) mappings.get("name-usage").getSource();
    assertEquals("\t", nameUsageSource.getFieldsTerminatedBy());
    assertNull(nameUsageSource.getFieldsEnclosedBy());
    verify(sourceManager).analyze(nameUsageSource);
  }

  /**
   * A ColDP doesn't have to have metadata: it's created with blank metadata, which it can't be published with.
   */
  @Test
  public void testCreateFromColDpArchiveWithoutMetadata() throws Exception {
    when(schemaManager.get(COL_DP)).thenReturn(colDpSchema());
    when(schemaManager.getSchemaIdentifier(COL_DP)).thenReturn(COL_DP_IDENTIFIER);
    when(sourceManager.add(any(Resource.class), any(File.class), anyString())).thenAnswer(invocation -> {
      TextFileSource source = new TextFileSource();
      source.setName(invocation.getArgument(2, String.class));
      source.setFile(invocation.getArgument(1, File.class));
      source.setIgnoreHeaderLines(1);
      return source;
    });

    Path archive = Files.createDirectory(tempDir.resolve("archive"));
    File nameUsage = Files.writeString(archive.resolve("NameUsage.tsv"), "ID\tscientificName\nt1\tAus bus\n").toFile();

    Resource resource = service.createFromFrictionlessDataPackage(SHORTNAME, archive.toFile(), COL_DP,
        List.of(nameUsage), creator, alog);

    assertInstanceOf(ColMetadata.class, resource.getDataPackageMetadata());
    assertNull(((ColMetadata) resource.getDataPackageMetadata()).getTitle());
    assertEquals(1, resource.getDataPackageMappings().size());
    verify(action, atLeastOnce()).getText("manage.resource.dp.create.coldp.no.metadata");
    verify(metadataReader, never()).readValue(any(File.class), any());
  }

  /**
   * Other data packages can't be created without their datapackage.json.
   */
  @Test
  public void testCreateFromCamtrapArchiveWithoutMetadataFails() throws Exception {
    Path archive = Files.createDirectory(tempDir.resolve("archive"));
    File deployments = Files.writeString(archive.resolve("deployments.csv"), "deploymentID\nd1\n").toFile();

    assertThrows(ImportException.class, () -> service.createFromFrictionlessDataPackage(SHORTNAME, archive.toFile(),
        CAMTRAP_DP, List.of(deployments), creator, alog));
  }

  @Test
  public void testColDpCsvIsCommaSeparatedAndQuoted() throws Exception {
    when(schemaManager.get(COL_DP)).thenReturn(colDpSchema());
    when(schemaManager.getSchemaIdentifier(COL_DP)).thenReturn(COL_DP_IDENTIFIER);
    when(sourceManager.add(any(Resource.class), any(File.class), anyString())).thenAnswer(invocation -> {
      TextFileSource source = new TextFileSource();
      source.setName(invocation.getArgument(2, String.class));
      source.setFile(invocation.getArgument(1, File.class));
      source.setIgnoreHeaderLines(1);
      return source;
    });
    when(metadataReader.readValue(any(File.class), any())).thenReturn(new ColMetadata());

    Path archive = Files.createDirectory(tempDir.resolve("archive"));
    File metadataYaml = Files.writeString(archive.resolve("metadata.yaml"), "title: x").toFile();
    File reference = Files.writeString(archive.resolve("reference.csv"), "ID,citation\nr1,\"A, ref\"\n").toFile();

    Resource resource = service.createFromFrictionlessDataPackage(SHORTNAME, archive.toFile(), COL_DP,
        List.of(metadataYaml, reference), creator, alog);

    TextFileSource source = (TextFileSource) resource.getDataPackageMappings().get(0).getSource();
    assertEquals(",", source.getFieldsTerminatedBy());
    assertEquals("\"", source.getFieldsEnclosedBy());
  }

  /**
   * A ColDP's own version (e.g. a Plazi timestamp) isn't taken for the resource's version.
   */
  @Test
  public void testCopyColDpMetadataClearsItsVersion() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("metadata.json"), "{}").toFile();
    ColMetadata metadata = new ColMetadata();
    metadata.setVersion("1792812013496");
    when(metadataReader.readValue(any(File.class), any())).thenReturn(metadata);

    ColMetadata copied = (ColMetadata) service.copyDatapackageMetadata(SHORTNAME, metadataFile, COL_DP, alog);

    assertNull(copied.getVersion());
    // written back, so it isn't read again when the resource is loaded
    verify(metadataReader).writeValue(new File(resourceDir, "datapackage.json"), copied);
  }

  private static Agent agent(String country) {
    Agent agent = new Agent();
    agent.setCountry(country);
    return agent;
  }

  /**
   * Country names of a ColDP's agents, as ChecklistBank exports can have them, are written as their codes. Codes,
   * countries that can't be parsed or aren't a country (the parser takes Europe and Unknown for ZZ, International
   * waters for XZ) and agents without a country are left as they are.
   */
  @Test
  public void testColDpCountriesAsCodes() {
    ColMetadata metadata = new ColMetadata();
    metadata.setCreator(List.of(agent("BE"), agent("Germany")));
    metadata.setPublisher(agent("Belgium"));
    metadata.setContact(agent(null));
    metadata.setEditor(List.of(agent("Europe"), agent("Unknown"), agent("International waters")));
    metadata.setContributor(List.of(agent("Belgium "), agent("Atlantis")));

    List<String[]> countriesAsCodes = ResourceImportServiceImpl.countriesAsCodes(metadata);

    assertEquals("BE", metadata.getCreator().get(0).getCountry());
    assertEquals("DE", metadata.getCreator().get(1).getCountry());
    assertEquals("BE", metadata.getPublisher().getCountry());
    assertEquals(List.of("Europe", "Unknown", "International waters"),
        metadata.getEditor().stream().map(Agent::getCountry).toList());
    assertNull(metadata.getContact().getCountry());
    assertEquals("BE", metadata.getContributor().get(0).getCountry());
    assertEquals("Atlantis", metadata.getContributor().get(1).getCountry());
    assertEquals(List.of("Germany → DE", "Belgium → BE", "Belgium → BE"),
        countriesAsCodes.stream().map(c -> c[0] + " → " + c[1]).toList());
  }

  /**
   * A ColDP's metadata is written back with its country names as their codes, each reported.
   */
  @Test
  public void testCopyColDpMetadataWritesCountriesAsCodes() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("metadata.yaml"), "title: x").toFile();
    ColMetadata metadata = new ColMetadata();
    metadata.setContributor(List.of(agent("Belgium")));
    when(metadataReader.readValue(any(File.class), any())).thenReturn(metadata);

    ColMetadata copied = (ColMetadata) service.copyDatapackageMetadata(SHORTNAME, metadataFile, COL_DP, alog);

    assertEquals("BE", copied.getContributor().get(0).getCountry());
    verify(metadataReader).writeValue(new File(resourceDir, "datapackage.json"), copied);
    verify(action).getText("manage.resource.dp.coldp.country.code", new String[] {"Belgium", "BE"});
  }

  /**
   * An uploaded ColDP metadata file with a country name isn't rejected: it's written as its code when copied.
   */
  @Test
  public void testValidateColDpMetadataFileWithCountryName() throws Exception {
    File metadataFile = FileUtils.getClasspathFile("frictionless/coldp/plazi/metadata.json");
    ColMetadata metadata = new MetadataReaderImpl(new JsonServiceImpl(), new YamlServiceImpl())
        .readValue(metadataFile, ColMetadata.class);
    metadata.setContributor(List.of(agent("Belgium")));
    when(metadataReader.readValue(metadataFile, ColMetadata.class)).thenReturn(metadata);

    service.validateDatapackageMetadataFile(action, metadataFile, ColMetadata.class);

    verify(action, never()).addActionError(anyString());
  }

  /**
   * A ColDP's license GBIF doesn't support is reported when its metadata is copied.
   */
  @Test
  public void testCopyColDpMetadataWarnsOfUnsupportedLicense() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("metadata.yaml"), "title: x").toFile();
    ColMetadata metadata = new ColMetadata();
    metadata.setLicense("CC BY-SA 4.0");
    when(metadataReader.readValue(any(File.class), any())).thenReturn(metadata);

    service.copyDatapackageMetadata(SHORTNAME, metadataFile, COL_DP, alog);

    verify(action).getText("manage.resource.dp.coldp.license.unsupported", new String[] {"CC BY-SA 4.0"});
  }

  /**
   * An imported EML's license GBIF doesn't support is reported, a supported one isn't.
   */
  @Test
  public void testCreateFromEmlWarnsOfUnsupportedLicense() throws Exception {
    File bySa = Files.writeString(tempDir.resolve("by-sa.xml"), emlWithRights(
        "This work is licensed under a <ulink url=\"http://creativecommons.org/licenses/by-sa/4.0/legalcode\">"
            + "<citetitle>Creative Commons Attribution-ShareAlike (CC-BY-SA) 4.0 License</citetitle></ulink>.")).toFile();

    service.createFromEml(SHORTNAME, bySa, creator, alog);

    verify(action).getText("manage.resource.license.unsupported",
        new String[] {"Creative Commons Attribution-ShareAlike (CC-BY-SA) 4.0 License"});
  }

  @Test
  public void testCreateFromEmlWithSupportedLicense() throws Exception {
    File by = Files.writeString(tempDir.resolve("by.xml"), emlWithRights(
        "This work is licensed under a <ulink url=\"http://creativecommons.org/licenses/by/4.0/legalcode\">"
            + "<citetitle>Creative Commons Attribution (CC-BY) 4.0 License</citetitle></ulink>.")).toFile();

    service.createFromEml(SHORTNAME, by, creator, alog);

    verify(action, never()).getText(eq("manage.resource.license.unsupported"), any(String[].class));
  }

  private static String emlWithRights(String intellectualRights) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<eml:eml xmlns:eml=\"https://eml.ecoinformatics.org/eml-2.2.0\" packageId=\"x\" system=\"http://gbif.org\">"
        + "<dataset><title>Licence test</title>"
        + "<intellectualRights><para>" + intellectualRights + "</para></intellectualRights>"
        + "</dataset></eml:eml>";
  }

  @Test
  public void testNormalizeColDpName() {
    assertEquals("nameusage", ResourceImportServiceImpl.normalizeColDpName("NameUsage"));
    assertEquals("nameusage", ResourceImportServiceImpl.normalizeColDpName("name-usage"));
    assertEquals("nameusage", ResourceImportServiceImpl.normalizeColDpName("name_usage"));
  }

  /**
   * File names in the plural are tables too, like in ChecklistBank.
   */
  @Test
  public void testColDpTableForName() {
    Map<String, String> tables = Map.of("speciesinteraction", "species-interaction", "taxon", "taxon",
        "reference", "reference");
    assertEquals("species-interaction", ResourceImportServiceImpl.colDpTableForName("SpeciesInteraction", tables));
    assertEquals("species-interaction", ResourceImportServiceImpl.colDpTableForName("speciesinteractions", tables));
    assertEquals("taxon", ResourceImportServiceImpl.colDpTableForName("Taxa", tables));
    assertEquals("reference", ResourceImportServiceImpl.colDpTableForName("references", tables));
    assertNull(ResourceImportServiceImpl.colDpTableForName("notes", tables));
  }

  private static DataPackageSchema colDpSchema() {
    return DataPackageSchema.builder()
        .identifier(COL_DP_IDENTIFIER)
        .name(COL_DP)
        .version("1.1")
        .tableSchemas(new LinkedHashSet<>(List.of(
            table("name-usage", "ID", "scientificName", "kingdom"),
            table("reference", "ID", "citation"))))
        .build();
  }

  private static DataPackageTableSchema table(String name, String... fieldNames) {
    DataPackageTableSchema table = new DataPackageTableSchema();
    table.setName(name);
    List<DataPackageField> fields = new ArrayList<>();
    for (String fieldName : fieldNames) {
      DataPackageField field = new DataPackageField();
      field.setName(fieldName);
      fields.add(field);
    }
    table.setFields(fields);
    return table;
  }

  @Test
  public void testCopyDatapackageMetadataWithInvalidContent() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    when(metadataReader.readValue(any(File.class), any()))
        .thenThrow(JsonMappingException.fromUnexpectedIOE(new IOException("unexpected")));

    ImportException e = assertThrows(ImportException.class,
        () -> service.copyDatapackageMetadata(SHORTNAME, metadataFile, CAMTRAP_DP, alog));
    assertTrue(e.getMessage().startsWith("Invalid metadata document"), e.getMessage());
  }

  @Test
  public void testCopyUnreadableDatapackageMetadataCleansUp() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{").toFile();
    when(metadataReader.readValue(any(File.class), any())).thenThrow(new IOException("broken"));

    assertThrows(ImportException.class, () -> service.copyDatapackageMetadata(SHORTNAME, metadataFile, CAMTRAP_DP, alog));
    assertFalse(resourceDir.exists());
  }

  @Test
  public void testCopyDatapackageMetadataReadsClassForType() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("datapackage.json"), "{}").toFile();
    CamtrapMetadata metadata = new CamtrapMetadata();
    when(metadataReader.readValue(new File(resourceDir, "datapackage.json"), CamtrapMetadata.class)).thenReturn(metadata);

    DataPackageMetadata copied = service.copyDatapackageMetadata(SHORTNAME, metadataFile, CAMTRAP_DP, alog);

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

  /**
   * ColDP metadata is validated against its own constraints, e.g. a title is required.
   */
  @Test
  public void testValidateInvalidColDpMetadataFile() throws Exception {
    File metadataFile = Files.writeString(tempDir.resolve("metadata.yaml"), "alias: x").toFile();
    when(metadataReader.readValue(metadataFile, ColMetadata.class)).thenReturn(new ColMetadata());
    when(action.getText(eq("validation.datapackage.metadata.coldp.invalid"), any(String[].class)))
        .thenAnswer(invocation -> String.join(": ", invocation.getArgument(1, String[].class)));

    assertThrows(InvalidMetadataException.class,
        () -> service.validateDatapackageMetadataFile(action, metadataFile, ColMetadata.class));

    ArgumentCaptor<String> errors = ArgumentCaptor.forClass(String.class);
    verify(action, atLeastOnce()).addActionError(errors.capture());
    assertTrue(errors.getAllValues().stream().anyMatch(e -> e.startsWith("title: ")), errors.getAllValues().toString());
    assertTrue(errors.getAllValues().stream().anyMatch(e -> e.startsWith("license: ")), errors.getAllValues().toString());
  }

  /**
   * Valid ColDP metadata (Plazi's) passes, with no errors.
   */
  @Test
  public void testValidateValidColDpMetadataFile() throws Exception {
    File metadataFile = FileUtils.getClasspathFile("frictionless/coldp/plazi/metadata.json");
    ColMetadata metadata = new MetadataReaderImpl(new JsonServiceImpl(), new YamlServiceImpl())
        .readValue(metadataFile, ColMetadata.class);
    when(metadataReader.readValue(metadataFile, ColMetadata.class)).thenReturn(metadata);

    service.validateDatapackageMetadataFile(action, metadataFile, ColMetadata.class);

    verify(action, never()).addActionError(anyString());
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
