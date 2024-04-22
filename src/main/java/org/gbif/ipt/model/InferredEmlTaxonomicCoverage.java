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

import org.gbif.ipt.action.portal.OrganizedTaxonomicCoverage;
import org.gbif.metadata.eml.ipt.model.TaxonomicCoverage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class InferredEmlTaxonomicCoverage {

  private TaxonomicCoverage data;
  // for UI representation
  private OrganizedTaxonomicCoverage organizedData;
  private boolean inferred = false;
  private Set<String> errors = new HashSet<>();
  private Map<String, String> rankWarnings = new HashMap<>();

  public void addError(String error) {
    errors.add(error);
  }

  public void addRankWarning(String rank, String warning) {
    rankWarnings.put(rank, warning);
  }
}
