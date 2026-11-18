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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageFieldMapping implements Serializable, Comparable<DataPackageFieldMapping> {

  @Serial
  private static final long serialVersionUID = 521368321389202377L;

  private Integer index;
  private String defaultValue;
  private DataPackageField field;
  private Map<String, String> translation;
  // a generated value instead of the column or fixed value; null: the column or fixed value
  private DataPackageValueKind valueKind;
  // TEMPLATE: e.g. "{occurrenceID}-id"
  private String template;
  // PAIRED: per list column of the mapping (same order), the column holding the matching list; null where there's none
  private List<Integer> pairColumns;

  public DataPackageFieldMapping(Integer index, DataPackageField field) {
    this.index = index;
    this.field = field;
  }

  /**
   * Compares two DataPackageFieldMapping lexicographically based on their names,
   *
   * @param fieldMapping DataPackageFieldMapping
   *
   * @return 0 if names are equal, -1 if argument is lexicographically less, 1 if argument is lexicographically greater
   */
  @Override
  public int compareTo(DataPackageFieldMapping fieldMapping) {
    return field.getName().compareTo(fieldMapping.getField().getName());
  }

  /**
   * @param pairColumns the pair columns, copied: the resource file can't hold an immutable list
   */
  public void setPairColumns(List<Integer> pairColumns) {
    this.pairColumns = pairColumns == null ? null : new ArrayList<>(pairColumns);
  }

  public boolean isMapped() {
    return index != null || StringUtils.isNotBlank(defaultValue) || valueKind != null;
  }
}
