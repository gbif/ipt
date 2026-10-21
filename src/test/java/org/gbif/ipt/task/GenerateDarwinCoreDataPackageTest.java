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
package org.gbif.ipt.task;

import org.gbif.datapackage.DataPackage;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.metadata.eml.ipt.model.Agent;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateDarwinCoreDataPackageTest {

  private static final String RESOURCE_SHORTNAME = "dwc-dp-res";
  private static final ObjectMapper JSON = new ObjectMapper();

  // minimal event table schema: fields in schema order
  private static final String EVENT_SCHEMA = "{"
      + "\"fields\": ["
      + "{\"name\": \"eventID\", \"type\": \"string\"},"
      + "{\"name\": \"eventDate\", \"type\": \"string\"},"
      + "{\"name\": \"locality\", \"type\": \"string\"}"
      + "],"
      + "\"primaryKey\": \"eventID\""
      + "}";

  @TempDir
  private File tmpDir;
  private File dataPackageFolder;

  private DataPackageSchema dataPackageSchema;
  private DataPackageTableSchema eventTableSchema;
  private SourceManager mockSourceManager;
  private DataPackageSchemaManager mockSchemaManager;
  private DataDir mockDataDir;
  private Resource resource;
  private File schemaFile;
  private GenerateDarwinCoreDataPackage generator;

  @BeforeEach
  public void init() throws Exception {
    // installed table schema; its remote URL is unreachable
    schemaFile = new File(tmpDir, "event.json");
    Files.writeString(schemaFile.toPath(), EVENT_SCHEMA);

    eventTableSchema = new DataPackageTableSchema();
    eventTableSchema.setName("event");
    eventTableSchema.setUrl(new URL("http://localhost:1/event.json"));
    eventTableSchema.setFields(List.of(field("eventID"), field("eventDate"), field("locality")));

    dataPackageSchema = DataPackageSchema.builder()
        .name("dwc-dp")
        .title("Darwin Core Data Package")
        .tableSchemas(Set.of(eventTableSchema))
        .build();

    dataPackageFolder = new File(tmpDir, "dp");
    dataPackageFolder.mkdir();

    mockDataDir = mock(DataDir.class);
    when(mockDataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(new File(tmpDir, "publication.log"));

    resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);

    mockSourceManager = mock(SourceManager.class);
    mockSchemaManager = mock(DataPackageSchemaManager.class);
    when(mockSchemaManager.getTableSchemaFile(dataPackageSchema, "event")).thenReturn(schemaFile);

    generator = new GenerateDarwinCoreDataPackage(resource, mock(ReportHandler.class), mockDataDir,
        mockSourceManager, mockSchemaManager, mock(AppConfig.class));
    generator.setDataPackageFolder(dataPackageFolder);
  }

  /**
   * Two mappings of the same table schema with different mapped fields: rows of both mappings must line up with
   * the header, with empty cells for the fields a mapping doesn't provide.
   */
  @Test
  public void testMultipleMappingsOfSameTableSchemaAlignColumns() throws Exception {
    // source A: eventID, eventDate
    Source sourceA = source("a", new String[] {"e1", "2020-01-01"}, new String[] {"e2", "2020-01-02"});
    DataPackageMapping mappingA = mapping(sourceA,
        new DataPackageFieldMapping(0, field("eventID")),
        new DataPackageFieldMapping(1, field("eventDate")));

    // source B: locality, eventID (source column order differs from schema order)
    Source sourceB = source("b", new String[] {"Copenhagen", "e3"});
    DataPackageMapping mappingB = mapping(sourceB,
        new DataPackageFieldMapping(0, field("locality")),
        new DataPackageFieldMapping(1, field("eventID")));

    generator.addDataResource(eventTableSchema, List.of(mappingA, mappingB), true);

    List<String> lines = Files.readAllLines(new File(dataPackageFolder, "event.csv").toPath(), StandardCharsets.UTF_8);
    assertEquals(List.of(
        "eventID,eventDate,locality",
        "e1,2020-01-01,",
        "e2,2020-01-02,",
        "e3,,Copenhagen"), lines);
  }

  /**
   * The table schema is read from the installed file, not downloaded from its URL.
   */
  @Test
  public void testTableSchemaReadFromInstalledFile() throws Exception {
    Source source = source("a", new String[] {"e1"});
    generator.addDataResource(eventTableSchema, List.of(mapping(source, new DataPackageFieldMapping(0, field("eventID")))),
        true);

    org.gbif.datapackage.DataPackageTableSchema schema = generator.getDataPackage().getResources().get(0).getTableSchema();
    assertEquals(List.of("eventID"), schema.getPrimaryKey());
  }

  @Test
  public void testTableSchemaNotInstalled() throws Exception {
    schemaFile.delete();
    Source source = source("a", new String[] {"e1"});

    assertThrows(GeneratorException.class, () -> generator.addDataResource(eventTableSchema,
        List.of(mapping(source, new DataPackageFieldMapping(0, field("eventID")))), true));
  }

  /**
   * The profile, which decides the DwC-DP version the analyser validates against, is the installed schema's.
   */
  @Test
  public void testProfileFromInstalledSchema() throws Exception {
    String profile = "https://rs.tdwg.org/dwc-dp/0.1/dwc-dp-profile.json";
    dataPackageSchema.setProfile(profile);
    mockMetadataFiles();

    generator.addDataPackageMetadata();

    assertEquals(profile, generator.getDataPackage().getProfile());
  }

  @Test
  public void testNoProfileInInstalledSchema() throws Exception {
    mockMetadataFiles();

    assertThrows(GeneratorException.class, () -> generator.addDataPackageMetadata());
  }

  /**
   * The report keeps the step generation failed at, for the publication stepper.
   */
  @Test
  public void testReportHasFailedStep() throws Exception {
    // installed schema without profile: fails while adding metadata
    mockMetadataFiles();
    assertEquals("WAITING", generator.report().getStep());

    assertThrows(GeneratorException.class, () -> generator.call());

    StatusReport report = generator.report();
    assertTrue(report.hasException());
    assertEquals("METADATA", report.getStep());
  }

  /**
   * A failure's root cause is in the messages, shown on the publication page and in the publication log in any mode,
   * and the validation report of a previous publication is deleted.
   */
  @Test
  public void testFailureMessageAndPreviousValidationReport() throws Exception {
    File previousReport = new File(tmpDir, "datapackage-validation-report.json");
    Files.writeString(previousReport.toPath(), "{}");
    when(mockDataDir.resourceDataPackageValidationReportFile(RESOURCE_SHORTNAME)).thenReturn(previousReport);
    // installed schema without profile: fails while adding metadata
    mockMetadataFiles();

    assertThrows(GeneratorException.class, () -> generator.call());

    assertFalse(previousReport.exists());
    String lastMessage = generator.report().getMessages().get(generator.report().getMessages().size() - 1).getMessage();
    assertTrue(lastMessage.startsWith("Data Package generation failed: Data package schema"), lastMessage);
  }

  /**
   * If the analyser crashes, the failure says why, and no validation report is written.
   */
  @Test
  public void testValidationCrash() throws Exception {
    File reportFile = new File(tmpDir, "datapackage-validation-report.json");
    when(mockDataDir.resourceDataPackageValidationReportFile(RESOURCE_SHORTNAME)).thenReturn(reportFile);
    DataPackage dataPackage = mock(DataPackage.class);
    when(dataPackage.validate(dataPackageFolder)).thenThrow(new IOException("Data package validation could not be executed",
        new SQLException("Invalid Input Error: Could not detect CSV dialect")));
    generator.setDataPackage(dataPackage);

    GeneratorException e = assertThrows(GeneratorException.class, () -> generator.validateArchive());

    assertEquals("Data package validation could not be run: Invalid Input Error: Could not detect CSV dialect",
        e.getMessage());
    assertFalse(reportFile.exists());
  }

  /**
   * A mapping to a table not in the installed schema prevents publication, its data would be left out.
   */
  @Test
  public void testMappingToMissingTableFails() throws Exception {
    DataPackageMapping mapping = mapping(source("a"), new DataPackageFieldMapping(0, field("eventID")));
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName("removedTable"));

    GeneratorException e = assertThrows(GeneratorException.class,
        () -> generator.checkMappingsResolved(List.of(mapping), dataPackageSchema));
    assertTrue(e.getMessage().contains("a → removedTable"), e.getMessage());
  }

  /**
   * Mapped fields not in the installed schema don't prevent publication, but are reported.
   */
  @Test
  public void testMappedFieldsMissingFromSchemaAreReported() throws Exception {
    DataPackageMapping mapping = mapping(source("a"),
        new DataPackageFieldMapping(0, field("eventID")), new DataPackageFieldMapping(1, field("removedField")));

    generator.checkMappingsResolved(List.of(mapping), dataPackageSchema);

    List<TaskMessage> messages = generator.report().getMessages();
    String last = messages.get(messages.size() - 1).getMessage();
    assertTrue(last.contains("a → event") && last.contains("removedField"), last);
  }

  private void mockMetadataFiles() throws Exception {
    File metadataFile = new File(tmpDir, "datapackage.json");
    Files.writeString(metadataFile.toPath(), "{\"profile\": \"data-package\"}");
    resource.setDataPackageIdentifier("http://rs.tdwg.org/dwc/dwc-dp");

    when(mockDataDir.resourceDatapackageMetadataFile(RESOURCE_SHORTNAME, resource.getCoreType())).thenReturn(metadataFile);
    when(mockDataDir.resourceEmlFile(RESOURCE_SHORTNAME))
        .thenReturn(org.gbif.utils.file.FileUtils.getClasspathFile("data/eml.xml"));
    when(mockSchemaManager.get("http://rs.tdwg.org/dwc/dwc-dp")).thenReturn(dataPackageSchema);
  }

  /**
   * Contributor roles depend on the EML agent type: creators are authors, contacts maintainers, metadata providers
   * wranglers and associated parties get a role derived from their own EML role.
   */
  @Test
  public void testContributorRolesFromEml() {
    Eml eml = new Eml();
    eml.addCreator(agent("Ada", "Lovelace"));
    eml.addContact(agent("Grace", "Hopper"));
    eml.addMetadataProvider(agent("Charles", "Babbage"));
    eml.addAssociatedParty(agent("Mary", "Somerville", "principalInvestigator"));

    List<Map<String, Object>> contributors = generator.buildContributors(eml);

    assertEquals(4, contributors.size());
    assertEquals("Ada Lovelace", contributors.get(0).get("title"));
    assertEquals("author", contributors.get(0).get("role"));
    assertEquals("Grace Hopper", contributors.get(1).get("title"));
    assertEquals("maintainer", contributors.get(1).get("role"));
    assertEquals("Charles Babbage", contributors.get(2).get("title"));
    assertEquals("wrangler", contributors.get(2).get("role"));
    assertEquals("Mary Somerville", contributors.get(3).get("title"));
    assertEquals("principalInvestigator", contributors.get(3).get("role"));
  }

  /**
   * EML roles with an exact data package equivalent are mapped, other roles are kept verbatim, a missing role
   * becomes "contributor".
   */
  @Test
  public void testAssociatedPartyRole() {
    assertEquals("author", GenerateDarwinCoreDataPackage.associatedPartyRole("author"));
    assertEquals("publisher", GenerateDarwinCoreDataPackage.associatedPartyRole("publisher"));
    assertEquals("maintainer", GenerateDarwinCoreDataPackage.associatedPartyRole("pointOfContact"));
    assertEquals("wrangler", GenerateDarwinCoreDataPackage.associatedPartyRole("metadataProvider"));
    assertEquals("custodianSteward", GenerateDarwinCoreDataPackage.associatedPartyRole("custodianSteward"));
    assertEquals("processor", GenerateDarwinCoreDataPackage.associatedPartyRole(" processor "));
    assertEquals("contributor", GenerateDarwinCoreDataPackage.associatedPartyRole(null));
    assertEquals("contributor", GenerateDarwinCoreDataPackage.associatedPartyRole(" "));
  }

  /**
   * Title is required by the data package schema: falls back to organisation, then position. Agents without any of
   * these are skipped.
   */
  @Test
  public void testContributorTitleFallback() {
    Agent organisationOnly = new Agent();
    organisationOnly.setOrganisation("GBIF");

    Agent positionOnly = new Agent();
    positionOnly.addPosition("Data manager");

    Agent emailOnly = new Agent();
    emailOnly.addEmail("nobody@example.org");

    Eml eml = new Eml();
    eml.addCreator(organisationOnly);
    eml.addCreator(positionOnly);
    eml.addCreator(emailOnly);

    List<Map<String, Object>> contributors = generator.buildContributors(eml);

    assertEquals(2, contributors.size());
    assertEquals("GBIF", contributors.get(0).get("title"));
    assertEquals("GBIF", contributors.get(0).get("organization"));
    assertEquals("Data manager", contributors.get(1).get("title"));
  }

  @Test
  public void testNoContributorsForEmlWithoutAgents() {
    assertTrue(generator.buildContributors(new Eml()).isEmpty());
  }

  /**
   * Keys on unmapped fields are removed together with the fields, keys on mapped fields are kept.
   */
  @Test
  public void testRemoveUnmappedFieldsRemovesTheirKeys() throws Exception {
    org.gbif.datapackage.DataPackageTableSchema schema = keysSchema();

    GenerateDarwinCoreDataPackage.removeUnmappedFields(schema,
        Set.of("event_pk", "eventDate", "eventProtocol_fk", "parentEventID"));

    JsonNode json = JSON.readTree(JSON.writeValueAsString(schema));
    assertEquals(List.of("event_pk", "eventDate", "eventProtocol_fk", "parentEventID"), fieldNames(json));
    assertEquals("[\"event_pk\"]", json.get("primaryKey").toString());
    assertEquals(1, json.get("foreignKeys").size());
    assertEquals("[\"eventProtocol_fk\"]", json.get("foreignKeys").get(0).get("fields").toString());
    // eventID not mapped
    assertFalse(json.has("weakPrimaryKey"));
    assertEquals(1, json.get("weakForeignKeys").size());
    assertEquals("parentEventID", json.get("weakForeignKeys").get(0).get("fields").asText());
  }

  /**
   * Without mapped key fields no key properties are written at all, including an empty primary key.
   */
  @Test
  public void testRemoveUnmappedFieldsWithoutMappedKeyFields() throws Exception {
    org.gbif.datapackage.DataPackageTableSchema schema = keysSchema();

    GenerateDarwinCoreDataPackage.removeUnmappedFields(schema, Set.of("eventDate"));

    JsonNode json = JSON.readTree(JSON.writeValueAsString(schema));
    assertEquals(List.of("eventDate"), fieldNames(json));
    assertFalse(json.has("primaryKey"));
    assertFalse(json.has("foreignKeys"));
    assertFalse(json.has("weakPrimaryKey"));
    assertFalse(json.has("weakForeignKeys"));
  }

  /**
   * Event table schema with all kinds of keys, as in DwC-DP 1.0_DEV.
   */
  private org.gbif.datapackage.DataPackageTableSchema keysSchema() throws Exception {
    File schemaFile = new File(tmpDir, "event-keys.json");
    Files.writeString(schemaFile.toPath(), "{"
        + "\"fields\": ["
        + "{\"name\": \"event_pk\"}, {\"name\": \"eventID\"}, {\"name\": \"eventDate\"},"
        + "{\"name\": \"parentEvent_fk\"}, {\"name\": \"eventProtocol_fk\"},"
        + "{\"name\": \"parentEventID\"}, {\"name\": \"eventConductedByID\"}"
        + "],"
        + "\"primaryKey\": \"event_pk\","
        + "\"weakPrimaryKey\": \"eventID\","
        + "\"foreignKeys\": ["
        + "{\"fields\": \"parentEvent_fk\", \"reference\": {\"resource\": \"\", \"fields\": \"event_pk\"}},"
        + "{\"fields\": \"eventProtocol_fk\", \"reference\": {\"resource\": \"protocol\", \"fields\": \"protocol_pk\"}}"
        + "],"
        + "\"weakForeignKeys\": ["
        + "{\"fields\": \"parentEventID\", \"reference\": {\"resource\": \"\", \"fields\": \"eventID\"}},"
        + "{\"fields\": \"eventConductedByID\", \"reference\": {\"resource\": \"agent\", \"fields\": \"agentID\"}}"
        + "]"
        + "}");
    return org.gbif.datapackage.DataPackageTableSchema.fromFile(schemaFile);
  }

  private static List<String> fieldNames(JsonNode schemaJson) {
    List<String> names = new ArrayList<>();
    schemaJson.get("fields").forEach(f -> names.add(f.get("name").asText()));
    return names;
  }

  private static Agent agent(String firstName, String lastName) {
    return agent(firstName, lastName, null);
  }

  private static Agent agent(String firstName, String lastName, String role) {
    Agent agent = new Agent();
    agent.setFirstName(firstName);
    agent.setLastName(lastName);
    agent.setRole(role);
    return agent;
  }

  private static DataPackageField field(String name) {
    DataPackageField field = new DataPackageField();
    field.setName(name);
    field.setType("string");
    return field;
  }

  private DataPackageMapping mapping(Source source, DataPackageFieldMapping... fieldMappings) {
    DataPackageMapping mapping = new DataPackageMapping();
    mapping.setSource(source);
    mapping.setDataPackageSchema(dataPackageSchema);
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName(eventTableSchema.getName()));
    mapping.setFields(new ArrayList<>(Arrays.asList(fieldMappings)));
    return mapping;
  }

  private Source source(String name, String[]... rows) throws Exception {
    TextFileSource source = new TextFileSource();
    source.setName(name);
    when(mockSourceManager.rowIterator(source)).thenReturn(new RowIterator(List.of(rows)));
    return source;
  }

  /**
   * In-memory source rows.
   */
  private static class RowIterator implements ClosableReportingIterator<String[]> {

    private final Iterator<String[]> rows;

    RowIterator(List<String[]> rows) {
      this.rows = rows.iterator();
    }

    @Override
    public boolean hasNext() {
      return rows.hasNext();
    }

    @Override
    public String[] next() {
      return rows.next();
    }

    @Override
    public boolean hasRowError() {
      return false;
    }

    @Override
    public String getErrorMessage() {
      return null;
    }

    @Override
    public Exception getException() {
      return null;
    }

    @Override
    public void close() {
    }
  }
}
