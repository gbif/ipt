/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.action.portal;

import java.util.ArrayList;
import java.util.List;

/**
 * Class similar to TaxonomicCoverage, but the TaxonomicKeywords are OrganizedTaxonomicKeywords. This conveniently
 * stores all scientific names and common names for a rank together. Each display name is simply the concatenation
 * of the scientific name, and the common name in parentheses. E.g. Plantae (plants).
 *
 * @see org.gbif.metadata.eml.TaxonKeyword in project gbif-metadata-profile
 */
public class OrganizedTaxonomicKeywords {

  private String rank;
  private List<String> displayNames = new ArrayList<String>();

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  public List<String> getDisplayNames() {
    return displayNames;
  }

  public void setDisplayNames(List<String> displayNames) {
    this.displayNames = displayNames;
  }
}
