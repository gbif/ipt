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
import java.util.Set;
import java.util.TreeSet;

public class DataSchemaMapping implements Serializable {

  private static final long serialVersionUID = -8441887797416795559L;

  private Source source;
  private DataSchema dataSchema;
  // TreeSet will ensure set of PropertyMappings remain ordered
  private Set<DataSchemaFieldMapping> fields = new TreeSet<>();
  private Integer idColumn;
  private String idSuffix;
  private Date lastModified;

  /**
   * @param peek Peek of File source.
   *
   * @return list of columns names depending on its mapping.
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

  public DataSchemaFieldMapping getField(String qname) {
    for (DataSchemaFieldMapping dsfm : fields) {
      if (dsfm.getField().getName().equals(qname)) {
        return dsfm;
      }
    }
    return null;
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public DataSchema getDataSchema() {
    return dataSchema;
  }

  public void setDataSchema(DataSchema dataSchema) {
    this.dataSchema = dataSchema;
  }

  public Set<DataSchemaFieldMapping> getFields() {
    return fields;
  }

  public void setFields(Set<DataSchemaFieldMapping> fields) {
    this.fields = fields;
  }

  public Integer getIdColumn() {
    return idColumn;
  }

  public void setIdColumn(Integer idColumn) {
    this.idColumn = idColumn;
  }

  public String getIdSuffix() {
    return idSuffix;
  }

  public void setIdSuffix(String idSuffix) {
    this.idSuffix = idSuffix;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }
}
