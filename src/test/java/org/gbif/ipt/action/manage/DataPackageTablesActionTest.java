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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldConstraints;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageFieldReference;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageRowMode;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.DataPackageValueKind;
import org.gbif.ipt.model.TextFileSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataPackageTablesActionTest {

  private static final List<String> COLUMNS = List.of("occurrenceID", "eventID", "eventDate", "locality",
      "scientificName", "organismQuantity", "recordedBy", "identifiedBy");

  private static final List<String[]> SAMPLE = List.of(
      new String[] {"OBS-001", "E1", "2024-05-12", "Fulltofta", "Erithacus rubecula", "2", "Anna Berg | Jonas Lind", "Anna Berg"},
      new String[] {"OBS-002", "E1", "2024-05-12", "Fulltofta", "Fringilla coelebs", "5", "Anna Berg | Jonas Lind", "Anna Berg"},
      new String[] {"OBS-003", "E1", "2024-05-12", "Fulltofta", "Parus major", "1", "Anna Berg", "Anna Berg"},
      new String[] {"OBS-004", "E2", "2024-05-13", "Söderåsen", "Sitta europaea", "1", "Jonas Lind", "Jonas Lind"},
      new String[] {"OBS-005", "E2", "2024-05-15", "Söderåsen", "Dendrocopos major", "2", "Jonas Lind", "Anna Berg"},
      new String[] {"OBS-006", "E3", "2024-05-14", "Stenshuvud", "Phylloscopus collybita", "3", "Anna Berg|Maja Ek", "Maja Ek"});

  @Test
  public void testSuggestionsFromAFlatSource() {
    TextFileSource source = new TextFileSource();
    source.setName("flat.csv");
    List<DataPackageTablesAction.Suggestion> s = DataPackageTablesAction.suggest(schema(), source, COLUMNS, SAMPLE);
    Map<String, DataPackageTablesAction.Suggestion> byId = s.stream()
        .collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    DataPackageTablesAction.Suggestion occ = byId.get("occurrence");
    assertEquals(6, occ.getRows());
    assertEquals(0, occ.getMapping().getField("occurrence_pk").getIndex());
    assertEquals(DataPackageValueKind.LINK, occ.getMapping().getField("event_fk").getValueKind());
    // every row has a count: detected
    assertEquals("detected", occ.getMapping().getField("occurrenceStatus").getDefaultValue());

    DataPackageTablesAction.Suggestion ev = byId.get("event");
    assertEquals(DataPackageRowMode.DISTINCT, ev.getMapping().getRowMode());
    assertEquals(3, ev.getRows());
    assertEquals(3, ev.getMapping().getField("locality").getIndex());
    // eventDate differs inside E2: left out, with a warning
    assertNull(ev.getMapping().getField("eventDate").getIndex());
    assertTrue(ev.getWarnings().get(0).startsWith("eventDate|E2"), ev.getWarnings().toString());
    // required and not in the source: asked for
    assertEquals("eventCategory", ev.getAsks().get(0).getField());

    DataPackageTablesAction.Suggestion agent = byId.get("agent");
    assertEquals(3, agent.getRows());
    assertEquals(List.of(6, 7), agent.getMapping().getSplitColumns());

    // recorders differ inside E1: linked to each occurrence, not to the event
    DataPackageTablesAction.Suggestion recorders = byId.get("occurrence-agent-role-recordedBy");
    assertEquals(9, recorders.getRows());
    assertEquals("manage.tables.why.role.varies", recorders.getWhyKey());
    assertNull(ev.getMapping().getField("recordedBy").getIndex());
    assertFalse(byId.containsKey("event-agent-role-recordedBy"));
    assertEquals(6, byId.get("occurrence-agent-role-identifiedBy").getRows());
    // no catalogue numbers: no material
    assertFalse(byId.containsKey("material"));
  }

  @Test
  public void testRecordersOfTheEventWithTheirIds() {
    TextFileSource source = new TextFileSource();
    source.setName("ids.csv");
    List<String> columns = List.of("occurrenceID", "eventID", "recordedBy", "recordedByID", "identifiedBy");
    List<String[]> sample = List.of(
        new String[] {"O1", "E1", "Anna Berg | Jonas Lind", "orcid-A | orcid-J", "Anna Berg"},
        new String[] {"O2", "E1", "Anna Berg | Jonas Lind", "orcid-A | orcid-J", "Maja Ek"},
        new String[] {"O3", "E2", "Anna Berg", "orcid-A", "Anna Berg"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    // the same recorders inside every event: on the event, the names and IDs as written
    DataPackageTablesAction.Suggestion ev = byId.get("event");
    assertEquals(2, ev.getMapping().getField("recordedBy").getIndex());
    assertEquals(3, ev.getMapping().getField("recordedByID").getIndex());
    assertTrue(ev.getWarnings().isEmpty(), ev.getWarnings().toString());
    DataPackageTablesAction.Suggestion roles = byId.get("event-agent-role-recordedBy");
    assertEquals("manage.tables.why.role.event", roles.getWhyKey());
    assertEquals(DataPackageMapping.WHOLE_ROW, roles.getMapping().getMergeKey());
    assertEquals(DataPackageValueKind.LINK, roles.getMapping().getField("event_fk").getValueKind());
    // E1: Anna, Jonas; E2: Anna; the second row of E1 repeats the first
    assertEquals(3, roles.getRows());
    assertFalse(byId.containsKey("occurrence-agent-role-recordedBy"));

    // a person's ID at the same place as their name; identifiedBy has no IDs
    DataPackageTablesAction.Suggestion agent = byId.get("agent");
    assertEquals("manage.tables.why.agent.ids", agent.getWhyKey());
    DataPackageFieldMapping agentId = agent.getMapping().getField("agentID");
    assertEquals(DataPackageValueKind.PAIRED, agentId.getValueKind());
    assertEquals(Arrays.asList(3, null), agentId.getPairColumns());
    assertTrue(agent.getPreview().stream().anyMatch(r -> Arrays.asList(r).containsAll(List.of("Jonas Lind", "orcid-J"))),
        agent.getPreview().stream().map(Arrays::toString).toList().toString());
  }

  @Test
  public void testSpecimensMakeMaterial() {
    TextFileSource source = new TextFileSource();
    source.setName("specimens.csv");
    List<String> columns = List.of("occurrenceID", "eventID", "institutionCode", "catalogNumber", "preparations",
        "scientificName");
    List<String[]> sample = List.of(
        new String[] {"O1", "E1", "NHMD", "12", "ethanol", "Pica pica"},
        new String[] {"O2", "E1", "NHMD", "13", "dried", "Corvus corax"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    DataPackageTablesAction.Suggestion mat = byId.get("material");
    assertEquals("manage.tables.why.material", mat.getWhyKey());
    assertEquals(2, mat.getRows());
    DataPackageMapping m = mat.getMapping();
    // no materialEntityID: a key from the institution and the catalogue number
    assertEquals("{institutionCode}:{catalogNumber}", m.getField("materialEntity_pk").getTemplate());
    assertEquals(DataPackageValueKind.LINK, m.getField("evidenceForOccurrence_fk").getValueKind());
    assertEquals(DataPackageValueKind.LINK, m.getField("collectionEvent_fk").getValueKind());
    assertEquals(4, m.getField("preparations").getIndex());
    // the identification stays with the occurrence
    assertNull(m.getField("scientificName").getIndex());
    assertEquals(5, byId.get("occurrence").getMapping().getField("scientificName").getIndex());
    assertTrue(DataPackageTablesAction.unused(columns,
        byId.values().stream().map(DataPackageTablesAction.Suggestion::getMapping).toList()).isEmpty());
  }

  @Test
  public void testEventsFromDateAndPlaceWithoutEventId() {
    TextFileSource source = new TextFileSource();
    source.setName("noevent.csv");
    List<String> columns = List.of("occurrenceID", "eventDate", "locality", "scientificName", "individualCount");
    List<String[]> sample = List.of(
        new String[] {"N-001", "2024-07-01", "Ystad", "Pica pica", "3"},
        new String[] {"N-002", "2024-07-01", "Ystad", "Corvus cornix", "1"},
        new String[] {"N-003", "2024-07-02", "Ales stenar", "Alauda arvensis", "1"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    DataPackageTablesAction.Suggestion ev = byId.get("event");
    assertEquals("manage.tables.why.event.made", ev.getWhyKey());
    assertEquals(DataPackageValueKind.TEMPLATE, ev.getMapping().getField("event_pk").getValueKind());
    assertEquals("{eventDate} {locality}", ev.getMapping().getField("event_pk").getTemplate());
    assertEquals(2, ev.getRows());
    // the key columns go to event, not occurrence
    assertEquals(1, ev.getMapping().getField("eventDate").getIndex());
    // occurrence links with the same rule: no fixed event_fk asked for
    DataPackageTablesAction.Suggestion occ = byId.get("occurrence");
    assertEquals(DataPackageValueKind.LINK, occ.getMapping().getField("event_fk").getValueKind());
    assertTrue(occ.getAsks().stream().noneMatch(a -> a.getField().equals("event_fk")));
    // nothing in the schema takes individualCount
    assertEquals(List.of("individualCount"),
        DataPackageTablesAction.unused(columns, byId.values().stream().map(DataPackageTablesAction.Suggestion::getMapping).toList()));
  }

  @Test
  public void testOneEventWithoutDateOrPlace() {
    TextFileSource source = new TextFileSource();
    source.setName("bare");
    List<String> columns = List.of("occurrenceID", "scientificName");
    List<String[]> sample = List.of(new String[] {"B-1", "Pica pica"}, new String[] {"B-2", "Corvus corax"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    DataPackageTablesAction.Suggestion ev = byId.get("event");
    assertEquals("manage.tables.why.event.one", ev.getWhyKey());
    assertEquals(1, ev.getRows());
    // its ID asked for, the source name until then
    assertEquals("bare", ev.getMapping().getField("event_pk").getDefaultValue());
    assertTrue(ev.getAsks().stream().anyMatch(a -> a.getField().equals("event_pk") && a.getValue().equals("bare")));
    assertEquals(DataPackageValueKind.LINK, byId.get("occurrence").getMapping().getField("event_fk").getValueKind());
  }

  /**
   * The parts of DwC-DP 1.0 the suggestions use: recorders on the event, identifiers on the occurrence, material.
   */
  private static DataPackageSchema schema() {
    DataPackageTableSchema event = table("event", "event_pk", List.of("event_pk", "eventID", "eventCategory", "eventDate",
        "locality", "recordedBy", "recordedBy_fk", "recordedByID"), Set.of("event_pk", "eventCategory"));
    DataPackageTableSchema occurrence = table("occurrence", "occurrence_pk",
        List.of("occurrence_pk", "occurrenceID", "event_fk", "occurrenceStatus", "scientificName", "organismQuantity",
            "identifiedBy", "identifiedByID"), Set.of("occurrence_pk", "event_fk", "occurrenceStatus"));
    occurrence.setForeignKeys(List.of(fk("event_fk", "event", "event_pk")));
    DataPackageTableSchema agent = table("agent", "agent_pk", List.of("agent_pk", "agentID", "preferredAgentName"), Set.of("agent_pk"));
    DataPackageTableSchema role = table("occurrence-agent-role", null,
        List.of("occurrence_fk", "agent_fk", "agentRole", "agentRoleOrder"), Set.of("occurrence_fk", "agent_fk", "agentRoleOrder"));
    role.setForeignKeys(List.of(fk("occurrence_fk", "occurrence", "occurrence_pk"), fk("agent_fk", "agent", "agent_pk")));
    DataPackageTableSchema eventRole = table("event-agent-role", null,
        List.of("event_fk", "agent_fk", "agentRole", "agentRoleOrder"), Set.of("event_fk", "agent_fk", "agentRoleOrder"));
    eventRole.setForeignKeys(List.of(fk("event_fk", "event", "event_pk"), fk("agent_fk", "agent", "agent_pk")));
    DataPackageTableSchema material = table("material", "materialEntity_pk",
        List.of("materialEntity_pk", "materialEntityID", "collectionEvent_fk", "evidenceForOccurrence_fk", "institutionCode",
            "collectionCode", "catalogNumber", "preparations", "scientificName"), Set.of("materialEntity_pk"));
    material.setForeignKeys(List.of(fk("collectionEvent_fk", "event", "event_pk"),
        fk("evidenceForOccurrence_fk", "occurrence", "occurrence_pk")));
    return DataPackageSchema.builder().name("dwc-dp").version("1.0")
        .tableSchemas(Set.of(event, occurrence, agent, role, eventRole, material)).build();
  }

  private static DataPackageTableSchema table(String name, String pk, List<String> fields, Set<String> required) {
    DataPackageTableSchema t = new DataPackageTableSchema();
    t.setName(name);
    t.setPrimaryKey(pk);
    List<DataPackageField> fs = new ArrayList<>();
    for (String f : fields) {
      DataPackageField field = new DataPackageField();
      field.setName(f);
      if (required.contains(f)) {
        DataPackageFieldConstraints c = new DataPackageFieldConstraints();
        c.setRequired(true);
        field.setConstraints(c);
      }
      fs.add(field);
    }
    t.setFields(fs);
    return t;
  }

  private static DataPackageTableSchemaForeignKey fk(String field, String table, String targetField) {
    DataPackageTableSchemaForeignKey fk = new DataPackageTableSchemaForeignKey();
    fk.setFields(field);
    DataPackageFieldReference ref = new DataPackageFieldReference();
    ref.setResource(table);
    ref.setFields(targetField);
    fk.setReference(ref);
    return fk;
  }
}
