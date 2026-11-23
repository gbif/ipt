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
      new String[] {"OBS-001", "E1", "2024-05-12", "Fulltofta", "Erithacus rubecula", "2", "Collector A | Collector B", "Collector A"},
      new String[] {"OBS-002", "E1", "2024-05-12", "Fulltofta", "Fringilla coelebs", "5", "Collector A | Collector B", "Collector A"},
      new String[] {"OBS-003", "E1", "2024-05-12", "Fulltofta", "Parus major", "1", "Collector A", "Collector A"},
      new String[] {"OBS-004", "E2", "2024-05-13", "Söderåsen", "Sitta europaea", "1", "Collector B", "Collector B"},
      new String[] {"OBS-005", "E2", "2024-05-15", "Söderåsen", "Dendrocopos major", "2", "Collector B", "Collector A"},
      new String[] {"OBS-006", "E3", "2024-05-14", "Stenshuvud", "Phylloscopus collybita", "3", "Collector A|Collector C", "Collector C"});

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
        new String[] {"O1", "E1", "Collector A | Collector B", "orcid-A | orcid-J", "Collector A"},
        new String[] {"O2", "E1", "Collector A | Collector B", "orcid-A | orcid-J", "Collector C"},
        new String[] {"O3", "E2", "Collector A", "orcid-A", "Collector A"});
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
    // E1: Collector A, Collector B; E2: Collector A; the second row of E1 repeats the first
    assertEquals(3, roles.getRows());
    assertFalse(byId.containsKey("occurrence-agent-role-recordedBy"));

    // a person's ID at the same place as their name; identifiedBy has no IDs
    DataPackageTablesAction.Suggestion agent = byId.get("agent");
    assertEquals("manage.tables.why.agent.ids", agent.getWhyKey());
    DataPackageFieldMapping agentId = agent.getMapping().getField("agentID");
    assertEquals(DataPackageValueKind.PAIRED, agentId.getValueKind());
    assertEquals(Arrays.asList(3, null), agentId.getPairColumns());
    assertTrue(agent.getPreview().stream().anyMatch(r -> Arrays.asList(r).containsAll(List.of("Collector B", "orcid-J"))),
        agent.getPreview().stream().map(Arrays::toString).toList().toString());
  }

  /**
   * Rows without an eventID aren't one event: publishing doesn't merge them, so the wizard counts and compares each
   * as an event of its own. Values count as written: "E1 " is E1.
   */
  @Test
  public void testRowsWithoutEventIdAreEventsOfTheirOwn() {
    TextFileSource source = new TextFileSource();
    source.setName("gaps.csv");
    List<String> columns = List.of("occurrenceID", "eventID", "eventDate", "locality");
    List<String[]> sample = List.of(
        new String[] {"O1", "E1", "2024-05-01", "Lund"},
        // written as E1 and Lund: the same event
        new String[] {"O2", "E1 ", "2024-05-01", "Lund "},
        new String[] {"O3", "", "2024-06-10", "Malmo"},
        new String[] {"O4", "", "2024-07-02", "Ystad"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    DataPackageTablesAction.Suggestion ev = byId.get("event");
    // E1 ("E1 " too) and the two rows without an eventID
    assertEquals(3, ev.getRows());
    // the date and place don't differ inside an event: they go to the event, no warning
    assertEquals(2, ev.getMapping().getField("eventDate").getIndex());
    assertEquals(3, ev.getMapping().getField("locality").getIndex());
    assertTrue(ev.getWarnings().isEmpty(), ev.getWarnings().toString());
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

  /**
   * A DNA metabarcoding source: one bulk sample per event, the taxa found in it one occurrence each. One material per
   * sample, an identification linking each occurrence to its sample (ticked), and the sample size in one survey per
   * event.
   */
  @Test
  public void testSharedSamples() {
    TextFileSource source = new TextFileSource();
    source.setName("metabarcoding.csv");
    List<String> columns = List.of("occurrenceID", "eventID", "eventDate", "materialSampleID", "institutionCode",
        "preparations", "scientificName", "organismQuantity", "sampleSizeValue", "sampleSizeUnit");
    List<String[]> sample = List.of(
        new String[] {"o1", "e1", "2024-07-01", "S1", "NHRS", "DNA extract", "Pica pica", "3", "1000", "metre"},
        new String[] {"o2", "e1", "2024-07-01", "S1", "NHRS", "DNA extract", "Corvus corax", "1", "1000", "metre"},
        new String[] {"o3", "e2", "2024-07-02", "S2", "NHRS", "DNA extract", "Parus major", "2", "500", "metre"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    DataPackageTablesAction.Suggestion mat = byId.get("material");
    assertEquals("manage.tables.why.material.sample", mat.getWhyKey());
    assertEquals(2, mat.getRows());
    DataPackageMapping m = mat.getMapping();
    assertEquals(DataPackageRowMode.DISTINCT, m.getRowMode());
    assertEquals("materialEntity_pk", m.getMergeKey());
    assertEquals(3, m.getField("materialEntity_pk").getIndex());
    assertEquals(3, m.getField("materialEntityID").getIndex());
    assertEquals(4, m.getField("institutionCode").getIndex());
    assertEquals(5, m.getField("preparations").getIndex());
    assertEquals(DataPackageValueKind.LINK, m.getField("collectionEvent_fk").getValueKind());
    // a sample serves several occurrences: linked through identification, its name stays with the occurrence
    assertNull(m.getField("evidenceForOccurrence_fk").getValueKind());
    assertNull(m.getField("scientificName").getIndex());

    DataPackageTablesAction.Suggestion id = byId.get("identification");
    assertTrue(id.isChecked());
    assertEquals("manage.tables.why.identification.sample", id.getWhyKey());
    assertEquals(3, id.getRows());
    assertEquals(DataPackageValueKind.LINK, id.getMapping().getField("occurrence_fk").getValueKind());
    assertEquals(DataPackageValueKind.LINK, id.getMapping().getField("materialEntity_fk").getValueKind());
    int sampleAt = id.getPreviewFields().indexOf("materialEntity_fk");
    assertEquals(List.of("S1", "S1", "S2"), id.getPreview().stream().map(r -> r[sampleAt]).toList());

    DataPackageTablesAction.Suggestion sv = byId.get("survey");
    assertEquals("manage.tables.why.survey", sv.getWhyKey());
    assertEquals(2, sv.getRows());
    assertEquals(1, sv.getMapping().getField("survey_pk").getIndex());
    assertEquals(DataPackageValueKind.LINK, sv.getMapping().getField("event_fk").getValueKind());
    assertEquals(8, sv.getMapping().getField("sampleSizeValue").getIndex());
    assertEquals(9, sv.getMapping().getField("sampleSizeUnit").getIndex());

    assertTrue(DataPackageTablesAction.unused(columns,
        byId.values().stream().map(DataPackageTablesAction.Suggestion::getMapping).toList()).isEmpty());
  }

  /**
   * A sample taken in two events isn't linked to an event: merging would keep the first row's.
   */
  @Test
  public void testSampleInSeveralEventsNotLinked() {
    TextFileSource source = new TextFileSource();
    source.setName("pooled.csv");
    List<String> columns = List.of("occurrenceID", "eventID", "materialSampleID", "scientificName");
    List<String[]> sample = List.of(
        new String[] {"o1", "e1", "S1", "Pica pica"},
        new String[] {"o2", "e2", "S1", "Corvus corax"});
    DataPackageTablesAction.Suggestion mat = DataPackageTablesAction.suggest(schema(), source, columns, sample).stream()
        .filter(x -> x.getId().equals("material")).findFirst().orElseThrow();
    assertEquals("manage.tables.why.material.sample.events", mat.getWhyKey());
    assertEquals(1, mat.getRows());
    assertNull(mat.getMapping().getField("collectionEvent_fk").getValueKind());
  }

  /**
   * A sample size that differs inside an event isn't put in survey: no survey suggested.
   */
  @Test
  public void testNoSurveyWhenSampleSizeDiffersInsideEvent() {
    TextFileSource source = new TextFileSource();
    source.setName("sizes.csv");
    List<String> columns = List.of("occurrenceID", "eventID", "scientificName", "sampleSizeValue");
    List<String[]> sample = List.of(
        new String[] {"o1", "e1", "Pica pica", "10"},
        new String[] {"o2", "e1", "Corvus corax", "20"});
    assertTrue(DataPackageTablesAction.suggest(schema(), source, columns, sample).stream()
        .noneMatch(x -> x.getId().equals("survey")));
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

  @Test
  public void testEventsWithoutOccurrences() {
    TextFileSource source = new TextFileSource();
    source.setName("events.csv");
    List<String> columns = List.of("eventID", "eventDate", "locality", "recordedBy", "identifiedBy", "catalogNumber");
    List<String[]> sample = List.of(
        new String[] {"a", "2025-03-26", "Fulltofta", "Tim", "Tim", "12"},
        new String[] {"b", "2025-03-27", "Ystad", "Tim | Collector B", "Tim", "13"},
        new String[] {"b", "2025-03-27", "Ystad", "Collector C", "Tim", "14"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    // no occurrence, no identification: the rows are events, keyed by eventID
    assertFalse(byId.containsKey("occurrence"));
    assertFalse(byId.containsKey("identification"));
    DataPackageTablesAction.Suggestion ev = byId.get("event");
    assertEquals("manage.tables.why.event.rows", ev.getWhyKey());
    assertEquals(0, ev.getMapping().getField("event_pk").getIndex());
    assertEquals(2, ev.getRows());
    assertEquals(2, ev.getMapping().getField("locality").getIndex());
    // recorders differ inside b: not on the event row, but each name linked to its event
    assertNull(ev.getMapping().getField("recordedBy").getIndex());
    DataPackageTablesAction.Suggestion roles = byId.get("event-agent-role-recordedBy");
    assertEquals("manage.tables.why.role.event.rows", roles.getWhyKey());
    assertEquals(DataPackageMapping.WHOLE_ROW, roles.getMapping().getMergeKey());
    assertEquals(4, roles.getRows());
    // the identifiers are linked to the specimens (there are catalogue numbers), not to an occurrence
    DataPackageTablesAction.Suggestion agent = byId.get("agent");
    assertEquals(List.of(3, 4), agent.getMapping().getSplitColumns());
    assertEquals(3, agent.getRows());
    assertFalse(byId.containsKey("occurrence-agent-role-identifiedBy"));
    assertEquals(DataPackageValueKind.LINK,
        byId.get("material-agent-role-identifiedBy").getMapping().getField("materialEntity_fk").getValueKind());
    // specimens linked to their event only
    DataPackageTablesAction.Suggestion mat = byId.get("material");
    assertEquals("manage.tables.why.material.event", mat.getWhyKey());
    assertEquals(DataPackageValueKind.LINK, mat.getMapping().getField("collectionEvent_fk").getValueKind());
    assertFalse(mat.getMapping().getField("evidenceForOccurrence_fk").isMapped());
    assertEquals(3, mat.getRows());
  }

  @Test
  public void testNoSuggestionsWithoutOccurrenceOrEventId() {
    TextFileSource source = new TextFileSource();
    source.setName("nokey.csv");
    List<String> columns = List.of("eventDate", "locality", "recordedBy");
    List<String[]> sample = List.<String[]>of(new String[] {"2025-03-26", "Fulltofta", "Tim"});
    assertTrue(DataPackageTablesAction.suggest(schema(), source, columns, sample).isEmpty());
    assertFalse(DataPackageTablesAction.canSplit(columns));
  }

  /**
   * No occurrenceID or eventID, but a materialEntityID: the rows are specimens. Events from the date and place, the
   * recorders on the event, the identifiers on the specimen, which keeps its own identification.
   */
  @Test
  public void testSpecimensWithoutOccurrences() {
    TextFileSource source = new TextFileSource();
    source.setName("specimens.csv");
    List<String> columns = List.of("materialEntityID", "eventDate", "locality", "scientificName", "recordedBy",
        "identifiedBy", "preparations");
    List<String[]> sample = List.of(
        new String[] {"M1", "2024-05-01", "Lund", "Pica pica", "Collector A", "Identifier C", "ethanol"},
        new String[] {"M2", "2024-05-01", "Lund", "Corvus corax", "Collector A", "Identifier C", "dried"},
        new String[] {"M3", "2024-06-01", "Ystad", "Parus major", "Collector B", "Identifier D", "dried"});
    assertTrue(DataPackageTablesAction.canSplit(columns));
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    assertFalse(byId.containsKey("occurrence"));
    DataPackageTablesAction.Suggestion mat = byId.get("material");
    assertEquals("manage.tables.why.material.rows", mat.getWhyKey());
    assertEquals(3, mat.getRows());
    DataPackageMapping m = mat.getMapping();
    assertEquals(0, m.getField("materialEntity_pk").getIndex());
    assertEquals(DataPackageValueKind.LINK, m.getField("collectionEvent_fk").getValueKind());
    assertNull(m.getField("evidenceForOccurrence_fk").getValueKind());
    // the specimen keeps its identification
    assertEquals(3, m.getField("scientificName").getIndex());
    assertEquals(5, m.getField("identifiedBy").getIndex());

    DataPackageTablesAction.Suggestion ev = byId.get("event");
    assertEquals("manage.tables.why.event.made", ev.getWhyKey());
    assertEquals(2, ev.getRows());

    assertTrue(byId.containsKey("event-agent-role-recordedBy"));
    DataPackageTablesAction.Suggestion identifiers = byId.get("material-agent-role-identifiedBy");
    assertEquals("manage.tables.why.role.material", identifiers.getWhyKey());
    assertEquals(3, identifiers.getRows());
    assertEquals(DataPackageValueKind.LINK, identifiers.getMapping().getField("materialEntity_fk").getValueKind());
    assertEquals("identifier", identifiers.getMapping().getField("agentRole").getDefaultValue());

    DataPackageTablesAction.Suggestion id = byId.get("identification");
    assertFalse(id.isChecked());
    assertEquals("manage.tables.why.identification.material", id.getWhyKey());
    assertEquals("{materialEntityID}-id", id.getMapping().getField("identification_pk").getTemplate());
    assertEquals(DataPackageValueKind.LINK, id.getMapping().getField("materialEntity_fk").getValueKind());
    assertNull(id.getMapping().getField("occurrence_fk").getValueKind());
    assertTrue(DataPackageTablesAction.unused(columns,
        byId.values().stream().map(DataPackageTablesAction.Suggestion::getMapping).toList()).isEmpty());
  }

  /**
   * Catalogue numbers and no date or place: the specimen key is made from the institution and the catalogue number,
   * there's no event, so the recorders are linked to the specimen.
   */
  @Test
  public void testCatalogueNumbersWithoutDateOrPlace() {
    TextFileSource source = new TextFileSource();
    source.setName("catalogue.csv");
    List<String> columns = List.of("institutionCode", "catalogNumber", "scientificName", "recordedBy");
    List<String[]> sample = List.of(
        new String[] {"NHMD", "12", "Pica pica", "Collector A | Collector B"},
        new String[] {"NHMD", "13", "Corvus corax", "Collector A"});
    Map<String, DataPackageTablesAction.Suggestion> byId = DataPackageTablesAction.suggest(schema(), source, columns, sample)
        .stream().collect(Collectors.toMap(DataPackageTablesAction.Suggestion::getId, x -> x));

    assertFalse(byId.containsKey("occurrence"));
    assertFalse(byId.containsKey("event"));
    DataPackageTablesAction.Suggestion mat = byId.get("material");
    assertEquals("manage.tables.why.material.rows.noEvent", mat.getWhyKey());
    assertEquals("{institutionCode}:{catalogNumber}", mat.getMapping().getField("materialEntity_pk").getTemplate());
    assertNull(mat.getMapping().getField("collectionEvent_fk").getValueKind());

    DataPackageTablesAction.Suggestion recorders = byId.get("material-agent-role-recordedBy");
    assertEquals("recorder", recorders.getMapping().getField("agentRole").getDefaultValue());
    assertEquals(3, recorders.getRows());
    assertEquals("{institutionCode}:{catalogNumber}-id",
        byId.get("identification").getMapping().getField("identification_pk").getTemplate());
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
            "collectionCode", "catalogNumber", "preparations", "scientificName", "identifiedBy"), Set.of("materialEntity_pk"));
    material.setForeignKeys(List.of(fk("collectionEvent_fk", "event", "event_pk"),
        fk("evidenceForOccurrence_fk", "occurrence", "occurrence_pk")));
    DataPackageTableSchema materialRole = table("material-agent-role", null,
        List.of("materialEntity_fk", "agent_fk", "agentRole", "agentRoleOrder"),
        Set.of("materialEntity_fk", "agent_fk", "agentRoleOrder"));
    materialRole.setForeignKeys(List.of(fk("materialEntity_fk", "material", "materialEntity_pk"),
        fk("agent_fk", "agent", "agent_pk")));
    DataPackageTableSchema identification = table("identification", "identification_pk",
        List.of("identification_pk", "occurrence_fk", "materialEntity_fk", "scientificName"), Set.of("identification_pk"));
    identification.setForeignKeys(List.of(fk("occurrence_fk", "occurrence", "occurrence_pk"),
        fk("materialEntity_fk", "material", "materialEntity_pk")));
    DataPackageTableSchema survey = table("survey", "survey_pk",
        List.of("survey_pk", "surveyID", "event_fk", "sampleSizeValue", "sampleSizeUnit", "identifiedBy"),
        Set.of("survey_pk", "event_fk"));
    survey.setForeignKeys(List.of(fk("event_fk", "event", "event_pk")));
    return DataPackageSchema.builder().name("dwc-dp").version("1.0")
        .tableSchemas(Set.of(event, occurrence, agent, role, eventRole, material, materialRole, identification, survey))
        .build();
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
