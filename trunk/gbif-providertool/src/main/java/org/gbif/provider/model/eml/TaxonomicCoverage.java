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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import static com.google.common.base.Objects.equal;

import java.io.Serializable;
import java.util.List;

/**
 * The description of the Taxonomic scope that the resource covers
 */
public class TaxonomicCoverage implements Serializable {
  /**
   * Generated
   */
  private static final long serialVersionUID = -1550877218411220807L;

  /**
   * A description of the range of taxa addressed in the data set or collection
   * 
   * @see http
   *      ://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#
   *      generalTaxonomicCoverage
   */
  private String description;

  /**
   * Structures keywords for coverage
   */
  private TaxonKeyword taxonKeyword = new TaxonKeyword();

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof TaxonomicCoverage)) {
      return false;
    }
    TaxonomicCoverage o = (TaxonomicCoverage) other;
    return equal(description, o.description) 
        && equal(taxonKeyword, o.taxonKeyword);
  }

  /**
   * Required for struts2 params-interceptor, Digester and deserializing from
   * XML
   */
  public TaxonomicCoverage() {
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the keywords
   */
  public TaxonKeyword getTaxonKeyword() {
    return taxonKeyword;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description, taxonKeyword);
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @param keywords the keywords to set
   */
  public void setTaxonKeyword(TaxonKeyword keyword) {
    this.taxonKeyword = keyword;
  }
  
  @Override
  public String toString() {
    return String.format("Description=%s, %s", description, taxonKeyword.toString());
  }

}