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

import java.util.List;

/**
 * Class similar to TaxonomicCoverage, but the TaxonomicKeywords are OrganizedTaxonomicKeywords.
 *
 *  * @see org.gbif.metadata.eml.TaxonomicCoverage in project gbif-metadata-profile
 */
public class OrganizedTaxonomicCoverage {

  private List<OrganizedTaxonomicKeywords> keywords;
  private String description;

  public List<OrganizedTaxonomicKeywords> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<OrganizedTaxonomicKeywords> keywords) {
    this.keywords = keywords;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
