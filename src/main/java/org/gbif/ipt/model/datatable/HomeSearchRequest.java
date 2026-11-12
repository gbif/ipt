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

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;

/**
 * A search of the public resources on the home page (portal/home.ftl): a search string, filters and a page.
 * Within a filter the values are alternatives (any of them matches); the filters must all match.
 */
@Data
public class HomeSearchRequest {

  public static final String SORT_PUBLISHED = "published";
  public static final String SORT_TITLE = "title";
  public static final String SORT_RECORDS = "records";

  public static final String UPDATED_MONTH = "month";
  public static final String UPDATED_YEAR = "year";
  public static final String UPDATED_OLDER = "older";

  // matches title, shortname, organisation, type, subtype, author, keywords and description
  private String search = "";
  // core types, lower case, e.g. occurrence
  private Set<String> types = new LinkedHashSet<>();
  // organisation keys; the default organisation's key for resources without one
  private Set<String> organisations = new LinkedHashSet<>();
  // licence codes, e.g. CC-BY-4.0, or "other" for another licence or none
  private Set<String> licences = new LinkedHashSet<>();
  // last published within a month or a year, or older than a year; empty for any time
  private String updated = "";
  private String sort = SORT_PUBLISHED;
  private long offset = 0;
  private int limit = 10;
  private String locale = "en";
}
