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
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.impl.JsonServiceImpl;
import org.gbif.ipt.service.manage.impl.MetadataReaderImpl;
import org.gbif.ipt.service.manage.impl.YamlServiceImpl;
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.gbif.ipt.config.Constants.COL_DP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
