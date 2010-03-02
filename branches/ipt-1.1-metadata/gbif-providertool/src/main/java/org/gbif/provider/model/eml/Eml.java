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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.gbif.provider.model.Resource;

import com.google.common.collect.Lists;

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
	 * Serialised data
	 */
	private int emlVersion = 0;

	/**
	 * The IPT resource (note is transient)
	 */
	private transient Resource resource;

	// Note that while Sets would be fine, to ease testing, Lists are
	// used to preserve ordering. A Set implementation that respects ordering
	// would also suffice
	// please refer to typed classes for descriptions of the properties and how
	// they
	// map to EML
	private List<KeywordSet> keywords = Lists.newArrayList();
	private List<Method> samplingMethods = Lists.newArrayList();
	private List<TaxonomicCoverage> taxonomicCoverages = Lists.newArrayList();
	private List<GeospatialCoverage> geospatialCoverages = Lists.newArrayList();
	private List<TemporalCoverage> temporalCoverages = Lists.newArrayList();
	private List<PhysicalData> physicalData = Lists.newArrayList();
	
	/**
	 * A resource that describes a literature citation that one might find in a
	 * bibliography. We cannot use
	 * http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml.html#citation
	 * because the IPT deals with /eml/dataset and not /eml/citation therefore
	 * these are found in the additionalMetadata section of the EML until a more
	 * appropriate place is identified
	 */
	private List<String> citations = Lists.newArrayList();

	/**
	 * The 'creator' element provides the full name of the person, organization,
	 * or position who created the resource.
	 * 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#creator
	 */
	private Agent resourceCreator = new Agent();

	/**
	 * The 'metadataProvider' element provides the full name of the person,
	 * organization, or position who created documentation for the resource.
	 * 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#metadataProvider
	 */
	private Agent metadataProvider = new Agent();

	/**
	 * The 'associatedParty' element provides the full name of other people,
	 * organizations, or positions who should be associated with the resource.
	 * These parties might play various roles in the creation or maintenance of
	 * the resource, and these roles should be indicated in the "role" element.
	 */
	private List<Agent> associatedParties = Lists.newArrayList();

	/**
	 * A rights management statement for the resource, or reference a service
	 * providing such information. Rights information encompasses Intellectual
	 * Property Rights (IPR), Copyright, and various Property Rights. In the
	 * case of a data set, rights might include requirements for use,
	 * requirements for attribution, or other requirements the owner would like
	 * to impose.
	 * 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#intellectualRights
	 */
	private String intellectualRights;

	/**
	 * The language in which the resource is written. This can be a well-known
	 * language name, or one of the ISO language codes to be more precise.
	 * 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#language
	 *      The IPT will always use ISO language codes
	 */
	private String language = "en";

	/**
	 * Language of the metadata composed of an ISO639-2/T three letter language
	 * code and an ISO3166-1 three letter country code.
	 */
	private String metadataLanguage = "en";

	/**
	 * The date that the resource was published. The format should be
	 * represented as: CCYY, which represents a 4 digit year, or as CCYY-MM-DD,
	 * which denotes the full year, month, and day. Note that month and day are
	 * optional components. Formats must conform to ISO 8601.
	 * http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#pubDate
	 */
	private Date pubDate;

	/**
	 * Date of metadata creation or the last metadata update Default to now(),
	 * but can be overridden
	 */
	private Date dateStamp = new Date();

	/**
	 * Dataset level to which the metadata applies. The default value for GBIF
	 * is “dataset”
	 * 
	 * @see http://www.fgdc.gov/standards/projects/incits-l1-standards-projects/NAP-Metadata/napMetadataProfileV101.pdf
	 */
	private String hierarchyLevel = "dataset";

	/**
	 * The GBIF metadata profile states "Describes other languages used in
	 * metadata free text description. Consists of language, country and
	 * characterEncoding" In Java world, a LocaleBundle handles this concisely
	 */
	private LocaleBundle metadataLocale;

	/**
	 * Identifier for the parent collection for this sub-collection. Enables a
	 * hierarchy of collections and sub collections to be built.
	 * 
	 * @see http://rs.tdwg.org/ontology/voc/Collection#isPartOfCollection
	 */
	private String parentCollectionId;

	/**
	 * Official name of the Collection in the local language.
	 * 
	 * @see http://purl.org/dc/elements/1.1/title 
	 * Note: this could potentially
	 * be sourced from the resource title, but this is declared explicitly
	 * in the GBIF IPT metadata profile, so must assume that this is
	 * required for a title in a different language, presumably to aid free
	 * text discovery in original language
	 */
	private String collectionName;

	/**
	 * The URI (LSID or URL) of the collection. In RDF, used as URI of the
	 * collection resource.
	 * 
	 * @see http://rs.tdwg.org/ontology/voc/Collection#collectionId
	 */
	private String collectionId;

	/**
	 * Picklist keyword indicating the process or technique used to prevent
	 * physical deterioration of non-living collections. Expected to contain an
	 * instance from the Specimen Preservation Method Type Term vocabulary.
	 * 
	 * @see http://rs.tdwg.org/ontology/voc/Collection#specimenPreservationMethod
	 */
	private String specimenPreservationMethod;

	/**
	 * A quantitative descriptor (number of specimens, samples or batches). The
	 * actual quantification could be covered by 1) an exact number of
	 * “JGI-units” in the collection plus a measure of uncertainty (+/- x); 2) a
	 * range of numbers (x to x), with the lower value representing an exact
	 * number, when the higher value is omitted.
	 */
	private JGTICuratorialUnit jgtiCuratorialUnit;

	/**
	 * This is not in the GBIF extended metadata document, but seems like a sensible placeholder
	 * that can be used to capture anything missing, and maps nicely in EML, therefore is added
	 */
	private String additionalInfo;
	
	/**
	 * This is not in the GBIF extended metadata document, but seems like a sensible field to maintain, 
	 * and maps nicely in EML, therefore is added
	 */
	private String purpose;
	
	/**
	 * The project this resource is associated with
	 */
	private Project project;
	
	// private LocaleBundle dataLocale;
	// private String description;
	// private String formationPeriod;
	// private String homepage;
	// private String distributionUrl;
	// private Set<String> kingdomCoverages = Sets.newHashSet();
	// private String livingTimePeriod;
	// private LocaleBundle resourceLocale;
	// private Point location;
	// private String placenameCoverageDescription;
	// private List<Agent> primaryContacts = Lists.newArrayList();
	// private Set<Attribute> resourceAttributes = Sets.newHashSet();
	// private String type;
	// private String publishPlace;
	// private GeospatialCoverage geographicCoverage;
	// private String taxonomicCoverageDescription;
	// private TimeKeyword temporalCoverage = new TimeKeyword();
	// private String methods;
	// private Project researchProject = new Project();
	// private TaxonKeyword lowestCommonTaxon;// TODO: verify: = new
	// TaxonKeyword();
	// private List<TaxonKeyword> taxonomicClassification = new
	// ArrayList<TaxonKeyword>();
	// private String samplingDescription;
	// private String qualityControl;
	// private String purpose;
	// private String maintenance;
	
	/**
	 * Default constructor needed by Struts2
	 */
	public Eml() {
		super();
		this.pubDate = new Date();
		this.resourceCreator.setRole(Role.ORIGINATOR);
		this.metadataProvider.setRole(Role.METADATA_PROVIDER);
	}

	public int increaseEmlVersion() {
		this.emlVersion += 1;
		return this.emlVersion;
	}
	
	public String getAbstract() {
		return resource.getDescription();
	}

	public void setAbstract(String description) {
		resource.setDescription(description);
	}

	public int getEmlVersion() {
		return emlVersion;
	}

	public String getGuid() {
		return resource.getGuid();
	}

	public void setGuid(String guid) {
		resource.setGuid(guid);
	}

	public String getLink() {
		return resource.getLink();
	}

	public Date getPubDate() {
		return pubDate;
	}

	public Resource getResource() {
		return resource;
	}

	public Agent getResourceCreator() {
		return resourceCreator;
	}

	public String getTitle() {
		return resource.getTitle();
	}

	public Agent resourceCreator() {
		return resourceCreator;
	}

	public void setEmlVersion(int emlVersion) {
		this.emlVersion = emlVersion;
	}

	public void setIntellectualRights(String intellectualRights) {
		this.intellectualRights = intellectualRights;
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

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setResourceCreator(Agent resourceCreator) {
		this.resourceCreator = resourceCreator;
	}

	public void setTitle(String title) {
		resource.setTitle(title);
	}

	/**
	 * utility to add Agents to the primary contacts This method was introduced
	 * to ease the Digester rules for parsing of EML
	 * 
	 * @param agent To add
	 */
	public void addAssociatedParty(Agent agent) {
		agent.setRole(Role.ASSOCIATED_PARTY);
		associatedParties.add(agent);
	}

	/**
	 * utility to add keywords to the keyword sets This method was introduced to
	 * ease the Digester rules for parsing of EML
	 * 
	 * @param agent To add
	 */
	public void addKeywordSet(KeywordSet keywordSet) {
		keywords.add(keywordSet);
	}

	/**
	 * utility to add a coverage to the coverages This method was introduced to
	 * ease the Digester rules for parsing of EML
	 * 
	 * @param coverage To add
	 */
	public void addGeospatialCoverage(GeospatialCoverage geospatialCoverage) {
		geospatialCoverages.add(geospatialCoverage);
	}

	/**
	 * utility to add a coverage to the coverages This method was introduced to
	 * ease the Digester rules for parsing of EML
	 * 
	 * @param coverage To add
	 */
	public void addTemporalCoverage(TemporalCoverage coverage) {
		temporalCoverages.add(coverage);
	}

	/**
	 * utility to add a coverage to the coverages This method was introduced to
	 * ease the Digester rules for parsing of EML
	 * 
	 * @param coverage To add
	 */
	public void addTaxonomicCoverage(TaxonomicCoverage coverage) {
		taxonomicCoverages.add(coverage);
	}

	/**
	 * Utility to set the date with a textual format The date that the resource
	 * was published. The format should be represented as: CCYY, which
	 * represents a 4 digit year, or as CCYY-MM-DD, which denotes the full year,
	 * month, and day. Note that month and day are optional components. Formats
	 * must conform to ISO 8601.
	 * http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#pubDate
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

	public List<GeospatialCoverage> getGeospatialCoverages() {
		return geospatialCoverages;
	}

	public void setGeospatialCoverages(List<GeospatialCoverage> geospatialCoverages) {
		this.geospatialCoverages = geospatialCoverages;
	}

	public List<TemporalCoverage> getTemporalCoverages() {
		return temporalCoverages;
	}

	public void setTemporalCoverages(List<TemporalCoverage> temporalCoverages) {
		this.temporalCoverages = temporalCoverages;
	}

	public List<TaxonomicCoverage> getTaxonomicCoverages() {
		return taxonomicCoverages;
	}

	public void setTaxonomicCoverages(List<TaxonomicCoverage> taxonomicCoverages) {
		this.taxonomicCoverages = taxonomicCoverages;
	}

	public List<Method> getSamplingMethods() {
		return samplingMethods;
	}

	public void setSamplingMethods(List<Method> samplingMethods) {
		this.samplingMethods = samplingMethods;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<KeywordSet> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<KeywordSet> keywords) {
		this.keywords = keywords;
	}

	public List<PhysicalData> getPhysicalData() {
		return physicalData;
	}

	public void setPhysicalData(List<PhysicalData> physicalData) {
		this.physicalData = physicalData;
	}

	public List<String> getCitations() {
		return citations;
	}

	public void setCitations(List<String> citations) {
		this.citations = citations;
	}

	public Agent getMetadataProvider() {
		return metadataProvider;
	}

	public void setMetadataProvider(Agent metadataProvider) {
		this.metadataProvider = metadataProvider;
	}

	public List<Agent> getAssociatedParties() {
		return associatedParties;
	}

	public void setAssociatedParties(List<Agent> associatedParties) {
		this.associatedParties = associatedParties;
	}

	public String getMetadataLanguage() {
		return metadataLanguage;
	}

	public void setMetadataLanguage(String metadataLanguage) {
		this.metadataLanguage = metadataLanguage;
	}

	public Date getDateStamp() {
		return dateStamp;
	}

	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}

	public String getHierarchyLevel() {
		return hierarchyLevel;
	}

	public void setHierarchyLevel(String hierarchyLevel) {
		this.hierarchyLevel = hierarchyLevel;
	}

	public LocaleBundle getMetadataLocale() {
		return metadataLocale;
	}

	public void setMetadataLocale(LocaleBundle metadataLocale) {
		this.metadataLocale = metadataLocale;
	}

	public String getParentCollectionId() {
		return parentCollectionId;
	}

	public void setParentCollectionId(String parentCollectionId) {
		this.parentCollectionId = parentCollectionId;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public String getSpecimenPreservationMethod() {
		return specimenPreservationMethod;
	}

	public void setSpecimenPreservationMethod(String specimenPreservationMethod) {
		this.specimenPreservationMethod = specimenPreservationMethod;
	}

	public JGTICuratorialUnit getJgtiCuratorialUnit() {
		return jgtiCuratorialUnit;
	}

	public void setJgtiCuratorialUnit(JGTICuratorialUnit jgtiCuratorialUnit) {
		this.jgtiCuratorialUnit = jgtiCuratorialUnit;
	}

	public String getIntellectualRights() {
		return intellectualRights;
	}

	public String getLanguage() {
		return language;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
}
