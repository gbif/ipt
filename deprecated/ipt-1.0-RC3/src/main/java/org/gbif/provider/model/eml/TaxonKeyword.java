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

import java.io.Serializable;

/**
 * TODO: Documentation.
 * 
 */
public class TaxonKeyword implements Serializable {
  private String scientificName;
  private String rank;
  private String commonName;

  public TaxonKeyword() {
    super();
  }

  public TaxonKeyword(String scientificName, String rank, String commonName) {
    super();
    this.scientificName = scientificName;
    this.rank = rank;
    this.commonName = commonName;
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

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  public void setScientificName(String scientificName) {
    this.scientificName = scientificName;
  }

}
