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

import com.google.common.collect.Lists;

import org.gbif.provider.model.Resource;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The EML model is a POJO representing the GBIF Extended Metadata Profile for
 * the IPT 1.1 In addition to standard Bean encapsulation, additional methods
 * exist to simplify the implementation of an EML XML parser.
 * 
 * @see org.gbif.provider.model.eml.EmlFactory
 */
public class Eml implements Serializable {
  /**
   * Generated
   */
  private static final long serialVersionUID = 770733523572837495L;

  /**
   * This is not in the GBIF extended metadata document, but seems like a
   * sensible placeholder that can be used to capture anything missing, and maps
   * nicely in EML, therefore is added
   */
  private String additionalInfo;

  private String alternateIdentifier;

  /**
   * The 'associatedParty' element provides the full name of other people,
   * organizations, or positions who should be associated with the resource.
   * These parties might play various roles in the creation or maintenance of
   * the resource, and these roles should be indicated in the "role" element.
   */
  private List<Agent> associatedParties = Lists.newArrayList();
  // private List<String> bibliographicCitations = Lists.newArrayList();
  private BibliographicCitationSet bibliographicCitationSet = new BibliographicCitationSet();

  /**
   * A resource that describes a literature citation for the resource, one that
   * might be found in a bibliography. We cannot use
   * http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml.html#citation
   * because the IPT deals with /eml/dataset and not /eml/citation therefore
   * these are found in the additionalMetadata section of the EML.
   */
  private String citation;
  /**
   * The URI (LSID or URL) of the collection. In RDF, used as URI of the
   * collection resource.
   * 
   * @see http://rs.tdwg.org/ontology/voc/Collection#collectionId
   */
  private String collectionId;
  /**
   * Official name of the Collection in the local language.
   * 
   * @see http://purl.org/dc/elements/1.1/title Note: this could potentially be
   *      sourced from the resource title, but this is declared explicitly in
   *      the GBIF IPT metadata profile, so must assume that this is required
   *      for a title in a different language, presumably to aid free text
   *      discovery in original language
   */
  private String collectionName;

  /**
   * Date of metadata creation or the last metadata update Default to now(), but
   * can be overridden
   */
  private Date dateStamp = new Date();
  /**
   * The distributionType URL is generally meant for informational purposes, and
   * the "function" attribute should be set to "information".
   * 
   */
  private String distributionUrl;
  /**
   * Serialised data
   */
  private int emlVersion = 0;
  private List<GeospatialCoverage> geospatialCoverages = Lists.newArrayList();
  // Support for a single geocoverage until multiple is enabled.
  private GeospatialCoverage geographicCoverage;

  /**
   * Dataset level to which the metadata applies. The default value for GBIF is
   * “dataset”
   * 
   * @see http
   *      ://www.fgdc.gov/standards/projects/incits-l1-standards-projects/NAP
   *      -Metadata/napMetadataProfileV101.pdf
   */
  private String hierarchyLevel = "dataset";

  /**
   * A rights management statement for the resource, or reference a service
   * providing such information. Rights information encompasses Intellectual
   * Property Rights (IPR), Copyright, and various Property Rights. In the case
   * of a data set, rights might include requirements for use, requirements for
   * attribution, or other requirements the owner would like to impose.
   * 
   * @see http
   *      ://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#
   *      intellectualRights
   */
  private String intellectualRights;

  /**
   * A quantitative descriptor (number of specimens, samples or batches). The
   * actual quantification could be covered by 1) an exact number of “JGI-units”
   * in the collection plus a measure of uncertainty (+/- x); 2) a range of
   * numbers (x to x), with the lower value representing an exact number, when
   * the higher value is omitted.
   */
  private List<JGTICuratorialUnit> jgtiCuratorialUnits = Lists.newArrayList();

  // Note that while Sets would be fine, to ease testing, Lists are
  // used to preserve ordering. A Set implementation that respects ordering
  // would also suffice
  // please refer to typed classes for descriptions of the properties and how
  // they map to EML
  private List<KeywordSet> keywords = Lists.newArrayList();

  /**
   * The language in which the resource is written. This can be a well-known
   * language name, or one of the ISO language codes to be more precise.
   * 
   * @see http
   *      ://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#
   *      language The IPT will always use ISO language codes
   */
  private String language = "en";

  /**
   * URL of the logo associated with a resource.
   * 
   */
  private String logoUrl;

  /**
   * Language of the metadata composed of an ISO639-2/T three letter language
   * code and an ISO3166-1 three letter country code.
   */
  private String metadataLanguage = "en";

  /**
   * The GBIF metadata profile states "Describes other languages used in
   * metadata free text description. Consists of language, country and
   * characterEncoding" In Java world, a LocaleBundle handles this concisely
   */
  private LocaleBundle metadataLocale;

  /**
   * The 'metadataProvider' element provides the full name of the person,
   * organization, or position who created documentation for the resource.
   * 
   * @see http
   *      ://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#
   *      metadataProvider
   */
  private Agent metadataProvider = new Agent();

  /**
   * Identifier for the parent collection for this sub-collection. Enables a
   * hierarchy of collections and sub collections to be built.
   * 
   * @see http://rs.tdwg.org/ontology/voc/Collection#isPartOfCollection
   */
  private String parentCollectionId;

  private List<PhysicalData> physicalData = Lists.newArrayList();

  /**
   * The project this resource is associated with
   */
  private Project project;

  /**
   * The date that the resource was published. The format should be represented
   * as: CCYY, which represents a 4 digit year, or as CCYY-MM-DD, which denotes
   * the full year, month, and day. Note that month and day are optional
   * components. Formats must conform to ISO 8601.
   * http://knb.ecoinformatics.org/
   * software/eml/eml-2.1.0/eml-resource.html#pubDate
   */
  private Date pubDate;

  /**
   * This is not in the GBIF extended metadata document, but seems like a
   * sensible field to maintain, and maps nicely in EML, therefore is added
   */
  private String purpose;

  /**
   * The IPT resource (note is transient)
   */
  private transient Resource resource;

  /**
   * The 'creator' element provides the full name of the person, organization,
   * or position who created the resource.
   * 
   * @see http
   *      ://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#
   *      creator
   */
  private Agent resourceCreator = new Agent();

  private List<Method> samplingMethods = Lists.newArrayList();

  /**
   * Picklist keyword indicating the process or technique used to prevent
   * physical deterioration of non-living collections. Expected to contain an
   * instance from the Specimen Preservation Method Type Term vocabulary.
   * 
   * @see http://rs.tdwg.org/ontology/voc/Collection#specimenPreservationMethod
   */
  private String specimenPreservationMethod;

  private List<TaxonomicCoverage> taxonomicCoverages = Lists.newArrayList();

  private List<TemporalCoverage> temporalCoverages = Lists.newArrayList();

  /**
   * Default constructor needed by Struts2
   */
  public Eml() {
    super();
    this.pubDate = new Date();
    this.resourceCreator.setRole(Role.ORIGINATOR);
    this.metadataProvider.setRole(Role.METADATA_PROVIDER);
  }

  /**
   * utility to add Agents to the primary contacts This method was introduced to
   * ease the Digester rules for parsing of EML
   * 
   * @param agent to add
   */
  public void addAssociatedParty(Agent agent) {
    if (agent.getRole() == null) {
      agent.setRole(Role.ASSOCIATED_PARTY);
    }
    associatedParties.add(agent);
  }

  /**
   * utility to add a bibliographic citation to the bibliographicCitations. This
   * method was introduced to ease the Digester rules for parsing of EML.
   * 
   * @param bibliographic citation to add
   */
  public void addBibliographicCitations(List<String> citations) {
    bibliographicCitationSet.getBibliographicCitations().addAll(citations);
  }

  /**
   * utility to add a coverage to the coverages This method was introduced to
   * ease the Digester rules for parsing of EML
   * 
   * @param coverage to add
   */
  public void addGeospatialCoverage(GeospatialCoverage geospatialCoverage) {
    geospatialCoverages.add(geospatialCoverage);
  }

  // /**
  // * utility to add a citation to the citations. This method was introduced to
  // * ease the Digester rules for parsing of EML
  // *
  // * @param citation to add
  // */
  // public void addCitation(String citation) {
  // this.citation=citation;
  // }

  /**
   * utility to add a jgtiCuratorialUnit to the list. This method was introduced
   * to ease the Digester rules for parsing of EML
   * 
   * @param jgtiCuratorialUnit to add
   */
  public void addJgtiCuratorialUnit(JGTICuratorialUnit unit) {
    jgtiCuratorialUnits.add(unit);
  }

  /**
   * utility to add keywords to the keyword sets This method was introduced to
   * ease the Digester rules for parsing of EML
   * 
   * @param agent to add
   */
  public void addKeywordSet(KeywordSet keywordSet) {
    keywords.add(keywordSet);
  }

  /**
   * utility to add a PhysicalData instance to the physicalData list. This
   * method was introduced to ease the Digester rules for parsing of EML
   * 
   * @param PhysicalData to add
   */
  public void addPhysicalData(PhysicalData physicalData) {
    this.physicalData.add(physicalData);
  }

  /**
   * utility to add a coverage to the coverages This method was introduced to
   * ease the Digester rules for parsing of EML
   * 
   * @param coverage to add
   */
  public void addTaxonomicCoverage(TaxonomicCoverage coverage) {
    taxonomicCoverages.add(coverage);
  }

  /**
   * utility to add a coverage to the coverages This method was introduced to
   * ease the Digester rules for parsing of EML
   * 
   * @param coverage to add
   */
  public void addTemporalCoverage(TemporalCoverage coverage) {
    temporalCoverages.add(coverage);
  }

  public String getAbstract() {
    return resource.getDescription();
  }

  public String getAdditionalInfo() {
    if (additionalInfo == null || additionalInfo.length() == 0) {
      return null;
    }
    return additionalInfo;
  }

  public String getAlternateIdentifier() {
    return alternateIdentifier;
  }

  public List<Agent> getAssociatedParties() {
    return associatedParties;
  }

  public List<String> getBibliographicCitations() {
    return bibliographicCitationSet.getBibliographicCitations();
  }

  public BibliographicCitationSet getBibliographicCitationSet() {
    return bibliographicCitationSet;
  }

  public String getCitation() {
    if (citation == null || citation.length() == 0) {
      return null;
    }
    return citation;
  }

  public String getCollectionId() {
    if (collectionId == null || collectionId.length() == 0) {
      return null;
    }
    return collectionId;
  }

  public String getCollectionName() {
    if (collectionName == null || collectionName.length() == 0) {
      return null;
    }
    return collectionName;
  }

  public Date getDateStamp() {
    return dateStamp;
  }

  public String getDistributionUrl() {
    if (distributionUrl == null || distributionUrl.length() == 0) {
      return null;
    }
    return distributionUrl;
  }

  public int getEmlVersion() {
    return emlVersion;
  }

  public GeospatialCoverage getGeographicCoverage() {
    return geographicCoverage;
  }

  public List<GeospatialCoverage> getGeospatialCoverages() {
    return geospatialCoverages;
  }

  public String getGuid() {
    return resource.getGuid();
  }

  public String getHierarchyLevel() {
    if (hierarchyLevel == null || hierarchyLevel.length() == 0) {
      return null;
    }
    return hierarchyLevel;
  }

  public String getIntellectualRights() {
    if (intellectualRights == null || intellectualRights.length() == 0) {
      return null;
    }
    return intellectualRights;
  }

  public List<JGTICuratorialUnit> getJgtiCuratorialUnits() {
    return jgtiCuratorialUnits;
  }

  public List<KeywordSet> getKeywords() {
    return keywords;
  }

  public String getLanguage() {
    if (language == null || language.length() == 0) {
      return null;
    }
    return language;
  }

  public String getLink() {
    return resource.getLink();
  }

  public String getLogoUrl() {
    if (logoUrl == null || logoUrl.length() == 0) {
      return null;
    }
    return logoUrl;
  }

  public String getMetadataLanguage() {
    if (metadataLanguage == null || metadataLanguage.length() == 0) {
      return null;
    }
    return metadataLanguage;
  }

  public LocaleBundle getMetadataLocale() {
    return metadataLocale;
  }

  public Agent getMetadataProvider() {
    return metadataProvider;
  }

  public String getParentCollectionId() {
    if (parentCollectionId == null || parentCollectionId.length() == 0) {
      return null;
    }
    return parentCollectionId;
  }

  public List<PhysicalData> getPhysicalData() {
    return physicalData;
  }

  public Project getProject() {
    return project;
  }

  public Date getPubDate() {
    return pubDate;
  }

  public String getPurpose() {
    if (purpose == null || purpose.length() == 0) {
      return null;
    }
    return purpose;
  }

  public Resource getResource() {
    return resource;
  }

  public Agent getResourceCreator() {
    return resourceCreator;
  }

  public List<Method> getSamplingMethods() {
    return samplingMethods;
  }

  public String getSpecimenPreservationMethod() {
    if (specimenPreservationMethod == null
        || specimenPreservationMethod.length() == 0) {
      return null;
    }
    return specimenPreservationMethod;
  }

  public List<TaxonomicCoverage> getTaxonomicCoverages() {
    return taxonomicCoverages;
  }

  public List<TemporalCoverage> getTemporalCoverages() {
    return temporalCoverages;
  }

  public String getTitle() {
    return resource.getTitle();
  }

  public int increaseEmlVersion() {
    this.emlVersion += 1;
    return this.emlVersion;
  }

  public Agent resourceCreator() {
    return resourceCreator;
  }

  public void setAbstract(String description) {
    resource.setDescription(description);
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public void setAlternateIdentifier(String alternateIdentifier) {
    this.alternateIdentifier = alternateIdentifier;
  }

  public void setAssociatedParties(List<Agent> associatedParties) {
    this.associatedParties = associatedParties;
  }

  public void setBibliographicCitations(List<String> val) {
    bibliographicCitationSet.setBibliographicCitations(val);
  }

  public void setBibliographicCitationSet(BibliographicCitationSet val) {
    bibliographicCitationSet = val;
  }

  public void setCitation(String citation) {
    this.citation = citation;
  }

  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  public void setDateStamp(Date dateStamp) {
    this.dateStamp = dateStamp;
  }

  public void setDistributionUrl(String distributionUrl) {
    this.distributionUrl = distributionUrl;
  }

  public void setEmlVersion(int emlVersion) {
    this.emlVersion = emlVersion;
  }

  public void setGeographicCoverage(GeospatialCoverage geospatialCoverage) {
    this.geographicCoverage = geospatialCoverage;
  }

  public void setGeospatialCoverages(
      List<GeospatialCoverage> geospatialCoverages) {
    this.geospatialCoverages = geospatialCoverages;
  }

  public void setGuid(String guid) {
    resource.setGuid(guid);
  }

  public void setHierarchyLevel(String hierarchyLevel) {
    this.hierarchyLevel = hierarchyLevel;
  }

  public void setIntellectualRights(String intellectualRights) {
    this.intellectualRights = intellectualRights;
  }

  public void setJgtiCuratorialUnits(List<JGTICuratorialUnit> jgtiCuratorialUnit) {
    this.jgtiCuratorialUnits = jgtiCuratorialUnit;
  }

  public void setKeywords(List<KeywordSet> keywords) {
    this.keywords = keywords;
  }

  public void setKeywordSet(List<KeywordSet> keywords) {
    this.keywords = keywords;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setLink(String link) {
    resource.setLink(link);
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public void setMetadataLanguage(String language) {
    this.metadataLanguage = language;
  }

  public void setMetadataLocale(LocaleBundle metadataLocale) {
    this.metadataLocale = metadataLocale;
  }

  public void setMetadataProvider(Agent metadataProvider) {
    this.metadataProvider = metadataProvider;
  }

  public void setParentCollectionId(String parentCollectionId) {
    this.parentCollectionId = parentCollectionId;
  }

  public void setPhysicalData(List<PhysicalData> physicalData) {
    this.physicalData = physicalData;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public void setPubDate(Date pubDate) {
    this.pubDate = pubDate;
  }

  /**
   * Utility to set the date with a textual format The date that the resource
   * was published. The format should be represented as: CCYY, which represents
   * a 4 digit year, or as CCYY-MM-DD, which denotes the full year, month, and
   * day. Note that month and day are optional components. Formats must conform
   * to ISO 8601.
   * http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource
   * .html#pubDate
   * 
   * @param dateString To set
   * @throws ParseException Should it be an erroneous format
   */
  public void setPubDate(String dateString) throws ParseException {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      pubDate = sdf.parse(dateString);
    } catch (ParseException e) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
      pubDate = sdf.parse(dateString);
    }
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setResourceCreator(Agent resourceCreator) {
    this.resourceCreator = resourceCreator;
  }

  public void setSamplingMethods(List<Method> samplingMethods) {
    this.samplingMethods = samplingMethods;
  }

  public void setSpecimenPreservationMethod(String specimenPreservationMethod) {
    this.specimenPreservationMethod = specimenPreservationMethod;
  }

  public void setTaxonomicCoverages(List<TaxonomicCoverage> taxonomicCoverages) {
    this.taxonomicCoverages = taxonomicCoverages;
  }

  public void setTemporalCoverages(List<TemporalCoverage> temporalCoverages) {
    this.temporalCoverages = temporalCoverages;
  }

  public void setTitle(String title) {
    resource.setTitle(title);
  }
}
