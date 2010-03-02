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

import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Index;
import org.hibernate.validator.NotNull;

import java.lang.reflect.Method;
import java.util.Date;
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

/**
 * This class can be used to encapsulate the properties of a Darwin Core record
 * and store it as a data store entity. The Darwin Core properties come from:
 * 
 * <pre>
 * http://code.google.com/p/darwincore/source/browse/trunk/xsd/tdwg_dwc_simple.xsd
 * </pre>
 * 
 * Note: This class has a natural ordering that is inconsistent with equals.
 * 
 */
@Entity
@Table(name = "darwin_core", uniqueConstraints = {@UniqueConstraint(columnNames = {
    "sourceId", "resource_fk"})})
@org.hibernate.annotations.Table(appliesTo = "Darwin_Core", indexes = {
    @Index(name = "latitude", columnNames = {"lat"}),
    @Index(name = "longitude", columnNames = {"lon"})})
public class DarwinCore implements CoreRecord, Comparable<DarwinCore> {

  /**
   * Enumeration of valid data types that Darwin Core properties can assume.
   * 
   */
  public static enum Type {
    INTEGER, DOUBLE, DATE, TOKEN, STRING;
  }

  /**
   * This class can be used to get the data type of a Darwin Core property name.
   * 
   */
  private static class TypeOracle {

    static final ImmutableSet<String> INTEGER_PROPERTIES = ImmutableSet.of(
        "month", "day", "startdayofyear", "enddayofyear", "individualcount");

    static final ImmutableSet<String> DOUBLE_PROPERTIES = ImmutableSet.of(
        "decimallongitude", "coordinateuncertaintyinmeters",
        "coordinateprecision", "maximumdepthinmeters",
        "maximumelevationinmeters", "minimumdepthinmeters",
        "minimumelevationinmeters", "maximumdistanceabovesurfaceinmeters",
        "minimumdistanceabovesurfaceinmeters", "pointradiusspatialfit",
        "footprintspatialfit");

    static final ImmutableSet<String> DATE_PROPERTIES = ImmutableSet.of(
        "modified", "dateidentified", "eventdate");

    static final ImmutableSet<String> TOKEN_PROPERTIES = ImmutableSet.of(
        "phylum", "class", "order", "family", "genus", "subgenus");

    static Type typeOf(String propertyName) {
      Preconditions.checkNotNull(propertyName);
      Preconditions.checkArgument(propertyName.length() > 0);
      String property = propertyName.toLowerCase();
      if (INTEGER_PROPERTIES.contains(property)) {
        return Type.INTEGER;
      } else if (DOUBLE_PROPERTIES.contains(property)) {
        return Type.DOUBLE;
      } else if (DATE_PROPERTIES.contains(property)) {
        return Type.DATE;
      } else if (TOKEN_PROPERTIES.contains(property)) {
        return Type.TOKEN;
      } else {
        return Type.STRING;
      }
    }
  }

  // Darwin Core terms from:
  // http://code.google.com/p/darwincore/source/browse/trunk/xsd/tdwg_dwc_simple.xsd
  public final static Set<String> DARWIN_CORE_TERMS = ImmutableSet.of(
      "acceptednameusage", "acceptednameusageid", "accessrights",
      "associatedmedia", "associatedoccurrences", "associatedreferences",
      "associatedsequences", "associatedtaxa", "basisofrecord", "bed",
      "behavior", "bibliographiccitation", "catalognumber", "class",
      "collectioncode", "collectionid", "continent", "coordinateprecision",
      "coordinateuncertaintyinmeters", "country", "countrycode", "county",
      "datageneralizations", "datasetid", "datasetname", "dateidentified",
      "day", "decimallatitude", "decimallongitude", "disposition",
      "dynamicproperties", "earliestageorloweststage",
      "earliesteonorlowesteonothem", "earliestepochorlowestseries",
      "earliesteraorlowesterathem", "earliestperiodorlowestsystem",
      "enddayofyear", "establishmentmeans", "eventdate", "eventid",
      "eventremarks", "eventtime", "family", "fieldnotes", "fieldnumber",
      "footprintsrs", "footprintspatialfit", "footprintwkt", "formation",
      "genus", "geodeticdatum", "geologicalcontextid", "georeferenceprotocol",
      "georeferenceremarks", "georeferencesources",
      "georeferenceverificationstatus", "georeferencedby", "group", "habitat",
      "higherclassification", "highergeography", "highergeographyid",
      "highestbiostratigraphiczone", "identificationid",
      "identificationqualifier", "identificationreferences",
      "identificationremarks", "identifiedby", "individualcount",
      "individualid", "informationwithheld", "infraspecificepithet",
      "institutioncode", "institutionid", "island", "islandgroup", "kingdom",
      "language", "latestageorhigheststage", "latesteonorhighesteonothem",
      "latestepochorhighestseries", "latesteraorhighesterathem",
      "latestperiodorhighestsystem", "lifestage", "lithostratigraphicterms",
      "locality", "locationaccordingto", "locationid", "locationremarks",
      "lowestbiostratigraphiczone", "maximumdepthinmeters",
      "maximumdistanceabovesurfaceinmeters", "maximumelevationinmeters",
      "member", "minimumdepthinmeters", "minimumdistanceabovesurfaceinmeters",
      "minimumelevationinmeters", "modified", "month", "municipality",
      "nameaccordingto", "nameaccordingtoid", "namepublishedin",
      "namepublishedinid", "nomenclaturalcode", "nomenclaturalstatus",
      "occurrencedetails", "occurrenceid", "occurrenceremarks",
      "occurrencestatus", "order", "originalnameusage", "originalnameusageid",
      "othercatalognumbers", "ownerinstitutioncode", "parentnameusage",
      "parentnameusageid", "phylum", "pointradiusspatialfit", "preparations",
      "previousidentifications", "recordnumber", "recordedby",
      "reproductivecondition", "rights", "rightsholder", "samplingeffort",
      "samplingprotocol", "scientificname", "scientificnameauthorship",
      "scientificnameid", "sex", "specificepithet", "startdayofyear",
      "stateprovince", "subgenus", "taxonconceptid", "taxonid", "taxonrank",
      "taxonremarks", "taxonomicstatus", "type", "typestatus",
      "verbatimcoordinatesystem", "verbatimcoordinates", "verbatimdepth",
      "verbatimelevation", "verbatimeventdate", "verbatimlatitude",
      "verbatimlocality", "verbatimlongitude", "verbatimsrs",
      "verbatimtaxonrank", "vernacularname", "waterbody", "year");

  private static final Log log = LogFactory.getLog(DarwinCore.class);

  /**
   * Returns the Java data type associated with a Darwin Core term. If the term
   * is null a {@link NullPointerException} is thrown. If the term is the empty
   * String a {@link IllegalArgumentException} is thrown. If the term is not a
   * Darwin Core term, null is returned.
   * 
   * @param term the Darwin Core term
   */
  public static Type dataType(String term) {
    Preconditions.checkNotNull(term);
    Preconditions.checkArgument(term.length() > 0);
    return DARWIN_CORE_TERMS.contains(term.toLowerCase())
        ? TypeOracle.typeOf(term) : null;
  }

  public static boolean isDarwinCoreTerm(String term) {
    return (term != null) && DARWIN_CORE_TERMS.contains(term.toLowerCase());
  }

  public static DarwinCore newMock(DataResource resource) {
    Random rnd = new Random();
    DarwinCore dwc = new DarwinCore();
    dwc.setResource(resource);
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

  /**
   * Static factory method that returns a new DarwinCore entity. If any of the
   * parameters are null, or if a String parameter is the empty String, an
   * {@link IllegalArgumentException} is thrown.
   * 
   * @param sourceId the source id
   * @param resource the resource
   * @param taxon the taxon
   * @param region the region
   * @param guid the guid
   */
  public static DarwinCore with(String sourceId, DataResource resource,
      Taxon taxon, Region region, String guid) {
    Preconditions.checkNotNull(sourceId);
    Preconditions.checkArgument(sourceId.length() > 0);
    Preconditions.checkNotNull(resource);
    Preconditions.checkNotNull(taxon);
    Preconditions.checkNotNull(region);
    Preconditions.checkNotNull(guid);
    Preconditions.checkArgument(guid.length() > 0);
    DarwinCore entity = new DarwinCore();
    entity.setSourceId(sourceId);
    entity.setResource(resource);
    entity.setTaxon(taxon);
    entity.setRegion(region);
    entity.setGuid(guid);
    return entity;
  }

  private static String geographicPath(DarwinCore entity, RegionType regionType) {
    Preconditions.checkNotNull(entity);
    Preconditions.checkNotNull(regionType);
    String path = StringUtils.trimToEmpty(entity.continent);
    if (regionType.compareTo(RegionType.Continent) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.continent);
    }
    if (regionType.compareTo(RegionType.Waterbody) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.waterbody);
    }
    if (regionType.compareTo(RegionType.IslandGroup) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.islandGroup);
    }
    if (regionType.compareTo(RegionType.Island) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.island);
    }
    if (regionType.compareTo(RegionType.Country) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.country);
    }
    if (regionType.compareTo(RegionType.State) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.stateProvince);
    }
    if (regionType.compareTo(RegionType.County) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.county);
    }
    if (regionType.compareTo(RegionType.Locality) >= 0) {
      path += "|" + StringUtils.trimToEmpty(entity.locality);
    }
    return path;
  }

  private static String higherTaxonName(DarwinCore entity, Rank rank) {
    Preconditions.checkNotNull(entity);
    Preconditions.checkNotNull(rank);
    if (rank == Rank.Species) {
      if (entity.specificEpithet != null) {
        return StringUtils.trimToNull(String.format("%s %s", entity.genus,
            entity.specificEpithet));
      } else {
        return null;
      }
    } else if (rank == Rank.InfraSpecies) {
      if (entity.infraspecificEpithet != null) {
        return StringUtils.trimToNull(String.format("%s %s %s %s",
            entity.genus, entity.specificEpithet, entity.taxonRank,
            entity.infraspecificEpithet));
      } else {
        return null;
      }
    } else if (rank == Rank.TerminalTaxon) {
      return entity.scientificName;
    } else {
      return StringUtils.trimToNull(propertyValue(entity,
          StringUtils.capitalize(rank.columnName)));
    }
  }

  private static String propertyValue(DarwinCore entity, String propName) {
    if (propName.equals("Class")) {
      propName = "Classs";
    }
    String getter = String.format("get%s", propName);
    String value = null;
    try {
      Method m = entity.getClass().getMethod(getter);
      Object obj = m.invoke(entity);
      if (obj != null) {
        value = obj.toString();
      }
    } catch (Exception e) {
      log.info(String.format("Unable to get property value for %s", propName));
    }
    return value;
  }

  private static String taxonomyPath(DarwinCore dc, Rank rank) {
    Preconditions.checkNotNull(dc);
    Preconditions.checkNotNull(rank);
    StringBuilder b = new StringBuilder(StringUtils.trimToEmpty(dc.kingdom));
    if (rank.compareTo(Rank.Phylum) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.phylum));
    }
    if (rank.compareTo(Rank.Class) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.classs));
    }
    if (rank.compareTo(Rank.Order) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.order));
    }
    if (rank.compareTo(Rank.Family) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.family));
    }
    if (rank.compareTo(Rank.Genus) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.genus));
    }
    if (rank.compareTo(Rank.Species) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.specificEpithet));
    }
    if (rank.compareTo(Rank.InfraSpecies) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.infraspecificEpithet));
    }
    if (rank.compareTo(Rank.TerminalTaxon) >= 0) {
      b.append("|" + StringUtils.trimToEmpty(dc.scientificName));
      b.append(" sec ");
      b.append(StringUtils.trimToEmpty(dc.nameAccordingTo));
    }
    return b.toString();
  }

  // Calculating the hashCode is expensive so we cache it:
  private int cachedHashCode = -1;

  // CoreRecord properties:
  private Long id;
  private String sourceId;
  private String link;
  private boolean isDeleted;
  private Date dateModified;
  @NotNull
  private String guid;
  @NotNull
  private DataResource resource;

  // Derived properties:
  private Point location = new Point();
  private Taxon taxon;
  private Region region;
  private Date collected;
  private Double elevation;
  private Double depth;

  // Simple DarwinCore properties from:
  // http://code.google.com/p/darwincore/source/browse/trunk/xsd/tdwg_dwc_simple.xsd
  private String acceptedNameUsage;
  private String acceptedNameUsageId;
  private String accessRights;
  private String associatedMedia;
  private String associatedOccurrences;
  private String associatedReferences;
  private String associatedSequences;
  private String associatedTaxa;
  private String basisOfRecord;
  private String bed;
  private String behavior;
  private String bibliographicCitation;
  private String catalogNumber;
  private String classs;
  private String collectionCode;
  private String collectionID;
  private String continent;
  private String coordinatePrecision;
  private String coordinateUncertaintyInMeters;
  private String country;
  private String countryCode;
  private String county;
  private String dataGeneralizations;
  private String datasetID;
  private String datasetName;
  private String dateIdentified;
  private String day;
  private String decimalLatitude;
  private String decimalLongitude;
  private String disposition;
  private String dynamicProperties;
  private String earliestAgeOrLowestStage;
  private String earliestEonOrLowestEonothem;
  private String earliestEpochOrLowestSeries;
  private String earliestEraOrLowestErathem;
  private String earliestPeriodOrLowestSystem;
  private String endDayOfYear;
  private String establishmentMeans;
  private String eventDate;
  private String eventId;
  private String eventRemarks;
  private String eventTime;
  private String family;
  private String fieldNotes;
  private String fieldNumber;
  private String footprintSRS;
  private String footprintSpatialFit;
  private String footprintWKT;
  private String formation;
  private String genus;
  private String geodeticDatum;
  private String geologicalContextId;
  private String georeferenceProtocol;
  private String georeferenceRemarks;
  private String georeferenceSources;
  private String georeferenceVerificationStatus;
  private String georeferencedBy;
  private String group;
  private String habitat;
  private String higherClassification;
  private String higherGeography;
  private String higherGeographyID;
  private String highestBiostratigraphicZone;
  private String identificationID;
  private String identificationQualifier;
  private String identificationReferences;
  private String identificationRemarks;
  private String identifiedBy;
  private String individualCount;
  private String individualID;
  private String informationWithheld;
  private String infraspecificEpithet;
  private String institutionCode;
  private String institutionId;
  private String island;
  private String islandGroup;
  private String kingdom;
  private String language;
  private String latestAgeOrHighestStage;
  private String latestEonOrHighestEonothem;
  private String latestEpochOrHighestSeries;
  private String latestEraOrHighestErathem;
  private String latestPeriodOrHighestSystem;
  private String lifeStage;
  private String lithostratigraphicTerms;
  private String locality;
  private String locationAccordingTo;
  private String locationId;
  private String locationRemarks;
  private String lowestBiostratigraphicZone;
  private String maximumDepthInMeters;
  private String maximumDistanceAboveSurfaceInMeters;
  private String maximumElevationInMeters;
  private String member;
  private String minimumDepthInMeters;
  private String minimumDistanceAboveSurfaceInMeters;
  private String minimumElevationInMeters;
  private String modified;
  private String month;
  private String municipality;
  private String nameAccordingTo;
  private String nameAccordingToId;
  private String namePublishedIn;
  private String namePublishedInId;
  private String nomenclaturalCode;
  private String nomenclaturalStatus;
  private String occurrenceDetails;
  private String occurrenceId;
  private String occurrenceRemarks;
  private String occurrenceStatus;
  private String order;
  private String originalNameUsage;
  private String originalNameUsageId;
  private String otherCatalogNumbers;
  private String ownerInstitutionCode;
  private String parentNameUsage;
  private String parentNameUsageId;
  private String phylum;
  private String pointRadiusSpatialFit;
  private String preparations;
  private String previousIdentifications;
  private String recordNumber;
  private String recordedBy;
  private String reproductiveCondition;
  private String rights;
  private String rightsHolder;
  private String samplingEffort;
  private String samplingProtocol;
  private String scientificName;
  private String scientificNameAuthorship;
  private String scientificNameId;
  private String sex;
  private String specificEpithet;
  private String startDayOfYear;
  private String stateProvince;
  private String subgenus;
  private String taxonConceptId;
  private String taxonID;
  private String taxonRank;
  private String taxonRemarks;
  private String taxonomicStatus;
  private String type;
  private String typeStatus;
  private String verbatimCoordinateSystem;
  private String verbatimCoordinates;
  private String verbatimDepth;
  private String verbatimElevation;
  private String verbatimEventDate;
  private String verbatimLatitude;
  private String verbatimLocality;
  private String verbatimLongitude;
  private String verbatimSRS;
  private String verbatimTaxonRank;
  private String vernacularName;
  private String waterbody;
  private String year;

  /**
   * Note: This class has a natural ordering that is inconsistent with equals.
   */
  public int compareTo(DarwinCore myClass) {
    return new CompareToBuilder().append(this.institutionCode,
        myClass.institutionCode).append(this.collectionCode,
        myClass.collectionCode).append(this.catalogNumber,
        myClass.catalogNumber).append(this.getScientificName(),
        myClass.getScientificName()).append(this.sourceId, myClass.sourceId).append(
        this.guid, myClass.guid).toComparison();
  }

  /**
   * Checks equality on {@link CoreRecord} properties (guid, link, sourceId) and
   * all Darwin Core properties.
   * 
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
    DarwinCore dc = (DarwinCore) object;
    return Objects.equal(guid, dc.guid)
        && Objects.equal(link, dc.link)
        && Objects.equal(sourceId, dc.sourceId)
        && Objects.equal(acceptedNameUsage, dc.acceptedNameUsage)
        && Objects.equal(acceptedNameUsageId, dc.acceptedNameUsageId)
        && Objects.equal(accessRights, dc.accessRights)
        && Objects.equal(associatedMedia, dc.associatedMedia)
        && Objects.equal(associatedOccurrences, dc.associatedOccurrences)
        && Objects.equal(associatedReferences, dc.associatedReferences)
        && Objects.equal(associatedSequences, dc.associatedSequences)
        && Objects.equal(associatedTaxa, dc.associatedTaxa)
        && Objects.equal(basisOfRecord, dc.basisOfRecord)
        && Objects.equal(bed, dc.bed)
        && Objects.equal(behavior, dc.behavior)
        && Objects.equal(bibliographicCitation, dc.bibliographicCitation)
        && Objects.equal(catalogNumber, dc.catalogNumber)
        && Objects.equal(collectionCode, dc.collectionCode)
        && Objects.equal(collectionID, dc.collectionID)
        && Objects.equal(continent, dc.continent)
        && Objects.equal(coordinatePrecision, dc.coordinatePrecision)
        && Objects.equal(coordinateUncertaintyInMeters,
            dc.coordinateUncertaintyInMeters)
        && Objects.equal(country, dc.country)
        && Objects.equal(countryCode, dc.countryCode)
        && Objects.equal(county, dc.county)
        && Objects.equal(dataGeneralizations, dc.dataGeneralizations)
        && Objects.equal(datasetID, dc.datasetID)
        && Objects.equal(datasetName, dc.datasetName)
        && Objects.equal(dateIdentified, dc.dateIdentified)
        && Objects.equal(day, dc.day)
        && Objects.equal(decimalLatitude, dc.decimalLatitude)
        && Objects.equal(decimalLongitude, dc.decimalLongitude)
        && Objects.equal(disposition, dc.disposition)
        && Objects.equal(dynamicProperties, dc.dynamicProperties)
        && Objects.equal(earliestAgeOrLowestStage, dc.earliestAgeOrLowestStage)
        && Objects.equal(earliestEonOrLowestEonothem,
            dc.earliestEonOrLowestEonothem)
        && Objects.equal(earliestEpochOrLowestSeries,
            dc.earliestEpochOrLowestSeries)
        && Objects.equal(earliestEraOrLowestErathem,
            dc.earliestEraOrLowestErathem)
        && Objects.equal(earliestPeriodOrLowestSystem,
            dc.earliestPeriodOrLowestSystem)
        && Objects.equal(endDayOfYear, dc.endDayOfYear)
        && Objects.equal(establishmentMeans, dc.establishmentMeans)
        && Objects.equal(eventDate, dc.eventDate)
        && Objects.equal(eventId, dc.eventId)
        && Objects.equal(eventRemarks, dc.eventRemarks)
        && Objects.equal(eventTime, dc.eventTime)
        && Objects.equal(family, dc.family)
        && Objects.equal(fieldNotes, dc.fieldNotes)
        && Objects.equal(fieldNumber, dc.fieldNumber)
        && Objects.equal(footprintSRS, dc.footprintSRS)
        && Objects.equal(footprintSpatialFit, dc.footprintSpatialFit)
        && Objects.equal(footprintWKT, dc.footprintWKT)
        && Objects.equal(formation, dc.formation)
        && Objects.equal(genus, dc.genus)
        && Objects.equal(geodeticDatum, dc.geodeticDatum)
        && Objects.equal(geologicalContextId, dc.geologicalContextId)
        && Objects.equal(georeferenceProtocol, dc.georeferenceProtocol)
        && Objects.equal(georeferenceRemarks, dc.georeferenceRemarks)
        && Objects.equal(georeferenceSources, dc.georeferenceSources)
        && Objects.equal(georeferenceVerificationStatus,
            dc.georeferenceVerificationStatus)
        && Objects.equal(georeferencedBy, dc.georeferencedBy)
        && Objects.equal(group, dc.group)
        && Objects.equal(habitat, dc.habitat)
        && Objects.equal(higherClassification, dc.higherClassification)
        && Objects.equal(higherGeography, dc.higherGeography)
        && Objects.equal(higherGeographyID, dc.higherGeographyID)
        && Objects.equal(highestBiostratigraphicZone,
            dc.highestBiostratigraphicZone)
        && Objects.equal(identificationID, dc.identificationID)
        && Objects.equal(identificationQualifier, dc.identificationQualifier)
        && Objects.equal(identificationReferences, dc.identificationReferences)
        && Objects.equal(identificationRemarks, dc.identificationRemarks)
        && Objects.equal(identifiedBy, dc.identifiedBy)
        && Objects.equal(individualCount, dc.individualCount)
        && Objects.equal(individualID, dc.individualID)
        && Objects.equal(informationWithheld, dc.informationWithheld)
        && Objects.equal(infraspecificEpithet, dc.infraspecificEpithet)
        && Objects.equal(institutionCode, dc.institutionCode)
        && Objects.equal(institutionId, dc.institutionId)
        && Objects.equal(island, dc.island)
        && Objects.equal(islandGroup, dc.islandGroup)
        && Objects.equal(kingdom, dc.kingdom)
        && Objects.equal(language, dc.language)
        && Objects.equal(latestAgeOrHighestStage, dc.latestAgeOrHighestStage)
        && Objects.equal(latestEonOrHighestEonothem,
            dc.latestEonOrHighestEonothem)
        && Objects.equal(latestEpochOrHighestSeries,
            dc.latestEpochOrHighestSeries)
        && Objects.equal(latestEraOrHighestErathem,
            dc.latestEraOrHighestErathem)
        && Objects.equal(latestPeriodOrHighestSystem,
            dc.latestPeriodOrHighestSystem)
        && Objects.equal(lifeStage, dc.lifeStage)
        && Objects.equal(lithostratigraphicTerms, dc.lithostratigraphicTerms)
        && Objects.equal(locality, dc.locality)
        && Objects.equal(locationAccordingTo, dc.locationAccordingTo)
        && Objects.equal(locationId, dc.locationId)
        && Objects.equal(locationRemarks, dc.locationRemarks)
        && Objects.equal(lowestBiostratigraphicZone,
            dc.lowestBiostratigraphicZone)
        && Objects.equal(maximumDepthInMeters, dc.maximumDepthInMeters)
        && Objects.equal(maximumDistanceAboveSurfaceInMeters,
            dc.maximumDistanceAboveSurfaceInMeters)
        && Objects.equal(maximumElevationInMeters, dc.maximumElevationInMeters)
        && Objects.equal(member, dc.member)
        && Objects.equal(minimumDepthInMeters, dc.minimumDepthInMeters)
        && Objects.equal(minimumDistanceAboveSurfaceInMeters,
            dc.minimumDistanceAboveSurfaceInMeters)
        && Objects.equal(minimumElevationInMeters, dc.minimumElevationInMeters)
        && Objects.equal(modified, dc.modified)
        && Objects.equal(month, dc.month)
        && Objects.equal(municipality, dc.municipality)
        && Objects.equal(nameAccordingTo, dc.nameAccordingTo)
        && Objects.equal(nameAccordingToId, dc.nameAccordingToId)
        && Objects.equal(namePublishedIn, dc.namePublishedIn)
        && Objects.equal(namePublishedInId, dc.namePublishedInId)
        && Objects.equal(nomenclaturalCode, dc.nomenclaturalCode)
        && Objects.equal(nomenclaturalStatus, dc.nomenclaturalStatus)
        && Objects.equal(occurrenceDetails, dc.occurrenceDetails)
        && Objects.equal(occurrenceId, dc.occurrenceId)
        && Objects.equal(occurrenceRemarks, dc.occurrenceRemarks)
        && Objects.equal(occurrenceStatus, dc.occurrenceStatus)
        && Objects.equal(order, dc.order)
        && Objects.equal(originalNameUsage, dc.originalNameUsage)
        && Objects.equal(originalNameUsageId, dc.originalNameUsageId)
        && Objects.equal(otherCatalogNumbers, dc.otherCatalogNumbers)
        && Objects.equal(ownerInstitutionCode, dc.ownerInstitutionCode)
        && Objects.equal(parentNameUsage, dc.parentNameUsage)
        && Objects.equal(parentNameUsageId, dc.parentNameUsageId)
        && Objects.equal(phylum, dc.phylum)
        && Objects.equal(pointRadiusSpatialFit, dc.pointRadiusSpatialFit)
        && Objects.equal(preparations, dc.preparations)
        && Objects.equal(previousIdentifications, dc.previousIdentifications)
        && Objects.equal(recordNumber, dc.recordNumber)
        && Objects.equal(recordedBy, dc.recordedBy)
        && Objects.equal(reproductiveCondition, dc.reproductiveCondition)
        && Objects.equal(rights, dc.rights)
        && Objects.equal(rightsHolder, dc.rightsHolder)
        && Objects.equal(samplingEffort, dc.samplingEffort)
        && Objects.equal(samplingProtocol, dc.samplingProtocol)
        && Objects.equal(scientificName, dc.scientificName)
        && Objects.equal(scientificNameAuthorship, dc.scientificNameAuthorship)
        && Objects.equal(scientificNameId, dc.scientificNameId)
        && Objects.equal(sex, dc.sex)
        && Objects.equal(specificEpithet, dc.specificEpithet)
        && Objects.equal(startDayOfYear, dc.startDayOfYear)
        && Objects.equal(stateProvince, dc.stateProvince)
        && Objects.equal(subgenus, dc.subgenus)
        && Objects.equal(taxonConceptId, dc.taxonConceptId)
        && Objects.equal(taxonID, dc.taxonID)
        && Objects.equal(taxonRank, dc.taxonRank)
        && Objects.equal(taxonRemarks, dc.taxonRemarks)
        && Objects.equal(taxonomicStatus, dc.taxonomicStatus)
        && Objects.equal(type, dc.type)
        && Objects.equal(typeStatus, dc.typeStatus)
        && Objects.equal(verbatimCoordinateSystem, dc.verbatimCoordinateSystem)
        && Objects.equal(verbatimCoordinates, dc.verbatimCoordinates)
        && Objects.equal(verbatimDepth, dc.verbatimDepth)
        && Objects.equal(verbatimElevation, dc.verbatimElevation)
        && Objects.equal(verbatimEventDate, dc.verbatimEventDate)
        && Objects.equal(verbatimLatitude, dc.verbatimLatitude)
        && Objects.equal(verbatimLocality, dc.verbatimLocality)
        && Objects.equal(verbatimLongitude, dc.verbatimLongitude)
        && Objects.equal(verbatimSRS, dc.verbatimSRS)
        && Objects.equal(verbatimTaxonRank, dc.verbatimTaxonRank)
        && Objects.equal(vernacularName, dc.vernacularName)
        && Objects.equal(waterbody, dc.waterbody)
        && Objects.equal(year, dc.year);
  }

  @Lob
  public String getAcceptedNameUsage() {
    return acceptedNameUsage;
  }

  @Lob
  public String getAcceptedNameUsageId() {
    return acceptedNameUsageId;
  }

  @Lob
  public String getAccessRights() {
    return accessRights;
  }

  @Lob
  public String getAssociatedMedia() {
    return associatedMedia;
  }

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

  @Lob
  public String getBed() {
    return bed;
  }

  // SampleEvent
  public String getBehavior() {
    return behavior;
  }

  @Lob
  public String getBibliographicCitation() {
    return bibliographicCitation;
  }

  @Column(length = 64)
  @Index(name = "idx_dwc_catalog_number")
  public String getCatalogNumber() {
    return catalogNumber;
  }

  @Column(length = 128)
  public String getClasss() {
    return classs;
  }

  @Index(name = "dwc_date_collected")
  public Date getCollected() {
    return collected;
  }

  @Column(length = 128)
  @Index(name = "idx_dwc_collection_code")
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
  @Index(name = "idx_dwc_country")
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

  @Lob
  public String getDataGeneralizations() {
    return dataGeneralizations;
  }

  @Column(length = 128)
  public String getDatasetID() {
    return datasetID;
  }

  @Lob
  public String getDatasetName() {
    return datasetName;
  }

  @Column(length = 64)
  public String getDateIdentified() {
    return dateIdentified;
  }

  public Date getDateModified() {
    return dateModified;
  }

  @Lob
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

  @Column(length = 128)
  public String getDisposition() {
    return disposition;
  }

  @Lob
  public String getDynamicProperties() {
    return dynamicProperties;
  }

  @Lob
  public String getEarliestAgeOrLowestStage() {
    return earliestAgeOrLowestStage;
  }

  @Lob
  public String getEarliestEonOrLowestEonothem() {
    return earliestEonOrLowestEonothem;
  }

  @Lob
  public String getEarliestEpochOrLowestSeries() {
    return earliestEpochOrLowestSeries;
  }

  @Lob
  public String getEarliestEraOrLowestErathem() {
    return earliestEraOrLowestErathem;
  }

  @Lob
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

  @Lob
  public String getEventDate() {
    return eventDate;
  }

  @Lob
  public String getEventId() {
    return eventId;
  }

  @Lob
  public String getEventRemarks() {
    return eventRemarks;
  }

  @Lob
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

  @Lob
  public String getFootprintSRS() {
    return footprintSRS;
  }

  public String getFootprintWKT() {
    return footprintWKT;
  }

  @Lob
  public String getFormation() {
    return formation;
  }

  @Column(length = 128)
  public String getGenus() {
    return genus;
  }

  @Column(length = 64)
  public String getGeodeticDatum() {
    return geodeticDatum;
  }

  @Transient
  public String getGeographyPath() {
    return geographicPath(this, RegionType.Locality);
  }

  @Transient
  public String getGeographyPath(RegionType type) {
    return geographicPath(this, type);
  }

  @Lob
  public String getGeologicalContextId() {
    return geologicalContextId;
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

  @Lob
  @Column(name = "group_")
  public String getGroup() {
    return group;
  }

  @Column(length = 128, unique = true)
  @Index(name = "guid")
  public String getGuid() {
    return guid;
  }

  public String getHabitat() {
    return habitat;
  }

  @Lob
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
    return StringUtils.trimToNull(propertyValue(this,
        StringUtils.capitalize(regionType.columnName)));
  }

  @Transient
  public String getHigherTaxonName(Rank rank) {
    Preconditions.checkNotNull(rank);
    return higherTaxonName(this, rank);
  }

  @Lob
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
  @Index(name = "idx_dwc_institution_code")
  public String getInstitutionCode() {
    return institutionCode;
  }

  @Lob
  public String getInstitutionId() {
    return institutionId;
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

  @Lob
  public String getLatestAgeOrHighestStage() {
    return latestAgeOrHighestStage;
  }

  @Lob
  public String getLatestEonOrHighestEonothem() {
    return latestEonOrHighestEonothem;
  }

  @Lob
  public String getLatestEpochOrHighestSeries() {
    return latestEpochOrHighestSeries;
  }

  @Lob
  public String getLatestEraOrHighestErathem() {
    return latestEraOrHighestErathem;
  }

  @Lob
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

  @Lob
  public String getLithostratigraphicTerms() {
    return lithostratigraphicTerms;
  }

  @Lob
  public String getLocality() {
    return locality;
  }

  @AttributeOverrides( {
      @AttributeOverride(name = "latitude", column = @Column(name = "lat")),
      @AttributeOverride(name = "longitude", column = @Column(name = "lon"))})
  public Point getLocation() {
    // to prevent NPE create new empty point in case it doesn't exist yet
    if (location == null) {
      location = new Point();
    }
    return location;
  }

  @Lob
  public String getLocationAccordingTo() {
    return locationAccordingTo;
  }

  @Lob
  public String getLocationId() {
    return locationId;
  }

  @Lob
  public String getLocationRemarks() {
    return locationRemarks;
  }

  @Transient
  public Double getLongitude() {
    return location.getLongitude();
  }

  @Lob
  public String getLowestBiostratigraphicZone() {
    return lowestBiostratigraphicZone;
  }

  @Column(length = 32)
  public String getMaximumDepthInMeters() {
    return maximumDepthInMeters;
  }

  @Lob
  public String getMaximumDistanceAboveSurfaceInMeters() {
    return maximumDistanceAboveSurfaceInMeters;
  }

  @Column(length = 32)
  public String getMaximumElevationInMeters() {
    return maximumElevationInMeters;
  }

  @Lob
  public String getMember() {
    return member;
  }

  @Column(length = 32)
  public String getMinimumDepthInMeters() {
    return minimumDepthInMeters;
  }

  @Lob
  public String getMinimumDistanceAboveSurfaceInMeters() {
    return minimumDistanceAboveSurfaceInMeters;
  }

  @Column(length = 32)
  public String getMinimumElevationInMeters() {
    return minimumElevationInMeters;
  }

  @Lob
  public String getModified() {
    return modified;
  }

  @Lob
  public String getMonth() {
    return month;
  }

  @Lob
  public String getMunicipality() {
    return municipality;
  }

  @Lob
  public String getNameAccordingTo() {
    return nameAccordingTo;
  }

  @Lob
  public String getNameAccordingToId() {
    return nameAccordingToId;
  }

  public String getNamePublishedIn() {
    return namePublishedIn;
  }

  @Lob
  public String getNamePublishedInId() {
    return namePublishedInId;
  }

  @Column(length = 64)
  public String getNomenclaturalCode() {
    return nomenclaturalCode;
  }

  @Column(length = 128)
  public String getNomenclaturalStatus() {
    return nomenclaturalStatus;
  }

  @Lob
  public String getOccurrenceDetails() {
    return occurrenceDetails;
  }

  @Lob
  public String getOccurrenceId() {
    return occurrenceId;
  }

  @Lob
  public String getOccurrenceRemarks() {
    return occurrenceRemarks;
  }

  @Lob
  public String getOccurrenceStatus() {
    return occurrenceStatus;
  }

  @Column(length = 128, name = "orderrr")
  public String getOrder() {
    return order;
  }

  @Lob
  public String getOriginalNameUsage() {
    return originalNameUsage;
  }

  @Lob
  public String getOriginalNameUsageId() {
    return originalNameUsageId;
  }

  public String getOtherCatalogNumbers() {
    return otherCatalogNumbers;
  }

  @Lob
  public String getOwnerInstitutionCode() {
    return ownerInstitutionCode;
  }

  @Lob
  public String getParentNameUsage() {
    return parentNameUsage;
  }

  @Lob
  public String getParentNameUsageId() {
    return parentNameUsageId;
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
    return propertyValue(this, property.getName());
  }

  @Lob
  public String getRecordedBy() {
    return recordedBy;
  }

  @Lob
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

  @Lob
  public String getSamplingEffort() {
    return samplingEffort;
  }

  public String getSamplingProtocol() {
    return samplingProtocol;
  }

  @Index(name = "idx_dwc_scientific_name")
  public String getScientificName() {
    return scientificName;
  }

  public String getScientificNameAuthorship() {
    return scientificNameAuthorship;
  }

  @Lob
  public String getScientificNameId() {
    return scientificNameId;
  }

  @Column(length = 128)
  public String getSex() {
    return sex;
  }

  @Column(length = 64)
  @Index(name = "dwc_source_id")
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

  @Lob
  public String getTaxonConceptId() {
    return taxonConceptId;
  }

  // Taxon => this.taxon
  @Column(length = 128)
  public String getTaxonID() {
    return taxonID;
  }

  @Column(length = 128)
  public String getTaxonomicStatus() {
    return taxonomicStatus;
  }

  @Transient
  public String getTaxonomyPath() {
    return taxonomyPath(this, Rank.TerminalTaxon);
  }

  @Transient
  public String getTaxonomyPath(Rank rank) {
    return taxonomyPath(this, rank);
  }

  @Column(length = 128)
  public String getTaxonRank() {
    return taxonRank;
  }

  @Lob
  public String getTaxonRemarks() {
    return taxonRemarks;
  }

  @Lob
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

  @Lob
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

  @Lob
  public String getVerbatimSRS() {
    return verbatimSRS;
  }

  @Lob
  public String getVerbatimTaxonRank() {
    return verbatimTaxonRank;
  }

  @Lob
  public String getVernacularName() {
    return vernacularName;
  }

  @Column(length = 128)
  public String getWaterbody() {
    return waterbody;
  }

  @Lob
  public String getYear() {
    return year;
  }

  /**
   * Calculates hashcode based on imported properties only and ignores all
   * secondary derived properties. Therefore id, deleted, lat/lon, modified,
   * created, region & taxon are ignored in the hashing.
   * 
   * Note: The hashcode value is calculated once and then cached.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (cachedHashCode == -1) {
      cachedHashCode = Objects.hashCode(guid, link, sourceId,
          acceptedNameUsage, acceptedNameUsageId, accessRights,
          associatedMedia, associatedOccurrences, associatedReferences,
          associatedSequences, associatedTaxa, basisOfRecord, bed, behavior,
          bibliographicCitation, catalogNumber, collectionCode, collectionID,
          continent, coordinatePrecision, coordinateUncertaintyInMeters,
          country, countryCode, county, dataGeneralizations, datasetID,
          datasetName, dateIdentified, day, decimalLatitude, decimalLongitude,
          disposition, dynamicProperties, earliestAgeOrLowestStage,
          earliestEonOrLowestEonothem, earliestEpochOrLowestSeries,
          earliestEraOrLowestErathem, earliestPeriodOrLowestSystem,
          endDayOfYear, establishmentMeans, eventDate, eventId, eventRemarks,
          eventTime, family, fieldNotes, fieldNumber, footprintSRS,
          footprintSpatialFit, footprintWKT, formation, genus, geodeticDatum,
          geologicalContextId, georeferenceProtocol, georeferenceRemarks,
          georeferenceSources, georeferenceVerificationStatus, georeferencedBy,
          group, habitat, higherClassification, higherGeography,
          higherGeographyID, highestBiostratigraphicZone, identificationID,
          identificationQualifier, identificationReferences,
          identificationRemarks, identifiedBy, individualCount, individualID,
          informationWithheld, infraspecificEpithet, institutionCode,
          institutionId, island, islandGroup, kingdom, language,
          latestAgeOrHighestStage, latestEonOrHighestEonothem,
          latestEpochOrHighestSeries, latestEraOrHighestErathem,
          latestPeriodOrHighestSystem, lifeStage, lithostratigraphicTerms,
          locality, locationAccordingTo, locationId, locationRemarks,
          lowestBiostratigraphicZone, maximumDepthInMeters,
          maximumDistanceAboveSurfaceInMeters, maximumElevationInMeters,
          member, minimumDepthInMeters, minimumDistanceAboveSurfaceInMeters,
          minimumElevationInMeters, modified, month, municipality,
          nameAccordingTo, nameAccordingToId, namePublishedIn,
          namePublishedInId, nomenclaturalCode, nomenclaturalStatus,
          occurrenceDetails, occurrenceId, occurrenceRemarks, occurrenceStatus,
          order, originalNameUsage, originalNameUsageId, otherCatalogNumbers,
          ownerInstitutionCode, parentNameUsage, parentNameUsageId, phylum,
          pointRadiusSpatialFit, preparations, previousIdentifications,
          recordNumber, recordedBy, reproductiveCondition, rights,
          rightsHolder, samplingEffort, samplingProtocol, scientificName,
          scientificNameAuthorship, scientificNameId, sex, specificEpithet,
          startDayOfYear, stateProvince, subgenus, taxonConceptId, taxonID,
          taxonRank, taxonRemarks, taxonomicStatus, type, typeStatus,
          verbatimCoordinateSystem, verbatimCoordinates, verbatimDepth,
          verbatimElevation, verbatimEventDate, verbatimLatitude,
          verbatimLocality, verbatimLongitude, verbatimSRS, verbatimTaxonRank,
          vernacularName, waterbody, year);
    }
    return cachedHashCode;
  }

  @Index(name = "deleted")
  public boolean isDeleted() {
    return isDeleted;
  }

  public void setAcceptedNameUsage(String acceptedNameUsage) {
    this.acceptedNameUsage = acceptedNameUsage;
  }

  public void setAcceptedNameUsageId(String acceptedNameUsageId) {
    this.acceptedNameUsageId = acceptedNameUsageId;
  }

  public void setAccessRights(String accessRights) {
    this.accessRights = accessRights;
  }

  public void setAssociatedMedia(String associatedMedia) {
    this.associatedMedia = associatedMedia;
  }

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

  public void setBibliographicCitation(String bibliographicCitation) {
    this.bibliographicCitation = bibliographicCitation;
  }

  public void setCatalogNumber(String catalogNumber) {
    this.catalogNumber = catalogNumber;
  }

  public void setClasss(String classs) {
    this.classs = classs;
  }

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

  public void setDateModified(Date modifiedDate) {
    this.dateModified = modifiedDate;
  }

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

  public void setEventDate(String eventDate) {
    this.eventDate = eventDate;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public void setEventRemarks(String eventRemarks) {
    this.eventRemarks = eventRemarks;
  }

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

  public void setGeologicalContextId(String geologicalContextId) {
    this.geologicalContextId = geologicalContextId;
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

  public void setInstitutionId(String institutionId) {
    this.institutionId = institutionId;
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

  public void setLocationId(String locationId) {
    this.locationId = locationId;
  }

  public void setLocationRemarks(String locationRemarks) {
    this.locationRemarks = locationRemarks;
  }

  public void setLowestBiostratigraphicZone(String lowestBiostratigraphicZone) {
    this.lowestBiostratigraphicZone = lowestBiostratigraphicZone;
  }

  public void setMaximumDepthInMeters(String maximumDepthInMeters) {
    this.maximumDepthInMeters = maximumDepthInMeters;
  }

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

  public void setMinimumDistanceAboveSurfaceInMeters(
      String minimumDistanceAboveSurfaceInMeters) {
    this.minimumDistanceAboveSurfaceInMeters = minimumDistanceAboveSurfaceInMeters;
  }

  public void setMinimumElevationInMeters(String minimumElevationInMeters) {
    this.minimumElevationInMeters = minimumElevationInMeters;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public void setMunicipality(String municipality) {
    this.municipality = municipality;
  }

  public void setNameAccordingTo(String nameAccordingTo) {
    this.nameAccordingTo = nameAccordingTo;
  }

  public void setNameAccordingToId(String nameAccordingToId) {
    this.nameAccordingToId = nameAccordingToId;
  }

  public void setNamePublishedIn(String namePublishedIn) {
    this.namePublishedIn = namePublishedIn;
  }

  public void setNamePublishedInId(String namePublishedInId) {
    this.namePublishedInId = namePublishedInId;
  }

  public void setNomenclaturalCode(String nomenclaturalCode) {
    this.nomenclaturalCode = nomenclaturalCode;
  }

  public void setNomenclaturalStatus(String nomenclaturalStatus) {
    this.nomenclaturalStatus = nomenclaturalStatus;
  }

  public void setOccurrenceDetails(String occurrenceDetails) {
    this.occurrenceDetails = occurrenceDetails;
  }

  public void setOccurrenceId(String occurrenceId) {
    this.occurrenceId = occurrenceId;
  }

  public void setOccurrenceRemarks(String occurrenceRemarks) {
    this.occurrenceRemarks = occurrenceRemarks;
  }

  public void setOccurrenceStatus(String occurrenceStatus) {
    this.occurrenceStatus = occurrenceStatus;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public void setOriginalNameUsage(String originalNameUsage) {
    this.originalNameUsage = originalNameUsage;
  }

  public void setOriginalNameUsageId(String originalNameUsageId) {
    this.originalNameUsageId = originalNameUsageId;
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

  public void setParentNameUsageId(String parentNameUsageId) {
    this.parentNameUsageId = parentNameUsageId;
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
    } catch (Exception e) {
      log.info(String.format("Unable to set property ", property.getName()));
    }
    return true;
  }

  public void setRecordedBy(String recordedBy) {
    this.recordedBy = recordedBy;
  }

  public void setRecordNumber(String recordNumber) {
    this.recordNumber = recordNumber;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public void setReproductiveCondition(String reproductiveCondition) {
    this.reproductiveCondition = reproductiveCondition;
  }

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

  public void setScientificNameId(String scientificNameId) {
    this.scientificNameId = scientificNameId;
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

  public void setTaxonConceptId(String taxonConceptId) {
    this.taxonConceptId = taxonConceptId;
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

  public void setVerbatimCoordinateSystem(String verbatimCoordinateSystem) {
    this.verbatimCoordinateSystem = verbatimCoordinateSystem;
  }

  public void setVerbatimDepth(String verbatimDepth) {
    this.verbatimDepth = verbatimDepth;
  }

  public void setVerbatimElevation(String verbatimElevation) {
    this.verbatimElevation = verbatimElevation;
  }

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

  public void setWaterbody(String waterbody) {
    this.waterbody = waterbody;
  }

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
        this.institutionCode).append("collectionCode", this.collectionCode).append(
        "catalogNumber", this.catalogNumber).append("country",
        this.getCountry()).append("guid", this.guid).toString();
  }
}
