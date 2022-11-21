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

import java.util.Objects;
import java.util.StringJoiner;

/**
 * This class represents data request for data tables (resource home table, resource manage table etc.)
 */
public class DatatableRequest {

  private String search = "";
  private int sortFieldIndex = 1;
  private String sortOrder = "asc";
  private long offset = 0;
  private int limit = 10;
  private String locale = "en";

  public String getSearch() {
    return search;
  }

  public void setSearch(String search) {
    this.search = search;
  }

  public int getSortFieldIndex() {
    return sortFieldIndex;
  }

  public void setSortFieldIndex(int sortFieldIndex) {
    this.sortFieldIndex = sortFieldIndex;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DatatableRequest that = (DatatableRequest) o;
    return Objects.equals(search, that.search)
        && Objects.equals(sortFieldIndex, that.sortFieldIndex)
        && Objects.equals(sortOrder, that.sortOrder)
        && Objects.equals(offset, that.offset)
        && Objects.equals(limit, that.limit)
        && Objects.equals(locale, that.locale);
  }

  @Override
  public int hashCode() {
    return Objects.hash(search, sortFieldIndex, sortOrder, offset, limit, locale);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DatatableRequest.class.getSimpleName() + "[", "]")
        .add("search='" + search + "'")
        .add("sortFieldIndex=" + sortFieldIndex)
        .add("sortOrder='" + sortOrder + "'")
        .add("offset=" + offset)
        .add("limit=" + limit)
        .add("locale='" + locale + "'")
        .toString();
  }
}
