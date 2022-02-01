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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class RecordFilter implements Serializable {

  public enum Comparator {
    IsNULL, IsNotNULL, Equals, NotEquals
  }

  public enum FilterTime {
    AfterTranslation, BeforeTranslation
  }

  private static final long serialVersionUID = 98709027465L;

  private Comparator comparator;
  private Integer column;
  private String param;
  private FilterTime filterTime;

  public Integer getColumn() {
    return column;
  }

  public Comparator getComparator() {
    return comparator;
  }

  public FilterTime getFilterTime() {
    return filterTime;
  }

  public Map<FilterTime, String> getFilterTimes() {
    Map<FilterTime, String> filterTimes = new HashMap<>();
    filterTimes.put(FilterTime.BeforeTranslation, "manage.mapping.filter.beforeTranslation");
    filterTimes.put(FilterTime.AfterTranslation, "manage.mapping.filter.afterTranslation");
    return filterTimes;
  }

  public String getParam() {
    return param;
  }

  /**
   * @param record values array representing record/row
   *
   * @return true if the record matches this filter criteria
   */
  public boolean matches(String[] record) {
    if (record != null && comparator != null && column != null) {
      String val = null;
      if (column < record.length) {
        val = StringUtils.trimToNull(record[column]);
      }
      switch (comparator) {
        case IsNULL:
          return val == null;
        case IsNotNULL:
          return val != null;
        case Equals:
          return val != null && val.equals(param);
        case NotEquals:
          return val == null || !val.equals(param);
      }
    }
    return true;
  }

  public void setColumn(Integer column) {
    this.column = column;
  }

  public void setComparator(Comparator comparator) {
    this.comparator = comparator;
  }

  public void setFilterTime(FilterTime filterTime) {
    this.filterTime = filterTime;
  }

  public void setParam(String param) {
    this.param = param;
  }

  @Override
  public String toString() {
    return "column: " + this.getColumn() + " - comparator: " + this.getComparator() + " - param: " + this.getParam()
      + " - filter time: " + filterTime;
  }
}
