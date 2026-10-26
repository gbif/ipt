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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldConstraints;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.impl.JsonServiceImpl;
import org.gbif.ipt.service.manage.impl.MetadataReaderImpl;
import org.gbif.ipt.service.manage.impl.YamlServiceImpl;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.gbif.ipt.config.Constants.COL_DP;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class GenerateDataPackageTest {

  private static final String RESOURCE_SHORTNAME = "dp-res";

  private static final String DEPLOYMENT_SCHEMA = "{"
      + "\"fields\": [{\"name\": \"deploymentID\", \"type\": \"string\"}],"
      + "\"primaryKey\": \"deploymentID\""
      + "}";

  @TempDir
  private File tmpDir;

  private File schemaFile;
  private DataPackageTableSchema tableSchema;
  private DataPackageMapping mapping;
  private GenerateDataPackage generator;
  private SourceManager sourceManager;
  private DataPackageSchemaManager schemaManager;

  @BeforeEach
  public void init() throws Exception {
    // installed table schema; its remote URL is unreachable
    schemaFile = new File(tmpDir, "deployments.json");
    Files.writeString(schemaFile.toPath(), DEPLOYMENT_SCHEMA);

    DataPackageField field = new DataPackageField();
    field.setName("deploymentID");
    field.setType("string");

    tableSchema = new DataPackageTableSchema();
    tableSchema.setName("deployments");
    tableSchema.setUrl(new URL("http://localhost:1/deployments.json"));
    tableSchema.setFields(List.of(field));

    DataPackageSchema dataPackageSchema = DataPackageSchema.builder()
        .name("camtrap-dp")
        .title("Camtrap DP")
        .tableSchemas(Set.of(tableSchema))
        .build();

    TextFileSource source = new TextFileSource();
    source.setName("deployments");
    SourceManager mockSourceManager = mock(SourceManager.class);
    sourceManager = mockSourceManager;
    when(mockSourceManager.rowIterator(source)).thenReturn(new RowIterator(List.<String[]>of(new String[] {"d1"})));

    mapping = new DataPackageMapping();
    mapping.setSource(source);
    mapping.setDataPackageSchema(dataPackageSchema);
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableSchema.getName()));
    mapping.setFields(new ArrayList<>(List.of(new DataPackageFieldMapping(0, field))));

    DataPackageSchemaManager mockSchemaManager = mock(DataPackageSchemaManager.class);
    schemaManager = mockSchemaManager;
    when(mockSchemaManager.getTableSchemaFile(dataPackageSchema, "deployments")).thenReturn(schemaFile);

    DataDir mockDataDir = mock(DataDir.class);
    when(mockDataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(new File(tmpDir, "publication.log"));

    Resource resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);

    File dataPackageFolder = new File(tmpDir, "dp");
    dataPackageFolder.mkdir();

    generator = new GenerateDataPackage(resource, mock(ReportHandler.class), mockDataDir, mockSourceManager,
        mockSchemaManager, mock(AppConfig.class), mock(MetadataReader.class));
    generator.setDataPackageFolder(dataPackageFolder);
  }

  /**
   * The table schema is read from the installed file, not downloaded from its URL.
   */
  @Test
  public void testTableSchemaReadFromInstalledFile() throws Exception {
    generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));

    assertEquals("deploymentID", generator.getDataPackage().getResource("deployments").getSchema().getPrimaryKey());
  }

  /**
   * A ColDP's descriptor has its metadata as frictionless has it: version, created, homepage and keywords, contributors
   * with a title and role, licenses with a name and path, the logo as image, and its tables. Additional properties don't
   * set the descriptor's own.
   */
  @Test
  public void testColDpDescriptor() throws Exception {
    File metadataYaml = new File(tmpDir, "metadata.yaml");
    Files.writeString(metadataYaml.toPath(), String.join("\n",
        "title: Cycads",
        "version: '3'",
        "license: CC BY 4.0",
        "url: https://cycadlist.org",
        "logo: https://cycadlist.org/logo.png",
        "keyword: [cycads]",
        "creator:",
        "  - given: Michael",
        "    family: Calonje",
        "    orcid: 0000-0001-9650-3136",
        "contact:",
        "  given: Michael",
        "  family: Calonje",
        "  email: michaelc@example.org",
        "customField: custom",
        "resources: not-a-resource",
        "contributors: not-contributors",
        ""));

    Resource resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);
    resource.setCoreType(COL_DP);
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(new File(tmpDir, "publication.log"));
    when(dataDir.resourceDatapackageMetadataFile(RESOURCE_SHORTNAME, COL_DP)).thenReturn(metadataYaml);
    GenerateDataPackage colDpGenerator = new GenerateDataPackage(resource, mock(ReportHandler.class), dataDir,
        sourceManager, schemaManager, mock(AppConfig.class),
        new MetadataReaderImpl(new JsonServiceImpl(), new YamlServiceImpl()));
    File dataPackageFolder = new File(tmpDir, "coldp");
    dataPackageFolder.mkdir();
    colDpGenerator.setDataPackageFolder(dataPackageFolder);

    colDpGenerator.addColMetadata();
    // the tables are added to it
    colDpGenerator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));
    File zip = new File(tmpDir, "coldp.zip");
    colDpGenerator.getDataPackage().write(zip, true);

    JsonNode descriptor;
    try (ZipFile zipFile = new ZipFile(zip)) {
      descriptor = new ObjectMapper().readTree(zipFile.getInputStream(zipFile.getEntry("datapackage.json")));
    }
    assertEquals("Cycads", descriptor.get("title").asText());
    assertEquals("3", descriptor.get("version").asText());
    assertTrue(descriptor.get("created").asText().endsWith("Z"));
    assertEquals("https://cycadlist.org", descriptor.get("homepage").asText());
    assertEquals("cycads", descriptor.get("keywords").get(0).asText());
    assertEquals("https://cycadlist.org/logo.png", descriptor.get("image").asText());

    JsonNode contributors = descriptor.get("contributors");
    assertEquals(2, contributors.size());
    assertEquals("Michael Calonje", contributors.get(0).get("title").asText());
    assertEquals("author", contributors.get(0).get("role").asText());
    assertEquals("https://orcid.org/0000-0001-9650-3136", contributors.get(0).get("path").asText());
    assertEquals("maintainer", contributors.get(1).get("role").asText());

    JsonNode license = descriptor.get("licenses").get(0);
    assertEquals("CC-BY-4.0", license.get("name").asText());
    assertEquals("https://creativecommons.org/licenses/by/4.0/", license.get("path").asText());

    assertEquals("custom", descriptor.get("customField").asText());
    assertEquals(1, descriptor.get("resources").size());
    assertEquals("deployments", descriptor.get("resources").get(0).get("name").asText());
  }

  /**
   * A ColDP's archive has its metadata.yaml.
   */
  @Test
  public void testColDpArchiveHasMetadataYaml() throws Exception {
    File published = new File(tmpDir, "datapackage-1.zip");
    GenerateDataPackage colDpGenerator = colDpGeneratorWithTable(
        new MetadataReaderImpl(new JsonServiceImpl(), new YamlServiceImpl()), published);

    colDpGenerator.bundleArchive();

    try (ZipFile zipFile = new ZipFile(published)) {
      String metadataYaml = new String(zipFile.getInputStream(zipFile.getEntry("metadata.yaml")).readAllBytes(),
          StandardCharsets.UTF_8);
      assertTrue(metadataYaml.contains("title: \"Cycads\""), metadataYaml);
    }
  }

  /**
   * A ColDP's archive isn't published without its metadata.yaml.
   */
  @Test
  public void testColDpArchiveNotPublishedWithoutMetadataYaml() throws Exception {
    MetadataReader failingWriter = spy(new MetadataReaderImpl(new JsonServiceImpl(), new YamlServiceImpl()));
    doThrow(new IOException("No space left on device")).when(failingWriter).writeValue(any(Writer.class), any());
    File published = new File(tmpDir, "datapackage-1.zip");
    GenerateDataPackage colDpGenerator = colDpGeneratorWithTable(failingWriter, published);

    assertThrows(GeneratorException.class, colDpGenerator::bundleArchive);
    assertFalse(published.exists());
  }

  /**
   * A ColDP generator with its descriptor and a table, ready to bundle into the published file.
   */
  private GenerateDataPackage colDpGeneratorWithTable(MetadataReader metadataReader, File published)
      throws Exception {
    File metadataYaml = new File(tmpDir, "metadata.yaml");
    Files.writeString(metadataYaml.toPath(), "title: Cycads\nlicense: CC0\n");

    Resource resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);
    resource.setCoreType(COL_DP);
    resource.setDataPackageMetadata(metadataReader.readValue(metadataYaml, ColMetadata.class));
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(new File(tmpDir, "publication.log"));
    when(dataDir.resourceDatapackageMetadataFile(RESOURCE_SHORTNAME, COL_DP)).thenReturn(metadataYaml);
    when(dataDir.tmpFile(anyString(), anyString())).thenReturn(new File(tmpDir, "tmp-datapackage.zip"));
    when(dataDir.resourceDataPackageFile(eq(RESOURCE_SHORTNAME), any())).thenReturn(published);

    GenerateDataPackage colDpGenerator = new GenerateDataPackage(resource, mock(ReportHandler.class), dataDir,
        sourceManager, schemaManager, mock(AppConfig.class), metadataReader);
    File dataPackageFolder = new File(tmpDir, "coldp");
    dataPackageFolder.mkdir();
    colDpGenerator.setDataPackageFolder(dataPackageFolder);
    colDpGenerator.addColMetadata();
    colDpGenerator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));
    return colDpGenerator;
  }

  @Test
  public void testTableSchemaNotInstalled() {
    schemaFile.delete();

    assertThrows(GeneratorException.class,
        () -> generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping)));
  }

  /**
   * Values are written in table schema field order, whatever the order of the field mappings, and field mappings of
   * fields not in the schema (anymore) are left out.
   */
  @Test
  public void testFieldMappingsInSchemaOrder() {
    DataPackageField deploymentID = field("deploymentID");
    DataPackageField locationName = field("locationName");
    DataPackageFieldMapping locationNameMapping = new DataPackageFieldMapping(1, locationName);
    DataPackageFieldMapping deploymentIDMapping = new DataPackageFieldMapping(0, deploymentID);
    DataPackageMapping outOfOrder = new DataPackageMapping();
    outOfOrder.setFields(new ArrayList<>(List.of(
        locationNameMapping, new DataPackageFieldMapping(2, field("removedField")), deploymentIDMapping)));

    List<DataPackageFieldMapping> inSchemaOrder = GenerateDataPackage.fieldMappingsInSchemaOrder(
        List.of(deploymentID, field("unmappedField"), locationName), outOfOrder);

    assertEquals(Arrays.asList(deploymentIDMapping, null, locationNameMapping), inSchemaOrder);
  }

  /**
   * A table schema field without a field mapping, e.g. added by a schema update, is written as an empty column.
   */
  @Test
  public void testFieldWithoutFieldMappingWrittenEmpty() throws Exception {
    tableSchema.setFields(List.of(tableSchema.getFields().get(0), field("locationName")));

    generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));

    List<String> lines = Files.readAllLines(new File(tmpDir, "dp/deployments.csv").toPath());
    assertEquals(List.of("deploymentID,locationName", "d1,"), lines);
  }

  private static DataPackageField field(String name) {
    DataPackageField field = new DataPackageField();
    field.setName(name);
    field.setType("string");
    return field;
  }

  private static DataPackageField enumField(String name, String... values) {
    DataPackageField field = field(name);
    field.setConstraints(DataPackageFieldConstraints.builder().vocabulary(List.of(values)).build());
    return field;
  }

  /**
   * Enum values in another case or with other separators are written as the schema has them; values matching none,
   * of fields without an enum, and that are already as in the enum are left as they are.
   */
  @Test
  public void testEnumValuesNormalised() {
    GenerateDataPackage.EnumValues enumValues = new GenerateDataPackage.EnumValues(List.of(
        enumField("status", "accepted", "provisionally accepted", "ambiguous synonym"),
        enumField("captureMethod", "activityDetection", "timeLapse"),
        field("remarks")));

    String[][] rows = {
        {"ACCEPTED", "TIME_LAPSE", "NATIVE"},
        {"PROVISIONALLY_ACCEPTED", "activity-detection", null},
        {"Ambiguous Synonym", "timeLapse", "x"},
        {"provisionally accepted", "timelapse", null},
        {"acepted", null, null},
    };
    for (String[] row : rows) {
      enumValues.normalise(row);
    }

    assertArrayEquals(new String[] {"accepted", "timeLapse", "NATIVE"}, rows[0]);
    assertArrayEquals(new String[] {"provisionally accepted", "activityDetection", null}, rows[1]);
    assertArrayEquals(new String[] {"ambiguous synonym", "timeLapse", "x"}, rows[2]);
    assertArrayEquals(new String[] {"provisionally accepted", "timeLapse", null}, rows[3]);
    assertArrayEquals(new String[] {"acepted", null, null}, rows[4]);
    assertEquals(List.of(
        "3 value(s) of status written as in its enum, ignoring case and separators (e.g. ACCEPTED → accepted)",
        "3 value(s) of captureMethod written as in its enum, ignoring case and separators (e.g. TIME_LAPSE → timeLapse)"),
        enumValues.report());
  }

  /**
   * Values of fields of other types than string are left as they are, even if they'd match an enum value ignoring
   * case and separators: there a - can be a minus sign or part of a date. A field without a type is a string field.
   */
  @Test
  public void testEnumValuesOfOtherTypesNotNormalised() {
    DataPackageField number = enumField("count", "1", "2");
    number.setType("number");
    DataPackageField integer = enumField("level", "1", "2");
    integer.setType("integer");
    DataPackageField date = enumField("eventDate", "2020-01-01", "2021-01-01");
    date.setType("date");
    DataPackageField untyped = enumField("status", "native", "alien");
    untyped.setType(null);
    GenerateDataPackage.EnumValues enumValues = new GenerateDataPackage.EnumValues(
        List.of(number, integer, date, untyped));
    String[] row = {"-1", "-2", "20200101", "NATIVE"};

    enumValues.normalise(row);

    assertArrayEquals(new String[] {"-1", "-2", "20200101", "native"}, row);
    assertEquals(List.of(
        "1 value(s) of status written as in its enum, ignoring case and separators (e.g. NATIVE → native)"),
        enumValues.report());
  }

  /**
   * The example in the report has the value trimmed, as it's written.
   */
  @Test
  public void testEnumValuesReportedTrimmed() {
    GenerateDataPackage.EnumValues enumValues = new GenerateDataPackage.EnumValues(
        List.of(enumField("status", "native", "alien")));
    String[] untrimmed = {" native "};
    String[] row = {"NATIVE "};

    enumValues.normalise(untrimmed);
    enumValues.normalise(row);

    assertArrayEquals(new String[] {" native "}, untrimmed);
    assertArrayEquals(new String[] {"native"}, row);
    assertEquals(List.of(
        "1 value(s) of status written as in its enum, ignoring case and separators (e.g. NATIVE → native)"),
        enumValues.report());
  }

  /**
   * Enum values that can't be told apart ignoring case and separators are left as they are.
   */
  @Test
  public void testAmbiguousEnumValuesNotNormalised() {
    GenerateDataPackage.EnumValues enumValues = new GenerateDataPackage.EnumValues(
        List.of(enumField("type", "sp-nov", "sp_nov", "other")));
    String[] row = {"SP NOV"};

    enumValues.normalise(row);

    assertArrayEquals(new String[] {"SP NOV"}, row);
    assertTrue(enumValues.report().isEmpty());
  }

  /**
   * Enum values are written to the data resource as the schema has them, after translation, and reported.
   */
  @Test
  public void testEnumValuesWrittenAsInSchema() throws Exception {
    DataPackageField status = enumField("status", "native", "alien");
    tableSchema.setFields(List.of(tableSchema.getFields().get(0), status));
    DataPackageFieldMapping statusMapping = new DataPackageFieldMapping(1, status);
    statusMapping.setTranslation(Map.of("introduced", "ALIEN"));
    mapping.getFields().add(statusMapping);
    when(sourceManager.rowIterator(mapping.getSource())).thenReturn(new RowIterator(List.of(
        new String[] {"d1", "NATIVE"}, new String[] {"d2", "introduced"}, new String[] {"d3", "native"})));

    generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));

    List<String> lines = Files.readAllLines(new File(tmpDir, "dp/deployments.csv").toPath());
    assertEquals(List.of("deploymentID,status", "d1,native", "d2,alien", "d3,native"), lines);
    assertTrue(generator.report().getMessages().stream()
        .anyMatch(m -> m.getMessage().startsWith("2 value(s) of status written as in its enum")));
  }

  /**
   * All invalid values are reported at once, per field, with how many rows have each, not just the first.
   */
  @Test
  public void testAllInvalidValuesReported() throws Exception {
    Files.writeString(schemaFile.toPath(), "{"
        + "\"fields\": ["
        + "{\"name\": \"deploymentID\", \"type\": \"string\", \"constraints\": {\"required\": true}},"
        + "{\"name\": \"status\", \"type\": \"string\", \"constraints\": {\"enum\": [\"native\", \"alien\"]}},"
        + "{\"name\": \"count\", \"type\": \"integer\"}],"
        + "\"primaryKey\": \"deploymentID\""
        + "}");
    DataPackageField status = field("status");
    DataPackageField count = field("count");
    count.setType("integer");
    tableSchema.setFields(List.of(tableSchema.getFields().get(0), status, count));
    mapping.getFields().add(new DataPackageFieldMapping(1, status));
    mapping.getFields().add(new DataPackageFieldMapping(2, count));
    when(sourceManager.rowIterator(mapping.getSource())).thenReturn(new RowIterator(List.of(
        new String[] {"d1", "sp. nov.", "1"}, new String[] {"d2", "native", "many"},
        new String[] {"d3", "sp. nov.", "2"}, new String[] {null, "nom. nud.", "3"})));
    generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));

    GeneratorException e = assertThrows(GeneratorException.class, generator::checkValues);

    assertEquals("Invalid values in 3 field(s) of 1 table(s): deployments.deploymentID, deployments.status, "
        + "deployments.count. Each field's invalid values are listed in the log.", e.getMessage());
    List<String> messages = generator.report().getMessages().stream().map(TaskMessage::getMessage).toList();
    assertTrue(messages.contains("Table deployments, field deploymentID: empty in 1 row, but it's required"),
        messages.toString());
    assertTrue(messages.contains("Table deployments, field status: values not allowed by its enum constraint in 3 rows: "
        + "'sp. nov.' (2 rows), 'nom. nud.' (1 row)"), messages.toString());
    assertTrue(messages.contains("Table deployments, field count: values not of type integer in 1 row: 'many' (1 row)"),
        messages.toString());
  }

  /**
   * A failure is in the messages, so the publication log says why publishing failed, also outside debug mode.
   */
  @Test
  public void testFailureInMessages() {
    Resource resource = new Resource();
    resource.setShortname(RESOURCE_SHORTNAME);
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourcePublicationLogFile(RESOURCE_SHORTNAME)).thenReturn(new File(tmpDir, "publication.log"));
    when(dataDir.tmpDir()).thenReturn(new File(tmpDir, "failing"));
    GenerateDataPackage failing = new GenerateDataPackage(resource, mock(ReportHandler.class), dataDir,
        sourceManager, schemaManager, mock(AppConfig.class), mock(MetadataReader.class));

    assertThrows(GeneratorException.class, failing::call);

    List<TaskMessage> messages = failing.report().getMessages();
    assertEquals("Data Package generation failed: Data package identifier or mappings are not set",
        messages.get(messages.size() - 1).getMessage());
  }

  /**
   * Primary keys more than one row has are all reported, with how many rows have each, with invalid values.
   */
  @Test
  public void testDuplicatePrimaryKeysReported() throws Exception {
    DataPackageField count = field("count");
    count.setType("integer");
    Files.writeString(schemaFile.toPath(), "{"
        + "\"fields\": [{\"name\": \"deploymentID\", \"type\": \"string\"}, {\"name\": \"count\", \"type\": \"integer\"}],"
        + "\"primaryKey\": \"deploymentID\""
        + "}");
    tableSchema.setFields(List.of(tableSchema.getFields().get(0), count));
    mapping.getFields().add(new DataPackageFieldMapping(1, count));
    when(sourceManager.rowIterator(mapping.getSource())).thenReturn(new RowIterator(List.of(
        new String[] {"d2", "1"}, new String[] {"d1", "1"}, new String[] {"d2", "1"}, new String[] {"d3", "many"},
        new String[] {"d1", "1"}, new String[] {"d2", "1"})));
    generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));

    GeneratorException e = assertThrows(GeneratorException.class, generator::checkValues);

    assertTrue(e.getMessage().startsWith("Invalid values in 2 field(s) of 1 table(s): deployments.deploymentID, "
        + "deployments.count."), e.getMessage());
    List<String> messages = generator.report().getMessages().stream().map(TaskMessage::getMessage).toList();
    assertTrue(messages.contains("Table deployments, field deploymentID: values not unique, but it's the primary key, "
        + "in 5 rows: 'd2' (3 rows), 'd1' (2 rows)"), messages.toString());
    assertTrue(messages.contains("Table deployments, field count: values not of type integer in 1 row: 'many' (1 row)"),
        messages.toString());
  }

  /**
   * A table with a primary key but no rows passes.
   */
  @Test
  public void testTableWithoutRowsPasses() throws Exception {
    when(sourceManager.rowIterator(mapping.getSource())).thenReturn(new RowIterator(List.of()));
    generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));

    generator.checkValues();
  }

  /**
   * Valid values pass.
   */
  @Test
  public void testValidValuesPass() throws Exception {
    generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping));

    generator.checkValues();
  }

  /**
   * Only the values of a field with the most rows are listed, the rows of others are counted together.
   */
  @Test
  public void testInvalidValuesWithMostRowsListed() {
    GenerateDataPackage.InvalidValues invalidValues =
        new GenerateDataPackage.InvalidValues("status", "not allowed by its enum constraint");
    for (int i = 0; i < 8; i++) {
      invalidValues.add("v" + i);
    }
    for (int i = 0; i < 3; i++) {
      invalidValues.add("v7");
      invalidValues.add("v6");
    }

    assertEquals("field status: values not allowed by its enum constraint in 14 rows: e.g. 'v6' (4 rows), "
        + "'v7' (4 rows), 'v0' (1 row), 'v1' (1 row), 'v2' (1 row) and other values (3 rows)",
        invalidValues.toString());
  }

  /**
   * A mapping to a table not in the installed schema prevents publication, its data would be left out.
   */
  @Test
  public void testMappingToMissingTableFails() {
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName("removedTable"));

    GeneratorException e = assertThrows(GeneratorException.class,
        () -> generator.checkMappingsResolved(List.of(mapping), mapping.getDataPackageSchema()));
    assertTrue(e.getMessage().contains("deployments → removedTable"), e.getMessage());
  }

  /**
   * Mapped fields not in the installed schema don't prevent publication, but are reported.
   */
  @Test
  public void testMappedFieldsMissingFromSchemaAreReported() throws Exception {
    mapping.getFields().add(new DataPackageFieldMapping(1, field("removedField")));

    generator.checkMappingsResolved(List.of(mapping), mapping.getDataPackageSchema());

    List<TaskMessage> messages = generator.report().getMessages();
    String last = messages.get(messages.size() - 1).getMessage();
    assertTrue(last.contains("deployments → deployments") && last.contains("removedField"), last);
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
