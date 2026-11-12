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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One page of public resources for the home page (portal/home.ftl), with the filter values and their counts.
 * Values are plain text: the page escapes them.
 */
@Data
public class HomeSearchResult {

  // all public resources
  private int total;
  // the resources matching the search and filters
  private int matched;
  private List<Item> items = new ArrayList<>();
  // by filter (types, organisations, licences, updated): its values, each counted with the search and the other
  // filters applied
  private Map<String, List<FacetValue>> facets = new LinkedHashMap<>();

  @Data
  public static class Item {
    private String shortname;
    private String title;
    private String logoUrl;
    // alias or name, null if none
    private String organisation;
    // lower case, e.g. occurrence
    private String type;
    private String typeLabel;
    private String subtypeLabel;
    private String description;
    private long records;
    // the records formatted for the locale
    private String recordsLabel;
    private boolean metadataOnly;
    // yyyy-MM-dd, and with the time (yyyy-MM-dd HH:mm:ss) for its tooltip
    private String lastPublished;
    private String lastPublishedTime;
    private String licence;
    private String licenceLabel;
    private String doi;
    private String doiUrl;
    private String gbifUrl;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FacetValue {
    private String value;
    private String label;
    private int count;
  }
}
