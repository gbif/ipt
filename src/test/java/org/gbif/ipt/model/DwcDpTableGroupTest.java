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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DwcDpTableGroupTest {

  /**
   * A table with an optional primary key and a foreign key to each of the given tables, like the DwC-DP 1.0 schemas
   */
  private static DataPackageTableSchema table(String name, String primaryKey, String... linkedTables) {
    List<DataPackageTableSchemaForeignKey> foreignKeys = Arrays.stream(linkedTables)
        .map(t -> DataPackageTableSchemaForeignKey.builder()
            .fields(t + "_fk")
            .reference(new DataPackageFieldReference(t, t + "_pk"))
            .build())
        .toList();
    return DataPackageTableSchema.builder().name(name).primaryKey(primaryKey).foreignKeys(foreignKeys).build();
  }

  @Test
  void eventsByName() {
    assertEquals(DwcDpTableGroup.EVENTS, DwcDpTableGroup.of(table("event", "event_pk", "protocol", "provenance")));
    assertEquals(DwcDpTableGroup.EVENTS, DwcDpTableGroup.of(table("occurrence", "occurrence_pk", "event", "protocol")));
    assertEquals(DwcDpTableGroup.EVENTS, DwcDpTableGroup.of(table("survey", "survey_pk", "event", "protocol")));
    assertEquals(DwcDpTableGroup.EVENTS,
        DwcDpTableGroup.of(table("organism-interaction", "organismInteraction_pk", "event", "occurrence")));
  }

  @Test
  void tablesWithPrimaryKeyAreEntities() {
    // has a key to event too, but isn't an event
    assertEquals(DwcDpTableGroup.ENTITIES,
        DwcDpTableGroup.of(table("chronometric-age", "chronometricAge_pk", "event", "protocol")));
    assertEquals(DwcDpTableGroup.ENTITIES, DwcDpTableGroup.of(table("agent", "agent_pk")));
    assertEquals(DwcDpTableGroup.ENTITIES, DwcDpTableGroup.of(table("material", "materialEntity_pk", "event")));
  }

  @Test
  void assertionsAndIdentifiersByName() {
    // two foreign keys, like a join: the parent and the protocol
    assertEquals(DwcDpTableGroup.ASSERTIONS, DwcDpTableGroup.of(table("event-assertion", null, "event", "protocol")));
    assertEquals(DwcDpTableGroup.ASSERTIONS, DwcDpTableGroup.of(table("agent-identifier", null, "agent")));
  }

  @Test
  void tablesLinkingTwoTablesAreJoins() {
    assertEquals(DwcDpTableGroup.JOINS, DwcDpTableGroup.of(table("event-media", null, "media", "event")));
    assertEquals(DwcDpTableGroup.JOINS,
        DwcDpTableGroup.of(table("event-reference", null, "bibliographic-resource", "event")));
    // both keys to the same table
    assertEquals(DwcDpTableGroup.JOINS, DwcDpTableGroup.of(table("agent-agent-role", null, "agent", "agent")));
    // a join a later version adds
    assertEquals(DwcDpTableGroup.JOINS, DwcDpTableGroup.of(table("organism-media", null, "media", "organism")));
  }

  @Test
  void otherTablesWithoutPrimaryKeyAreEntities() {
    assertEquals(DwcDpTableGroup.ENTITIES,
        DwcDpTableGroup.of(table("organism-relationship", null, "organism")));
    assertEquals(DwcDpTableGroup.ENTITIES, DwcDpTableGroup.of(table("resource-relationship", null)));
    assertEquals(DwcDpTableGroup.ENTITIES, DwcDpTableGroup.of(DataPackageTableSchema.builder().build()));
  }

  @Test
  void groupIsKept() {
    DataPackageTableSchema table = table("event-media", null, "media", "event");
    DwcDpTableGroup group = table.getDwcDpGroup();
    table.setForeignKeys(List.of());
    assertSame(group, table.getDwcDpGroup());
    assertEquals("joins", group.getKey());
  }

  @Test
  void keyIgnoresDefaultLocale() {
    Locale before = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr"));
      assertEquals("assertions", DwcDpTableGroup.ASSERTIONS.getKey());
      assertEquals("entities", DwcDpTableGroup.ENTITIES.getKey());
    } finally {
      Locale.setDefault(before);
    }
  }
}
