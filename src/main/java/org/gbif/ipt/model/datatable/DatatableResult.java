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
package org.gbif.ipt.model.datatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.struts2.json.annotations.JSON;

/**
 * This class represents source data for data tables (resource home table, resource manage table etc.)
 */
public class DatatableResult {
  private int totalRecords;
  private int totalDisplayRecords;
  private List<List<String>> data = new ArrayList<>();

  @JSON(name = "iTotalRecords")
  public int getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords(int totalRecords) {
    this.totalRecords = totalRecords;
  }

  @JSON(name = "iTotalDisplayRecords")
  public int getTotalDisplayRecords() {
    return totalDisplayRecords;
  }

  public void setTotalDisplayRecords(int totalDisplayRecords) {
    this.totalDisplayRecords = totalDisplayRecords;
  }

  @JSON(name = "aaData")
  public List<List<String>> getData() {
    return data;
  }

  public void setData(List<List<String>> data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DatatableResult that = (DatatableResult) o;
    return totalRecords == that.totalRecords
        && totalDisplayRecords == that.totalDisplayRecords
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalRecords, totalDisplayRecords, data);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DatatableResult.class.getSimpleName() + "[", "]")
        .add("iTotalRecords=" + totalRecords)
        .add("iTotalDisplayRecords=" + totalDisplayRecords)
        .add("aaData=" + data)
        .toString();
  }
}
