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
import java.util.Objects;
import java.util.StringJoiner;

public class DataSchemaFieldMapping implements Serializable, Comparable<DataSchemaFieldMapping> {

  private static final long serialVersionUID = 521368321389202377L;

  private Integer index;
  private String defaultValue;
  private DataSchemaField field;

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public DataSchemaField getField() {
    return field;
  }

  public void setField(DataSchemaField field) {
    this.field = field;
  }

  /**
   * Compares two DataSchemaFieldMapping lexicographically based on their names,
   *
   * @param fieldMapping DataSchemaFieldMapping
   *
   * @return 0 if names are equal, -1 if argument is lexicographically less, 1 if argument is lexicographically greater
   */
  @Override
  public int compareTo(DataSchemaFieldMapping fieldMapping) {
    return field.getName().compareTo(fieldMapping.getField().getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataSchemaFieldMapping that = (DataSchemaFieldMapping) o;
    return Objects.equals(index, that.index) && Objects.equals(defaultValue, that.defaultValue) && Objects.equals(field, that.field);
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, defaultValue, field);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataSchemaFieldMapping.class.getSimpleName() + "[", "]")
        .add("index=" + index)
        .add("defaultValue='" + defaultValue + "'")
        .add("field='" + field + "'")
        .toString();
  }
}
