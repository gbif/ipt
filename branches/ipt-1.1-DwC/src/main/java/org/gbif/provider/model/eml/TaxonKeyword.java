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
 * This class can be used to encapsulate taxonomic keyword information.
 * 
 * Note that this class is immuatable. New instances can be created using the
 * create method.
 * 
 */
public class TaxonKeyword implements Serializable {

  private static final long serialVersionUID = -7870655444855755937L;

  /**
   * Creates a new instance of TaxonKeyword.
   * 
   * @param scientificName the scientific name
   * @param rank the rank
   * @param commonName the common name
   * @return new instance of TaxonKeyword
   */
  public static TaxonKeyword create(String scientificName, String rank,
      String commonName) {
    return new TaxonKeyword(scientificName, rank, commonName);
  }

  private final String scientificName;
  private final String rank;
  private final String commonName;

  private TaxonKeyword(String scientificName, String rank, String commonName) {
    this.scientificName = scientificName;
    this.rank = rank;
    this.commonName = commonName;
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

  public String getCommonName() {
    return commonName;
  }

  public String getRank() {
    return rank;
  }

  public String getScientificName() {
    return scientificName;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(scientificName, rank, commonName);
  }

  @Override
  public String toString() {
    return String.format("ScientificName=%s, Rank=%s, CommonName=%s",
        scientificName, rank, commonName);
  }
}
