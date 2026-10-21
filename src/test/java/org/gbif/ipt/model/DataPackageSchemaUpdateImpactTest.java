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
package org.gbif.ipt.model;

import org.gbif.ipt.model.DataPackageSchemaUpdateImpact.MappingImpact;
import org.gbif.ipt.model.DataPackageSchemaUpdateImpact.ResourceImpact;
import org.gbif.ipt.model.DataPackageSchemaUpdateImpact.Severity;
import org.gbif.ipt.model.DataPackageSchemaUpdateImpact.TableChange;
import org.gbif.ipt.model.voc.PublicationMode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataPackageSchemaUpdateImpactTest {

  private static final DataPackageSchema INSTALLED = schema("1.0",
      table("event", field("eventID", true), field("samplingEffort", false), field("locality", false)),
      table("occurrence", field("occurrenceID", true), field("verbatimLabel", false)),
      table("chronometric-age", field("chronometricAgeID", true)));

  private static final DataPackageSchema UPDATE = schema("1.1",
      table("event", field("eventID", true), field("eventType", true), field("locality", true)),
      table("occurrence", field("occurrenceID", true), field("verbatimLabels", false)),
      table("provenance", field("provenanceID", true)));

  @Test
  public void testSchemaChanges() {
    DataPackageSchemaUpdateImpact impact = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of());

    assertEquals(List.of("provenance"), impact.getAddedTables());
    assertEquals(List.of("chronometric-age"), impact.getRemovedTables());

    TableChange event = impact.getTableChanges().get(0);
    assertEquals("event", event.getName());
    assertEquals(List.of("eventType"), event.getAddedFields());
    assertEquals(List.of("samplingEffort"), event.getRemovedFields());
    assertEquals(List.of("eventType", "locality"), event.getNewlyRequiredFields());

    TableChange occurrence = impact.getTableChanges().get(1);
    assertEquals(Map.of("verbatimLabel", "verbatimLabels"), occurrence.getPossiblyRenamedFields());

    assertEquals(2, impact.getAddedFieldsCount());
    assertEquals(2, impact.getRemovedFieldsCount());
    assertEquals(2, impact.getNewlyRequiredFieldsCount());
    assertEquals(1, impact.getPossiblyRenamedFieldsCount());
  }

  @Test
  public void testIsSimilar() {
    assertTrue(TableChange.isSimilar("verbatimLabel", "verbatimLabels"));
    assertTrue(TableChange.isSimilar("event_type", "eventType"));
    // a couple of edits in a short name don't make a rename
    assertFalse(TableChange.isSimilar("sex", "set"));
    assertFalse(TableChange.isSimilar("samplingEffort", "eventType"));
  }

  /**
   * A removed field is paired with the closest similar added field, whatever their order.
   */
  @Test
  public void testPossiblyRenamedToClosestField() {
    DataPackageSchema installed = schema("1.0", table("event", field("eventID", true), field("samplingProtocol", false)));
    DataPackageSchema update = schema("1.1",
        table("event", field("eventID", true), field("samplingProtocolID", false), field("SamplingProtocol", false)));

    TableChange change = DataPackageSchemaUpdateImpact.of(installed, update, List.of()).getTableChanges().get(0);

    assertEquals(Map.of("samplingProtocol", "SamplingProtocol"), change.getPossiblyRenamedFields());
  }

  /**
   * A removed mapped table or a new required field that isn't mapped blocks publication; auto-publishing is flagged.
   */
  @Test
  public void testResourceBlocked() {
    Resource resource = resource("blocked",
        mapping("chrono.csv", "chronometric-age", "chronometricAgeID"),
        mapping("events.csv", "event", "eventID", "locality"));
    resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);

    ResourceImpact impact = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of(resource))
        .getResources().get(0);

    assertEquals(Severity.BLOCKS_PUBLICATION, impact.getSeverity());
    assertTrue(impact.isAutoPublishing());
    MappingImpact chrono = impact.getMappings().get(0);
    assertTrue(chrono.isTableRemoved());
    MappingImpact events = impact.getMappings().get(1);
    assertFalse(events.isTableRemoved());
    // locality is newly required too, but mapped
    assertEquals(List.of("eventType"), events.getMissingRequiredFields());
  }

  /**
   * Removed mapped fields need review; unmapped removed fields don't affect the resource. A removed mapped field
   * possibly renamed to an unmapped field is one change.
   */
  @Test
  public void testResourceNeedsReview() {
    Resource resource = resource("review",
        mapping("occ.csv", "occurrence", "occurrenceID", "verbatimLabel"),
        mapping("events.csv", "event", "eventID", "eventType", "locality", "samplingEffort"));

    ResourceImpact impact = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of(resource))
        .getResources().get(0);

    assertEquals(Severity.NEEDS_REVIEW, impact.getSeverity());
    MappingImpact occurrence = impact.getMappings().get(0);
    assertEquals(Map.of("verbatimLabel", "verbatimLabels"), occurrence.getPossiblyRenamedFields());
    assertTrue(occurrence.getRemovedFields().isEmpty());
    assertTrue(occurrence.getMissingRequiredFields().isEmpty());
    MappingImpact events = impact.getMappings().get(1);
    assertEquals(List.of("samplingEffort"), events.getRemovedFields());
    assertTrue(events.getPossiblyRenamedFields().isEmpty());
    assertEquals(1, impact.getPossiblyRenamedFieldsCount());
    assertEquals(1, impact.getRemovedFieldsCount());
  }

  /**
   * A mapped field possibly renamed to a required field blocks publication until it's mapped, and isn't also reported
   * as a removed field and a missing required field.
   */
  @Test
  public void testRenamedToRequiredField() {
    DataPackageSchema installed = schema("1.0", table("event-media", field("eventID", true), field("mediaID", true)));
    DataPackageSchema update = schema("1.1", table("event-media", field("event_fk", true), field("media_fk", true)));
    Resource resource = resource("renamed", mapping("media.csv", "event-media", "eventID", "mediaID"));

    ResourceImpact impact = DataPackageSchemaUpdateImpact.of(installed, update, List.of(resource))
        .getResources().get(0);

    assertEquals(Severity.BLOCKS_PUBLICATION, impact.getSeverity());
    MappingImpact mapping = impact.getMappings().get(0);
    assertEquals(Map.of("eventID", "event_fk", "mediaID", "media_fk"), mapping.getPossiblyRenamedFields());
    assertEquals(Set.of("event_fk", "media_fk"), mapping.getRequiredRenamedFields());
    assertTrue(mapping.getRemovedFields().isEmpty());
    assertTrue(mapping.getMissingRequiredFields().isEmpty());
    assertEquals(2, impact.getPossiblyRenamedFieldsCount());
    assertEquals(0, impact.getMissingRequiredFieldsCount());
  }

  /**
   * A removed mapped field possibly renamed to a field that's mapped already isn't a rename, just a removed field.
   */
  @Test
  public void testRenameTargetMappedAlready() {
    Resource resource = resource("both", mapping("occ.csv", "occurrence", "occurrenceID", "verbatimLabel", "verbatimLabels"));

    MappingImpact mapping = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of(resource))
        .getResources().get(0).getMappings().get(0);

    assertTrue(mapping.getPossiblyRenamedFields().isEmpty());
    assertEquals(List.of("verbatimLabel"), mapping.getRemovedFields());
  }

  /**
   * A field mapped with a default value only is mapped.
   */
  @Test
  public void testFieldMappedWithDefaultValue() {
    DataPackageMapping events = mapping("events.csv", "event", "eventID", "locality");
    DataPackageFieldMapping eventType = new DataPackageFieldMapping(null, field("eventType", false));
    eventType.setDefaultValue("survey");
    events.getFields().add(eventType);
    Resource resource = resource("default", events);

    ResourceImpact impact = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of(resource))
        .getResources().get(0);

    assertEquals(Severity.NONE, impact.getSeverity());
  }

  /**
   * A field mapping without a column or default value isn't mapped: a required field it maps is missing.
   */
  @Test
  public void testEmptyFieldMappingNotMapped() {
    DataPackageMapping events = mapping("events.csv", "event", "eventID", "locality");
    events.getFields().add(new DataPackageFieldMapping(null, field("eventType", false)));
    Resource resource = resource("empty", events);

    MappingImpact mapping = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of(resource))
        .getResources().get(0).getMappings().get(0);

    assertEquals(List.of("eventType"), mapping.getMissingRequiredFields());
  }

  @Test
  public void testResourceWithoutMappings() {
    DataPackageSchemaUpdateImpact impact = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE,
        List.of(resource("unmapped")));

    assertEquals(Severity.NONE, impact.getSeverity());
    assertNull(impact.getResources().get(0).getTableRequirementsProblem());
    assertEquals(1, impact.getUnaffectedResources().size());
  }

  /**
   * Table requirements the mapped tables already don't meet aren't caused by the update.
   */
  @Test
  public void testTableRequirementsNotMetAlready() {
    DataPackageTableSchemaRequirement requirement = new DataPackageTableSchemaRequirement();
    requirement.setRequired(List.of("event", "occurrence"));
    DataPackageSchema installed = schema("1.0", table("event", field("eventID", true)));
    installed.setTableSchemasRequirements(requirement);
    DataPackageSchema update = schema("1.1", table("event", field("eventID", true)));
    update.setTableSchemasRequirements(requirement);
    Resource resource = resource("requirements", mapping("events.csv", "event", "eventID"));

    ResourceImpact impact = DataPackageSchemaUpdateImpact.of(installed, update, List.of(resource))
        .getResources().get(0);

    assertNull(impact.getTableRequirementsProblem());
    assertEquals(Severity.NONE, impact.getSeverity());
  }

  @Test
  public void testWithoutResources() {
    DataPackageSchemaUpdateImpact impact = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of());

    assertEquals(Severity.NONE, impact.getSeverity());
    assertTrue(impact.getAffectedResources().isEmpty());
    assertTrue(impact.getUnaffectedResources().isEmpty());
  }

  /**
   * Problems the resource already has aren't caused by the update.
   */
  @Test
  public void testExistingProblemsLeftOut() {
    DataPackageSchema installed = schema("1.0", table("event", field("eventID", true), field("eventType", true)));
    DataPackageSchema update = schema("1.1", table("event", field("eventID", true), field("eventType", true)));
    // eventType is required and unmapped in both versions; the table "removed" is already missing
    Resource resource = resource("existing",
        mapping("events.csv", "event", "eventID"),
        mapping("old.csv", "removed", "x"));

    DataPackageSchemaUpdateImpact impact = DataPackageSchemaUpdateImpact.of(installed, update, List.of(resource));

    assertFalse(impact.isSchemaChanged());
    assertEquals(Severity.NONE, impact.getResources().get(0).getSeverity());
    assertEquals(1, impact.getUnaffectedResources().size());
    assertTrue(impact.getAffectedResources().isEmpty());
  }

  /**
   * Mapped tables no longer meeting the table requirements block publication.
   */
  @Test
  public void testTableRequirements() {
    DataPackageSchema installed = schema("1.0", table("event", field("eventID", true)));
    DataPackageSchema update = schema("1.1", table("event", field("eventID", true)));
    DataPackageTableSchemaRequirement requirement = new DataPackageTableSchemaRequirement();
    requirement.setRequired(List.of("event", "occurrence"));
    update.setTableSchemasRequirements(requirement);
    Resource resource = resource("requirements", mapping("events.csv", "event", "eventID"));

    ResourceImpact impact = DataPackageSchemaUpdateImpact.of(installed, update, List.of(resource))
        .getResources().get(0);

    assertEquals(Severity.BLOCKS_PUBLICATION, impact.getSeverity());
    assertTrue(impact.getTableRequirementsProblem().contains("occurrence"), impact.getTableRequirementsProblem());
    assertFalse(impact.isAutoPublishing());
  }

  @Test
  public void testResourcesSortedBySeverity() {
    Resource unaffected = resource("unaffected", mapping("e.csv", "event", "eventID", "eventType", "locality"));
    Resource review = resource("review", mapping("occ.csv", "occurrence", "occurrenceID", "verbatimLabel"));
    Resource blocked = resource("blocked", mapping("chrono.csv", "chronometric-age", "chronometricAgeID"));

    DataPackageSchemaUpdateImpact impact = DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE,
        List.of(unaffected, review, blocked));

    assertEquals(List.of("blocked", "review", "unaffected"),
        impact.getResources().stream().map(r -> r.getResource().getShortname()).toList());
    assertNull(impact.getResources().get(2).getTableRequirementsProblem());
    assertEquals(Severity.BLOCKS_PUBLICATION, impact.getSeverity());
    assertEquals(1, impact.getBlockedResourcesCount());
    assertEquals(1, impact.getResourcesNeedingReviewCount());
    assertEquals(Severity.NONE, DataPackageSchemaUpdateImpact.of(INSTALLED, UPDATE, List.of(unaffected)).getSeverity());
  }

  /**
   * A mapping is affected by the update exactly when it's listed in the impact of the update.
   */
  @Test
  public void testAffects() {
    // only unchanged fields mapped: the renamed field isn't
    assertFalse(DataPackageSchemaUpdateImpact.affects(mapping("occ.csv", "occurrence", "occurrenceID"),
        INSTALLED, UPDATE));
    // mapped field possibly renamed
    assertTrue(DataPackageSchemaUpdateImpact.affects(
        mapping("occ.csv", "occurrence", "occurrenceID", "verbatimLabel"), INSTALLED, UPDATE));
    // newly required field not mapped
    assertTrue(DataPackageSchemaUpdateImpact.affects(mapping("events.csv", "event", "eventID", "locality"),
        INSTALLED, UPDATE));
    // table removed
    assertTrue(DataPackageSchemaUpdateImpact.affects(mapping("ages.csv", "chronometric-age", "chronometricAgeID"),
        INSTALLED, UPDATE));
  }

  private static DataPackageSchema schema(String version, DataPackageTableSchema... tables) {
    return DataPackageSchema.builder()
        .identifier("http://example.org/dp")
        .name("dp")
        .title("DP")
        .version(version)
        .tableSchemas(new LinkedHashSet<>(List.of(tables)))
        .tableSchemasRequirements(new DataPackageTableSchemaRequirement())
        .build();
  }

  private static DataPackageTableSchema table(String name, DataPackageField... fields) {
    DataPackageTableSchema table = new DataPackageTableSchema();
    table.setName(name);
    table.setFields(List.of(fields));
    return table;
  }

  private static DataPackageField field(String name, boolean required) {
    DataPackageField field = new DataPackageField();
    field.setName(name);
    field.setType("string");
    if (required) {
      DataPackageFieldConstraints constraints = new DataPackageFieldConstraints();
      constraints.setRequired(true);
      field.setConstraints(constraints);
    }
    return field;
  }

  private static Resource resource(String shortname, DataPackageMapping... mappings) {
    Resource resource = new Resource();
    resource.setShortname(shortname);
    resource.setDataPackageIdentifier(INSTALLED.getIdentifier());
    for (DataPackageMapping mapping : mappings) {
      mapping.setDataPackageSchema(INSTALLED);
      resource.addDataPackageMapping(mapping);
    }
    return resource;
  }

  /**
   * Mapping of a source to a table, with the given fields mapped to columns.
   */
  private static DataPackageMapping mapping(String sourceName, String tableName, String... mappedFields) {
    TextFileSource source = new TextFileSource();
    source.setName(sourceName);
    DataPackageMapping mapping = new DataPackageMapping();
    mapping.setSource(source);
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableName));
    List<DataPackageFieldMapping> fields = new ArrayList<>();
    for (int i = 0; i < mappedFields.length; i++) {
      fields.add(new DataPackageFieldMapping(i, field(mappedFields[i], false)));
    }
    mapping.setFields(fields);
    return mapping;
  }
}
