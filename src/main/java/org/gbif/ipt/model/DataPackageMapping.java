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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageMapping implements Serializable {

  @Serial
  private static final long serialVersionUID = -8441887797416795559L;

  private static final Logger LOG = LogManager.getLogger(DataPackageMapping.class);

  /** merge key for "keep identical rows once": the whole row is the key, for tables without a key such as roles */
  public static final String WHOLE_ROW = "*";

  private Source source;
  private DataPackageSchema dataPackageSchema;
  // version of the schema the mapping was made against, the installed one may be newer
  private String version;
  private DataPackageTableSchemaName dataPackageTableSchemaName;
  private List<DataPackageFieldMapping> fields = new ArrayList<>();
  private int fieldsMapped;
  private RecordFilter filter;
  // how the rows are made; null: one row per source row
  private DataPackageRowMode rowMode;
  // ITEMS: the source columns holding lists, and what separates their items
  private List<Integer> splitColumns;
  private String separator;
  // DISTINCT (and optionally ITEMS): rows with the same value of this field merge into one; WHOLE_ROW: identical rows do
  private String mergeKey;
  private Date lastModified;
  // the problem count of the last Result check and when it was made, in memory only: the overview shows it without
  // reading the source again (see getResultProblems)
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private transient Integer resultProblems;
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private transient Date resultChecked;

  /**
   * @param peek Peek of File source.
   *
   * @return list of column names depending on its mapping.
   */
  public List<String> getColumns(List<String[]> peek) {
    if (!peek.isEmpty()) {
      int columnsCount = peek.get(0).length;
      List<String> columns = new ArrayList<>(columnsCount);
      for (int count = 0; count < columnsCount; count++) {
        String value = null;
        for (String[] row : peek) {
          if (row[count] != null && !(row[count].isEmpty())) {
            // add column number and first value as example
            // e.g. Column #2 - Puma conco...
            value = row[count].length() > 10 ? row[count].substring(0, 10) + "..." : row[count];
            break;
          }
        }
        if (value == null) {
          columns.add("Column #" + (count + 1));
        } else {
          columns.add("Column #" + (count + 1) + " - " + value);
        }
      }
      return columns;
    }
    return new ArrayList<>();
  }

  public DataPackageFieldMapping getField(String name) {
    if (fields != null) {
      int index = 0;
      for (DataPackageFieldMapping dsfm : fields) {
        if (dsfm.getField() == null) {
          LOG.error("Data package field mapping has null for field. Index: {}", index);
        } else if (dsfm.getField().getName().equals(name)) {
          return dsfm;
        }
        index++;
      }
    }
    return null;
  }

  /**
   * @return mapped field mappings of fields that aren't in the installed schema (anymore), e.g. after a schema update.
   * Unmapped ones are left out: there's nothing to keep, publish or review
   */
  public List<DataPackageFieldMapping> getUnresolvedFieldMappings() {
    if (fields == null) {
      return List.of();
    }
    DataPackageTableSchema table = getTableSchema();
    Set<String> fieldNames = table == null || table.getFields() == null
        ? Set.of()
        : table.getFields().stream().map(DataPackageField::getName).collect(Collectors.toSet());

    return fields.stream()
        .filter(fm -> fm.getField() != null && fm.isMapped() && !fieldNames.contains(fm.getField().getName()))
        .collect(Collectors.toList());
  }

  /**
   * @return true if the mapped table isn't in the installed schema (anymore), e.g. after a schema update
   */
  public boolean isTableSchemaMissing() {
    return dataPackageTableSchemaName != null && getTableSchema() == null;
  }

  /**
   * @return names of the mapped fields that aren't in the installed table schema (anymore), empty if the table is missing
   */
  public List<String> getUnresolvedFieldNames() {
    if (isTableSchemaMissing()) {
      return List.of();
    }
    return getUnresolvedFieldMappings().stream()
        .map(fm -> fm.getField().getName())
        .collect(Collectors.toList());
  }

  /**
   * @return label of the mapping for messages, e.g. "occurrences.csv → occurrence"
   */
  public String getLabel() {
    return (source != null ? source.getName() : "?") + " → "
        + (dataPackageTableSchemaName != null ? dataPackageTableSchemaName.getName() : "?");
  }

  /**
   * @param splitColumns the source columns holding lists, copied: the resource file can't hold an immutable list
   */
  public void setSplitColumns(List<Integer> splitColumns) {
    this.splitColumns = splitColumns == null ? null : new ArrayList<>(splitColumns);
  }

  /**
   * @return how the rows are made, ROW when not set
   */
  public DataPackageRowMode getRowModeOrDefault() {
    return rowMode == null ? DataPackageRowMode.ROW : rowMode;
  }

  /**
   * Remembers the problem count of a Result check of this mapping (DataPackageRowsPreview), made now.
   */
  public void rememberResultProblems(int problems) {
    resultProblems = problems;
    resultChecked = new Date();
  }

  /**
   * @param changedSince when the resource's sources or mappings last changed; a check made before that is outdated
   * @return the problem count of the last Result check, null if there's none or it's outdated
   */
  public Integer getResultProblems(Date changedSince) {
    if (resultChecked == null || (changedSince != null && resultChecked.before(changedSince))) {
      return null;
    }
    return resultProblems;
  }

  /**
   * @return true when identical rows merge ({@link #WHOLE_ROW}), not rows with the same key
   */
  public boolean isWholeRowMerge() {
    return WHOLE_ROW.equals(getEffectiveMergeKey());
  }

  /**
   * @return the field rows merge by, {@link #WHOLE_ROW} for identical rows, null when they don't merge
   */
  public String getEffectiveMergeKey() {
    DataPackageRowMode mode = getRowModeOrDefault();
    if (mode == DataPackageRowMode.ROW || mergeKey == null || mergeKey.isBlank()) {
      return null;
    }
    return mergeKey;
  }

  public DataPackageTableSchema getTableSchema() {
    if (dataPackageSchema == null || dataPackageTableSchemaName == null) {
      return null;
    }
    DataPackageTableSchema result = dataPackageSchema.tableSchemaByName(dataPackageTableSchemaName.getName());
    if (result == null) {
      LOG.warn("No table schema found for name '{}'. Available: {}",
          dataPackageTableSchemaName,
          dataPackageSchema.getTableSchemas().stream().map(DataPackageTableSchema::getName).toList());
    }
    return result;
  }

  public List<DataPackageField> getMissingRequiredFields() {
    DataPackageTableSchema table = getTableSchema();
    if (table == null || table.getFields() == null) {
      return List.of();
    }

    return table.getFields().stream()
        .filter(DataPackageField::isRequired)
        .filter(field -> !isFieldMapped(field))
        .collect(Collectors.toList());
  }

  private boolean isFieldMapped(DataPackageField field) {
    DataPackageFieldMapping fm = getField(field.getName());
    return fm != null && fm.isMapped();
  }
}
