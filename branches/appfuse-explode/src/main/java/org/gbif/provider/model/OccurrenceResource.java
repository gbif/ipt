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

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * A specific resource representing the external data source for uploading DWC
 * records.
 * 
 */
@Entity
public class OccurrenceResource extends DataResource {
  public static final String DWC_GUID_PROPERTY = "SampleID";
  private BBox bbox = new BBox();
  private Integer featureHash;
  // cached statistics
  private int recWithCoordinates;
  private int recWithCountry;
  private int recWithAltitude;
  private int recWithDate;
  // distinct number of Region entities
  private int numRegions;
  private int numTerminalRegions;
  private int numCountries;

  public BBox getBbox() {
    return bbox;
  }

  @Override
  @Transient
  public String getDwcGuidPropertyName() {
    return DWC_GUID_PROPERTY;
  }

  public Integer getFeatureHash() {
    return featureHash;
  }

  public int getNumCountries() {
    return numCountries;
  }

  public int getNumRegions() {
    return numRegions;
  }

  public int getNumTerminalRegions() {
    return numTerminalRegions;
  }

  public int getRecWithAltitude() {
    return recWithAltitude;
  }

  public int getRecWithCoordinates() {
    return recWithCoordinates;
  }

  public int getRecWithCountry() {
    return recWithCountry;
  }

  public int getRecWithDate() {
    return recWithDate;
  }

  @Override
  public void resetStats() {
    bbox = new BBox();
    recWithCoordinates = 0;
    recWithCountry = 0;
    recWithAltitude = 0;
    recWithDate = 0;
    numCountries = 0;
    numRegions = 0;
    numTerminalRegions = 0;

    super.resetStats();
  }

  public void setBbox(BBox bbox) {
    this.bbox = bbox;
  }

  public void setFeatureHash(Integer featureHash) {
    this.featureHash = featureHash;
  }

  public void setNumCountries(int numCountries) {
    this.numCountries = numCountries;
  }

  public void setNumRegions(int numRegions) {
    this.numRegions = numRegions;
  }

  public void setNumTerminalRegions(int numTerminalRegions) {
    this.numTerminalRegions = numTerminalRegions;
  }

  public void setRecWithAltitude(int recWithAltitude) {
    this.recWithAltitude = recWithAltitude;
  }

  public void setRecWithCoordinates(int recWithCoordinates) {
    this.recWithCoordinates = recWithCoordinates;
  }

  public void setRecWithCountry(int recWithCountry) {
    this.recWithCountry = recWithCountry;
  }

  public void setRecWithDate(int recWithDate) {
    this.recWithDate = recWithDate;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).appendSuper(super.toString()).toString();
  }
}
