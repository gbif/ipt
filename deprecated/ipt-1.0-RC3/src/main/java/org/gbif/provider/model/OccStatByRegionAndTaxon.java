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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class OccStatByRegionAndTaxon implements ResourceRelatedObject {
  private Long id;
  private Resource resource;
  private Taxon taxon;
  private Region region;
  private int numOcc;
  private BBox bbox = new BBox();

  public OccStatByRegionAndTaxon() {
    super();
  }

  public OccStatByRegionAndTaxon(Resource resource, Taxon taxon, Region region,
      Long numOcc) {
    super();
    this.resource = resource;
    this.taxon = taxon;
    this.region = region;
    this.numOcc = numOcc.intValue();
  }

  /**
   * Used by OccStatManager in HQL...
   * 
   * @param resource
   * @param taxon
   * @param region
   * @param numOcc
   * @param minY
   * @param minX
   * @param maxY
   * @param maxX
   */
  public OccStatByRegionAndTaxon(Resource resource, Taxon taxon, Region region,
      Long numOcc, Double minY, Double minX, Double maxY, Double maxX) {
    // new OccStatByRegionAndTaxon(res, t, r, count(d),
    // min(d.location.latitude), min(d.location.longitude),
    // max(d.location.latitude), max(d.location.longitude))
    // latitude=y, longitude=x
    super();
    this.resource = resource;
    this.taxon = taxon;
    this.region = region;
    this.numOcc = numOcc.intValue();
    this.bbox = new BBox(minY, minX, maxY, maxX);
  }

  public BBox getBbox() {
    return bbox;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  public int getNumOcc() {
    return numOcc;
  }

  @ManyToOne(optional = true)
  public Region getRegion() {
    return region;
  }

  @ManyToOne(optional = false)
  public Resource getResource() {
    return resource;
  }

  @Transient
  public Long getResourceId() {
    return resource.getId();
  }

  @ManyToOne(optional = true)
  public Taxon getTaxon() {
    return taxon;
  }

  public void incrementNumOcc() {
    this.numOcc++;
  }

  public void setBbox(BBox bbox) {
    this.bbox = bbox;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setNumOcc(int numOcc) {
    this.numOcc = numOcc;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setTaxon(Taxon taxon) {
    this.taxon = taxon;
  }
}
