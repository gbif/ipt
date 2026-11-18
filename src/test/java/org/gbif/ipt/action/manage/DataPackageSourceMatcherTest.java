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
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Sources shaped like the tables, as in the Insektmobilen DwC-DP: one file per table, columns named as the fields.
 */
public class DataPackageSourceMatcherTest {

  private static final DataPackageSchema SCHEMA = DataPackageSchema.builder().name("dwc-dp").version("1.0")
      .tableSchemas(Set.of(
          table("event", "event_pk", "event_pk", "eventID", "eventDate", "locality", "survey_fk"),
          table("survey", "survey_pk", "survey_pk", "event_fk", "samplingPerformedBy", "surveyTarget_fk"),
          table("survey-target", "surveyTarget_pk", "surveyTarget_pk", "surveyTargetType", "surveyTargetValue"),
          table("survey-survey-target", null, "survey_fk", "surveyTarget_fk"),
          table("occurrence", "occurrence_pk", "occurrence_pk", "occurrenceID", "event_fk", "scientificName",
              "surveyTarget_fk", "occurrenceStatus")))
      .build();

  @Test
  public void testMatchesByColumns() {
    DataPackageSourceMatcher.Match m = DataPackageSourceMatcher.match("occurrencetable_manualedit",
        List.of("occurrence_pk", "event_fk", "scientificName", "occurrenceStatus", "occurrenceID", "surveyTarget_fk"),
        SCHEMA);
    assertEquals("occurrence", m.table());
    assertEquals(6, m.getMatchedColumns());
    assertEquals(6, m.columns());
  }

  @Test
  public void testTableKeyOrFullFitDecides() {
    // survey_fk and surveyTarget_fk are all fields of survey-survey-target, which has no key; survey would need survey_pk
    DataPackageSourceMatcher.Match m = DataPackageSourceMatcher.match("surveysurveytargettable",
        List.of("survey_fk", "surveyTarget_fk"), SCHEMA);
    assertEquals("survey-survey-target", m.table());

    // having the primary key settles it
    m = DataPackageSourceMatcher.match("data", List.of("surveyTarget_pk", "surveyTargetType"), SCHEMA);
    assertEquals("survey-target", m.table());
  }

  @Test
  public void testNameAloneIsNoMatch() {
    // a name isn't enough: "noevent" holds no events, and columns a, b fit nothing
    assertNull(DataPackageSourceMatcher.match("eventtable_charactersfix", List.of("a", "b"), SCHEMA).table());
    assertNull(DataPackageSourceMatcher.match("noevent", List.of("occurrenceID", "eventDate"), SCHEMA).table());
  }

  @Test
  public void testFlatFileIsNoTable() {
    // one row per occurrence, with event columns too: no key and no table name, so no table, though event matches some
    DataPackageSourceMatcher.Match m = DataPackageSourceMatcher.match("base",
        List.of("occurrenceID", "eventID", "eventDate", "locality", "scientificName", "recordedBy"), SCHEMA);
    assertNull(m.table());
    assertEquals(3, m.matched().get("event"));
  }

  @Test
  public void testTieGoesTheSameWayEveryTime() {
    // a role file fits both role tables fully; the first by name wins, whatever order the schema's set gives
    DataPackageSchema roles = DataPackageSchema.builder().name("dwc-dp").version("1.0")
        .tableSchemas(Set.of(
            table("occurrence-agent-role", null, "occurrence_fk", "agent_fk", "agentRole"),
            table("event-agent-role", null, "event_fk", "agent_fk", "agentRole")))
        .build();
    for (int i = 0; i < 5; i++) {
      assertEquals("event-agent-role",
          DataPackageSourceMatcher.match("roles", List.of("agent_fk", "agentRole"), roles).table());
    }
    // the file name breaks the tie
    assertEquals("occurrence-agent-role",
        DataPackageSourceMatcher.match("occurrence_agent_roles", List.of("agent_fk", "agentRole"), roles).table());
  }

  @Test
  public void testNoSuggestion() {
    DataPackageSourceMatcher.Match m = DataPackageSourceMatcher.match("notes", List.of("a", "b"), SCHEMA);
    assertNull(m.table());
    assertEquals(0, m.matched().size());
  }

  private static DataPackageTableSchema table(String name, String pk, String... fields) {
    DataPackageTableSchema t = new DataPackageTableSchema();
    t.setName(name);
    t.setPrimaryKey(pk);
    List<DataPackageField> list = new ArrayList<>();
    for (String f : fields) {
      DataPackageField field = new DataPackageField();
      field.setName(f);
      list.add(field);
    }
    t.setFields(list);
    return t;
  }
}
