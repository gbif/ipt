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

import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageFieldReference;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageRowMode;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.DataPackageValueKind;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataPackageRowBuilderTest {

  // occurrenceID, eventID, recordedBy
  private static final List<String> COLUMNS = List.of("occurrenceID", "eventID", "recordedBy");
  private static final String[] ROW = {"OBS-1", "E1", "Anna Berg |  Jonas  Lind"};

  private DataPackageSchema schema;
  private Source source;

  @BeforeEach
  public void init() {
    DataPackageTableSchema agent = table("agent", "agent_pk", List.of("agent_pk", "preferredAgentName"));
    DataPackageTableSchema occurrence = table("occurrence", "occurrence_pk", List.of("occurrence_pk", "event_fk"));
    DataPackageTableSchema role = table("occurrence-agent-role", null,
        List.of("occurrence_fk", "agent_fk", "agentRole", "agentRoleOrder"));
    role.setForeignKeys(List.of(fk("occurrence_fk", "occurrence", "occurrence_pk"), fk("agent_fk", "agent", "agent_pk")));
    schema = DataPackageSchema.builder().name("dwc-dp").tableSchemas(Set.of(agent, occurrence, role)).build();
    TextFileSource s = new TextFileSource();
    s.setName("flat.csv");
    source = s;
  }

  @Test
  public void testOneRowPerItemWithPositionsAndNormalizedNames() {
    DataPackageMapping agent = mapping("agent", source,
        kind("agent_pk", DataPackageValueKind.ID), kind("preferredAgentName", DataPackageValueKind.ITEM));
    agent.setRowMode(DataPackageRowMode.ITEMS);
    agent.setSplitColumns(List.of(2));

    DataPackageRowBuilder b = new DataPackageRowBuilder(agent, List.of("agent_pk", "preferredAgentName"), List.of(agent), COLUMNS);
    assertTrue(b.getProblems().isEmpty(), b.getProblems().toString());
    List<String[]> rows = b.build(ROW);
    assertEquals(2, rows.size());
    assertEquals("Anna Berg", rows.get(0)[1]);
    assertEquals("Jonas Lind", rows.get(1)[1]);
    assertEquals(DataPackageRowBuilder.key("agent", "Anna Berg"), rows.get(0)[0]);
    // UUID version 5
    assertEquals(5, UUID.fromString(rows.get(0)[0]).version());
  }

  @Test
  public void testKeysAreStableAndPerTable() {
    assertEquals(DataPackageRowBuilder.key("agent", "Anna Berg"), DataPackageRowBuilder.key("agent", " Anna   Berg "));
    assertNotEquals(DataPackageRowBuilder.key("agent", "Anna Berg"), DataPackageRowBuilder.key("event", "Anna Berg"));
    assertNotEquals(DataPackageRowBuilder.key("agent", "Anna Berg"), DataPackageRowBuilder.key("agent", "A. Berg"));
  }

  @Test
  public void testLinkedKeysMatchTheLinkedTable() {
    DataPackageMapping agent = mapping("agent", source, kind("agent_pk", DataPackageValueKind.ID));
    agent.setRowMode(DataPackageRowMode.ITEMS);
    agent.setSplitColumns(List.of(2));
    DataPackageMapping occurrence = mapping("occurrence", source, column("occurrence_pk", 0));
    DataPackageMapping role = mapping("occurrence-agent-role", source,
        kind("occurrence_fk", DataPackageValueKind.LINK), kind("agent_fk", DataPackageValueKind.LINK),
        constant("agentRole", "recorder"), kind("agentRoleOrder", DataPackageValueKind.POSITION));
    role.setRowMode(DataPackageRowMode.ITEMS);
    role.setSplitColumns(List.of(2));
    List<DataPackageMapping> all = List.of(agent, occurrence, role);

    DataPackageRowBuilder b = new DataPackageRowBuilder(role,
        List.of("occurrence_fk", "agent_fk", "agentRole", "agentRoleOrder"), all, COLUMNS);
    assertTrue(b.getProblems().isEmpty(), b.getProblems().toString());
    List<String[]> rows = b.build(ROW);
    assertArrayEquals(new String[] {"OBS-1", DataPackageRowBuilder.key("agent", "Jonas Lind"), "recorder", "2"}, rows.get(1));

    List<String[]> agents = new DataPackageRowBuilder(agent, List.of("agent_pk"), all, COLUMNS).build(ROW);
    assertEquals(agents.get(1)[0], rows.get(1)[1]);
  }

  @Test
  public void testLinkToAnotherSourceIsAProblem() {
    TextFileSource other = new TextFileSource();
    other.setName("agents.csv");
    DataPackageMapping agent = mapping("agent", other, column("agent_pk", 0));
    DataPackageMapping role = mapping("occurrence-agent-role", source, kind("agent_fk", DataPackageValueKind.LINK));
    DataPackageRowBuilder b = new DataPackageRowBuilder(role, List.of("agent_fk"), List.of(agent, role), COLUMNS);
    assertEquals("manage.mapping.rows.problem.noLinkedMapping", b.getProblems().get(0).key());
  }

  @Test
  public void testTemplateAndItemWithoutList() {
    DataPackageMapping occurrence = mapping("occurrence", source, template("occurrence_pk", "{eventID}-{occurrenceID}-{unknown}"));
    DataPackageRowBuilder b = new DataPackageRowBuilder(occurrence, List.of("occurrence_pk"), List.of(occurrence), COLUMNS);
    assertEquals("E1-OBS-1-{unknown}", b.build(ROW).get(0)[0]);

    DataPackageMapping wrong = mapping("occurrence", source, kind("occurrence_pk", DataPackageValueKind.ITEM));
    DataPackageRowBuilder w = new DataPackageRowBuilder(wrong, List.of("occurrence_pk"), List.of(wrong), COLUMNS);
    assertEquals("manage.mapping.rows.problem.itemWithoutList", w.getProblems().get(0).key());
  }

  @Test
  public void testCompareFindsDifferingFieldsAndFillsEmptyOnes() {
    List<DataPackageRowBuilder.Conflict> c = DataPackageRowBuilder.compare(List.of("a", "b"), "k",
        new String[] {"1", "x"}, 3, new String[] {"1", "y"}, 7);
    assertEquals(1, c.size());
    assertEquals("b", c.get(0).field());
    assertEquals(7, c.get(0).otherLine());

    // an empty value differs from nothing: a person's ID given on some rows only is filled in
    String[] merged = {"1", null};
    assertTrue(DataPackageRowBuilder.compare(List.of("a", "b"), "k", merged, 1, new String[] {"1", "x"}, 2).isEmpty());
    DataPackageRowBuilder.fill(merged, new String[] {"1", "x"});
    assertArrayEquals(new String[] {"1", "x"}, merged);
    DataPackageRowBuilder.fill(merged, new String[] {"1", null});
    assertArrayEquals(new String[] {"1", "x"}, merged);
  }

  @Test
  public void testPairedItemsTakeTheSamePlaceInTheirOwnList() {
    // names and their IDs in two pairs of lists; an empty name keeps its place, an ID can be missing
    List<String> cols = List.of("occurrenceID", "recordedBy", "recordedByID", "identifiedBy", "identifiedByID");
    String[] row = {"O1", "Anna Berg | | Jonas Lind", " | | orcid-J", "Maja Ek", "orcid-M"};
    DataPackageFieldMapping agentId = kind("agentID", DataPackageValueKind.PAIRED);
    agentId.setPairColumns(java.util.Arrays.asList(2, 4));
    DataPackageMapping agent = mapping("agent", source,
        kind("agent_pk", DataPackageValueKind.ID), kind("preferredAgentName", DataPackageValueKind.ITEM), agentId);
    agent.setRowMode(DataPackageRowMode.ITEMS);
    agent.setSplitColumns(List.of(1, 3));

    DataPackageRowBuilder b = new DataPackageRowBuilder(agent, List.of("preferredAgentName", "agentID"), List.of(agent), cols);
    assertTrue(b.getProblems().isEmpty(), b.getProblems().toString());
    List<String[]> rows = b.build(row);
    assertEquals(3, rows.size());
    assertArrayEquals(new String[] {"Anna Berg", null}, rows.get(0));
    assertArrayEquals(new String[] {"Jonas Lind", "orcid-J"}, rows.get(1));
    assertArrayEquals(new String[] {"Maja Ek", "orcid-M"}, rows.get(2));

    DataPackageMapping noPairs = mapping("agent", source, kind("agentID", DataPackageValueKind.PAIRED));
    noPairs.setRowMode(DataPackageRowMode.ITEMS);
    noPairs.setSplitColumns(List.of(1));
    assertEquals("manage.mapping.rows.problem.noPairs",
        new DataPackageRowBuilder(noPairs, List.of("agentID"), List.of(noPairs), cols).getProblems().get(0).key());
  }

  @Test
  public void testWholeRowMergeKey() {
    DataPackageMapping role = mapping("occurrence-agent-role", source,
        constant("agentRole", "recorder"), kind("agentRoleOrder", DataPackageValueKind.POSITION));
    role.setRowMode(DataPackageRowMode.ITEMS);
    role.setSplitColumns(List.of(2));
    role.setMergeKey(DataPackageMapping.WHOLE_ROW);
    DataPackageRowBuilder b = new DataPackageRowBuilder(role, List.of("agentRole", "agentRoleOrder"), List.of(role), COLUMNS);
    assertTrue(b.getProblems().isEmpty(), b.getProblems().toString());
    assertTrue(b.isMerging());
    assertEquals(b.mergeKey(new String[] {"recorder", "1"}), b.mergeKey(new String[] {"recorder", "1"}));
    assertNotEquals(b.mergeKey(new String[] {"recorder", "1"}), b.mergeKey(new String[] {"recorder", "2"}));

    // one row per source row never merges
    role.setRowMode(null);
    assertTrue(!new DataPackageRowBuilder(role, List.of("agentRole"), List.of(role), COLUMNS).isMerging());
  }

  // ---------- helpers ----------

  private static DataPackageTableSchema table(String name, String pk, List<String> fields) {
    DataPackageTableSchema t = new DataPackageTableSchema();
    t.setName(name);
    t.setPrimaryKey(pk);
    List<DataPackageField> fs = new ArrayList<>();
    for (String f : fields) {
      DataPackageField field = new DataPackageField();
      field.setName(f);
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

  private DataPackageMapping mapping(String table, Source src, DataPackageFieldMapping... fields) {
    DataPackageMapping m = new DataPackageMapping();
    m.setSource(src);
    m.setDataPackageSchema(schema);
    m.setDataPackageTableSchemaName(new DataPackageTableSchemaName(table));
    m.setFields(new ArrayList<>(List.of(fields)));
    return m;
  }

  private static DataPackageField field(String name) {
    DataPackageField f = new DataPackageField();
    f.setName(name);
    return f;
  }

  private static DataPackageFieldMapping kind(String name, DataPackageValueKind kind) {
    DataPackageFieldMapping fm = new DataPackageFieldMapping();
    fm.setField(field(name));
    fm.setValueKind(kind);
    return fm;
  }

  private static DataPackageFieldMapping column(String name, int index) {
    return new DataPackageFieldMapping(index, field(name));
  }

  private static DataPackageFieldMapping constant(String name, String value) {
    DataPackageFieldMapping fm = new DataPackageFieldMapping();
    fm.setField(field(name));
    fm.setDefaultValue(value);
    return fm;
  }

  private static DataPackageFieldMapping template(String name, String template) {
    DataPackageFieldMapping fm = kind(name, DataPackageValueKind.TEMPLATE);
    fm.setTemplate(template);
    return fm;
  }
}
