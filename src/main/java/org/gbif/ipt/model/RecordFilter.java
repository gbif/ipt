/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.model;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * @author markus
 * 
 */
public class RecordFilter implements Serializable {
  public enum Comparator {
    IsNULL, IsNotNULL, Equals, NotEquals
  }

  private static final long serialVersionUID = 98709027465L;;

  private Comparator comparator;
  private Integer column;
  private String param;

  public Integer getColumn() {
    return column;
  }

  public Comparator getComparator() {
    return comparator;
  }

  public String getParam() {
    return param;
  }

  /**
   * @param record
   * @return true if the record matches this filter criteria
   */
  public boolean matches(String[] record) {
    if (record != null && comparator != null && column != null) {
      String val = record.length < column ? null : StringUtils.trimToNull(record[column]);
      switch (comparator) {
        case IsNULL:
          return val == null;
        case IsNotNULL:
          return val != null;
        case Equals:
          return val != null && val.equals(param);
        case NotEquals:
          if (val == null) {
            return true;
          }
          return !val.equals(param);
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

  public void setParam(String param) {
    this.param = param;
  }
}
