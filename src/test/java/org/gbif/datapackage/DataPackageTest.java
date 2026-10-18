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
package org.gbif.datapackage;

import org.gbif.dp.analysis.api.DatapackageAnalysisResult;
import org.gbif.dp.validator.api.DescriptorViolationType;
import org.gbif.dp.validator.api.ValidationIssue;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataPackageTest {

  private static final String PROFILE = "https://dwc-prerelease.rs.tdwg.org/dwc-dp/1.0_DEV/dwc-dp-profile.json";

  private static final String EVENT_SCHEMA = "{"
      + "\"fields\": ["
      + field("event_pk", "Event (Primary Key)", "http://rs.tdwg.org/dwc/terms/eventID") + ","
      + field("eventCategory", "Event Category", "http://rs.tdwg.org/dwc/terms/eventType") + ","
      + field("eventProtocol_fk", "Event Protocol (Foreign Key)", "http://rs.tdwg.org/dwc/terms/protocolID")
      + "],"
      + "\"primaryKey\": \"event_pk\","
      + "\"foreignKeys\": [{\"fields\": \"eventProtocol_fk\","
      + " \"reference\": {\"resource\": \"protocol\", \"fields\": \"protocol_pk\"}}]"
      + "}";

  private static final String PROTOCOL_SCHEMA = "{"
      + "\"fields\": ["
      + field("protocol_pk", "Protocol (Primary Key)", "http://rs.tdwg.org/dwc/terms/protocolID")
      + "],"
      + "\"primaryKey\": \"protocol_pk\""
      + "}";

  @TempDir
  private File dir;

  /**
   * A foreign key to a table that is not in the package is an error for packages published by the IPT
   * (the analyser default is a warning).
   */
  @Test
  public void testForeignKeyToMissingTableIsError() throws Exception {
    DataPackage dataPackage = dataPackage();
    dataPackage.addResource(resource("event", EVENT_SCHEMA, "event_pk,eventCategory,eventProtocol_fk\ne1,event,p1\n"));

    DatapackageAnalysisResult result = dataPackage.validate(dir).result();

    assertFalse(result.descriptorValidation().valid());
    List<ValidationIssue> fkIssues = foreignKeyIssues(result);
    assertEquals(1, fkIssues.size());
    assertEquals(ValidationIssue.Severity.ERROR, fkIssues.get(0).severity());
  }

  @Test
  public void testForeignKeyToTableInPackageIsNotReported() throws Exception {
    DataPackage dataPackage = dataPackage();
    dataPackage.addResource(resource("event", EVENT_SCHEMA, "event_pk,eventCategory,eventProtocol_fk\ne1,event,p1\n"));
    dataPackage.addResource(resource("protocol", PROTOCOL_SCHEMA, "protocol_pk\np1\n"));

    DatapackageAnalysisResult result = dataPackage.validate(dir).result();

    assertTrue(foreignKeyIssues(result).isEmpty());
  }

  private DataPackage dataPackage() throws Exception {
    Files.copy(FileUtils.getClasspathFile("data/eml.xml").toPath(), new File(dir, "eml.xml").toPath());

    DataPackage dataPackage = new DataPackage();
    dataPackage.setName("fk-test");
    dataPackage.setProfile(PROFILE);
    dataPackage.setProperty("contributors", List.of(Map.of("title", "Test", "role", "author")));
    return dataPackage;
  }

  private DataPackageResource resource(String name, String schemaJson, String csv) throws Exception {
    File schemaFile = new File(dir, name + "-schema.json");
    Files.writeString(schemaFile.toPath(), schemaJson);
    Files.writeString(new File(dir, name + ".csv").toPath(), csv);

    DataPackageResource resource = DataPackageResource.fromFile(name, name + ".csv");
    resource.setTableSchema(DataPackageTableSchema.fromFile(schemaFile));
    resource.setInlineSchema(true);
    return resource;
  }

  private static List<ValidationIssue> foreignKeyIssues(DatapackageAnalysisResult result) {
    return result.descriptorValidation().issues().stream()
        .filter(i -> DescriptorViolationType.FK_UNKNOWN_REFERENCE_RESOURCE.name().equals(i.code()))
        .toList();
  }

  private static String field(String name, String title, String isVersionOf) {
    return "{\"name\": \"" + name + "\", \"title\": \"" + title + "\", \"description\": \"" + title + "\","
        + " \"type\": \"string\", \"dcterms:isVersionOf\": \"" + isVersionOf + "\"}";
  }
}
