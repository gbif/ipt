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

import java.util.Locale;
import java.util.Set;

/**
 * The kind of a Darwin Core Data Package table, as the resource overview groups them. Worked out from the table's keys,
 * so tables of a later DwC-DP version get a group without a list to update.
 */
public enum DwcDpTableGroup {
  EVENTS,
  ENTITIES,
  ASSERTIONS,
  JOINS;

  /**
   * The event tables. Their keys don't tell them from the other tables with a key to event (chronometric-age).
   */
  private static final Set<String> EVENT_TABLES = Set.of("event", "occurrence", "organism-interaction", "survey");

  /**
   * Events by name; tables without a primary key: assertions and identifiers by name, joins when they link two or more
   * tables; any other table is an entity.
   */
  public static DwcDpTableGroup of(DataPackageTableSchema table) {
    String name = table.getName() == null ? "" : table.getName();
    if (EVENT_TABLES.contains(name)) {
      return EVENTS;
    }
    if (table.getPrimaryKey() == null || table.getPrimaryKey().isBlank()) {
      if (name.endsWith("-assertion") || name.endsWith("-identifier")) {
        return ASSERTIONS;
      }
      long linked = table.getForeignKeys() == null ? 0 : table.getForeignKeys().stream()
          .filter(fk -> fk.getReference() != null && fk.getReference().getResource() != null
              && !fk.getReference().getResource().isBlank())
          .count();
      if (linked >= 2) {
        return JOINS;
      }
    }
    return ENTITIES;
  }

  /**
   * The part of the label's key, e.g. manage.overview.data.group.events
   */
  public String getKey() {
    return name().toLowerCase(Locale.ROOT);
  }
}
