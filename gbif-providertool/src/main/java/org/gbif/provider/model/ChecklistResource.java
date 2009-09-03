/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.provider.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * External datasource driven resource representing a taxonomic checklist
 * 
 */
@Entity
public class ChecklistResource extends DataResource {
  public static final String DWC_GUID_PROPERTY = "TaxonID";
  public static final String DWC_GROUPS = "Taxon,DublinCore";
  private int numCommonNames;
  private int numCommonNameLanguages;
  private int numDistributions;
  private int numDistributionRegions;

  @Override
  @Transient
  public List<String> getAdditionalIdentifiers() {
    List<String> ids = super.getAdditionalIdentifiers();
    ids.add("AcceptedTaxonID");
    ids.add("AcceptedTaxon");
    ids.add("HigherTaxonID");
    ids.add("HigherTaxon");
    ids.add("BasionymID");
    ids.add("Basionym");
    return ids;
  }

  @Override
  @Transient
  public String getDwcGuidPropertyName() {
    return DWC_GUID_PROPERTY;
  }

  public int getNumCommonNameLanguages() {
    return numCommonNameLanguages;
  }

  public int getNumCommonNames() {
    return numCommonNames;
  }

  public int getNumDistributionRegions() {
    return numDistributionRegions;
  }

  public int getNumDistributions() {
    return numDistributions;
  }

  @Override
  public void resetStats() {
    numCommonNameLanguages = 0;
    numCommonNames = 0;
    numDistributionRegions = 0;
    numDistributions = 0;
    super.resetStats();
  }

  public void setNumCommonNameLanguages(int numCommonNameLanguages) {
    this.numCommonNameLanguages = numCommonNameLanguages;
  }

  public void setNumCommonNames(int numCommonNames) {
    this.numCommonNames = numCommonNames;
  }

  public void setNumDistributionRegions(int numDistributionRegions) {
    this.numDistributionRegions = numDistributionRegions;
  }

  public void setNumDistributions(int numDistributions) {
    this.numDistributions = numDistributions;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).appendSuper(super.toString()).toString();
  }
}
