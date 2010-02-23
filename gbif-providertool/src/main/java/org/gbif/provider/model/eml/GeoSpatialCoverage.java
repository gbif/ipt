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

import org.gbif.provider.model.BBox;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.Set;

/**
 * This class can be used to encapsulate geospatial converage information.
 * 
 * Note that this class is immuatable. New instances can be created using the
 * create method.
 * 
 */
public class GeoSpatialCoverage implements Serializable {

  private static final long serialVersionUID = -7639582552916192696L;

  /**
   * Creates a new instance of GeoSpatialCoverage.
   * 
   * @param boundingCoordinates the bounding coordinates
   * @param description the description
   * @param keywords the keywords
   * @param taxonomicSystem the taxonomic system
   * @return new instance of GeoSpatialCoverage
   */
  public static GeoSpatialCoverage create(BBox boundingCoordinates,
      String description, Set<String> keywords, String taxonomicSystem) {
    if (boundingCoordinates == null) {
      boundingCoordinates = BBox.newWorldInstance();
    }
    ImmutableSet<String> kw = ImmutableSet.of();
    if (keywords != null) {
      kw = ImmutableSet.copyOf(keywords);
    }
    return new GeoSpatialCoverage(boundingCoordinates, description, kw,
        taxonomicSystem);
  }

  private final String description;
  private final ImmutableSet<String> keywords;
  private final String taxonomicSystem;
  private final BBox boundingCoordinates;

  private GeoSpatialCoverage(BBox boundingCoordinates, String description,
      ImmutableSet<String> keywords, String taxonomicSystem) {
    this.boundingCoordinates = boundingCoordinates;
    this.description = description;
    this.keywords = keywords;
    this.taxonomicSystem = taxonomicSystem;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GeoSpatialCoverage)) {
      return false;
    }
    GeoSpatialCoverage o = (GeoSpatialCoverage) other;
    return equal(description, o.description) && equal(keywords, o.keywords)
        && equal(taxonomicSystem, o.taxonomicSystem)
        && equal(boundingCoordinates, o.boundingCoordinates);
  }

  public BBox getBoundingCoordinates() {
    return boundingCoordinates;
  }

  public String getDescription() {
    return description;
  }

  public ImmutableSet<String> getKeywords() {
    return keywords;
  }

  public String getTaxonomicSystem() {
    return taxonomicSystem;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description, keywords, taxonomicSystem,
        boundingCoordinates);
  }

  @Override
  public String toString() {
    return String.format(
        "Description=%s, Keywords=[%s], TaxonomicSystem=%s, BoundingCoordinates=%s",
        description, keywords, taxonomicSystem, boundingCoordinates);
  }
}
