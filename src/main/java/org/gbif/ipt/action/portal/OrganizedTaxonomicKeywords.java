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
package org.gbif.ipt.action.portal;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Class similar to TaxonomicCoverage, but the TaxonomicKeywords are OrganizedTaxonomicKeywords. This conveniently
 * stores all scientific names and common names for a rank together. Each display name is simply the concatenation
 * of the scientific name, and the common name in parentheses. E.g. Plantae (plants).
 * <p>
 * @see org.gbif.metadata.eml.ipt.model.TaxonKeyword in project gbif-metadata-profile
 */
@Data
public class OrganizedTaxonomicKeywords {

  private String rank;
  private List<String> displayNames = new ArrayList<>();
}
