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
import org.gbif.utils.file.ClosableReportingIterator;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    when(mockSourceManager.rowIterator(source)).thenReturn(new RowIterator(List.<String[]>of(new String[] {"d1"})));

    mapping = new DataPackageMapping();
    mapping.setSource(source);
    mapping.setDataPackageSchema(dataPackageSchema);
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableSchema.getName()));
    mapping.setFields(new ArrayList<>(List.of(new DataPackageFieldMapping(0, field))));

    DataPackageSchemaManager mockSchemaManager = mock(DataPackageSchemaManager.class);
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

  @Test
  public void testTableSchemaNotInstalled() {
    schemaFile.delete();

    assertThrows(GeneratorException.class,
        () -> generator.addDataResource("camtrap-dp", tableSchema, List.of(mapping)));
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
