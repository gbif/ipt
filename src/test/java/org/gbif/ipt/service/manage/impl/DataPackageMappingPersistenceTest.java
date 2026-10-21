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

import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.Resource;
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
import org.gbif.ipt.service.admin.DataPackageSchemaManager;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Data package mappings stored in resource.xml, across a schema update.
 */
public class DataPackageMappingPersistenceTest {

  private static final String IDENTIFIER = "http://example.org/dp";

  private DataPackageSchemaManager schemaManager;
  private XStreamResourcePersister persister;

  @BeforeEach
  public void setUp() {
    schemaManager = mock(DataPackageSchemaManager.class);
    DataPackageIdentifierConverter schemaConverter = new DataPackageIdentifierConverter(schemaManager);
    TableSchemaNameConverter tableSchemaNameConverter = new TableSchemaNameConverter();
    ResourceConvertersManager converters = new ResourceConvertersManager(mock(UserEmailConverter.class),
        mock(OrganisationKeyConverter.class), mock(ExtensionMappingConverter.class),
        mock(ExtensionRowTypeConverter.class), mock(ConceptTermConverter.class), schemaConverter,
        tableSchemaNameConverter, new DataPackageFieldConverter(schemaConverter, tableSchemaNameConverter),
        mock(JdbcInfoConverter.class));
    persister = new XStreamResourcePersister(converters, mock(PasswordEncrypter.class));
  }

  /**
   * A mapping made against version 1.0, loaded with version 2.0 installed: it keeps its version, and the field version
   * 2.0 doesn't have anymore is kept, by name.
   */
  @Test
  public void testMappingAcrossSchemaUpdate() {
    DataPackageSchema v1 = schema("1.0", "eventID", "removedField");
    Resource resource = new Resource();
    resource.setDataPackageIdentifier(IDENTIFIER);
    DataPackageMapping storedMapping = mapping(v1, "eventID", "removedField");
    storedMapping.setVersion("1.0");
    resource.addDataPackageMapping(storedMapping);
    String stored = save(resource);

    DataPackageSchema v2 = schema("2.0", "eventID");
    when(schemaManager.get(IDENTIFIER)).thenReturn(v2);
    when(schemaManager.getVersion(IDENTIFIER)).thenReturn("2.0");
    Resource loaded = load(stored);
    assertFalse(ResourceLoaderImpl.backfillDataPackageMappingVersions(loaded, schemaManager));

    DataPackageMapping mapping = loaded.getDataPackageMappings().get(0);
    assertEquals("1.0", mapping.getVersion());
    assertSame(v2.tableSchemaByName("event").getFields().get(0), mapping.getField("eventID").getField());
    assertEquals(List.of("removedField"), fieldNames(mapping.getUnresolvedFieldMappings()));

    // kept through the next save
    String saved = save(loaded);
    assertTrue(saved.contains("<field>removedField</field>"), saved);
    assertTrue(saved.contains("<version>1.0</version>"), saved);
  }

  /**
   * Resources stored by development versions with a resource data package version still load, without it.
   */
  @Test
  public void testLoadWithResourceDataPackageVersion() {
    DataPackageSchema schema = schema("1.0", "eventID");
    when(schemaManager.get(IDENTIFIER)).thenReturn(schema);
    Resource resource = new Resource();
    resource.setDataPackageIdentifier(IDENTIFIER);
    resource.addDataPackageMapping(mapping(schema, "eventID"));
    String stored = save(resource).replace("</dataPackageIdentifier>",
        "</dataPackageIdentifier>\n  <dataPackageVersion>1.0</dataPackageVersion>");

    String saved = save(load(stored));

    assertTrue(stored.contains("<dataPackageVersion>"), stored);
    assertFalse(saved.contains("<dataPackageVersion>"), saved);
  }

  /**
   * Mappings stored before mappings had a version get the installed version, others keep theirs.
   */
  @Test
  public void testBackfillDataPackageMappingVersions() {
    DataPackageSchema schema = schema("2.0", "eventID");
    Resource resource = new Resource();
    resource.setDataPackageIdentifier(IDENTIFIER);
    DataPackageMapping withoutVersion = mapping(schema, "eventID");
    DataPackageMapping withVersion = mapping(schema, "eventID");
    withVersion.setVersion("1.0");
    resource.addDataPackageMapping(withoutVersion);
    resource.addDataPackageMapping(withVersion);

    // schema not installed: no version to take
    assertFalse(ResourceLoaderImpl.backfillDataPackageMappingVersions(resource, schemaManager));
    assertNull(withoutVersion.getVersion());

    when(schemaManager.getVersion(IDENTIFIER)).thenReturn("2.0");
    assertTrue(ResourceLoaderImpl.backfillDataPackageMappingVersions(resource, schemaManager));
    assertEquals("2.0", withoutVersion.getVersion());
    assertEquals("1.0", withVersion.getVersion());
    assertFalse(ResourceLoaderImpl.backfillDataPackageMappingVersions(resource, schemaManager));
  }

  /**
   * All field mappings are unresolved when the mapped table isn't in the schema (anymore).
   */
  @Test
  public void testUnresolvedFieldMappingsOfRemovedTable() {
    DataPackageMapping mapping = mapping(schema("1.0", "eventID"), "eventID");
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName("removedTable"));

    assertEquals(List.of("eventID"), fieldNames(mapping.getUnresolvedFieldMappings()));
  }

  @Test
  public void testMappingIssues() {
    DataPackageMapping mapping = mapping(schema("1.0", "eventID"), "eventID");
    mapping.getFields().add(new DataPackageFieldMapping(1, field("removedField")));

    assertFalse(mapping.isTableSchemaMissing());
    assertEquals(List.of("removedField"), mapping.getUnresolvedFieldNames());
    assertEquals("? → event", mapping.getLabel());

    // a missing table is reported instead of its fields
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName("removedTable"));
    assertTrue(mapping.isTableSchemaMissing());
    assertTrue(mapping.getUnresolvedFieldNames().isEmpty());
  }

  private Resource load(String xml) {
    return persister.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }

  private String save(Resource resource) {
    StringWriter writer = new StringWriter();
    persister.save(resource, writer);
    return writer.toString();
  }

  private static DataPackageSchema schema(String version, String... fieldNames) {
    DataPackageTableSchema table = new DataPackageTableSchema();
    table.setName("event");
    table.setFields(Stream.of(fieldNames).map(DataPackageMappingPersistenceTest::field).collect(Collectors.toList()));
    return DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version(version).tableSchemas(Set.of(table))
        .build();
  }

  private static DataPackageMapping mapping(DataPackageSchema schema, String... fieldNames) {
    DataPackageMapping mapping = new DataPackageMapping();
    mapping.setDataPackageSchema(schema);
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName("event"));
    List<DataPackageFieldMapping> fields = new ArrayList<>();
    for (int i = 0; i < fieldNames.length; i++) {
      String name = fieldNames[i];
      DataPackageField field = schema.tableSchemaByName("event").getFields().stream()
          .filter(f -> f.getName().equals(name))
          .findFirst()
          .orElseThrow();
      fields.add(new DataPackageFieldMapping(i, field));
    }
    mapping.setFields(fields);
    return mapping;
  }

  private static DataPackageField field(String name) {
    DataPackageField field = new DataPackageField();
    field.setName(name);
    return field;
  }

  private static List<String> fieldNames(List<DataPackageFieldMapping> fieldMappings) {
    return fieldMappings.stream().map(fm -> fm.getField().getName()).collect(Collectors.toList());
  }
}
