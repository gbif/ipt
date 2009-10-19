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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
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
 * The core class for taxon occurrence records with normalised properties used
 * by the webapp. The generated property values can be derived from different
 * extensions like DarwinCore or ABCD but the ones here are used for creating
 * most of the webapp functionality
 * 
 */
@Entity
@Table(name = "Darwin_Core", uniqueConstraints = { @UniqueConstraint(columnNames = {
    "sourceId", "resource_fk" }) })
@org.hibernate.annotations.Table(appliesTo = "Darwin_Core", indexes = {
    @org.hibernate.annotations.Index(name = "latitude", columnNames = { "lat" }),
    @org.hibernate.annotations.Index(name = "longitude", columnNames = { "lon" }) })
/**
 * TODO: Documentation.
 * 
 */
public class DarwinCore implements CoreRecord, Comparable<DarwinCore> {
  public static final Set<String> INTEGER_PROPERTIES = new HashSet<String>(
      Arrays.asList("Year", "Month", "Day", "StartDayOfYear", "EndDayOfYear",
          "IndividualCount"));
  public static final Set<String> DOUBLE_PROPERTIES = new HashSet<String>(
      Arrays.asList("DecimalLatitude", "DecimalLongitude",
          "CoordinateUncertaintyInMeters", "CoordinatePrecision",
          "MaximumDepthInMeters", "MaximumElevationInMeters",
          "MinimumDepthInMeters", "MinimumElevationInMeters",
          "MaximumDistanceAboveSurfaceInMeters",
          "MinimumDistanceAboveSurfaceInMeters", "pointRadiusSpatialFit",
          "footprintSpatialFit"));
  public static final Set<String> DATE_PROPERTIES = new HashSet<String>(Arrays
      .asList("Modified", "DateIdentified", "EventDate"));
  public static final Set<String> TOKEN_PROPERTIES = new HashSet<String>(Arrays
      .asList("kingdom", "phylum", "class", "order", "family", "genus",
          "subgenus"));

  public static DarwinCore newInstance(DataResource resource) {
    DarwinCore dwc = new DarwinCore();
    dwc.resource = resource;
    return dwc;
  }

  public static DarwinCore newMock(DataResource resource) {
    Random rnd = new Random();
    DarwinCore dwc = DarwinCore.newInstance(resource);
    // populate instance
    // set unique sourceId to ensure we can save this record. Otherwise we might
    // get a non unique constraint exception...
    String guid = UUID.randomUUID().toString();
    String sourceId = rnd.nextInt(99999999) + "";
    dwc.setSourceId(sourceId);
    dwc.setGuid(guid);
    dwc.setCatalogNumber("rbgk-" + sourceId + "-x");
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

  // for core record
  private Long id;
  private String sourceId;
  // TODO: It looks under Occurrence as if the guid is supposed to be the
  // occurrenceID, but what if the record is for a Taxon instead?
  @NotNull
  private String guid;
  private String link;
  private boolean isDeleted;

  private Date modified;
  @NotNull
  private DataResource resource;

  // DarwinCore derived fields. calculated from raw Strings
  private Point location = new Point();
  private Taxon taxon;
  private Region region;
  private Date collected;

  private Double elevation;
  private Double depth;

  // TODO: Determine if these other typed terms need to be have typed variables
  private int yearint, monthint, dayint, startday, endday, indivcount;
  private Double coorduncertainty, coordprecision;
  private Double minelev, maxelev, mindepth, maxdepth, minabove, maxabove;
  private Double pointradiusfit, footprintfit;
  private Date identified;

  // 
  // All Simple DarwinCore terms
  // 
  // Dublin Core terms - all record-level

  // TODO: Should the type have an @NotNull annotation?
  private String type;
  // dcterms:modified = this.modified
  private String language;
  private String rights;
  private String rightsHolder;
  private String accessRights; // was private String accessConstraints;
  private String bibliographicCitation; // was private String citation;

  // Other record-level terms
  private String institutionID;
  private String collectionID;
  private String datasetID;
  private String institutionCode;
  private String collectionCode;
  private String datasetName;
  private String ownerInstitutionCode;
  private String basisOfRecord;
  private String informationWithheld;
  private String dataGeneralizations;
  private String dynamicProperties;

  // Occurrence
  // TODO: It looks as if the guid is supposed to be the occurrenceID, but
  // what if the record is for a Taxon instead?
  // occurrenceID = this.guid
  private String catalogNumber;
  private String occurrenceDetails;
  private String occurrenceRemarks;

  // deprecated private String catalogNumberNumeric;
  private String recordNumber; // was private String collectorNumber;
  private String recordedBy; // was private String collector;
  private String individualID;
  private String individualCount;
  private String sex;
  private String lifeStage;
  private String reproductiveCondition;
  private String behavior;
  private String establishmentMeans;
  private String occurrenceStatus;
  private String preparations;
  private String disposition;
  private String otherCatalogNumbers;
  private String previousIdentifications;
  private String associatedMedia;
  private String associatedReferences;
  private String associatedOccurrences; // was private String associatedSamples;
  private String associatedSequences;
  private String associatedTaxa;
  // deprecated private String sampleAttributes;

  // Event
  private String eventID; // was private String samplingEventID;
  private String samplingProtocol;
  private String samplingEffort;
  // deprecated private String earliestDateCollected;
  // deprecated private String latestDateCollected;
  // deprecated private String startTimeOfDay;
  // deprecated private String endTimeOfDay;
  private String eventDate;
  private String eventTime;
  private String startDayOfYear;
  private String endDayOfYear;
  private String year; // was private String yearSampled;
  private String month; // was private String monthOfYear;
  private String day; // was private String dayOfMonth;
  private String verbatimEventDate; // was private String
  // verbatimCollectingDate;
  private String habitat;
  private String fieldNumber;
  private String fieldNotes;
  private String eventRemarks; // was private String samplingEventRemarks;
  // deprecated private String samplingEventAttributes;

  // Location => this.region
  private String locationID; // was private String samplingLocationID;
  private String higherGeographyID;
  private String higherGeography;
  private String continent;
  private String waterBody;
  private String islandGroup;
  private String island;
  private String country;
  private String countryCode;
  private String stateProvince;
  private String county;
  private String municipality;
  private String locality;
  private String verbatimLocality;
  private String verbatimElevation;
  private String minimumElevationInMeters;
  private String maximumElevationInMeters;
  private String verbatimDepth;
  private String minimumDepthInMeters;
  private String maximumDepthInMeters;
  private String minimumDistanceAboveSurfaceInMeters;
  // was private String distanceAboveSurfaceInMetersMinimum;
  private String maximumDistanceAboveSurfaceInMeters;
  // was private String distanceAboveSurfaceInMetersMaximum;
  private String locationAccordingTo;
  private String locationRemarks; // was private String samplingLocationRemarks;
  private String verbatimCoordinates;
  private String verbatimLatitude;
  private String verbatimLongitude;
  private String verbatimCoordinateSystem;
  private String verbatimSRS;
  private String decimalLatitude;
  private String decimalLongitude;
  private String geodeticDatum;
  private String coordinateUncertaintyInMeters;
  private String coordinatePrecision;
  private String pointRadiusSpatialFit;
  private String footprintWKT;
  private String footprintSRS;
  private String footprintSpatialFit;
  private String georeferencedBy;
  private String georeferenceProtocol;
  private String georeferenceSources;
  private String georeferenceVerificationStatus;
  private String georeferenceRemarks;

  // GeologicalContext
  private String geologicalContextID;
  private String earliestEonOrLowestEonothem;
  private String latestEonOrHighestEonothem;
  private String earliestEraOrLowestErathem;
  private String latestEraOrHighestErathem;
  private String earliestPeriodOrLowestSystem;
  private String latestPeriodOrHighestSystem;
  private String earliestEpochOrLowestSeries;
  private String latestEpochOrHighestSeries;
  private String earliestAgeOrLowestStage;
  private String latestAgeOrHighestStage;
  private String lowestBiostratigraphicZone;
  private String highestBiostratigraphicZone;
  private String lithostratigraphicTerms;
  private String group;
  private String formation;
  private String member;
  private String bed;

  // Identification
  private String identificationID;
  private String identifiedBy;
  private String dateIdentified;
  private String identificationReferences;
  private String identificationRemarks;
  private String identificationQualifier;
  private String typeStatus;

  // Taxon => this.taxon
  private String taxonID;
  private String scientificNameID;
  private String acceptedNameUsageID; // was private String acceptedTaxonID;
  private String parentNameUsageID; // was private String higherTaxonID;
  private String originalNameUsageID; // was private String basionymID;
  private String nameAccordingToID;
  private String namePublishedInID;
  private String taxonConceptID;
  private String scientificName;
  // deprecated private String binomial;
  private String acceptedNameUsage; // was private String acceptedTaxon;
  private String parentNameUsage;
  private String originalNameUsage; // was private String basionym;
  private String nameAccordingTo; // was private String taxonAccordingTo;
  private String namePublishedIn;
  private String higherClassification; // was private String higherTaxon;
  private String kingdom;
  private String phylum;
  private String classs;
  private String order;
  private String family;
  private String genus;
  private String subgenus;
  private String specificEpithet;
  private String infraspecificEpithet;
  private String taxonRank;
  private String verbatimTaxonRank;
  private String scientificNameAuthorship;
  private String vernacularName;
  private String nomenclaturalCode;
  private String taxonomicStatus;
  private String nomenclaturalStatus;
  private String taxonRemarks;

  // ResourceRelationship terms omitted - not part of Simple Darwin Core
  // MeasurementOrFact terms omitted - not part of Simple Darwin Core

  public int compareTo(DarwinCore myClass) {
    return new CompareToBuilder().append(this.institutionCode,
        myClass.institutionCode).append(this.collectionCode,
        myClass.collectionCode).append(this.catalogNumber,
        myClass.catalogNumber).append(this.getScientificName(),
        myClass.getScientificName()).append(this.sourceId, myClass.sourceId)
        .append(this.guid, myClass.guid).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
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

  // deprecated in favor of acceptedNameUsage
  // public String getAcceptedTaxon() {
  // return acceptedTaxon;
  // }
  public String getAcceptedNameUsage() {
    return acceptedNameUsage;
  }

  // deprecated in favor of acceptedNameUsageID
  // @Column(length = 128)
  // public String getAcceptedTaxonID() {
  // return acceptedTaxonID;
  // }
  @Column(length = 128)
  public String getAcceptedNameUsageID() {
    return acceptedNameUsageID;
  }

  // deprecated in favor of accessRights
  // @Lob
  // public String getAccessConstraints() {
  // return accessConstraints;
  // }
  @Lob
  public String getAccessRights() {
    return accessRights;
  }

  @Lob
  public String getAssociatedMedia() {
    return associatedMedia;
  }

  // deprecated in favor of associatedOccurrences
  // @Lob
  // public String getAssociatedSamples() {
  // return associatedSamples;
  // }
  @Lob
  public String getAssociatedOccurrences() {
    return associatedOccurrences;
  }

  @Lob
  public String getAssociatedReferences() {
    return associatedReferences;
  }

  @Lob
  public String getAssociatedSequences() {
    return associatedSequences;
  }

  @Lob
  public String getAssociatedTaxa() {
    return associatedTaxa;
  }

  @Column(length = 64)
  @org.hibernate.annotations.Index(name = "idx_dwc_basis_of_record")
  public String getBasisOfRecord() {
    return basisOfRecord;
  }

  public String getBed() {
    return bed;
  }

  public String getBehavior() {
    return behavior;
  }

  // deprecated in favor of bibliographicCitation
  // public String getCitation() {
  // return citation;
  // }
  public String getBibliographicCitation() {
    return bibliographicCitation;
  }

  @Column(length = 64)
  @org.hibernate.annotations.Index(name = "idx_dwc_catalog_number")
  public String getCatalogNumber() {
    return catalogNumber;
  }

  @Column(length = 128)
  public String getClasss() {
    return classs;
  }

  @org.hibernate.annotations.Index(name = "dwc_date_collected")
  public Date getCollected() {
    return collected;
  }

  @Column(length = 128)
  @org.hibernate.annotations.Index(name = "idx_dwc_collection_code")
  public String getCollectionCode() {
    return collectionCode;
  }

  @Column(length = 128)
  public String getCollectionID() {
    return collectionID;
  }

  @Column(length = 128)
  public String getContinent() {
    return continent;
  }

  @Column(length = 64)
  public String getCoordinatePrecision() {
    return coordinatePrecision;
  }

  @Column(length = 32)
  public String getCoordinateUncertaintyInMeters() {
    return coordinateUncertaintyInMeters;
  }

  @Transient
  public Long getCoreId() {
    return id;
  }

  @Column(length = 128)
  @org.hibernate.annotations.Index(name = "idx_dwc_country")
  public String getCountry() {
    return country;
  }

  @Column(length = 32)
  public String getCountryCode() {
    return countryCode;
  }

  public String getCounty() {
    return county;
  }

  // deprecated in favor of dataGeneralizations
  // public String getGeneralizations() {
  // return generalizations;
  // }
  public String getDataGeneralizations() {
    return dataGeneralizations;
  }

  @Column(length = 128)
  public String getDatasetID() {
    return datasetID;
  }

  // TODO: Should datasetName have an index, as collectionCode does?
  @Column(length = 128)
  // @org.hibernate.annotations.Index(name = "idx_dwc_dataset_name")
  public String getDatasetName() {
    return datasetName;
  }

  @Column(length = 64)
  public String getDateIdentified() {
    return dateIdentified;
  }

  // deprecated in favor of day
  // @Column(length = 16)
  // public String getDayOfMonth() {
  // return dayOfMonth;
  // }
  @Column(length = 16)
  public String getDay() {
    return day;
  }

  @Column(length = 32)
  public String getDecimalLatitude() {
    return decimalLatitude;
  }

  @Column(length = 32)
  public String getDecimalLongitude() {
    return decimalLongitude;
  }

  public Double getDepth() {
    return depth;
  }

  // deprecated
  // @Column(length = 128)
  // public String getBinomial() {
  // return binomial;
  // }

  @Column(length = 128)
  public String getDisposition() {
    return disposition;
  }

  // deprecated
  // @Column(length = 64)
  // public String getCatalogNumberNumeric() {
  // return catalogNumberNumeric;
  // }

  public String getDynamicProperties() {
    return dynamicProperties;
  }

  public String getEarliestAgeOrLowestStage() {
    return earliestAgeOrLowestStage;
  }

  public String getEarliestEonOrLowestEonothem() {
    return earliestEonOrLowestEonothem;
  }

  public String getEarliestEpochOrLowestSeries() {
    return earliestEpochOrLowestSeries;
  }

  public String getEarliestEraOrLowestErathem() {
    return earliestEraOrLowestErathem;
  }

  public String getEarliestPeriodOrLowestSystem() {
    return earliestPeriodOrLowestSystem;
  }

  public Double getElevation() {
    return elevation;
  }

  @Column(length = 16)
  public String getEndDayOfYear() {
    return endDayOfYear;
  }

  public String getEstablishmentMeans() {
    return establishmentMeans;
  }

  // deprecated in favor of eventDate
  // @Column(length = 64)
  // public String getEarliestDateCollected() {
  // return earliestDateCollected;
  // }
  // @Column(length = 64)
  // public String getLatestDateCollected() {
  // return latestDateCollected;
  // }
  @Column(length = 64)
  public String getEventDate() {
    return eventDate;
  }

  // deprecated in favor of eventID
  // @Column(length = 64)
  // public String getSamplingEventID() {
  // return samplingEventID;
  // }
  @Column(length = 64)
  public String getEventID() {
    return eventID;
  }

  // deprecated in favor of eventRemarks
  // @Lob
  // public String getSamplingEventRemarks() {
  // return samplingEventRemarks;
  // }
  @Lob
  public String getEventRemarks() {
    return eventRemarks;
  }

  // deprecated in favor of eventTime
  // @Column(length = 32)
  // public String getEndTimeOfDay() {
  // return endTimeOfDay;
  // }
  // @Column(length = 32)
  // public String getStartTimeOfDay() {
  // return startTimeOfDay;
  // }
  @Column(length = 32)
  public String getEventTime() {
    return eventTime;
  }

  @Column(length = 128)
  public String getFamily() {
    return family;
  }

  @Lob
  public String getFieldNotes() {
    return fieldNotes;
  }

  @Column(length = 64)
  public String getFieldNumber() {
    return fieldNumber;
  }

  @Column(length = 128)
  public String getFootprintSpatialFit() {
    return footprintSpatialFit;
  }

  @Column(length = 128)
  public String getFootprintSRS() {
    return footprintSRS;
  }

  public String getFootprintWKT() {
    return footprintWKT;
  }

  public String getFormation() {
    return formation;
  }

  @Column(length = 128)
  public String getGenus() {
    return genus;
  }

  @Column(length = 128)
  public String getGeodeticDatum() {
    return geodeticDatum;
  }

  @Transient
  public String getGeographyPath() {
    return getGeographyPath(RegionType.Locality);
  }

  @Transient
  public String getGeographyPath(RegionType regionType) {
    // String path = StringUtils.trimToEmpty(getKingdom()); // jrw - assumed bug
    String path = StringUtils.trimToEmpty(getContinent());
    if (regionType.compareTo(RegionType.Continent) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getContinent());
    }
    if (regionType.compareTo(RegionType.Waterbody) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getWaterBody());
    }
    if (regionType.compareTo(RegionType.IslandGroup) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getIslandGroup());
    }
    if (regionType.compareTo(RegionType.Island) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getIsland());
    }
    if (regionType.compareTo(RegionType.Country) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getCountry());
    }
    if (regionType.compareTo(RegionType.State) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getStateProvince());
    }
    if (regionType.compareTo(RegionType.County) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getCounty());
    }
    if (regionType.compareTo(RegionType.Locality) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getLocality());
    }
    return path;
  }

  public String getGeologicalContextID() {
    return geologicalContextID;
  }

  @Column(length = 128)
  public String getGeoreferencedBy() {
    return georeferencedBy;
  }

  @Column(length = 128)
  public String getGeoreferenceProtocol() {
    return georeferenceProtocol;
  }

  @Lob
  public String getGeoreferenceRemarks() {
    return georeferenceRemarks;
  }

  public String getGeoreferenceSources() {
    return georeferenceSources;
  }

  @Column(length = 128)
  public String getGeoreferenceVerificationStatus() {
    return georeferenceVerificationStatus;
  }

  public String getGroup() {
    return group;
  }

  @Column(length = 128, unique = true)
  @org.hibernate.annotations.Index(name = "guid")
  public String getGuid() {
    return guid;
  }

  public String getHabitat() {
    return habitat;
  }

  // deprecated in favor of higherClassification
  // public String getHigherTaxon() {
  // return higherTaxon;
  // }
  public String getHigherClassification() {
    return higherClassification;
  }

  public String getHigherGeography() {
    return higherGeography;
  }

  @Column(length = 128)
  public String getHigherGeographyID() {
    return higherGeographyID;
  }

  @Transient
  public String getHigherGeographyName(RegionType regionType) {
    return StringUtils.trimToNull(getPropertyValue(StringUtils
        .capitalise(regionType.columnName)));
  }

  @Transient
  public String getHigherTaxonName(Rank rank) {
    if (rank == Rank.Species) {
      if (getSpecificEpithet() != null) {
        return StringUtils.trimToNull(String.format("%s %s", getGenus(),
            getSpecificEpithet()));
      } else {
        return null;
      }
    } else if (rank == Rank.InfraSpecies) {
      if (getInfraspecificEpithet() != null) {
        return StringUtils.trimToNull(String.format("%s %s %s %s", getGenus(),
            getSpecificEpithet(), getTaxonRank(), getInfraspecificEpithet()));
      } else {
        return null;
      }
    } else if (rank == Rank.TerminalTaxon) {
      return getScientificName();
    } else {
      return StringUtils.trimToNull(getPropertyValue(StringUtils
          .capitalise(rank.columnName)));
    }
  }

  public String getHighestBiostratigraphicZone() {
    return highestBiostratigraphicZone;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @Column(length = 128)
  public String getIdentificationID() {
    return identificationID;
  }

  @Column(length = 64)
  public String getIdentificationQualifier() {
    return identificationQualifier;
  }

  @Lob
  public String getIdentificationReferences() {
    return identificationReferences;
  }

  @Lob
  public String getIdentificationRemarks() {
    return identificationRemarks;
  }

  @Column(length = 128)
  public String getIdentifiedBy() {
    return identifiedBy;
  }

  @Column(length = 64)
  public String getIndividualCount() {
    return individualCount;
  }

  @Column(length = 64)
  public String getIndividualID() {
    return individualID;
  }

  @Lob
  public String getInformationWithheld() {
    return informationWithheld;
  }

  @Column(length = 128)
  public String getInfraspecificEpithet() {
    return infraspecificEpithet;
  }

  @Column(length = 128)
  @org.hibernate.annotations.Index(name = "idx_dwc_institution_code")
  public String getInstitutionCode() {
    return institutionCode;
  }

  @Column(length = 128)
  public String getInstitutionID() {
    return institutionID;
  }

  public String getIsland() {
    return island;
  }

  @Column(length = 128)
  public String getIslandGroup() {
    return islandGroup;
  }

  @Column(length = 128)
  public String getKingdom() {
    return kingdom;
  }

  @Column(length = 64)
  public String getLanguage() {
    return language;
  }

  public String getLatestAgeOrHighestStage() {
    return latestAgeOrHighestStage;
  }

  public String getLatestEonOrHighestEonothem() {
    return latestEonOrHighestEonothem;
  }

  public String getLatestEpochOrHighestSeries() {
    return latestEpochOrHighestSeries;
  }

  public String getLatestEraOrHighestErathem() {
    return latestEraOrHighestErathem;
  }

  public String getLatestPeriodOrHighestSystem() {
    return latestPeriodOrHighestSystem;
  }

  @Transient
  public Double getLatitude() {
    return location.getLatitude();
  }

  @Column(length = 128)
  public String getLifeStage() {
    return lifeStage;
  }

  public String getLink() {
    return link;
  }

  public String getLithostratigraphicTerms() {
    return lithostratigraphicTerms;
  }

  @Lob
  public String getLocality() {
    return locality;
  }

  @AttributeOverrides( {
      @AttributeOverride(name = "latitude", column = @Column(name = "lat")),
      @AttributeOverride(name = "longitude", column = @Column(name = "lon")) })
  public Point getLocation() {
    // to prevent NPE create new empty point in case it doesn't exist yet
    if (location == null) {
      location = new Point();
    }
    return location;
  }

  public String getLocationAccordingTo() {
    return locationAccordingTo;
  }

  // deprecated in favor of locationID
  // @Column(length = 128)
  // public String getSamplingLocationID() {
  // return samplingLocationID;
  // }
  @Column(length = 128)
  public String getLocationID() {
    return locationID;
  }

  // deprecated in favor of locationRemarks
  // @Lob
  // public String getSamplingLocationRemarks() {
  // return samplingLocationRemarks;
  // }
  @Lob
  public String getLocationRemarks() {
    return locationRemarks;
  }

  @Transient
  public Double getLongitude() {
    return location.getLongitude();
  }

  public String getLowestBiostratigraphicZone() {
    return lowestBiostratigraphicZone;
  }

  @Column(length = 32)
  public String getMaximumDepthInMeters() {
    return maximumDepthInMeters;
  }

  // deprecated in favor of maximumDistanceAboveSurfaceInMeters
  // @Column(length = 32)
  // public String getDistanceAboveSurfaceInMetersMaximum() {
  // return distanceAboveSurfaceInMetersMaximum;
  // }
  @Column(length = 32)
  public String getMaximumDistanceAboveSurfaceInMeters() {
    return maximumDistanceAboveSurfaceInMeters;
  }

  @Column(length = 32)
  public String getMaximumElevationInMeters() {
    return maximumElevationInMeters;
  }

  public String getMember() {
    return member;
  }

  @Column(length = 32)
  public String getMinimumDepthInMeters() {
    return minimumDepthInMeters;
  }

  // deprecated in favor of minimumDistanceAboveSurfaceInMeters
  // @Column(length = 32)
  // public String getDistanceAboveSurfaceInMetersMinimum() {
  // return distanceAboveSurfaceInMetersMinimum;
  // }
  @Column(length = 32)
  public String getMinimumDistanceAboveSurfaceInMeters() {
    return minimumDistanceAboveSurfaceInMeters;
  }

  @Column(length = 32)
  public String getMinimumElevationInMeters() {
    return minimumElevationInMeters;
  }

  public Date getModified() {
    return modified;
  }

  // deprecated in favor of month
  // @Column(length = 16)
  // public String getMonthOfYear() {
  // return monthOfYear;
  // }
  @Column(length = 16)
  public String getMonth() {
    return month;
  }

  public String getMunicipality() {
    return municipality;
  }

  // deprecated in favor of nameAccordingTo
  // public String getTaxonAccordingTo() {
  // return taxonAccordingTo;
  // }
  public String getNameAccordingTo() {
    return nameAccordingTo;
  }

  @Column(length = 128)
  public String getNameAccordingToID() {
    return nameAccordingToID;
  }

  public String getNamePublishedIn() {
    return namePublishedIn;
  }

  @Column(length = 128)
  public String getNamePublishedInID() {
    return namePublishedInID;
  }

  @Column(length = 64)
  public String getNomenclaturalCode() {
    return nomenclaturalCode;
  }

  @Column(length = 128)
  public String getNomenclaturalStatus() {
    return nomenclaturalStatus;
  }

  // deprecated in favor of occurrenceDetails
  // public String getSampleDetails() {
  // return sampleDetails;
  // }
  public String getOccurrenceDetails() {
    return occurrenceDetails;
  }

  // deprecated in favor of OccurrenceRemarks
  // @Lob
  // public String getSampleRemarks() {
  // return sampleRemarks;
  // }
  @Lob
  public String getOccurrenceRemarks() {
    return occurrenceRemarks;
  }

  public String getOccurrenceStatus() {
    return occurrenceStatus;
  }

  @Column(length = 128, name = "orderrr")
  public String getOrder() {
    return order;
  }

  // deprecated in favor of originalNameUsage
  // public String getBasionym() {
  // return basionym;
  // }
  public String getOriginalNameUsage() {
    return originalNameUsage;
  }

  // deprecated in favor of originalNameUsageID
  // @Column(length = 128)
  // public String getBasionymID() {
  // return basionymID;
  // }
  @Column(length = 128)
  public String getOriginalNameUsageID() {
    return originalNameUsageID;
  }

  public String getOtherCatalogNumbers() {
    return otherCatalogNumbers;
  }

  @Column(length = 128)
  public String getOwnerInstitutionCode() {
    return ownerInstitutionCode;
  }

  public String getParentNameUsage() {
    return parentNameUsage;
  }

  // deprecated in favor of parentNameUsageID
  // @Column(length = 128)
  // public String getHigherTaxonID() {
  // return higherTaxonID;
  // }
  @Column(length = 128)
  public String getParentNameUsageID() {
    return parentNameUsageID;
  }

  @Column(length = 128)
  public String getPhylum() {
    return phylum;
  }

  @Column(length = 64)
  public String getPointRadiusSpatialFit() {
    return pointRadiusSpatialFit;
  }

  public String getPreparations() {
    return preparations;
  }

  @Lob
  public String getPreviousIdentifications() {
    return previousIdentifications;
  }

  @Transient
  public String getPropertyValue(ExtensionProperty property) {
    return getPropertyValue(property.getName());
  }

  // deprecated in favor of recordedBy
  // @Column(length = 128)
  // public String getCollector() {
  // return collector;
  // }
  @Column(length = 128)
  public String getRecordedBy() {
    return recordedBy;
  }

  // deprecated
  // @Lob
  // public String getSampleAttributes() {
  // return sampleAttributes;
  // }

  // deprecated in favor of recordNumber
  // @Column(length = 64)
  // public String getCollectorNumber() {
  // return collectorNumber;
  // }
  @Column(length = 64)
  public String getRecordNumber() {
    return recordNumber;
  }

  @ManyToOne(optional = true)
  public Region getRegion() {
    return region;
  }

  public String getReproductiveCondition() {
    return reproductiveCondition;
  }

  // deprecated
  // @Lob
  // public String getSamplingEventAttributes() {
  // return samplingEventAttributes;
  // }

  @ManyToOne
  public DataResource getResource() {
    return resource;
  }

  @Transient
  public Long getResourceId() {
    return resource.getId();
  }

  @Lob
  public String getRights() {
    return rights;
  }

  @Column(length = 128)
  public String getRightsHolder() {
    return rightsHolder;
  }

  @Transient
  public String getSampleID() {
    return guid;
  }

  public String getSamplingEffort() {
    return samplingEffort;
  }

  public String getSamplingProtocol() {
    return samplingProtocol;
  }

  @org.hibernate.annotations.Index(name = "idx_dwc_scientific_name")
  public String getScientificName() {
    return scientificName;
  }

  public String getScientificNameAuthorship() {
    return scientificNameAuthorship;
  }

  @Column(length = 128)
  public String getScientificNameID() {
    return scientificNameID;
  }

  @Column(length = 128)
  public String getSex() {
    return sex;
  }

  @Column(length = 64)
  @org.hibernate.annotations.Index(name = "dwc_source_id")
  public String getSourceId() {
    return sourceId;
  }

  @Column(length = 128)
  public String getSpecificEpithet() {
    return specificEpithet;
  }

  @Column(length = 16)
  public String getStartDayOfYear() {
    return startDayOfYear;
  }

  public String getStateProvince() {
    return stateProvince;
  }

  @Column(length = 128)
  public String getSubgenus() {
    return subgenus;
  }

  @ManyToOne(optional = true)
  public Taxon getTaxon() {
    return taxon;
  }

  @Column(length = 128)
  public String getTaxonConceptID() {
    return taxonConceptID;
  }

  @Column(length = 128)
  public String getTaxonID() {
    return taxonID;
  }

  @Column(length = 128)
  public String getTaxonomicStatus() {
    return taxonomicStatus;
  }

  /**
   * 9 rank strings joined by | pipe symbol, starting with kingdom, ending in
   * Genus, SpeciesEpi, InfraSpeciesEpi and finally ScientificName
   * 
   * @return
   */
  @Transient
  public String getTaxonomyPath() {
    return getTaxonomyPath(Rank.TerminalTaxon);
  }

  @Transient
  public String getTaxonomyPath(Rank rank) {
    String path = StringUtils.trimToEmpty(getKingdom());
    if (rank.compareTo(Rank.Phylum) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getPhylum());
    }
    if (rank.compareTo(Rank.Class) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getClasss());
    }
    if (rank.compareTo(Rank.Order) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getOrder());
    }
    if (rank.compareTo(Rank.Family) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getFamily());
    }
    if (rank.compareTo(Rank.Genus) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getGenus());
    }
    if (rank.compareTo(Rank.Species) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getSpecificEpithet());
    }
    if (rank.compareTo(Rank.InfraSpecies) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getInfraspecificEpithet());
    }
    if (rank.compareTo(Rank.TerminalTaxon) >= 0) {
      path += "|" + StringUtils.trimToEmpty(getScientificName()) + " sec "
          + StringUtils.trimToEmpty(getNameAccordingTo());
    }
    return path;
  }

  @Column(length = 128)
  public String getTaxonRank() {
    return taxonRank;
  }

  public String getTaxonRemarks() {
    return taxonRemarks;
  }

  // TODO: Does type need an index, as basisOfRecord does?
  @Column(length = 64)
  // @org.hibernate.annotations.Index(name = "idx_dwc_type")
  public String getType() {
    return type;
  }

  @Column(length = 128)
  public String getTypeStatus() {
    return typeStatus;
  }

  public String getVerbatimCoordinates() {
    return verbatimCoordinates;
  }

  @Column(length = 128)
  public String getVerbatimCoordinateSystem() {
    return verbatimCoordinateSystem;
  }

  @Column(length = 128)
  public String getVerbatimDepth() {
    return verbatimDepth;
  }

  @Column(length = 128)
  public String getVerbatimElevation() {
    return verbatimElevation;
  }

  // deprecated in favor of verbatimEventDate
  // @Column(length = 128)
  // public String getVerbatimCollectingDate() {
  // return verbatimCollectingDate;
  // }
  @Column(length = 128)
  public String getVerbatimEventDate() {
    return verbatimEventDate;
  }

  @Column(length = 128)
  public String getVerbatimLatitude() {
    return verbatimLatitude;
  }

  @Lob
  public String getVerbatimLocality() {
    return verbatimLocality;
  }

  @Column(length = 128)
  public String getVerbatimLongitude() {
    return verbatimLongitude;
  }

  @Column(length = 128)
  public String getVerbatimSRS() {
    return verbatimSRS;
  }

  public String getVerbatimTaxonRank() {
    return verbatimTaxonRank;
  }

  public String getVernacularName() {
    return vernacularName;
  }

  @Column(length = 128)
  public String getWaterBody() {
    return waterBody;
  }

  // deprecated in favor of year
  // @Column(length = 16)
  // public String getYearSampled() {
  // return yearSampled;
  // }
  @Column(length = 16)
  public String getYear() {
    return year;
  }

  /**
   * Works on the raw imported properties and Ignores all secondary derived
   * properties. Therefore id, isDeleted, lat/longAsFloat, modified, created,
   * location, region, taxon, collected, elevation, depth are ignored in the
   * hashing.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    // TODO: This is a faulty hashCode. Two records with different content can
    // have the same code. In fact, at least two records are guaranteed to have
    // the same code if there is a number of records equal to the size of int.
    // Change to long.
    int result = 17;
    result = 31 * result + (guid != null ? guid.hashCode() : 0);
    result = 31 * result + (link != null ? link.hashCode() : 0);
    result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);

    // Dublin Core terms - all record-level
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (language != null ? language.hashCode() : 0);
    result = 31 * result + (rights != null ? rights.hashCode() : 0);
    result = 31 * result + (rightsHolder != null ? rightsHolder.hashCode() : 0);
    result = 31 * result + (accessRights != null ? accessRights.hashCode() : 0);
    result = 31
        * result
        + (bibliographicCitation != null ? bibliographicCitation.hashCode() : 0);

    // Other record-level terms
    result = 31 * result
        + (institutionID != null ? institutionID.hashCode() : 0);
    result = 31 * result + (collectionID != null ? collectionID.hashCode() : 0);
    result = 31 * result + (datasetID != null ? datasetID.hashCode() : 0);
    result = 31 * result
        + (institutionCode != null ? institutionCode.hashCode() : 0);
    result = 31 * result
        + (collectionCode != null ? collectionCode.hashCode() : 0);
    result = 31 * result + (datasetName != null ? datasetName.hashCode() : 0);
    result = 31 * result
        + (ownerInstitutionCode != null ? ownerInstitutionCode.hashCode() : 0);
    result = 31 * result
        + (basisOfRecord != null ? basisOfRecord.hashCode() : 0);
    result = 31 * result
        + (informationWithheld != null ? informationWithheld.hashCode() : 0);
    result = 31 * result
        + (dataGeneralizations != null ? dataGeneralizations.hashCode() : 0);
    result = 31 * result
        + (dynamicProperties != null ? dynamicProperties.hashCode() : 0);

    // Occurrence
    // occurrenceID = this.guid
    result = 31 * result
        + (catalogNumber != null ? catalogNumber.hashCode() : 0);
    result = 31 * result
        + (occurrenceDetails != null ? occurrenceDetails.hashCode() : 0);
    result = 31 * result
        + (occurrenceRemarks != null ? occurrenceRemarks.hashCode() : 0);
    result = 31 * result + (recordNumber != null ? recordNumber.hashCode() : 0);
    result = 31 * result + (recordedBy != null ? recordedBy.hashCode() : 0);
    result = 31 * result + (individualID != null ? individualID.hashCode() : 0);
    result = 31 * result
        + (individualCount != null ? individualCount.hashCode() : 0);
    result = 31 * result + (sex != null ? sex.hashCode() : 0);
    result = 31 * result + (lifeStage != null ? lifeStage.hashCode() : 0);
    result = 31
        * result
        + (reproductiveCondition != null ? reproductiveCondition.hashCode() : 0);
    result = 31 * result + (behavior != null ? behavior.hashCode() : 0);
    result = 31 * result
        + (establishmentMeans != null ? establishmentMeans.hashCode() : 0);
    result = 31 * result
        + (occurrenceStatus != null ? occurrenceStatus.hashCode() : 0);
    result = 31 * result + (preparations != null ? preparations.hashCode() : 0);
    result = 31 * result + (disposition != null ? disposition.hashCode() : 0);
    result = 31 * result
        + (otherCatalogNumbers != null ? otherCatalogNumbers.hashCode() : 0);
    result = 31
        * result
        + (previousIdentifications != null ? previousIdentifications.hashCode()
            : 0);
    result = 31 * result
        + (associatedMedia != null ? associatedMedia.hashCode() : 0);
    result = 31 * result
        + (associatedReferences != null ? associatedReferences.hashCode() : 0);
    result = 31
        * result
        + (associatedOccurrences != null ? associatedOccurrences.hashCode() : 0);
    result = 31 * result
        + (associatedSequences != null ? associatedSequences.hashCode() : 0);
    result = 31 * result
        + (associatedTaxa != null ? associatedTaxa.hashCode() : 0);

    // Event
    result = 31 * result + (eventID != null ? eventID.hashCode() : 0);
    result = 31 * result
        + (samplingProtocol != null ? samplingProtocol.hashCode() : 0);
    result = 31 * result
        + (samplingEffort != null ? samplingEffort.hashCode() : 0);
    result = 31 * result + (eventDate != null ? eventDate.hashCode() : 0);
    result = 31 * result + (eventTime != null ? eventTime.hashCode() : 0);
    result = 31 * result
        + (startDayOfYear != null ? startDayOfYear.hashCode() : 0);
    result = 31 * result + (endDayOfYear != null ? endDayOfYear.hashCode() : 0);
    result = 31 * result + (year != null ? year.hashCode() : 0);
    result = 31 * result + (month != null ? month.hashCode() : 0);
    result = 31 * result + (day != null ? day.hashCode() : 0);
    result = 31 * result
        + (verbatimEventDate != null ? verbatimEventDate.hashCode() : 0);
    result = 31 * result + (habitat != null ? habitat.hashCode() : 0);
    result = 31 * result + (fieldNumber != null ? fieldNumber.hashCode() : 0);
    result = 31 * result + (fieldNotes != null ? fieldNotes.hashCode() : 0);
    result = 31 * result + (eventRemarks != null ? eventRemarks.hashCode() : 0);

    // Location => this.region
    result = 31 * result + (locationID != null ? locationID.hashCode() : 0);
    result = 31 * result
        + (higherGeographyID != null ? higherGeographyID.hashCode() : 0);
    result = 31 * result
        + (higherGeography != null ? higherGeography.hashCode() : 0);
    result = 31 * result + (continent != null ? continent.hashCode() : 0);
    result = 31 * result + (waterBody != null ? waterBody.hashCode() : 0);
    result = 31 * result + (islandGroup != null ? islandGroup.hashCode() : 0);
    result = 31 * result + (island != null ? island.hashCode() : 0);
    result = 31 * result + (country != null ? country.hashCode() : 0);
    result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
    result = 31 * result
        + (stateProvince != null ? stateProvince.hashCode() : 0);
    result = 31 * result + (county != null ? county.hashCode() : 0);
    result = 31 * result + (municipality != null ? municipality.hashCode() : 0);
    result = 31 * result + (locality != null ? locality.hashCode() : 0);
    result = 31 * result
        + (verbatimLocality != null ? verbatimLocality.hashCode() : 0);
    result = 31 * result
        + (verbatimElevation != null ? verbatimElevation.hashCode() : 0);
    result = 31
        * result
        + (minimumElevationInMeters != null ? minimumElevationInMeters
            .hashCode() : 0);
    result = 31
        * result
        + (maximumElevationInMeters != null ? maximumElevationInMeters
            .hashCode() : 0);
    result = 31 * result
        + (verbatimDepth != null ? verbatimDepth.hashCode() : 0);
    result = 31 * result
        + (minimumDepthInMeters != null ? minimumDepthInMeters.hashCode() : 0);
    result = 31 * result
        + (maximumDepthInMeters != null ? maximumDepthInMeters.hashCode() : 0);
    result = 31
        * result
        + (minimumDistanceAboveSurfaceInMeters != null ? minimumDistanceAboveSurfaceInMeters
            .hashCode()
            : 0);
    result = 31
        * result
        + (maximumDistanceAboveSurfaceInMeters != null ? maximumDistanceAboveSurfaceInMeters
            .hashCode()
            : 0);
    result = 31 * result
        + (locationAccordingTo != null ? locationAccordingTo.hashCode() : 0);
    result = 31 * result
        + (locationRemarks != null ? locationRemarks.hashCode() : 0);
    result = 31 * result
        + (verbatimCoordinates != null ? verbatimCoordinates.hashCode() : 0);
    result = 31 * result
        + (verbatimLatitude != null ? verbatimLatitude.hashCode() : 0);
    result = 31 * result
        + (verbatimLongitude != null ? verbatimLongitude.hashCode() : 0);
    result = 31
        * result
        + (verbatimCoordinateSystem != null ? verbatimCoordinateSystem
            .hashCode() : 0);
    result = 31 * result + (verbatimSRS != null ? verbatimSRS.hashCode() : 0);
    result = 31 * result
        + (decimalLatitude != null ? decimalLatitude.hashCode() : 0);
    result = 31 * result
        + (decimalLongitude != null ? decimalLongitude.hashCode() : 0);
    result = 31 * result
        + (geodeticDatum != null ? geodeticDatum.hashCode() : 0);
    result = 31
        * result
        + (coordinateUncertaintyInMeters != null ? coordinateUncertaintyInMeters
            .hashCode()
            : 0);
    result = 31 * result
        + (coordinatePrecision != null ? coordinatePrecision.hashCode() : 0);
    result = 31
        * result
        + (pointRadiusSpatialFit != null ? pointRadiusSpatialFit.hashCode() : 0);
    result = 31 * result + (footprintWKT != null ? footprintWKT.hashCode() : 0);
    result = 31 * result + (footprintSRS != null ? footprintSRS.hashCode() : 0);
    result = 31 * result
        + (footprintSpatialFit != null ? footprintSpatialFit.hashCode() : 0);
    result = 31 * result
        + (georeferencedBy != null ? georeferencedBy.hashCode() : 0);
    result = 31 * result
        + (georeferenceProtocol != null ? georeferenceProtocol.hashCode() : 0);
    result = 31 * result
        + (georeferenceSources != null ? georeferenceSources.hashCode() : 0);
    result = 31
        * result
        + (georeferenceVerificationStatus != null ? georeferenceVerificationStatus
            .hashCode()
            : 0);
    result = 31 * result
        + (georeferenceRemarks != null ? georeferenceRemarks.hashCode() : 0);

    // GeologicalContext
    result = 31 * result
        + (geologicalContextID != null ? geologicalContextID.hashCode() : 0);
    result = 31
        * result
        + (earliestEonOrLowestEonothem != null ? earliestEonOrLowestEonothem
            .hashCode() : 0);
    result = 31
        * result
        + (latestEonOrHighestEonothem != null ? latestEonOrHighestEonothem
            .hashCode() : 0);
    result = 31
        * result
        + (earliestEraOrLowestErathem != null ? earliestEraOrLowestErathem
            .hashCode() : 0);
    result = 31
        * result
        + (latestEraOrHighestErathem != null ? latestEraOrHighestErathem
            .hashCode() : 0);
    result = 31
        * result
        + (earliestPeriodOrLowestSystem != null ? earliestPeriodOrLowestSystem
            .hashCode() : 0);
    result = 31
        * result
        + (latestPeriodOrHighestSystem != null ? latestPeriodOrHighestSystem
            .hashCode() : 0);
    result = 31
        * result
        + (earliestEpochOrLowestSeries != null ? earliestEpochOrLowestSeries
            .hashCode() : 0);
    result = 31
        * result
        + (latestEpochOrHighestSeries != null ? latestEpochOrHighestSeries
            .hashCode() : 0);
    result = 31
        * result
        + (earliestAgeOrLowestStage != null ? earliestAgeOrLowestStage
            .hashCode() : 0);
    result = 31
        * result
        + (latestAgeOrHighestStage != null ? latestAgeOrHighestStage.hashCode()
            : 0);
    result = 31
        * result
        + (lowestBiostratigraphicZone != null ? lowestBiostratigraphicZone
            .hashCode() : 0);
    result = 31
        * result
        + (highestBiostratigraphicZone != null ? highestBiostratigraphicZone
            .hashCode() : 0);
    result = 31
        * result
        + (lithostratigraphicTerms != null ? lithostratigraphicTerms.hashCode()
            : 0);
    result = 31 * result + (group != null ? group.hashCode() : 0);
    result = 31 * result + (formation != null ? formation.hashCode() : 0);
    result = 31 * result + (member != null ? member.hashCode() : 0);
    result = 31 * result + (bed != null ? bed.hashCode() : 0);

    // Identification
    result = 31 * result
        + (identificationID != null ? identificationID.hashCode() : 0);
    result = 31 * result + (identifiedBy != null ? identifiedBy.hashCode() : 0);
    result = 31 * result
        + (dateIdentified != null ? dateIdentified.hashCode() : 0);
    result = 31
        * result
        + (identificationReferences != null ? identificationReferences
            .hashCode() : 0);
    result = 31
        * result
        + (identificationRemarks != null ? identificationRemarks.hashCode() : 0);
    result = 31
        * result
        + (identificationQualifier != null ? identificationQualifier.hashCode()
            : 0);
    result = 31 * result + (typeStatus != null ? typeStatus.hashCode() : 0);

    // Taxon => this.taxon
    result = 31 * result + (taxonID != null ? taxonID.hashCode() : 0);
    result = 31 * result
        + (scientificNameID != null ? scientificNameID.hashCode() : 0);
    result = 31 * result
        + (acceptedNameUsageID != null ? acceptedNameUsageID.hashCode() : 0);
    result = 31 * result
        + (parentNameUsageID != null ? parentNameUsageID.hashCode() : 0);
    result = 31 * result
        + (originalNameUsageID != null ? originalNameUsageID.hashCode() : 0);
    result = 31 * result
        + (nameAccordingToID != null ? nameAccordingToID.hashCode() : 0);
    result = 31 * result
        + (namePublishedInID != null ? namePublishedInID.hashCode() : 0);
    result = 31 * result
        + (taxonConceptID != null ? taxonConceptID.hashCode() : 0);
    result = 31 * result
        + (scientificName != null ? scientificName.hashCode() : 0);
    result = 31 * result
        + (acceptedNameUsage != null ? acceptedNameUsage.hashCode() : 0);
    result = 31 * result
        + (parentNameUsage != null ? parentNameUsage.hashCode() : 0);
    result = 31 * result
        + (originalNameUsage != null ? originalNameUsage.hashCode() : 0);
    result = 31 * result
        + (nameAccordingTo != null ? nameAccordingTo.hashCode() : 0);
    result = 31 * result
        + (namePublishedIn != null ? namePublishedIn.hashCode() : 0);
    result = 31 * result
        + (higherClassification != null ? higherClassification.hashCode() : 0);
    result = 31 * result + (kingdom != null ? kingdom.hashCode() : 0);
    result = 31 * result + (phylum != null ? phylum.hashCode() : 0);
    result = 31 * result + (classs != null ? classs.hashCode() : 0);
    result = 31 * result + (order != null ? order.hashCode() : 0);
    result = 31 * result + (family != null ? family.hashCode() : 0);
    result = 31 * result + (genus != null ? genus.hashCode() : 0);
    result = 31 * result + (subgenus != null ? subgenus.hashCode() : 0);
    result = 31 * result
        + (specificEpithet != null ? specificEpithet.hashCode() : 0);
    result = 31 * result
        + (infraspecificEpithet != null ? infraspecificEpithet.hashCode() : 0);
    result = 31 * result + (taxonRank != null ? taxonRank.hashCode() : 0);
    result = 31 * result
        + (verbatimTaxonRank != null ? verbatimTaxonRank.hashCode() : 0);
    result = 31
        * result
        + (scientificNameAuthorship != null ? scientificNameAuthorship
            .hashCode() : 0);
    result = 31 * result
        + (vernacularName != null ? vernacularName.hashCode() : 0);
    result = 31 * result
        + (nomenclaturalCode != null ? nomenclaturalCode.hashCode() : 0);
    result = 31 * result
        + (taxonomicStatus != null ? taxonomicStatus.hashCode() : 0);
    result = 31 * result
        + (nomenclaturalStatus != null ? nomenclaturalStatus.hashCode() : 0);
    result = 31 * result + (taxonRemarks != null ? taxonRemarks.hashCode() : 0);
    return result;
  }

  @org.hibernate.annotations.Index(name = "deleted")
  public boolean isDeleted() {
    return isDeleted;
  }

  // deprecated in favor of acceptedNameUsage
  // public void setAcceptedTaxon(String acceptedTaxon) {
  // this.acceptedTaxon = acceptedTaxon;
  // }
  public void setAcceptedNameUsage(String acceptedNameUsage) {
    this.acceptedNameUsage = acceptedNameUsage;
  }

  // deprecated in favor of acceptedNameUsageID
  // public void setAcceptedTaxonID(String acceptedTaxonID) {
  // this.acceptedTaxonID = acceptedTaxonID;
  // }
  public void setAcceptedNameUsageID(String acceptedNameUsageID) {
    this.acceptedNameUsageID = acceptedNameUsageID;
  }

  // depreacted in favor of accessRights
  // public void setAccessConstraints(String accessConstraints) {
  // this.accessConstraints = accessConstraints;
  // }
  public void setAccessRights(String accessRights) {
    this.accessRights = accessRights;
  }

  public void setAssociatedMedia(String associatedMedia) {
    this.associatedMedia = associatedMedia;
  }

  // deprecated in favor of associatedOccurrences
  // public void setAssociatedSamples(String associatedSamples) {
  // this.associatedSamples = associatedSamples;
  // }
  public void setAssociatedOccurrences(String associatedOccurrences) {
    this.associatedOccurrences = associatedOccurrences;
  }

  public void setAssociatedReferences(String associatedReferences) {
    this.associatedReferences = associatedReferences;
  }

  public void setAssociatedSequences(String associatedSequences) {
    this.associatedSequences = associatedSequences;
  }

  public void setAssociatedTaxa(String associatedTaxa) {
    this.associatedTaxa = associatedTaxa;
  }

  public void setBasisOfRecord(String basisOfRecord) {
    this.basisOfRecord = basisOfRecord;
  }

  public void setBed(String bed) {
    this.bed = bed;
  }

  public void setBehavior(String behavior) {
    this.behavior = behavior;
  }

  // deprecated in favor of bibliographicCitation
  // public void setCitation(String citation) {
  // this.citation = citation;
  // }
  public void setBibliographicCitation(String bibliographicCitation) {
    this.bibliographicCitation = bibliographicCitation;
  }

  public void setCatalogNumber(String catalogNumber) {
    this.catalogNumber = catalogNumber;
  }

  // depreacted
  // public void setBinomial(String binomial) {
  // this.binomial = binomial;
  // }

  public void setClasss(String classs) {
    this.classs = classs;
  }

  // depreacted
  // public void setCatalogNumberNumeric(String catalogNumberNumeric) {
  // this.catalogNumberNumeric = catalogNumberNumeric;
  // }

  public void setCollected(Date dateCollected) {
    this.collected = dateCollected;
  }

  public void setCollectionCode(String collectionCode) {
    this.collectionCode = collectionCode;
  }

  public void setCollectionID(String collectionID) {
    this.collectionID = collectionID;
  }

  public void setContinent(String continent) {
    this.continent = continent;
  }

  public void setCoordinatePrecision(String coordinatePrecision) {
    this.coordinatePrecision = coordinatePrecision;
  }

  public void setCoordinateUncertaintyInMeters(
      String coordinateUncertaintyInMeters) {
    this.coordinateUncertaintyInMeters = coordinateUncertaintyInMeters;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public void setCounty(String county) {
    this.county = county;
  }

  // deprecated in favor of dataGeneralizations
  // public void setGeneralizations(String generalizations) {
  // this.generalizations = generalizations;
  // }
  public void setDataGeneralizations(String dataGeneralizations) {
    this.dataGeneralizations = dataGeneralizations;
  }

  public void setDatasetID(String datasetID) {
    this.datasetID = datasetID;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public void setDateIdentified(String dateIdentified) {
    this.dateIdentified = dateIdentified;
  }

  // deprecated in favor of day
  // public void setDayOfMonth(String dayOfMonth) {
  // this.dayOfMonth = dayOfMonth;
  // }
  public void setDay(String day) {
    this.day = day;
  }

  public void setDecimalLatitude(String decimalLatitude) {
    this.decimalLatitude = decimalLatitude;
  }

  public void setDecimalLongitude(String decimalLongitude) {
    this.decimalLongitude = decimalLongitude;
  }

  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public void setDepth(Double depth) {
    this.depth = depth;
  }

  public void setDisposition(String disposition) {
    this.disposition = disposition;
  }

  public void setDynamicProperties(String dynamicProperties) {
    this.dynamicProperties = dynamicProperties;
  }

  public void setEarliestAgeOrLowestStage(String earliestAgeOrLowestStage) {
    this.earliestAgeOrLowestStage = earliestAgeOrLowestStage;
  }

  public void setEarliestEonOrLowestEonothem(String earliestEonOrLowestEonothem) {
    this.earliestEonOrLowestEonothem = earliestEonOrLowestEonothem;
  }

  public void setEarliestEpochOrLowestSeries(String earliestEpochOrLowestSeries) {
    this.earliestEpochOrLowestSeries = earliestEpochOrLowestSeries;
  }

  public void setEarliestEraOrLowestErathem(String earliestEraOrLowestErathem) {
    this.earliestEraOrLowestErathem = earliestEraOrLowestErathem;
  }

  public void setEarliestPeriodOrLowestSystem(
      String earliestPeriodOrLowestSystem) {
    this.earliestPeriodOrLowestSystem = earliestPeriodOrLowestSystem;
  }

  public void setElevation(Double elevation) {
    this.elevation = elevation;
  }

  public void setEndDayOfYear(String endDayOfYear) {
    this.endDayOfYear = endDayOfYear;
  }

  public void setEstablishmentMeans(String establishmentMeans) {
    this.establishmentMeans = establishmentMeans;
  }

  // deprecated in favor of eventDate
  // public void setEarliestDateCollected(String earliestDateCollected) {
  // this.earliestDateCollected = earliestDateCollected;
  // }
  // deprecated in favor of eventDate
  // public void setLatestDateCollected(String latestDateCollected) {
  // this.latestDateCollected = latestDateCollected;
  // }
  public void setEventDate(String eventDate) {
    this.eventDate = eventDate;
  }

  // deprecated in favor of eventID
  // public void setSamplingEventID(String samplingEventID) {
  // this.samplingEventID = samplingEventID;
  // }
  public void setEventID(String eventID) {
    this.eventID = eventID;
  }

  // deprecated in favor of eventRemarks
  // public void setSamplingEventRemarks(String samplingEventRemarks) {
  // this.samplingEventRemarks = samplingEventRemarks;
  // }
  public void setEventRemarks(String eventRemarks) {
    this.eventRemarks = eventRemarks;
  }

  // deprecated in favor of eventTime
  // public void setEndTimeOfDay(String endTimeOfDay) {
  // this.endTimeOfDay = endTimeOfDay;
  // }
  // deprecated in favor of eventTime
  // public void setStartTimeOfDay(String startTimeOfDay) {
  // this.startTimeOfDay = startTimeOfDay;
  // }
  public void setEventTime(String eventTime) {
    this.eventTime = eventTime;
  }

  public void setFamily(String family) {
    this.family = family;
  }

  public void setFieldNotes(String fieldNotes) {
    this.fieldNotes = fieldNotes;
  }

  public void setFieldNumber(String fieldNumber) {
    this.fieldNumber = fieldNumber;
  }

  public void setFootprintSpatialFit(String footprintSpatialFit) {
    this.footprintSpatialFit = footprintSpatialFit;
  }

  public void setFootprintSRS(String footprintSRS) {
    this.footprintSRS = footprintSRS;
  }

  public void setFootprintWKT(String footprintWKT) {
    this.footprintWKT = footprintWKT;
  }

  public void setFormation(String formation) {
    this.formation = formation;
  }

  public void setGenus(String genus) {
    this.genus = genus;
  }

  public void setGeodeticDatum(String geodeticDatum) {
    this.geodeticDatum = geodeticDatum;
  }

  public void setGeologicalContextID(String geologicalContextID) {
    this.geologicalContextID = geologicalContextID;
  }

  public void setGeoreferencedBy(String georeferencedBy) {
    this.georeferencedBy = georeferencedBy;
  }

  public void setGeoreferenceProtocol(String georeferenceProtocol) {
    this.georeferenceProtocol = georeferenceProtocol;
  }

  public void setGeoreferenceRemarks(String georeferenceRemarks) {
    this.georeferenceRemarks = georeferenceRemarks;
  }

  public void setGeoreferenceSources(String georeferenceSources) {
    this.georeferenceSources = georeferenceSources;
  }

  public void setGeoreferenceVerificationStatus(
      String georeferenceVerificationStatus) {
    this.georeferenceVerificationStatus = georeferenceVerificationStatus;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setHabitat(String habitat) {
    this.habitat = habitat;
  }

  // deprecated in favor of higherClassification
  // public void setHigherTaxon(String higherTaxon) {
  // this.higherTaxon = higherTaxon;
  // }
  public void setHigherClassification(String higherClassification) {
    this.higherClassification = higherClassification;
  }

  public void setHigherGeography(String higherGeography) {
    this.higherGeography = higherGeography;
  }

  public void setHigherGeographyID(String higherGeographyID) {
    this.higherGeographyID = higherGeographyID;
  }

  public void setHighestBiostratigraphicZone(String highestBiostratigraphicZone) {
    this.highestBiostratigraphicZone = highestBiostratigraphicZone;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setIdentificationID(String identificationID) {
    this.identificationID = identificationID;
  }

  public void setIdentificationQualifier(String identificationQualifier) {
    this.identificationQualifier = identificationQualifier;
  }

  public void setIdentificationReferences(String identificationReferences) {
    this.identificationReferences = identificationReferences;
  }

  public void setIdentificationRemarks(String identificationRemarks) {
    this.identificationRemarks = identificationRemarks;
  }

  public void setIdentifiedBy(String identifiedBy) {
    this.identifiedBy = identifiedBy;
  }

  public void setIndividualCount(String individualCount) {
    this.individualCount = individualCount;
  }

  public void setIndividualID(String individualID) {
    this.individualID = individualID;
  }

  public void setInformationWithheld(String informationWithheld) {
    this.informationWithheld = informationWithheld;
  }

  public void setInfraspecificEpithet(String infraspecificEpithet) {
    this.infraspecificEpithet = infraspecificEpithet;
  }

  public void setInstitutionCode(String institutionCode) {
    this.institutionCode = institutionCode;
  }

  public void setInstitutionID(String institutionID) {
    this.institutionID = institutionID;
  }

  public void setIsland(String island) {
    this.island = island;
  }

  public void setIslandGroup(String islandGroup) {
    this.islandGroup = islandGroup;
  }

  public void setKingdom(String kingdom) {
    this.kingdom = kingdom;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setLatestAgeOrHighestStage(String latestAgeOrHighestStage) {
    this.latestAgeOrHighestStage = latestAgeOrHighestStage;
  }

  public void setLatestEonOrHighestEonothem(String latestEonOrHighestEonothem) {
    this.latestEonOrHighestEonothem = latestEonOrHighestEonothem;
  }

  public void setLatestEpochOrHighestSeries(String latestEpochOrHighestSeries) {
    this.latestEpochOrHighestSeries = latestEpochOrHighestSeries;
  }

  public void setLatestEraOrHighestErathem(String latestEraOrHighestErathem) {
    this.latestEraOrHighestErathem = latestEraOrHighestErathem;
  }

  public void setLatestPeriodOrHighestSystem(String latestPeriodOrHighestSystem) {
    this.latestPeriodOrHighestSystem = latestPeriodOrHighestSystem;
  }

  public void setLifeStage(String lifeStage) {
    this.lifeStage = lifeStage;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setLithostratigraphicTerms(String lithostratigraphicTerms) {
    this.lithostratigraphicTerms = lithostratigraphicTerms;
  }

  public void setLocality(String locality) {
    this.locality = locality;
  }

  public void setLocation(Point location) {
    this.location = location;
  }

  public void setLocationAccordingTo(String locationAccordingTo) {
    this.locationAccordingTo = locationAccordingTo;
  }

  // deprecated in favor of locationID
  // public void setSamplingLocationID(String samplingLocationID) {
  // this.samplingLocationID = samplingLocationID;
  // }
  public void setLocationID(String locationID) {
    this.locationID = locationID;
  }

  // deprecated in favor of locationRemarks
  // public void setSamplingLocationRemarks(String samplingLocationRemarks) {
  // this.samplingLocationRemarks = samplingLocationRemarks;
  // }
  public void setLocationRemarks(String locationRemarks) {
    this.locationRemarks = locationRemarks;
  }

  public void setLowestBiostratigraphicZone(String lowestBiostratigraphicZone) {
    this.lowestBiostratigraphicZone = lowestBiostratigraphicZone;
  }

  public void setMaximumDepthInMeters(String maximumDepthInMeters) {
    this.maximumDepthInMeters = maximumDepthInMeters;
  }

  // deprecated in favor of maximumDistanceAboveSurfaceInMeters
  // public void setDistanceAboveSurfaceInMetersMaximum(
  // String distanceAboveSurfaceInMetersMaximum) {
  // this.distanceAboveSurfaceInMetersMaximum =
  // distanceAboveSurfaceInMetersMaximum;
  // }
  public void setMaximumDistanceAboveSurfaceInMeters(
      String maximumDistanceAboveSurfaceInMeters) {
    this.maximumDistanceAboveSurfaceInMeters = maximumDistanceAboveSurfaceInMeters;
  }

  public void setMaximumElevationInMeters(String maximumElevationInMeters) {
    this.maximumElevationInMeters = maximumElevationInMeters;
  }

  public void setMember(String member) {
    this.member = member;
  }

  public void setMinimumDepthInMeters(String minimumDepthInMeters) {
    this.minimumDepthInMeters = minimumDepthInMeters;
  }

  // deprecated in favor of maximumDistanceAboveSurfaceInMeters
  // public void setDistanceAboveSurfaceInMetersMinimum(
  // String distanceAboveSurfaceInMetersMinimum) {
  // this.distanceAboveSurfaceInMetersMinimum =
  // distanceAboveSurfaceInMetersMinimum;
  // }
  public void setMinimumDistanceAboveSurfaceInMeters(
      String minimumDistanceAboveSurfaceInMeters) {
    this.minimumDistanceAboveSurfaceInMeters = minimumDistanceAboveSurfaceInMeters;
  }

  public void setMinimumElevationInMeters(String minimumElevationInMeters) {
    this.minimumElevationInMeters = minimumElevationInMeters;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  // deprecated in favor of month
  // public void setMonthOfYear(String monthOfYear) {
  // this.monthOfYear = monthOfYear;
  // }
  public void setMonth(String month) {
    this.month = month;
  }

  public void setMunicipality(String municipality) {
    this.municipality = municipality;
  }

  // deprecated in favor of nameAccordingTo
  // public void setTaxonAccordingTo(String taxonAccordingTo) {
  // this.taxonAccordingTo = taxonAccordingTo;
  // }
  public void setNameAccordingTo(String nameAccordingTo) {
    this.nameAccordingTo = nameAccordingTo;
  }

  public void setNameAccordingToID(String nameAccordingToID) {
    this.nameAccordingToID = nameAccordingToID;
  }

  public void setNamePublishedIn(String namePublishedIn) {
    this.namePublishedIn = namePublishedIn;
  }

  public void setNamePublishedInID(String namePublishedInID) {
    this.namePublishedInID = namePublishedInID;
  }

  public void setNomenclaturalCode(String nomenclaturalCode) {
    this.nomenclaturalCode = nomenclaturalCode;
  }

  public void setNomenclaturalStatus(String nomenclaturalStatus) {
    this.nomenclaturalStatus = nomenclaturalStatus;
  }

  // deprecated in favor of occurrenceDetails
  // public void setSampleDetails(String sampleDetails) {
  // this.sampleDetails = sampleDetails;
  // }
  public void setOccurrenceDetails(String occurrenceDetails) {
    this.occurrenceDetails = occurrenceDetails;
  }

  // deprecated in favor of occurrenceRemarks
  // public void setSampleRemarks(String sampleRemarks) {
  // this.sampleRemarks = sampleRemarks;
  // }
  public void setOccurrenceRemarks(String occurrenceRemarks) {
    this.occurrenceRemarks = occurrenceRemarks;
  }

  public void setOccurrenceStatus(String occurrenceStatus) {
    this.occurrenceStatus = occurrenceStatus;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  // deprecated in favor of originalNameUsage
  // public void setBasionym(String basionym) {
  // this.basionym = basionym;
  // }
  public void setOriginalNameUsage(String originalNameUsage) {
    this.originalNameUsage = originalNameUsage;
  }

  // deprecated in favor of originalNameUsageID
  // public void setBasionymID(String basionymID) {
  // this.basionymID = basionymID;
  // }
  public void setOriginalNameUsageID(String originalNameUsageID) {
    this.originalNameUsageID = originalNameUsageID;
  }

  public void setOtherCatalogNumbers(String otherCatalogNumbers) {
    this.otherCatalogNumbers = otherCatalogNumbers;
  }

  public void setOwnerInstitutionCode(String ownerInstitutionCode) {
    this.ownerInstitutionCode = ownerInstitutionCode;
  }

  public void setParentNameUsage(String parentNameUsage) {
    this.parentNameUsage = parentNameUsage;
  }

  // deprecated in favor of parentNameUsageID
  // public void setHigherTaxonID(String higherTaxonID) {
  // this.higherTaxonID = higherTaxonID;
  // }
  public void setParentNameUsageID(String parentNameUsageID) {
    this.parentNameUsageID = parentNameUsageID;
  }

  public void setPhylum(String phylum) {
    this.phylum = phylum;
  }

  public void setPointRadiusSpatialFit(String pointRadiusSpatialFit) {
    this.pointRadiusSpatialFit = pointRadiusSpatialFit;
  }

  public void setPreparations(String preparations) {
    this.preparations = preparations;
  }

  public void setPreviousIdentifications(String previousIdentifications) {
    this.previousIdentifications = previousIdentifications;
  }

  public boolean setPropertyValue(ExtensionProperty property, String value) {
    try {
      Method m = this.getClass().getMethod(
          String.format("set%s", property.getName()), String.class);
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

  // deprecated in favor of recordedBy
  // public void setCollector(String collector) {
  // this.collector = collector;
  // }
  public void setRecordedBy(String recordedBy) {
    this.recordedBy = recordedBy;
  }

  // deprecated
  // public void setSampleAttributes(String sampleAttributes) {
  // this.sampleAttributes = sampleAttributes;
  // }

  // deprecated in favor of recordNumber
  // public void setCollectorNumber(String collectorNumber) {
  // this.collectorNumber = collectorNumber;
  // }
  public void setRecordNumber(String recordNumber) {
    this.recordNumber = recordNumber;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public void setReproductiveCondition(String reproductiveCondition) {
    this.reproductiveCondition = reproductiveCondition;
  }

  // deprecated
  // public void setSamplingEventAttributes(String samplingEventAttributes) {
  // this.samplingEventAttributes = samplingEventAttributes;
  // }

  public void setResource(DataResource resource) {
    this.resource = resource;
  }

  public void setRights(String rights) {
    this.rights = rights;
  }

  public void setRightsHolder(String rightsHolder) {
    this.rightsHolder = rightsHolder;
  }

  public void setSampleID(String sampleID) {
    this.guid = sampleID;
  }

  public void setSamplingEffort(String samplingEffort) {
    this.samplingEffort = samplingEffort;
  }

  public void setSamplingProtocol(String samplingProtocol) {
    this.samplingProtocol = samplingProtocol;
  }

  public void setScientificName(String scientificName) {
    this.scientificName = scientificName;
  }

  public void setScientificNameAuthorship(String scientificNameAuthorship) {
    this.scientificNameAuthorship = scientificNameAuthorship;
  }

  public void setScientificNameID(String scientificNameID) {
    this.scientificNameID = scientificNameID;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public void setSpecificEpithet(String specificEpithet) {
    this.specificEpithet = specificEpithet;
  }

  public void setStartDayOfYear(String startDayOfYear) {
    this.startDayOfYear = startDayOfYear;
  }

  public void setStateProvince(String stateProvince) {
    this.stateProvince = stateProvince;
  }

  public void setSubgenus(String subgenus) {
    this.subgenus = subgenus;
  }

  public void setTaxon(Taxon taxon) {
    this.taxon = taxon;
  }

  public void setTaxonConceptID(String taxonConceptID) {
    this.taxonConceptID = taxonConceptID;
  }

  public void setTaxonID(String taxonID) {
    this.taxonID = taxonID;
  }

  public void setTaxonomicStatus(String taxonomicStatus) {
    this.taxonomicStatus = taxonomicStatus;
  }

  public void setTaxonRank(String taxonRank) {
    this.taxonRank = taxonRank;
  }

  public void setTaxonRemarks(String taxonRemarks) {
    this.taxonRemarks = taxonRemarks;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setTypeStatus(String typeStatus) {
    this.typeStatus = typeStatus;
  }

  public void setVerbatimCoordinates(String verbatimCoordinates) {
    this.verbatimCoordinates = verbatimCoordinates;
  }

  public void setVerbatimDepth(String verbatimDepth) {
    this.verbatimDepth = verbatimDepth;
  }

  public void setVerbatimElevation(String verbatimElevation) {
    this.verbatimElevation = verbatimElevation;
  }

  // deprecated in favor of verbatimEventDate
  // public void setVerbatimCollectingDate(String verbatimCollectingDate) {
  // this.verbatimCollectingDate = verbatimCollectingDate;
  // }
  public void setVerbatimEventDate(String verbatimEventDate) {
    this.verbatimEventDate = verbatimEventDate;
  }

  public void setVerbatimLatitude(String verbatimLatitude) {
    this.verbatimLatitude = verbatimLatitude;
  }

  public void setVerbatimLocality(String verbatimLocality) {
    this.verbatimLocality = verbatimLocality;
  }

  public void setVerbatimLongitude(String verbatimLongitude) {
    this.verbatimLongitude = verbatimLongitude;
  }

  public void setVerbatimSRS(String verbatimSRS) {
    this.verbatimSRS = verbatimSRS;
  }

  public void setVerbatimTaxonRank(String verbatimTaxonRank) {
    this.verbatimTaxonRank = verbatimTaxonRank;
  }

  public void setVernacularName(String vernacularName) {
    this.vernacularName = vernacularName;
  }

  public void setWaterBody(String waterBody) {
    this.waterBody = waterBody;
  }

  // deprecated in favor of year
  // public void setYearSampled(String yearSampled) {
  // this.yearSampled = yearSampled;
  // }
  public void setYear(String year) {
    this.year = year;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("id", this.getId()).append(
        "basisOfRecord", this.basisOfRecord).append("scientificName",
        this.getScientificName()).append("localID", this.getSourceId()).append(
        "deleted", this.isDeleted()).append("institutionCode",
        this.institutionCode).append("collectionCode", this.collectionCode)
        .append("catalogNumber", this.catalogNumber).append("country",
            this.getCountry()).append("guid", this.guid).toString();
  }

  @Transient
  private String getPropertyValue(String propName) {
    if (propName.equals("Class")) {
      propName = "Classs";
    }
    String getter = String.format("get%s", propName);
    String value = null;
    try {
      Method m = this.getClass().getMethod(getter);
      Object obj = m.invoke(this);
      if (obj != null) {
        value = obj.toString();
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
}
