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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageMapping implements Serializable {

  private static final long serialVersionUID = -8441887797416795559L;

  private static final Logger LOG = LogManager.getLogger(DataPackageMapping.class);

  private Source source;
  private DataPackageSchema dataPackageSchema;
  private DataPackageTableSchemaName dataPackageTableSchemaName;
  private List<DataPackageFieldMapping> fields = new ArrayList<>();
  private int fieldsMapped;
  private RecordFilter filter;
  private Date lastModified;

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
          if (row[count] != null && !(row[count].length() == 0)) {
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
}
