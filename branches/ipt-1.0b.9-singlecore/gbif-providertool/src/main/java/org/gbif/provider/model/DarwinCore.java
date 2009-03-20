/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.hibernate.validator.NotNull;


/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
@Table(name="Darwin_Core"
	, uniqueConstraints = {@UniqueConstraint(columnNames={"localId", "resource_fk"})}
) 

@org.hibernate.annotations.Table(appliesTo = "Darwin_Core", indexes = { 	
		@org.hibernate.annotations.Index(name="latitude", columnNames = { "lat" }), 
		@org.hibernate.annotations.Index(name="longitude", columnNames = { "lon" }) 
		})
public class DarwinCore implements CoreRecord, Comparable<DarwinCore>{
	public static final Set<String> INTEGER_PROPERTIES = new HashSet<String>(Arrays.asList("YearSampled","DayOfMonth","StartDayOfYear","EndDayOfYear","IndividualCount","CatalogNumberNumeric"));
	public static final Set<String> DOUBLE_PROPERTIES = new HashSet<String>(Arrays.asList("CoordinateUncertaintyInMeters", "MaximumDepthInMeters", "MaximumElevationInMeters","DistanceAboveSurfaceInMetersMaximum","DistanceAboveSurfaceInMetersMinimum"));
	public static final Set<String> DATE_PROPERTIES = new HashSet<String>(Arrays.asList("DateIdentified", "LatestDateCollected"));
	public static final Set<String> TOKEN_PROPERTIES = new HashSet<String>(Arrays.asList("kingdom", "phylum", "class", "order", "family", "genus", "subgenus"));	


	// for core record
	private Long id;
	private String localId;
	@NotNull
	private String guid;
	private String link;
	private boolean isDeleted;
	private Date modified;
	@NotNull
	private DataResource resource;

	// DarinCore derived fields. calculated from raw Strings
	private Point location = new Point();
	private Taxon taxon;
	private Region region;
	private Date collected;
	private Double elevation;
	private Double depth;

	// 
	// All DarwinCore terms 
	// excl. Event/AttributeSample, ResourceRelationship and 
	// 
	// DublinCore
	// dc:modified = this.modified
	private String language;
	private String rights;
	private String rightsHolder;
	// Dataset
	private String datasetID;
	// Sample
	// sampleID = this.guid
	private String institutionCode;
	private String collectionCode;
	private String collectionID;
	private String basisOfRecord;
	private String accessConstraints;
	private String informationWithheld;
	private String generalizations;
	private String sampleDetails;
	private String sampleRemarks;
	private String catalogNumber;
	private String catalogNumberNumeric;
	private String individualID;
	private String individualCount;
	private String citation;
	private String sex;
	private String lifeStage;
	private String reproductiveCondition;
	private String establishmentMeans;
	private String sampleAttributes;
	private String preparations;
	private String disposition;
	private String otherCatalogNumbers;
	private String associatedMedia;
	private String associatedReferences;
	private String associatedSamples;
	private String associatedSequences;
	private String associatedTaxa;
	// SampleEvent
	private String samplingEventID;
	private String samplingProtocol;
	private String verbatimCollectingDate;
	private String earliestDateCollected;
	private String latestDateCollected;
	private String startDayOfYear;
	private String endDayOfYear;
	private String startTimeOfDay;
	private String endTimeOfDay;
	private String yearSampled;
	private String monthOfYear;
	private String dayOfMonth;
	private String habitat;
	private String behavior;
	private String collector;
	private String collectorNumber;
	private String fieldNumber;
	private String fieldNotes;
	private String samplingEventAttributes;
	private String samplingEventRemarks;	
	// SampleLocation => this.region
	private String samplingLocationID;
	private String higherGeographyID;
	private String higherGeography;
	private String continent;
	private String waterbody;
	private String islandGroup;
	private String island;
	private String country;
	private String countryCode;
	private String stateProvince;
	private String county;
	private String locality;
	private String verbatimLocality;
	private String verbatimElevation;
	private String minimumElevationInMeters;
	private String maximumElevationInMeters;
	private String verbatimDepth;
	private String minimumDepthInMeters;
	private String maximumDepthInMeters;
	private String distanceAboveSurfaceInMetersMinimum;
	private String distanceAboveSurfaceInMetersMaximum;
	private String decimalLatitude;
	private String decimalLongitude;
	private String geodeticDatum;
	private String coordinateUncertaintyInMeters;
	private String coordinatePrecision;
	private String pointRadiusSpatialFit;
	private String verbatimCoordinates;
	private String verbatimLatitude;
	private String verbatimLongitude;
	private String georeferencedBy;
	private String georeferenceProtocol;
	private String verbatimCoordinateSystem;
	private String georeferenceSources;
	private String georeferenceVerificationStatus;
	private String georeferenceRemarks;
	private String footprintWKT;
	private String footprintSpatialFit;
	private String samplingLocationRemarks;	
	// Identification
	private String identificationID;
	private String identifiedBy;
	private String dateIdentified;
	private String identificationReferences;
	private String identificationRemarks;
	private String previousIdentifications;
	private String identificationQualifier;
	private String typeStatus;
	// Taxon => this.taxon
	private String taxonID;
	private String scientificName;
	private String binomial;
	private String higherTaxonID;
	private String higherTaxon;
	private String kingdom;
	private String phylum;
	private String classs;
	private String order;
	private String family;
	private String genus;
	private String subgenus;
	private String specificEpithet;
	private String taxonRank;
	private String infraspecificEpithet;
	private String scientificNameAuthorship;
	private String nomenclaturalCode;
	private String taxonAccordingTo;
	private String namePublishedIn;
	private String taxonomicStatus;
	private String nomenclaturalStatus;
	private String acceptedTaxonID;
	private String acceptedTaxon;
	private String basionymID;
	private String basionym;	


	
	public static DarwinCore newInstance(DataResource resource){
		DarwinCore dwc = new DarwinCore();
		dwc.resource=resource;
		return dwc;
	}
	public static DarwinCore newMock(DataResource resource){
		Random rnd = new Random();
		DarwinCore dwc = DarwinCore.newInstance(resource);
		// populate instance
		// set unique localId to ensure we can save this record. Otherwise we might get a non unique constraint exception...
		String guid = UUID.randomUUID().toString();
		String localId = rnd.nextInt(99999999)+"";
		dwc.setLocalId(localId);
		dwc.setGuid(guid);
		dwc.setCatalogNumber("rbgk-"+localId+"-x");
		dwc.setBasisOfRecord("PreservedSpecimen");
		dwc.setInstitutionCode("RBGK");
		// location
		dwc.setCountry("PL");
		// taxonomy
		dwc.setScientificName("Abies alba L.");
		dwc.setGenus("Abies");
		dwc.setFamily("Pinaceae");
		return dwc;
	}
	
	

	// CORE RECORD
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Transient
	public Long getCoreId() {
		return id;
	}

	@Column(length=64)
	@org.hibernate.annotations.Index(name="dwc_local_id")
	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}	

	@Column(length=128, unique=true)
	@org.hibernate.annotations.Index(name="guid")
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	@org.hibernate.annotations.Index(name="deleted")
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@ManyToOne
	public DataResource getResource() {
		return resource;
	}
	public void setResource(DataResource resource) {
		this.resource = resource;
	}
	@Transient
	public Long getResourceId() {
		return resource.getId();
	}
	

	// OTHER	
	@ManyToOne(optional = true)
	public Taxon getTaxon() {
		return taxon;
	}
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

	@ManyToOne(optional = true)
	public Region getRegion() {
		return region;
	}
	public void setRegion(Region region) {
		this.region = region;
	}
	
	@org.hibernate.annotations.Index(name="dwc_date_collected")
    public Date getCollected() {
		return collected;
	}
	public void setCollected(Date dateCollected) {
		this.collected = dateCollected;
	}
	
	@AttributeOverrides({
        @AttributeOverride(name="latitude", column = @Column(name="lat")), 
        @AttributeOverride(name="longitude", column = @Column(name="lon")) 
    })
	public Point getLocation() {
    	// to prevent NPE create new empty point in case it doesnt exist yet
    	if (location==null){
    		location=new Point();
    	}
		return location;
	}
	public void setLocation(Point location) {
		this.location = location;
	}
	
	@Transient
	public Double getLatitude() {
		return location.getLatitude();
	}
	@Transient
	public Double getLongitude() {
		return location.getLongitude();
	}

	
	public Double getElevation() {
		return elevation;
	}
	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}
	public Double getDepth() {
		return depth;
	}
	public void setDepth(Double depth) {
		this.depth = depth;
	}
	

	@Transient
	public String getPropertyValue(ExtensionProperty property){
		return getPropertyValue(property.getName());
	}
	@Transient
	private String getPropertyValue(String propName){
		if (propName.equals("Class")){
			propName = "Classs";
		}
		String getter = String.format("get%s", propName);
		String value = null;
		try {
			Method m = this.getClass().getMethod(getter);
			Object obj = m.invoke(this);
			if (obj!=null){
				value=obj.toString();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return value;
	}
	public boolean setPropertyValue(ExtensionProperty property, String value){
		try {
			Method m = this.getClass().getMethod(String.format("set%s", property.getName()), String.class);
			m.invoke(this, value);
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}	
	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", this.getId())
			.append("basisOfRecord", this.basisOfRecord)
			.append("scientificName",this.getScientificName())
			.append("localID", this.getLocalId())
			.append("deleted", this.isDeleted())
			.append("institutionCode", this.institutionCode)
			.append("collectionCode",this.collectionCode)
			.append("catalogNumber",this.catalogNumber)
			.append("country",this.getCountry())
			.append("guid", this.guid)
			.toString();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof DarwinCore)) {
			return false;
		}
		DarwinCore dwc = (DarwinCore) object;
        return this.hashCode() == dwc.hashCode();		
	}
	
	/**
	 * Works on the raw imported properties and Ignores all secondary derived properties.
	 * Therefore id, deleted, lat/longAsFloat, modified,created, region & taxon are ignored in the hashing
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        // core record
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (localId != null ? localId.hashCode() : 0);
        // Sample
        result = 31 * result + (getInstitutionCode() != null ? getInstitutionCode().hashCode() : 0);
        result = 31 * result + (getCollectionCode() != null ? getCollectionCode().hashCode() : 0);
        result = 31 * result + (getCollectionID() != null ? getCollectionID().hashCode() : 0);
        result = 31 * result + (getBasisOfRecord() != null ? getBasisOfRecord().hashCode() : 0);
        result = 31 * result + (getAccessConstraints() != null ? getAccessConstraints().hashCode() : 0);
        result = 31 * result + (getInformationWithheld() != null ? getInformationWithheld().hashCode() : 0);
        result = 31 * result + (getGeneralizations() != null ? getGeneralizations().hashCode() : 0);
        result = 31 * result + (getSampleDetails() != null ? getSampleDetails().hashCode() : 0);
        result = 31 * result + (getSampleRemarks() != null ? getSampleRemarks().hashCode() : 0);
        result = 31 * result + (getCatalogNumber() != null ? getCatalogNumber().hashCode() : 0);
        result = 31 * result + (getCatalogNumberNumeric() != null ? getCatalogNumberNumeric().hashCode() : 0);
        result = 31 * result + (getIndividualID() != null ? getIndividualID().hashCode() : 0);
        result = 31 * result + (getIndividualCount() != null ? getIndividualCount().hashCode() : 0);
        result = 31 * result + (getCitation() != null ? getCitation().hashCode() : 0);
        result = 31 * result + (getSex() != null ? getSex().hashCode() : 0);
        result = 31 * result + (getLifeStage() != null ? getLifeStage().hashCode() : 0);
        result = 31 * result + (getReproductiveCondition() != null ? getReproductiveCondition().hashCode() : 0);
        result = 31 * result + (getEstablishmentMeans() != null ? getEstablishmentMeans().hashCode() : 0);
        result = 31 * result + (getSampleAttributes() != null ? getSampleAttributes().hashCode() : 0);
        result = 31 * result + (getPreparations() != null ? getPreparations().hashCode() : 0);
        result = 31 * result + (getDisposition() != null ? getDisposition().hashCode() : 0);
        result = 31 * result + (getOtherCatalogNumbers() != null ? getOtherCatalogNumbers().hashCode() : 0);
        result = 31 * result + (getAssociatedMedia() != null ? getAssociatedMedia().hashCode() : 0);
        result = 31 * result + (getAssociatedReferences() != null ? getAssociatedReferences().hashCode() : 0);
        result = 31 * result + (getAssociatedSamples() != null ? getAssociatedSamples().hashCode() : 0);
        result = 31 * result + (getAssociatedSequences() != null ? getAssociatedSequences().hashCode() : 0);
        result = 31 * result + (getAssociatedTaxa() != null ? getAssociatedTaxa().hashCode() : 0);
        // SampleEvent
        result = 31 * result + (getSamplingEventID() != null ? getSamplingEventID().hashCode() : 0);
        result = 31 * result + (getSamplingProtocol() != null ? getSamplingProtocol().hashCode() : 0);
        result = 31 * result + (getVerbatimCollectingDate() != null ? getVerbatimCollectingDate().hashCode() : 0);
        result = 31 * result + (getEarliestDateCollected() != null ? getEarliestDateCollected().hashCode() : 0);
        result = 31 * result + (getLatestDateCollected() != null ? getLatestDateCollected().hashCode() : 0);
        result = 31 * result + (getStartDayOfYear() != null ? getStartDayOfYear().hashCode() : 0);
        result = 31 * result + (getEndDayOfYear() != null ? getEndDayOfYear().hashCode() : 0);
        result = 31 * result + (getStartTimeOfDay() != null ? getStartTimeOfDay().hashCode() : 0);
        result = 31 * result + (getEndTimeOfDay() != null ? getEndTimeOfDay().hashCode() : 0);
        result = 31 * result + (getYearSampled() != null ? getYearSampled().hashCode() : 0);
        result = 31 * result + (getMonthOfYear() != null ? getMonthOfYear().hashCode() : 0);
        result = 31 * result + (getDayOfMonth() != null ? getDayOfMonth().hashCode() : 0);
        result = 31 * result + (getHabitat() != null ? getHabitat().hashCode() : 0);
        result = 31 * result + (getBehavior() != null ? getBehavior().hashCode() : 0);
        result = 31 * result + (getCollector() != null ? getCollector().hashCode() : 0);
        result = 31 * result + (getCollectorNumber() != null ? getCollectorNumber().hashCode() : 0);
        result = 31 * result + (getFieldNumber() != null ? getFieldNumber().hashCode() : 0);
        result = 31 * result + (getFieldNotes() != null ? getFieldNotes().hashCode() : 0);
        result = 31 * result + (getSamplingEventAttributes() != null ? getSamplingEventAttributes().hashCode() : 0);
        result = 31 * result + (getSamplingEventRemarks() != null ? getSamplingEventRemarks().hashCode() : 0);	
        // SampleLocation => this.region
        result = 31 * result + (getSamplingLocationID() != null ? getSamplingLocationID().hashCode() : 0);
        result = 31 * result + (getHigherGeographyID() != null ? getHigherGeographyID().hashCode() : 0);
        result = 31 * result + (getHigherGeography() != null ? getHigherGeography().hashCode() : 0);
        result = 31 * result + (getContinent() != null ? getContinent().hashCode() : 0);
        result = 31 * result + (getWaterbody() != null ? getWaterbody().hashCode() : 0);
        result = 31 * result + (getIslandGroup() != null ? getIslandGroup().hashCode() : 0);
        result = 31 * result + (getIsland() != null ? getIsland().hashCode() : 0);
        result = 31 * result + (getCountry() != null ? getCountry().hashCode() : 0);
        result = 31 * result + (getCountryCode() != null ? getCountryCode().hashCode() : 0);
        result = 31 * result + (getStateProvince() != null ? getStateProvince().hashCode() : 0);
        result = 31 * result + (getCounty() != null ? getCounty().hashCode() : 0);
        result = 31 * result + (getLocality() != null ? getLocality().hashCode() : 0);
        result = 31 * result + (getVerbatimLocality() != null ? getVerbatimLocality().hashCode() : 0);
        result = 31 * result + (getVerbatimElevation() != null ? getVerbatimElevation().hashCode() : 0);
        result = 31 * result + (getMinimumElevationInMeters() != null ? getMinimumElevationInMeters().hashCode() : 0);
        result = 31 * result + (getMaximumElevationInMeters() != null ? getMaximumElevationInMeters().hashCode() : 0);
        result = 31 * result + (getVerbatimDepth() != null ? getVerbatimDepth().hashCode() : 0);
        result = 31 * result + (getMinimumDepthInMeters() != null ? getMinimumDepthInMeters().hashCode() : 0);
        result = 31 * result + (getMaximumDepthInMeters() != null ? getMaximumDepthInMeters().hashCode() : 0);
        result = 31 * result + (getDistanceAboveSurfaceInMetersMinimum() != null ? getDistanceAboveSurfaceInMetersMinimum().hashCode() : 0);
        result = 31 * result + (getDistanceAboveSurfaceInMetersMaximum() != null ? getDistanceAboveSurfaceInMetersMaximum().hashCode() : 0);
        result = 31 * result + (getDecimalLatitude() != null ? getDecimalLatitude().hashCode() : 0);
        result = 31 * result + (getDecimalLongitude() != null ? getDecimalLongitude().hashCode() : 0);
        result = 31 * result + (getGeodeticDatum() != null ? getGeodeticDatum().hashCode() : 0);
        result = 31 * result + (getCoordinateUncertaintyInMeters() != null ? getCoordinateUncertaintyInMeters().hashCode() : 0);
        result = 31 * result + (getCoordinatePrecision() != null ? getCoordinatePrecision().hashCode() : 0);
        result = 31 * result + (getPointRadiusSpatialFit() != null ? getPointRadiusSpatialFit().hashCode() : 0);
        result = 31 * result + (getVerbatimCoordinates() != null ? getVerbatimCoordinates().hashCode() : 0);
        result = 31 * result + (getVerbatimLatitude() != null ? getVerbatimLatitude().hashCode() : 0);
        result = 31 * result + (getVerbatimLongitude() != null ? getVerbatimLongitude().hashCode() : 0);
        result = 31 * result + (getGeoreferencedBy() != null ? getGeoreferencedBy().hashCode() : 0);
        result = 31 * result + (getGeoreferenceProtocol() != null ? getGeoreferenceProtocol().hashCode() : 0);
        result = 31 * result + (getVerbatimCoordinateSystem() != null ? getVerbatimCoordinateSystem().hashCode() : 0);
        result = 31 * result + (getGeoreferenceSources() != null ? getGeoreferenceSources().hashCode() : 0);
        result = 31 * result + (getGeoreferenceVerificationStatus() != null ? getGeoreferenceVerificationStatus().hashCode() : 0);
        result = 31 * result + (getGeoreferenceRemarks() != null ? getGeoreferenceRemarks().hashCode() : 0);
        result = 31 * result + (getFootprintWKT() != null ? getFootprintWKT().hashCode() : 0);
        result = 31 * result + (getFootprintSpatialFit() != null ? getFootprintSpatialFit().hashCode() : 0);
        result = 31 * result + (getSamplingLocationRemarks() != null ? getSamplingLocationRemarks().hashCode() : 0);	
        // Identification
        result = 31 * result + (getIdentificationID() != null ? getIdentificationID().hashCode() : 0);
        result = 31 * result + (getIdentifiedBy() != null ? getIdentifiedBy().hashCode() : 0);
        result = 31 * result + (getDateIdentified() != null ? getDateIdentified().hashCode() : 0);
        result = 31 * result + (getIdentificationReferences() != null ? getIdentificationReferences().hashCode() : 0);
        result = 31 * result + (getIdentificationRemarks() != null ? getIdentificationRemarks().hashCode() : 0);
        result = 31 * result + (getPreviousIdentifications() != null ? getPreviousIdentifications().hashCode() : 0);
        result = 31 * result + (getIdentificationQualifier() != null ? getIdentificationQualifier().hashCode() : 0);
        result = 31 * result + (getTypeStatus() != null ? getTypeStatus().hashCode() : 0);
        // Taxon => this.taxon
        result = 31 * result + (getTaxonID() != null ? getTaxonID().hashCode() : 0);
        result = 31 * result + (getScientificName() != null ? getScientificName().hashCode() : 0);
        result = 31 * result + (getBinomial() != null ? getBinomial().hashCode() : 0);
        result = 31 * result + (getHigherTaxonID() != null ? getHigherTaxonID().hashCode() : 0);
        result = 31 * result + (getHigherTaxon() != null ? getHigherTaxon().hashCode() : 0);
        result = 31 * result + (getKingdom() != null ? getKingdom().hashCode() : 0);
        result = 31 * result + (getPhylum() != null ? getPhylum().hashCode() : 0);
        result = 31 * result + (getClasss() != null ? getClasss().hashCode() : 0);
        result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
        result = 31 * result + (getFamily() != null ? getFamily().hashCode() : 0);
        result = 31 * result + (getGenus() != null ? getGenus().hashCode() : 0);
        result = 31 * result + (getSubgenus() != null ? getSubgenus().hashCode() : 0);
        result = 31 * result + (getSpecificEpithet() != null ? getSpecificEpithet().hashCode() : 0);
        result = 31 * result + (getTaxonRank() != null ? getTaxonRank().hashCode() : 0);
        result = 31 * result + (getInfraspecificEpithet() != null ? getInfraspecificEpithet().hashCode() : 0);
        result = 31 * result + (getScientificNameAuthorship() != null ? getScientificNameAuthorship().hashCode() : 0);
        result = 31 * result + (getNomenclaturalCode() != null ? getNomenclaturalCode().hashCode() : 0);
        result = 31 * result + (getTaxonAccordingTo() != null ? getTaxonAccordingTo().hashCode() : 0);
        result = 31 * result + (getNamePublishedIn() != null ? getNamePublishedIn().hashCode() : 0);
        result = 31 * result + (getTaxonomicStatus() != null ? getTaxonomicStatus().hashCode() : 0);
        result = 31 * result + (getNomenclaturalStatus() != null ? getNomenclaturalStatus().hashCode() : 0);
        result = 31 * result + (getAcceptedTaxonID() != null ? getAcceptedTaxonID().hashCode() : 0);
        result = 31 * result + (getAcceptedTaxon() != null ? getAcceptedTaxon().hashCode() : 0);
        result = 31 * result + (getBasionymID() != null ? getBasionymID().hashCode() : 0);
        result = 31 * result + (getBasionym() != null ? getBasionym().hashCode() : 0);	
  
        return result;
	}
	
	public int compareTo(DarwinCore myClass) {
		return new CompareToBuilder()
				.append(this.institutionCode, myClass.institutionCode)
				.append(this.collectionCode, myClass.collectionCode)
				.append(this.catalogNumber, myClass.catalogNumber)
				.append(this.getScientificName(),	myClass.getScientificName())
				.append(this.localId, myClass.localId)
				.append(this.guid,myClass.guid)
				.toComparison();
	}
	
	@Transient
	public String getHigherTaxonName(Rank rank){
		if (rank==Rank.Species){
			if (getSpecificEpithet()!=null){
				return StringUtils.trimToNull(String.format("%s %s", getGenus(), getSpecificEpithet()));
			}else{
				return null;
			}
		}else if (rank==Rank.InfraSpecies){
			if (getInfraspecificEpithet()!=null){
				return StringUtils.trimToNull(String.format("%s %s %s %s", getGenus(), getSpecificEpithet(), getTaxonRank(), getInfraspecificEpithet()));				
			}else{
				return null;
			}
		}else{
			return StringUtils.trimToNull(getPropertyValue(StringUtils.capitalise(rank.columnName)));
		}
	}
	/** 9 rank strings joined by | pipe symbol, starting with kingdom, ending in Genus, SpeciesEpi, InfraSpeciesEpi and finally ScientificName
	 * @return
	 */
	@Transient
	public String getTaxonomyPath() {
		return getTaxonomyPath(Rank.InfraSpecies)+"|"+getScientificName();
	}
	@Transient
	public String getTaxonomyPath(Rank rank) {
		String path = StringUtils.trimToEmpty(getKingdom());
		if (rank.compareTo(Rank.Phylum) >= 0){
			path += "|"+StringUtils.trimToEmpty(getPhylum());
		}
		if (rank.compareTo(Rank.Class) >= 0){
			path += "|"+StringUtils.trimToEmpty(getClasss());
		}
		if (rank.compareTo(Rank.Order) >= 0){
			path += "|"+StringUtils.trimToEmpty(getOrder());
		}
		if (rank.compareTo(Rank.Family) >= 0){
			path += "|"+StringUtils.trimToEmpty(getFamily());
		}
		if (rank.compareTo(Rank.Genus) >= 0){
			path += "|"+StringUtils.trimToEmpty(getGenus());
		}
		if (rank.compareTo(Rank.Species) >= 0){
			path += "|"+StringUtils.trimToEmpty(getSpecificEpithet());
		}
		if (rank.compareTo(Rank.InfraSpecies) >= 0){
			path += "|"+StringUtils.trimToEmpty(getInfraspecificEpithet());
		}
		return path;
	}
	
	@Transient
	public String getHigherGeographyName(RegionType regionType){
		return StringUtils.trimToNull(getPropertyValue(StringUtils.capitalise(regionType.columnName)));
	}
	@Transient
	public String getGeographyPath() {
		return getGeographyPath(RegionType.Locality);
	}
	@Transient
	public String getGeographyPath(RegionType regionType) {
		String path = StringUtils.trimToEmpty(getKingdom());
		if (regionType.compareTo(RegionType.Continent) >= 0){
			path += "|"+StringUtils.trimToEmpty(getContinent());
		}
		if (regionType.compareTo(RegionType.Waterbody) >= 0){
			path += "|"+StringUtils.trimToEmpty(getWaterbody());
		}
		if (regionType.compareTo(RegionType.IslandGroup) >= 0){
			path += "|"+StringUtils.trimToEmpty(getIslandGroup());
		}
		if (regionType.compareTo(RegionType.Island) >= 0){
			path += "|"+StringUtils.trimToEmpty(getIsland());
		}
		if (regionType.compareTo(RegionType.Country) >= 0){
			path += "|"+StringUtils.trimToEmpty(getCountry());
		}
		if (regionType.compareTo(RegionType.State) >= 0){
			path += "|"+StringUtils.trimToEmpty(getStateProvince());
		}
		if (regionType.compareTo(RegionType.County) >= 0){
			path += "|"+StringUtils.trimToEmpty(getCounty());
		}
		if (regionType.compareTo(RegionType.Locality) >= 0){
			path += "|"+StringUtils.trimToEmpty(getLocality());
		}		
		return path;
	}

	

	
	
	
	
	
	
	
	// SETTER/GETTER of regular dwc terms
	//
	// DUBLIN CORE
	@Column(length=64)
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	@Lob
	public String getRights() {
		return rights;
	}
	public void setRights(String rights) {
		this.rights = rights;
	}
	@Column(length=128)
	public String getRightsHolder() {
		return rightsHolder;
	}
	public void setRightsHolder(String rightsHolder) {
		this.rightsHolder = rightsHolder;
	}
	
	// DATASET
	@Column(length=128)
	public String getDatasetID() {
		return datasetID;
	}
	public void setDatasetID(String datasetID) {
		this.datasetID = datasetID;
	}
	
	// SAMPLE
	@Transient
	public String getSampleID() {
		return guid;
	}
	public void setSampleID(String sampleID) {
		this.guid = sampleID;
	}
	@Column(length=128)
	@org.hibernate.annotations.Index(name="idx_dwc_institution_code")
	public String getInstitutionCode() {
		return institutionCode;
	}
	public void setInstitutionCode(String institutionCode) {
		this.institutionCode = institutionCode;
	}
	@Column(length=128)
	@org.hibernate.annotations.Index(name="idx_dwc_collection_code")
	public String getCollectionCode() {
		return collectionCode;
	}
	public void setCollectionCode(String collectionCode) {
		this.collectionCode = collectionCode;
	}
	@Column(length=128)
	public String getCollectionID() {
		return collectionID;
	}
	public void setCollectionID(String collectionID) {
		this.collectionID = collectionID;
	}
	@Column(length=64)
	@org.hibernate.annotations.Index(name="idx_dwc_basis_of_record")
	public String getBasisOfRecord() {
		return basisOfRecord;
	}
	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}
	@Lob
	public String getAccessConstraints() {
		return accessConstraints;
	}
	public void setAccessConstraints(String accessConstraints) {
		this.accessConstraints = accessConstraints;
	}
	@Lob
	public String getInformationWithheld() {
		return informationWithheld;
	}
	public void setInformationWithheld(String informationWithheld) {
		this.informationWithheld = informationWithheld;
	}
	public String getGeneralizations() {
		return generalizations;
	}
	public void setGeneralizations(String generalizations) {
		this.generalizations = generalizations;
	}
	public String getSampleDetails() {
		return sampleDetails;
	}
	public void setSampleDetails(String sampleDetails) {
		this.sampleDetails = sampleDetails;
	}
	@Lob
	public String getSampleRemarks() {
		return sampleRemarks;
	}
	public void setSampleRemarks(String sampleRemarks) {
		this.sampleRemarks = sampleRemarks;
	}
	@Column(length=64)
	@org.hibernate.annotations.Index(name="idx_dwc_catalog_number")
	public String getCatalogNumber() {
		return catalogNumber;
	}
	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}
	@Column(length=64)
	public String getCatalogNumberNumeric() {
		return catalogNumberNumeric;
	}
	public void setCatalogNumberNumeric(String catalogNumberNumeric) {
		this.catalogNumberNumeric = catalogNumberNumeric;
	}
	@Column(length=64)
	public String getIndividualID() {
		return individualID;
	}
	public void setIndividualID(String individualID) {
		this.individualID = individualID;
	}
	@Column(length=64)
	public String getIndividualCount() {
		return individualCount;
	}
	public void setIndividualCount(String individualCount) {
		this.individualCount = individualCount;
	}
	public String getCitation() {
		return citation;
	}
	public void setCitation(String citation) {
		this.citation = citation;
	}
	@Column(length=128)
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	@Column(length=128)
	public String getLifeStage() {
		return lifeStage;
	}
	public void setLifeStage(String lifeStage) {
		this.lifeStage = lifeStage;
	}
	public String getReproductiveCondition() {
		return reproductiveCondition;
	}
	public void setReproductiveCondition(String reproductiveCondition) {
		this.reproductiveCondition = reproductiveCondition;
	}
	public String getEstablishmentMeans() {
		return establishmentMeans;
	}
	public void setEstablishmentMeans(String establishmentMeans) {
		this.establishmentMeans = establishmentMeans;
	}
	@Lob
	public String getSampleAttributes() {
		return sampleAttributes;
	}
	public void setSampleAttributes(String sampleAttributes) {
		this.sampleAttributes = sampleAttributes;
	}
	public String getPreparations() {
		return preparations;
	}
	public void setPreparations(String preparations) {
		this.preparations = preparations;
	}
	@Column(length=128)
	public String getDisposition() {
		return disposition;
	}
	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}
	public String getOtherCatalogNumbers() {
		return otherCatalogNumbers;
	}
	public void setOtherCatalogNumbers(String otherCatalogNumbers) {
		this.otherCatalogNumbers = otherCatalogNumbers;
	}
	@Lob
	public String getAssociatedMedia() {
		return associatedMedia;
	}
	public void setAssociatedMedia(String associatedMedia) {
		this.associatedMedia = associatedMedia;
	}
	@Lob
	public String getAssociatedReferences() {
		return associatedReferences;
	}
	public void setAssociatedReferences(String associatedReferences) {
		this.associatedReferences = associatedReferences;
	}
	@Lob
	public String getAssociatedSamples() {
		return associatedSamples;
	}
	public void setAssociatedSamples(String associatedSamples) {
		this.associatedSamples = associatedSamples;
	}
	@Lob
	public String getAssociatedSequences() {
		return associatedSequences;
	}
	public void setAssociatedSequences(String associatedSequences) {
		this.associatedSequences = associatedSequences;
	}
	@Lob
	public String getAssociatedTaxa() {
		return associatedTaxa;
	}
	public void setAssociatedTaxa(String associatedTaxa) {
		this.associatedTaxa = associatedTaxa;
	}
	
	// SampleEvent
	public String getBehavior() {
		return behavior;
	}
	public void setBehavior(String behavior) {
		this.behavior = behavior;
	}
	@Column(length=64)
	public String getSamplingEventID() {
		return samplingEventID;
	}
	public void setSamplingEventID(String samplingEventID) {
		this.samplingEventID = samplingEventID;
	}
	public String getSamplingProtocol() {
		return samplingProtocol;
	}
	public void setSamplingProtocol(String samplingProtocol) {
		this.samplingProtocol = samplingProtocol;
	}
	@Column(length=128)
	public String getVerbatimCollectingDate() {
		return verbatimCollectingDate;
	}
	public void setVerbatimCollectingDate(String verbatimCollectingDate) {
		this.verbatimCollectingDate = verbatimCollectingDate;
	}
	@Column(length=64)
	public String getEarliestDateCollected() {
		return earliestDateCollected;
	}
	public void setEarliestDateCollected(String earliestDateCollected) {
		this.earliestDateCollected = earliestDateCollected;
	}
	@Column(length=64)
	public String getLatestDateCollected() {
		return latestDateCollected;
	}
	public void setLatestDateCollected(String latestDateCollected) {
		this.latestDateCollected = latestDateCollected;
	}
	@Column(length=16)
	public String getStartDayOfYear() {
		return startDayOfYear;
	}
	public void setStartDayOfYear(String startDayOfYear) {
		this.startDayOfYear = startDayOfYear;
	}
	@Column(length=16)
	public String getEndDayOfYear() {
		return endDayOfYear;
	}
	public void setEndDayOfYear(String endDayOfYear) {
		this.endDayOfYear = endDayOfYear;
	}
	@Column(length=32)
	public String getStartTimeOfDay() {
		return startTimeOfDay;
	}
	public void setStartTimeOfDay(String startTimeOfDay) {
		this.startTimeOfDay = startTimeOfDay;
	}
	@Column(length=32)
	public String getEndTimeOfDay() {
		return endTimeOfDay;
	}
	public void setEndTimeOfDay(String endTimeOfDay) {
		this.endTimeOfDay = endTimeOfDay;
	}
	@Column(length=16)
	public String getYearSampled() {
		return yearSampled;
	}
	public void setYearSampled(String yearSampled) {
		this.yearSampled = yearSampled;
	}
	@Column(length=16)
	public String getMonthOfYear() {
		return monthOfYear;
	}
	public void setMonthOfYear(String monthOfYear) {
		this.monthOfYear = monthOfYear;
	}
	@Column(length=16)
	public String getDayOfMonth() {
		return dayOfMonth;
	}
	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
	public String getHabitat() {
		return habitat;
	}
	public void setHabitat(String habitat) {
		this.habitat = habitat;
	}
	@Column(length=128)
	public String getCollector() {
		return collector;
	}
	public void setCollector(String collector) {
		this.collector = collector;
	}
	@Column(length=64)
	public String getCollectorNumber() {
		return collectorNumber;
	}
	public void setCollectorNumber(String collectorNumber) {
		this.collectorNumber = collectorNumber;
	}
	@Column(length=64)
	public String getFieldNumber() {
		return fieldNumber;
	}
	public void setFieldNumber(String fieldNumber) {
		this.fieldNumber = fieldNumber;
	}
	@Lob
	public String getFieldNotes() {
		return fieldNotes;
	}
	public void setFieldNotes(String fieldNotes) {
		this.fieldNotes = fieldNotes;
	}
	@Lob
	public String getSamplingEventAttributes() {
		return samplingEventAttributes;
	}
	public void setSamplingEventAttributes(String samplingEventAttributes) {
		this.samplingEventAttributes = samplingEventAttributes;
	}
	@Lob
	public String getSamplingEventRemarks() {
		return samplingEventRemarks;
	}
	public void setSamplingEventRemarks(String samplingEventRemarks) {
		this.samplingEventRemarks = samplingEventRemarks;
	}
	
	// SampleLocation => this.region
	@Column(length=128)
	public String getSamplingLocationID() {
		return samplingLocationID;
	}
	public void setSamplingLocationID(String samplingLocationID) {
		this.samplingLocationID = samplingLocationID;
	}
	@Column(length=128)
	public String getHigherGeographyID() {
		return higherGeographyID;
	}
	public void setHigherGeographyID(String higherGeographyID) {
		this.higherGeographyID = higherGeographyID;
	}
	public String getHigherGeography() {
		return higherGeography;
	}
	public void setHigherGeography(String higherGeography) {
		this.higherGeography = higherGeography;
	}
	@Column(length=128)
	public String getContinent() {
		return continent;
	}
	public void setContinent(String continent) {
		this.continent = continent;
	}
	@Column(length=128)
	public String getWaterbody() {
		return waterbody;
	}
	public void setWaterbody(String waterbody) {
		this.waterbody = waterbody;
	}
	@Column(length=128)
	public String getIslandGroup() {
		return islandGroup;
	}
	public void setIslandGroup(String islandGroup) {
		this.islandGroup = islandGroup;
	}
	public String getIsland() {
		return island;
	}
	public void setIsland(String island) {
		this.island = island;
	}
	@Column(length=128)
	@org.hibernate.annotations.Index(name="idx_dwc_country")
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	@Column(length=32)
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getStateProvince() {
		return stateProvince;
	}
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	@Lob
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	@Lob
	public String getVerbatimLocality() {
		return verbatimLocality;
	}
	public void setVerbatimLocality(String verbatimLocality) {
		this.verbatimLocality = verbatimLocality;
	}
	@Column(length=128)
	public String getVerbatimElevation() {
		return verbatimElevation;
	}
	public void setVerbatimElevation(String verbatimElevation) {
		this.verbatimElevation = verbatimElevation;
	}
	@Column(length=32)
	public String getMinimumElevationInMeters() {
		return minimumElevationInMeters;
	}
	public void setMinimumElevationInMeters(String minimumElevationInMeters) {
		this.minimumElevationInMeters = minimumElevationInMeters;
	}
	@Column(length=32)
	public String getMaximumElevationInMeters() {
		return maximumElevationInMeters;
	}
	public void setMaximumElevationInMeters(String maximumElevationInMeters) {
		this.maximumElevationInMeters = maximumElevationInMeters;
	}
	@Column(length=128)
	public String getVerbatimDepth() {
		return verbatimDepth;
	}
	public void setVerbatimDepth(String verbatimDepth) {
		this.verbatimDepth = verbatimDepth;
	}
	@Column(length=32)
	public String getMinimumDepthInMeters() {
		return minimumDepthInMeters;
	}
	public void setMinimumDepthInMeters(String minimumDepthInMeters) {
		this.minimumDepthInMeters = minimumDepthInMeters;
	}
	@Column(length=32)
	public String getMaximumDepthInMeters() {
		return maximumDepthInMeters;
	}
	public void setMaximumDepthInMeters(String maximumDepthInMeters) {
		this.maximumDepthInMeters = maximumDepthInMeters;
	}
	@Column(length=32)
	public String getDistanceAboveSurfaceInMetersMinimum() {
		return distanceAboveSurfaceInMetersMinimum;
	}
	public void setDistanceAboveSurfaceInMetersMinimum(
			String distanceAboveSurfaceInMetersMinimum) {
		this.distanceAboveSurfaceInMetersMinimum = distanceAboveSurfaceInMetersMinimum;
	}
	@Column(length=32)
	public String getDistanceAboveSurfaceInMetersMaximum() {
		return distanceAboveSurfaceInMetersMaximum;
	}
	public void setDistanceAboveSurfaceInMetersMaximum(
			String distanceAboveSurfaceInMetersMaximum) {
		this.distanceAboveSurfaceInMetersMaximum = distanceAboveSurfaceInMetersMaximum;
	}
	@Column(length=32)
	public String getDecimalLatitude() {
		return decimalLatitude;
	}
	public void setDecimalLatitude(String decimalLatitude) {
		this.decimalLatitude = decimalLatitude;
	}
	@Column(length=32)
	public String getDecimalLongitude() {
		return decimalLongitude;
	}
	public void setDecimalLongitude(String decimalLongitude) {
		this.decimalLongitude = decimalLongitude;
	}
	@Column(length=64)
	public String getGeodeticDatum() {
		return geodeticDatum;
	}
	public void setGeodeticDatum(String geodeticDatum) {
		this.geodeticDatum = geodeticDatum;
	}
	@Column(length=32)
	public String getCoordinateUncertaintyInMeters() {
		return coordinateUncertaintyInMeters;
	}
	public void setCoordinateUncertaintyInMeters(
			String coordinateUncertaintyInMeters) {
		this.coordinateUncertaintyInMeters = coordinateUncertaintyInMeters;
	}
	@Column(length=64)
	public String getCoordinatePrecision() {
		return coordinatePrecision;
	}
	public void setCoordinatePrecision(String coordinatePrecision) {
		this.coordinatePrecision = coordinatePrecision;
	}
	@Column(length=64)
	public String getPointRadiusSpatialFit() {
		return pointRadiusSpatialFit;
	}
	public void setPointRadiusSpatialFit(String pointRadiusSpatialFit) {
		this.pointRadiusSpatialFit = pointRadiusSpatialFit;
	}
	public String getVerbatimCoordinates() {
		return verbatimCoordinates;
	}
	public void setVerbatimCoordinates(String verbatimCoordinates) {
		this.verbatimCoordinates = verbatimCoordinates;
	}
	@Column(length=128)
	public String getVerbatimLatitude() {
		return verbatimLatitude;
	}
	public void setVerbatimLatitude(String verbatimLatitude) {
		this.verbatimLatitude = verbatimLatitude;
	}
	@Column(length=128)
	public String getVerbatimLongitude() {
		return verbatimLongitude;
	}
	public void setVerbatimLongitude(String verbatimLongitude) {
		this.verbatimLongitude = verbatimLongitude;
	}
	@Column(length=128)
	public String getGeoreferencedBy() {
		return georeferencedBy;
	}
	public void setGeoreferencedBy(String georeferencedBy) {
		this.georeferencedBy = georeferencedBy;
	}
	@Column(length=128)
	public String getGeoreferenceProtocol() {
		return georeferenceProtocol;
	}
	public void setGeoreferenceProtocol(String georeferenceProtocol) {
		this.georeferenceProtocol = georeferenceProtocol;
	}
	@Column(length=128)
	public String getVerbatimCoordinateSystem() {
		return verbatimCoordinateSystem;
	}
	public void setVerbatimCoordinateSystem(String verbatimCoordinateSystem) {
		this.verbatimCoordinateSystem = verbatimCoordinateSystem;
	}
	public String getGeoreferenceSources() {
		return georeferenceSources;
	}
	public void setGeoreferenceSources(String georeferenceSources) {
		this.georeferenceSources = georeferenceSources;
	}
	@Column(length=128)
	public String getGeoreferenceVerificationStatus() {
		return georeferenceVerificationStatus;
	}
	public void setGeoreferenceVerificationStatus(
			String georeferenceVerificationStatus) {
		this.georeferenceVerificationStatus = georeferenceVerificationStatus;
	}
	@Lob
	public String getGeoreferenceRemarks() {
		return georeferenceRemarks;
	}
	public void setGeoreferenceRemarks(String georeferenceRemarks) {
		this.georeferenceRemarks = georeferenceRemarks;
	}
	public String getFootprintWKT() {
		return footprintWKT;
	}
	public void setFootprintWKT(String footprintWKT) {
		this.footprintWKT = footprintWKT;
	}
	@Column(length=128)
	public String getFootprintSpatialFit() {
		return footprintSpatialFit;
	}
	public void setFootprintSpatialFit(String footprintSpatialFit) {
		this.footprintSpatialFit = footprintSpatialFit;
	}
	@Lob
	public String getSamplingLocationRemarks() {
		return samplingLocationRemarks;
	}
	public void setSamplingLocationRemarks(String samplingLocationRemarks) {
		this.samplingLocationRemarks = samplingLocationRemarks;
	}
	// Identification
	@Column(length=128)
	public String getIdentificationID() {
		return identificationID;
	}
	public void setIdentificationID(String identificationID) {
		this.identificationID = identificationID;
	}
	@Column(length=128)
	public String getIdentifiedBy() {
		return identifiedBy;
	}
	public void setIdentifiedBy(String identifiedBy) {
		this.identifiedBy = identifiedBy;
	}
	@Column(length=64)
	public String getDateIdentified() {
		return dateIdentified;
	}
	public void setDateIdentified(String dateIdentified) {
		this.dateIdentified = dateIdentified;
	}
	@Lob
	public String getIdentificationReferences() {
		return identificationReferences;
	}
	public void setIdentificationReferences(String identificationReferences) {
		this.identificationReferences = identificationReferences;
	}
	@Lob
	public String getIdentificationRemarks() {
		return identificationRemarks;
	}
	public void setIdentificationRemarks(String identificationRemarks) {
		this.identificationRemarks = identificationRemarks;
	}
	@Lob
	public String getPreviousIdentifications() {
		return previousIdentifications;
	}
	public void setPreviousIdentifications(String previousIdentifications) {
		this.previousIdentifications = previousIdentifications;
	}
	@Column(length=64)
	public String getIdentificationQualifier() {
		return identificationQualifier;
	}
	public void setIdentificationQualifier(String identificationQualifier) {
		this.identificationQualifier = identificationQualifier;
	}
	@Column(length=128)
	public String getTypeStatus() {
		return typeStatus;
	}
	public void setTypeStatus(String typeStatus) {
		this.typeStatus = typeStatus;
	}
	// Taxon => this.taxon
	@Column(length=128)
	public String getTaxonID() {
		return taxonID;
	}
	public void setTaxonID(String taxonID) {
		this.taxonID = taxonID;
	}
	@org.hibernate.annotations.Index(name="idx_dwc_scientific_name")
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	@Column(length=128)
	public String getBinomial() {
		return binomial;
	}
	public void setBinomial(String binomial) {
		this.binomial = binomial;
	}
	@Column(length=128)
	public String getHigherTaxonID() {
		return higherTaxonID;
	}
	public void setHigherTaxonID(String higherTaxonID) {
		this.higherTaxonID = higherTaxonID;
	}
	public String getHigherTaxon() {
		return higherTaxon;
	}
	public void setHigherTaxon(String higherTaxon) {
		this.higherTaxon = higherTaxon;
	}
	@Column(length=128)
	public String getKingdom() {
		return kingdom;
	}
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}
	@Column(length=128)
	public String getPhylum() {
		return phylum;
	}
	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}
	@Column(length=128)
	public String getClasss() {
		return classs;
	}
	public void setClasss(String classs) {
		this.classs = classs;
	}
	@Column(length=128, name="orderrr")
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	@Column(length=128)
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	@Column(length=128)
	public String getGenus() {
		return genus;
	}
	public void setGenus(String genus) {
		this.genus = genus;
	}
	@Column(length=128)
	public String getSubgenus() {
		return subgenus;
	}
	public void setSubgenus(String subgenus) {
		this.subgenus = subgenus;
	}
	@Column(length=128)
	public String getSpecificEpithet() {
		return specificEpithet;
	}
	public void setSpecificEpithet(String specificEpithet) {
		this.specificEpithet = specificEpithet;
	}
	@Column(length=128)
	public String getTaxonRank() {
		return taxonRank;
	}
	public void setTaxonRank(String taxonRank) {
		this.taxonRank = taxonRank;
	}
	@Column(length=128)
	public String getInfraspecificEpithet() {
		return infraspecificEpithet;
	}
	public void setInfraspecificEpithet(String infraspecificEpithet) {
		this.infraspecificEpithet = infraspecificEpithet;
	}
	public String getScientificNameAuthorship() {
		return scientificNameAuthorship;
	}
	public void setScientificNameAuthorship(String scientificNameAuthorship) {
		this.scientificNameAuthorship = scientificNameAuthorship;
	}
	@Column(length=64)
	public String getNomenclaturalCode() {
		return nomenclaturalCode;
	}
	public void setNomenclaturalCode(String nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}
	public String getTaxonAccordingTo() {
		return taxonAccordingTo;
	}
	public void setTaxonAccordingTo(String taxonAccordingTo) {
		this.taxonAccordingTo = taxonAccordingTo;
	}
	public String getNamePublishedIn() {
		return namePublishedIn;
	}
	public void setNamePublishedIn(String namePublishedIn) {
		this.namePublishedIn = namePublishedIn;
	}
	@Column(length=128)
	public String getTaxonomicStatus() {
		return taxonomicStatus;
	}
	public void setTaxonomicStatus(String taxonomicStatus) {
		this.taxonomicStatus = taxonomicStatus;
	}
	@Column(length=128)
	public String getNomenclaturalStatus() {
		return nomenclaturalStatus;
	}
	public void setNomenclaturalStatus(String nomenclaturalStatus) {
		this.nomenclaturalStatus = nomenclaturalStatus;
	}
	@Column(length=128)
	public String getAcceptedTaxonID() {
		return acceptedTaxonID;
	}
	public void setAcceptedTaxonID(String acceptedTaxonID) {
		this.acceptedTaxonID = acceptedTaxonID;
	}
	public String getAcceptedTaxon() {
		return acceptedTaxon;
	}
	public void setAcceptedTaxon(String acceptedTaxon) {
		this.acceptedTaxon = acceptedTaxon;
	}
	@Column(length=128)
	public String getBasionymID() {
		return basionymID;
	}
	public void setBasionymID(String basionymID) {
		this.basionymID = basionymID;
	}
	public String getBasionym() {
		return basionym;
	}
	public void setBasionym(String basionym) {
		this.basionym = basionym;
	}
}
