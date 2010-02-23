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

import org.gbif.provider.model.Point;
import org.gbif.provider.model.Resource;

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public class Eml implements Serializable {

  // New properties to support GBIF Extended Metadata Profile:
  private String resourceAbstract;
  private Set<String> citations = Sets.newHashSet();
  private String collectionId;
  private LocaleBundle dataLocale;
  private Date dateStamp; // TODO: What does this date represent?
  private String description;
  private String formationPeriod;
  private String hierarchyLevel;
  private String homepage;
  private Set<JgtiCuratorialUnit> jgtiCuratorialUnits = Sets.newHashSet();
  private Set<String> kingdomCoverages = Sets.newHashSet();
  private String livingTimePeriod;
  private LocaleBundle resourceLocale;
  private Point location;
  private LocaleBundle metadataLocale;
  private Set<Method> samplingMethods;
  private String parentCollectionId;
  private Set<PhysicalData> physicalData = Sets.newHashSet();
  private String placenameCoverageDescription;
  private Set<Agent> primaryContacts = Sets.newHashSet();
  private Set<Project> projects = Sets.newHashSet();
  private Set<Attribute> resourceAttributes = Sets.newHashSet();
  private String specimenPreservationMethod;
  private Set<TaxonomicCoverage> taxonomicCoverages = Sets.newHashSet();
  private Set<GeoSpatialCoverage> geoSpatialCoverages = Sets.newHashSet();
  private Set<TemporalCoverage> temporalCoverages = Sets.newHashSet();
  private String title;
  private String type;

  // Proposed properties to deprecate in support of the GBIF Extended Metadata
  // Profile:

  // Replace by geoSpatialCoverages:
  private GeoSpatialCoverage geographicCoverage;

  // Replace by taxonomicCoverages:
  private String taxonomicCoverageDescription;

  // Replace by temporalCoverages:
  private TimeKeyword temporalCoverage = new TimeKeyword();

  // Replace by samplingMethods:
  private String methods;

  // Replace by projects:
  private Project researchProject = new Project();

  // Properties unaffected by GBIF Extended Metadata Profile integration:

  private static final long serialVersionUID = 770733523572837495L;
  private transient Resource resource;
  // serialised data
  private int emlVersion = 0;
  private Agent resourceCreator = new Agent();
  private Date pubDate;
  private String language = "en";
  private String intellectualRights;
  // keywords
  private List<String> keywords = new ArrayList<String>();
  private TaxonKeyword lowestCommonTaxon;// TODO: verify: = new TaxonKeyword();
  private List<TaxonKeyword> taxonomicClassification = new ArrayList<TaxonKeyword>();
  // methods
  private String samplingDescription;
  private String qualityControl;
  // other
  private String purpose;
  private String maintenance;

  public Eml() {
    super();
    this.pubDate = new Date();
    this.resourceCreator.setRole(Role.ORIGINATOR);
  }

  public void addKeyword(String keyword) {
    this.keywords.add(keyword);
  }

  public String getAbstract() {
    return resource.getDescription();
  }

  public int getEmlVersion() {
    return emlVersion;
  }

  public GeoSpatialCoverage getGeographicCoverage() {
    return geographicCoverage;
  }

  public String getGuid() {
    return resource.getGuid();
  }

  public String getIntellectualRights() {
    return intellectualRights;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public String getLanguage() {
    return language;
  }

  public String getLink() {
    return resource.getLink();
  }

  public TaxonKeyword getLowestCommonTaxon() {
    return lowestCommonTaxon;
  }

  public String getMaintenance() {
    return maintenance;
  }

  // regular getter/setter

  public String getMethods() {
    return methods;
  }

  public Date getPubDate() {
    return pubDate;
  }

  public String getPurpose() {
    return purpose;
  }

  public String getQualityControl() {
    return qualityControl;
  }

  public Project getResearchProject() {
    return researchProject;
  }

  public Resource getResource() {
    return resource;
  }

  public Agent getResourceCreator() {
    return resourceCreator;
  }

  public String getSamplingDescription() {
    return samplingDescription;
  }

  public List<TaxonKeyword> getTaxonomicClassification() {
    return taxonomicClassification;
  }

  public String getTaxonomicCoverageDescription() {
    return taxonomicCoverageDescription;
  }

  public TimeKeyword getTemporalCoverage() {
    return temporalCoverage;
  }

  public String getTitle() {
    return resource.getTitle();
  }

  public int increaseEmlVersion() {
    this.emlVersion += 1;
    return this.emlVersion;
  }

  public TaxonKeyword lowestCommonTaxon() {
    return lowestCommonTaxon;
  }

  // cant replace instance, just modify their properties
  public Agent resourceCreator() {
    return resourceCreator;
  }

  public void setAbstract(String text) {
    resource.setDescription(text);
  }

  public void setEmlVersion(int emlVersion) {
    this.emlVersion = emlVersion;
  }

  public void setGeographicCoverage(GeoSpatialCoverage geographicCoverage) {
    this.geographicCoverage = geographicCoverage;
  }

  public void setIntellectualRights(String intellectualRights) {
    this.intellectualRights = intellectualRights;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setLink(String link) {
    resource.setLink(link);
  }

  public void setLowestCommonTaxon(TaxonKeyword lowestCommonTaxon) {
    this.lowestCommonTaxon = lowestCommonTaxon;
  }

  public void setMaintenance(String maintenance) {
    this.maintenance = maintenance;
  }

  public void setMethods(String methods) {
    this.methods = methods;
  }

  public void setPubDate(Date pubDate) {
    this.pubDate = pubDate;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public void setQualityControl(String qualityControl) {
    this.qualityControl = qualityControl;
  }

  public void setResearchProject(Project researchProject) {
    this.researchProject = researchProject;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setResourceCreator(Agent resourceCreator) {
    this.resourceCreator = resourceCreator;
  }

  public void setSamplingDescription(String samplingDescription) {
    this.samplingDescription = samplingDescription;
  }

  public void setTaxonomicClassification(
      List<TaxonKeyword> taxonomicClassification) {
    this.taxonomicClassification = taxonomicClassification;
  }

  public void setTaxonomicCoverageDescription(
      String taxonomicCoverageDescription) {
    this.taxonomicCoverageDescription = taxonomicCoverageDescription;
  }

  public void setTemporalCoverage(TimeKeyword temporalCoverage) {
    this.temporalCoverage = temporalCoverage;
  }

  public void setTitle(String title) {
    resource.setTitle(title);
  }

  public TimeKeyword temporalCoverage() {
    return temporalCoverage;
  }

}
