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
package org.gbif.provider.model.eml;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * This class can be used to encapsulate taxonomic keyword information
 */
public class TaxonKeyword implements Serializable {
  /**
   * Generated
   */
  private static final long serialVersionUID = -7870655444855755937L;

  /**
   * The name representing the taxonomic rank of the taxon being described ,
   * e.g., Orca
   * 
   * @see http
   *      ://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#
   *      taxonRankValue
   */
  private String scientificName;

  /**
   * the name of the taxonomic rank for which the Taxon rank value is provided,
   * e.g., Genus
   * 
   * @see http
   *      ://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#
   *      taxonRankName
   */
  private String rank;

  /**
   * The common/vernacular name(s) for the organisms in the dataset/collection @
   * http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#
   * commonName
   */
  private String commonName;

  /**
   * Required by Struts2
   */
  public TaxonKeyword() {
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof TaxonKeyword)) {
      return false;
    }
    TaxonKeyword o = (TaxonKeyword) other;
    return equal(scientificName, o.scientificName) && equal(rank, o.rank)
        && equal(commonName, o.commonName);
  }

  /**
   * @return the commonName
   */
  public String getCommonName() {
    return commonName;
  }

  /**
   * @return the rank
   */
  public String getRank() {
    return rank;
  }

  /**
   * @return the scientificName
   */
  public String getScientificName() {
    return scientificName;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(scientificName, rank, commonName);
  }

  /**
   * @param commonName the commonName to set
   */
  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  /**
   * @param rank the rank to set
   */
  public void setRank(String rank) {
    this.rank = rank;
  }

  /**
   * @param scientificName the scientificName to set
   */
  public void setScientificName(String scientificName) {
    this.scientificName = scientificName;
  }
}
