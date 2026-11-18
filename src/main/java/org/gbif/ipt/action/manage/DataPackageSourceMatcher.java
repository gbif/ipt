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
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Suggests the table a source fills, for sources already shaped like the tables of a data package (one file per
 * table). Its columns are matched to the fields of each table the way new mappings are auto-mapped
 * ({@link DataPackageMappingAction#automap}); the table with the most matched columns wins. Having the table's primary
 * key, and the table's name in the source's name ("occurrencetable_v2" for occurrence), count extra.
 * <p>
 * A table is only suggested when the source has the table's key or, for a table without a key, every column is one
 * of its fields. A flat file sharing a few column names with event (eventID, eventDate) isn't the event table, and
 * neither is a file just named "noevent"; the name only helps between tables that fit.
 */
public final class DataPackageSourceMatcher {

  private static final int KEY_BONUS = 3;
  private static final int NAME_BONUS = 2;

  /**
   * A source's suggested table, and how many of its columns each table would map.
   *
   * @param table the suggested table, null when the source has no table's key and fits no table without one
   * @param columns the number of columns of the source
   * @param matched the number of columns each table maps, only tables mapping at least one
   */
  public record Match(String table, int columns, Map<String, Integer> matched) {

    public int getMatchedColumns() {
      return table == null ? 0 : matched.getOrDefault(table, 0);
    }
  }

  private DataPackageSourceMatcher() {
  }

  public static Match match(String sourceName, List<String> columns, DataPackageSchema schema) {
    Map<String, Integer> matched = new LinkedHashMap<>();
    String best = null;
    int bestScore = 0;
    int bestHint = 0;
    String name = DataPackageMappingAction.normalizeName(Objects.toString(sourceName, ""));

    for (DataPackageTableSchema table : schema.getTableSchemas()) {
      int count = matchedColumns(table, columns);
      if (count > 0) {
        matched.put(table.getName(), count);
      }
      String tableName = DataPackageMappingAction.normalizeName(table.getName());
      // the longest table name in the source's name: "surveysurveytargettable" is survey-survey-target, not survey
      int hint = !tableName.isEmpty() && name.contains(tableName) ? tableName.length() : 0;
      boolean key = hasKey(table, columns);
      boolean keyless = table.getPrimaryKey() == null || table.getPrimaryKey().isBlank();
      if (!key && !(keyless && count > 0 && count == columns.size())) {
        continue;
      }
      int score = count + (key ? KEY_BONUS : 0) + (hint > 0 ? NAME_BONUS : 0);
      if (score > bestScore || (score == bestScore && score > 0 && hint > bestHint)) {
        best = table.getName();
        bestScore = score;
        bestHint = hint;
      }
    }
    return new Match(best, columns.size(), matched);
  }

  /**
   * @return how many columns the table's fields map, as auto-mapping would
   */
  static int matchedColumns(DataPackageTableSchema table, List<String> columns) {
    List<DataPackageFieldMapping> fields = new ArrayList<>();
    for (DataPackageField f : table.getFields()) {
      fields.add(new DataPackageFieldMapping(null, f));
    }
    DataPackageMappingAction.automap(fields, columns);
    return (int) fields.stream().map(DataPackageFieldMapping::getIndex).filter(Objects::nonNull).distinct().count();
  }

  private static boolean hasKey(DataPackageTableSchema table, List<String> columns) {
    String pk = table.getPrimaryKey();
    if (pk == null || pk.isBlank()) {
      return false;
    }
    String key = DataPackageMappingAction.normalizeName(pk);
    return columns.stream().anyMatch(c -> c != null && key.equals(DataPackageMappingAction.normalizeName(c)));
  }
}
