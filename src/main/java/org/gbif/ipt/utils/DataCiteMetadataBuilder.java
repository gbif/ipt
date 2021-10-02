/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.doi.metadata.datacite.Affiliation;
import org.gbif.doi.metadata.datacite.Box;
import org.gbif.doi.metadata.datacite.ContributorType;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.metadata.datacite.DateType;
import org.gbif.doi.metadata.datacite.DescriptionType;
import org.gbif.doi.metadata.datacite.NameIdentifier;
import org.gbif.doi.metadata.datacite.ObjectFactory;
import org.gbif.doi.metadata.datacite.RelatedIdentifierType;
import org.gbif.doi.metadata.datacite.RelationType;
import org.gbif.doi.metadata.datacite.ResourceType;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.GeospatialCoverage;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.TemporalCoverageType;
import org.gbif.metadata.eml.UserId;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataCiteMetadataBuilder {

  public static final String DOI_IDENTIFIER_TYPE = "DOI";
  public static final String ORCID_NAME_IDENTIFIER_SCHEME = "ORCID";
  public static final String RESEARCHERID_NAME_IDENTIFIER_SCHEME = "ResearcherID";

  public static final String DWC_FORMAT_NAME = "Data as a Darwin Core Archive file";
  public static final String EML_FORMAT_NAME = "Metadata as an EML file";
  public static final String RTF_FORMAT_NAME = "Metadata as an RTF file";

  public static final String ALTERNATE_IDENTIFIER_TYPE_TEXT = "Another identifier for this resource";

  public static final String CONTACT_ROLE = "pointOfContact";
  public static final String METADATA_PROVIDER_ROLE = "metadataProvider";

  public static final String HTTP_PROTOCOL = "http";
  public static final String RECORDS_NAME = "records";


  private static final Logger LOG = LogManager.getLogger(DataCiteMetadataBuilder.class);
  private static final ObjectFactory FACTORY = new ObjectFactory();
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  public static DataCiteMetadata createDataCiteMetadata(DOI doi, Resource resource) throws InvalidMetadataException {

    DataCiteMetadata dataCiteMetadata = FACTORY.createDataCiteMetadata();

    // add DOI (mandatory)
    DataCiteMetadata.Identifier identifier = getDOIIdentifier(doi);
    dataCiteMetadata.setIdentifier(identifier);

    // resource metadata (Eml)
    Eml eml = resource.getEml();

    // add list of titles (mandatory)
    DataCiteMetadata.Titles titles = convertEmlTitles(eml);
    dataCiteMetadata.setTitles(titles);

    // add list of creators (mandatory)
    DataCiteMetadata.Creators creators = convertEmlCreators(eml.getCreators());
    dataCiteMetadata.setCreators(creators);

    // publisher (mandatory)
    final DataCiteMetadata.Publisher publisher = FACTORY.createDataCiteMetadataPublisher();
    publisher.setValue(getPublisher(resource));
    dataCiteMetadata.setPublisher(publisher);

    // publication year (mandatory)
    String publicationYear = getPublicationYear(eml);
    dataCiteMetadata.setPublicationYear(publicationYear);

    // add resourceType (mandatory)
    DataCiteMetadata.ResourceType resourceType = getResourceType(resource);
    dataCiteMetadata.setResourceType(resourceType);

    // version (optional according to DataCite, mandatory and thus never null according to IPT)
    dataCiteMetadata.setVersion(resource.getEmlVersion().toPlainString());

    // add list of contributors (recommended)
    List<Agent> emlContributors =
      prepareContributorsFromEmlAgents(eml.getContacts(), eml.getMetadataProviders(), eml.getAssociatedParties());
    DataCiteMetadata.Contributors contributors = convertEmlContributors(emlContributors);
    dataCiteMetadata.setContributors(contributors);

    // add list of subjects (recommended)
    DataCiteMetadata.Subjects subjects = convertEmlKeywords(eml.getKeywords(), eml.getMetadataLanguage());
    dataCiteMetadata.setSubjects(subjects);

    // add list of Dates (recommended)
    DataCiteMetadata.Dates dates = convertEmlDates(eml.getDateStamp(), eml.getTemporalCoverages());
    dataCiteMetadata.setDates(dates);

    // add language (optional)
    dataCiteMetadata.setLanguage(eml.getLanguage());

    // add list of alternate identifier (optional)
    DataCiteMetadata.AlternateIdentifiers alternateIdentifiers =
      convertEmlAlternateIdentifiers(eml.getAlternateIdentifiers());
    dataCiteMetadata.setAlternateIdentifiers(alternateIdentifiers);

    // add list of related identifiers stemming from bibliographic citations
    DataCiteMetadata.RelatedIdentifiers ridsBibliographicCitations =
      convertRelatedIdentifiers(eml.getBibliographicCitations(), eml.getPhysicalData());
    dataCiteMetadata.setRelatedIdentifiers(ridsBibliographicCitations);

    // add formats (optional)
    DataCiteMetadata.Formats formats = getFormats(resource);
    dataCiteMetadata.setFormats(formats);

    // add size (optional)
    DataCiteMetadata.Sizes sizes = getSizes(resource);
    dataCiteMetadata.setSizes(sizes);

    // add rights (optional)
    DataCiteMetadata.RightsList rights = getRightsList(eml);
    dataCiteMetadata.setRightsList(rights);

    // add resource description (recommended)
    DataCiteMetadata.Descriptions descriptions = getDescriptions(eml);
    dataCiteMetadata.setDescriptions(descriptions);

    // add geo box (recommended)
    DataCiteMetadata.GeoLocations geoLocations = getGeoLocations(eml.getGeospatialCoverages());
    dataCiteMetadata.setGeoLocations(geoLocations);

    return dataCiteMetadata;
  }

  /**
   * Retrieve the DOI identifier assigned to the resource. DataCite metadata schema (v4) requires the identifier.
   *
   * @param doi DOI identifier assigned to resource
   *
   * @return DataCite identifier of type DOI
   */
  protected static DataCiteMetadata.Identifier getDOIIdentifier(@NotNull DOI doi) {
    DataCiteMetadata.Identifier identifier = FACTORY.createDataCiteMetadataIdentifier();
    identifier.setValue(doi.getDoiName());
    identifier.setIdentifierType(DOI_IDENTIFIER_TYPE);
    return identifier;
  }

  /**
   * Retrieve the DataCite resource Formats, different depending on whether resource is metadata only or has published
   * data.
   *
   * @return DataCite Formats
   */
  protected static DataCiteMetadata.Formats getFormats(Resource resource) {
    DataCiteMetadata.Formats formats = FACTORY.createDataCiteMetadataFormats();
    if (resource.getCoreType() != null && resource.getCoreType()
      .equalsIgnoreCase(Resource.CoreRowType.METADATA.toString())) {
      formats.getFormat().add(EML_FORMAT_NAME);
      formats.getFormat().add(RTF_FORMAT_NAME);
    } else {
      formats.getFormat().add(DWC_FORMAT_NAME);
      formats.getFormat().add(EML_FORMAT_NAME);
      formats.getFormat().add(RTF_FORMAT_NAME);
    }
    return formats;
  }

  /**
   * Retrieve the DataCite resource sizes - # of records only for now.
   *
   * @return DataCite resource Sizes
   */
  protected static DataCiteMetadata.Sizes getSizes(Resource resource) {
    DataCiteMetadata.Sizes sizes = FACTORY.createDataCiteMetadataSizes();
    // # of records published
    if (resource.hasPublishedData()) {
      sizes.getSize().add(resource.getRecordsPublished() + " " + RECORDS_NAME);
    }
    return sizes;
  }

  /**
   * Convert the Eml description into DataCite Description
   *
   * @return DataCite Description
   */
  protected static DataCiteMetadata.Descriptions getDescriptions(Eml eml) {
    DataCiteMetadata.Descriptions descriptions = FACTORY.createDataCiteMetadataDescriptions();
    if (!eml.getDescription().isEmpty()) {
      for (String para : eml.getDescription()) {
        if (StringUtils.isNotBlank(para)) {
          DataCiteMetadata.Descriptions.Description description = FACTORY.createDataCiteMetadataDescriptionsDescription();
          description.setDescriptionType(DescriptionType.ABSTRACT);
          description.setLang(eml.getMetadataLanguage());
          description.getContent().add(para);
          descriptions.getDescription().add(description);
        }
      }
    }
    return descriptions;
  }

  /**
   * Convert the Eml GeospatialCoverage into DataCite GeoLocations, using the bounding box and description only.
   *
   * @return DataCite GeoLocations
   */
  protected static DataCiteMetadata.GeoLocations getGeoLocations(List<GeospatialCoverage> geospatialCoverages) {
    DataCiteMetadata.GeoLocations geoLocations = FACTORY.createDataCiteMetadataGeoLocations();
    for (GeospatialCoverage coverage : geospatialCoverages) {
      if (StringUtils.isNotBlank(coverage.getDescription())) {
        DataCiteMetadata.GeoLocations.GeoLocation geoLocation = FACTORY.createDataCiteMetadataGeoLocationsGeoLocation();

        geoLocation.getGeoLocationPlaceOrGeoLocationPointOrGeoLocationBox()
            .add(coverage.getDescription());
        if (coverage.getBoundingCoordinates().isValid()) {
          Box box = FACTORY.createBox();
          // min is south and west, max is north and east
          box.setWestBoundLongitude(coverage.getBoundingCoordinates().getMin().getLongitude().floatValue());
          box.setEastBoundLongitude(coverage.getBoundingCoordinates().getMax().getLongitude().floatValue());
          box.setSouthBoundLatitude(coverage.getBoundingCoordinates().getMin().getLatitude().floatValue());
          box.setNorthBoundLatitude(coverage.getBoundingCoordinates().getMax().getLatitude().floatValue());
          geoLocation.getGeoLocationPlaceOrGeoLocationPointOrGeoLocationBox()
              .add(box);
        }
        geoLocations.getGeoLocation().add(geoLocation);
      }
    }
    return geoLocations;
  }

  /**
   * Convert Eml intellectualRights into DataCite RightsList using only the license title and license URL.
   *
   * @return DataCite RightsList
   */
  protected static DataCiteMetadata.RightsList getRightsList(Eml eml) {
    DataCiteMetadata.RightsList rightsList = FACTORY.createDataCiteMetadataRightsList();
    if (StringUtils.isNotBlank(eml.parseLicenseUrl()) && StringUtils.isNotBlank(eml.parseLicenseTitle())) {
      DataCiteMetadata.RightsList.Rights rights = FACTORY.createDataCiteMetadataRightsListRights();
      rights.setValue(eml.parseLicenseTitle());
      rights.setRightsURI(eml.parseLicenseUrl());
      rightsList.getRights().add(rights);
    }
    return rightsList;
  }

  /**
   * Convert list of EML alternateIdentifiers into DataCite alternateIdentifiers.
   *
   * @param alternateIdentifiers Eml alternateIdentifier list
   *
   * @return list of DataCite alternativeIdentifier
   */
  protected static DataCiteMetadata.AlternateIdentifiers convertEmlAlternateIdentifiers(
    List<String> alternateIdentifiers) {
    DataCiteMetadata.AlternateIdentifiers alternates = FACTORY.createDataCiteMetadataAlternateIdentifiers();
    for (String alternateIdentifier : alternateIdentifiers) {
      DataCiteMetadata.AlternateIdentifiers.AlternateIdentifier alternate =
        FACTORY.createDataCiteMetadataAlternateIdentifiersAlternateIdentifier();
      alternate.setValue(alternateIdentifier);
      alternate.setAlternateIdentifierType(ALTERNATE_IDENTIFIER_TYPE_TEXT);
      alternates.getAlternateIdentifier().add(alternate);
    }
    return alternates;
  }

  /**
   * Convert list of EML bibliographicCitations, and list of EML PhysicalData, into DataCite relatedIdentifiers.
   *
   * @param bibliographicCitations Eml bibliographicCitations list
   *
   * @return list of DataCite relatedIdentifiers
   */
  protected static DataCiteMetadata.RelatedIdentifiers convertRelatedIdentifiers(List<Citation> bibliographicCitations,
    List<PhysicalData> physicalDatas) {
    DataCiteMetadata.RelatedIdentifiers rids = FACTORY.createDataCiteMetadataRelatedIdentifiers();
    // from bibliographic citations
    for (Citation citation : bibliographicCitations) {
      if (StringUtils.isNotBlank(citation.getIdentifier())) {
        DataCiteMetadata.RelatedIdentifiers.RelatedIdentifier rid =
          FACTORY.createDataCiteMetadataRelatedIdentifiersRelatedIdentifier();
        rid.setValue(citation.getIdentifier());
        rid.setRelationType(RelationType.REFERENCES);
        rid.setRelatedIdentifierType(RelatedIdentifierType.URL);
        rids.getRelatedIdentifier().add(rid);
      }
    }
    // from PhysicalData
    for (PhysicalData data : physicalDatas) {
      if (StringUtils.isNotBlank(data.getDistributionUrl())) {
        if (data.getDistributionUrl().startsWith(HTTP_PROTOCOL)) {
          DataCiteMetadata.RelatedIdentifiers.RelatedIdentifier rid =
            FACTORY.createDataCiteMetadataRelatedIdentifiersRelatedIdentifier();
          try {
            URI val = new URI(data.getDistributionUrl());
            rid.setValue(val.toString());
            rid.setRelatedIdentifierType(RelatedIdentifierType.URL);
            rid.setRelationType(RelationType.IS_VARIANT_FORM_OF);
            rids.getRelatedIdentifier().add(rid);
          } catch (URISyntaxException e) {
            LOG.error("Failed to convert distributionUrl into URI: " + data.getDistributionUrl());
          }
        }
      }
    }
    return rids;
  }

  /**
   * Retrieve the ResourceType, using the formula Dataset/Resource Type=Resource Core Type.
   * DataCite schema (v4) requires the resource type.
   *
   * @param resource resource
   *
   * @return DataCite ResourceType
   *
   * @throws InvalidMetadataException if mandatory resource type cannot be retrieved
   */
  protected static DataCiteMetadata.ResourceType getResourceType(Resource resource) throws InvalidMetadataException {
    DataCiteMetadata.ResourceType resourceType = FACTORY.createDataCiteMetadataResourceType();
    resourceType.setResourceTypeGeneral(ResourceType.DATASET);
    if (resource.getCoreType() != null) {
      resourceType.setValue(StringUtils.capitalize(resource.getCoreType().toLowerCase()));
      return resourceType;
    } else {
      throw new InvalidMetadataException("DataCite schema (v4) requires a resource type");
    }
  }

  /**
   * Retrieve the publisher from the IPT resource, equal to the name of the publishing organisation. DataCite schema
   * (v4) requires the publisher. This method ensures that the default organisation "No organisation" cannot be used.
   *
   * @param resource IPT resource
   *
   * @return the publisher name
   *
   * @throws InvalidMetadataException if mandatory publisher cannot be retrieved
   */
  protected static String getPublisher(Resource resource) throws InvalidMetadataException {
    if (resource.getOrganisation() != null && StringUtils.isNotBlank(resource.getOrganisation().getName())
        && !resource.getOrganisation().getKey().equals(Constants.DEFAULT_ORG_KEY)) {
      return resource.getOrganisation().getName();
    } else {
      throw new InvalidMetadataException("DataCite schema (v4) requires a publishing organisation");
    }
  }

  /**
   * Retrieve the publication year from Eml dateStamp, which stores the date the resource was created. DataCite schema
   * (v4) requires the publication year.
   *
   * @param eml EML
   *
   * @return the publication year
   *
   * @throws InvalidMetadataException if mandatory publication year cannot be retrieved
   */
  protected static String getPublicationYear(Eml eml) throws InvalidMetadataException {
    if (eml.getDateStamp() != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(eml.getDateStamp());
      return String.valueOf(cal.get(Calendar.YEAR));
    } else {
      throw new InvalidMetadataException("DataCite schema (v4) requires the publication year");
    }
  }

  /**
   * Convert list of EML creators into DataCite creators. DataCite metadata schema (v4.0) requires at least one
   * creator. The DataCite metadata schema allows the creator to "be a corporate/institutional or personal name."
   *
   * @param agents EML agents list
   *
   * @return DataCite creators list
   *
   * @throws org.gbif.doi.service.InvalidMetadataException if mandatory number of creators cannot be created/returned
   */
  protected static DataCiteMetadata.Creators convertEmlCreators(List<Agent> agents) throws InvalidMetadataException {
    DataCiteMetadata.Creators creators = FACTORY.createDataCiteMetadataCreators();
    if (!agents.isEmpty()) {
      for (Agent agent : agents) {
        DataCiteMetadata.Creators.Creator creator = FACTORY.createDataCiteMetadataCreatorsCreator();
        // name is mandatory, in order of priority:
        // 1. try agent name
        if (StringUtils.isNotBlank(agent.getFullName())) {
          final DataCiteMetadata.Creators.Creator.CreatorName creatorName = FACTORY.createDataCiteMetadataCreatorsCreatorCreatorName();
          creatorName.setValue(agent.getFullName());
          creator.setCreatorName(creatorName);
          // identifier is optional, however, there can only be one and the name of the identifier scheme is mandatory
          if (!agent.getUserIds().isEmpty()) {
            for (UserId userId : agent.getUserIds()) {
              NameIdentifier nid = convertEmlUserIdIntoCreatorNameIdentifier(userId);
              if (nid != null) {
                creator.getNameIdentifier().add(nid);
                break;
              }
            }
          }
          // affiliation is optional
          if (StringUtils.isNotBlank(agent.getOrganisation())) {
            final Affiliation affiliation = FACTORY.createAffiliation();
            affiliation.setValue(agent.getOrganisation());
            creator.getAffiliation().add(affiliation);
          }
        }
        // 2. try organisation name
        else if (StringUtils.isNotBlank(agent.getOrganisation())) {
          final DataCiteMetadata.Creators.Creator.CreatorName creatorName = FACTORY.createDataCiteMetadataCreatorsCreatorCreatorName();
          creatorName.setValue(agent.getOrganisation());
          creator.setCreatorName(creatorName);
        }
        // otherwise if no name, organisation name, or position name found, throw exception
        else {
          throw new InvalidMetadataException(
            "DataCite schema (v4) requires creator have a name! Creator can be an organisation or person. Check creator/agent: "
            + agent);
        }
        // add to list
        creators.getCreator().add(creator);
      }
      return creators;
    } else {
      throw new InvalidMetadataException("DataCite schema (v4) requires at least one creator");
    }
  }

  /**
   * DataCite contributors are created from the combined set of EML contact, metadataProvider and associatedParty
   * agents. MetadataProvider and Contact Agents don't have a role, so set. The role is converted to DataCite
   * ContributorType in later processing.
   *
   * @param metadataProviders EML metadataProviders list
   * @param contacts          EML contacts list
   * @param associatedParties EML associatedParties list
   *
   * @return list of agents converted into DataCite contributors
   */
  private static List<Agent> prepareContributorsFromEmlAgents(List<Agent> contacts, List<Agent> metadataProviders,
    List<Agent> associatedParties) {
    List<Agent> ls = new ArrayList<>();

    // add type to contacts
    for (Agent contact : contacts) {
      contact.setRole(CONTACT_ROLE);
    }
    ls.addAll(contacts);

    // add type to metadataProviders
    for (Agent metadataProvider : metadataProviders) {
      metadataProvider.setRole(METADATA_PROVIDER_ROLE);
    }
    ls.addAll(metadataProviders);

    // add associatedParties as they are
    ls.addAll(associatedParties);
    return ls;
  }

  /**
   * Convert list of EML contacts, metadataProviders, and associatedParties into DataCite contributors.
   * DataCite metadata schema (v4.0) does not require contributors, they are recommended though.
   *
   * @param agents EML agents list, composed from EML contacts, metadataProviders, and associatedParties
   *
   * @return DataCite contributors list
   *
   * @throws org.gbif.doi.service.InvalidMetadataException if name or type cannot be set
   */
  protected static DataCiteMetadata.Contributors convertEmlContributors(List<Agent> agents)
    throws InvalidMetadataException {
    DataCiteMetadata.Contributors contributors = FACTORY.createDataCiteMetadataContributors();
    for (Agent agent : agents) {
      DataCiteMetadata.Contributors.Contributor contributor = FACTORY.createDataCiteMetadataContributorsContributor();
      // name is mandatory, in order of priority:
      // 1. try agent name
      if (StringUtils.isNotBlank(agent.getFullName())) {
        final DataCiteMetadata.Contributors.Contributor.ContributorName contributorName = FACTORY.createDataCiteMetadataContributorsContributorContributorName();
        contributorName.setValue(agent.getFullName());
        contributor.setContributorName(contributorName);
        // identifier is optional, however, there can only be one and the name of the identifier scheme is mandatory
        if (!agent.getUserIds().isEmpty()) {
          for (UserId userId : agent.getUserIds()) {
            NameIdentifier nid = convertEmlUserIdIntoContributorNameIdentifier(userId);
            if (nid != null) {
              contributor.getNameIdentifier().add(nid);
              break;
            }
          }
        }
        // affiliation is optional
        if (StringUtils.isNotBlank(agent.getOrganisation())) {
          final Affiliation affiliation = FACTORY.createAffiliation();
          affiliation.setValue(agent.getOrganisation());
          contributor.getAffiliation().add(affiliation);
        }
      }
      // 2. try organisation name
      else if (StringUtils.isNotBlank(agent.getOrganisation())) {
        final DataCiteMetadata.Contributors.Contributor.ContributorName contributorName = FACTORY.createDataCiteMetadataContributorsContributorContributorName();
        contributorName.setValue(agent.getOrganisation());
        contributor.setContributorName(contributorName);
      }
      // 3. try position name
      else if (StringUtils.isNotBlank(agent.getPosition())) {
        final DataCiteMetadata.Contributors.Contributor.ContributorName contributorName = FACTORY.createDataCiteMetadataContributorsContributorContributorName();
        contributorName.setValue(agent.getPosition());
        contributor.setContributorName(contributorName);
        // affiliation is optional
        if (StringUtils.isNotBlank(agent.getOrganisation())) {
          final Affiliation affiliation = FACTORY.createAffiliation();
          affiliation.setValue(agent.getOrganisation());
          contributor.getAffiliation().add(affiliation);
        }
      }
      // otherwise if no name, organisation name, or position name found, throw exception
      else {
        throw new InvalidMetadataException(
          "DataCite schema (v4) requires contributor have a name! Check contributor/agent: " + agent);
      }

      // contributorType is mandatory, if not found throw exception
      String role = agent.getRole();
      if (StringUtils.isBlank(role)) {
        throw new InvalidMetadataException(
          "DataCite schema (v4) requires contributor have a type! Check contributor/agent: " + agent);
      }

      // contributor type, defaulting to RELATED_PERSON if no suitable match exists
      ContributorType type;
      if (role.equalsIgnoreCase("editor") || role.equalsIgnoreCase("publisher")) {
        type = ContributorType.EDITOR;
      } else if (role.equalsIgnoreCase("contentProvider")) {
        type = ContributorType.DATA_COLLECTOR;
      } else if (role.equalsIgnoreCase("custodianSteward")) {
        type = ContributorType.DATA_MANAGER;
      } else if (role.equalsIgnoreCase("curator") || role.equalsIgnoreCase(METADATA_PROVIDER_ROLE)) {
        type = ContributorType.DATA_CURATOR;
      } else if (role.equalsIgnoreCase("distributor")) {
        type = ContributorType.DISTRIBUTOR;
      } else if (role.equalsIgnoreCase("owner")) {
        type = ContributorType.RIGHTS_HOLDER;
      } else if (role.equalsIgnoreCase(CONTACT_ROLE)) {
        type = ContributorType.CONTACT_PERSON;
      } else if (role.equalsIgnoreCase("originator")) {
        type = ContributorType.DATA_COLLECTOR;
      } else if (role.equalsIgnoreCase("principalInvestigator")) {
        type = ContributorType.PROJECT_LEADER;
      } else if (role.equalsIgnoreCase("processor") || role.equalsIgnoreCase("publisher") || role
        .equalsIgnoreCase("programmer")) {
        type = ContributorType.PRODUCER;
      } else {
        type = ContributorType.RELATED_PERSON;
      }
      contributor.setContributorType(type);

      // add to list
      contributors.getContributor().add(contributor);
    }
    return contributors;
  }

  /**
   * Convert list of EML titles into DataCite titles. Only the title is mandatory, the type is optional.
   * EML version 1.1 only contains a single title, with language derived from metadata language. DataCite metadata
   * schema (v4) requires at least one title.
   *
   * @param eml Eml
   *
   * @return DataCite titles list
   *
   * @throws org.gbif.doi.service.InvalidMetadataException if mandatory number of titles cannot be created/returned
   */
  protected static DataCiteMetadata.Titles convertEmlTitles(Eml eml) throws InvalidMetadataException {
    if (StringUtils.isNotBlank(eml.getTitle())) {
      DataCiteMetadata.Titles titles = FACTORY.createDataCiteMetadataTitles();
      DataCiteMetadata.Titles.Title primary = FACTORY.createDataCiteMetadataTitlesTitle();
      primary.setValue(eml.getTitle());
      primary.setLang(eml.getMetadataLanguage());
      titles.getTitle().add(primary);
      return titles;
    } else {
      throw new InvalidMetadataException("DataCite schema (v4) requires at least one title");
    }
  }

  /**
   * Convert list of EML KeywordSet into DataCite subjects. Only the value is mandatory, the schemeURI and
   * subjectScheme are optional.
   *
   * @param keywordSets Eml list of KeywordSet
   * @param language    Eml metadata language (3-letter ISO country code)
   *
   * @return DataCite subjects list
   *
   * @throws org.gbif.doi.service.InvalidMetadataException if mandatory number of titles cannot be created/returned
   */
  protected static DataCiteMetadata.Subjects convertEmlKeywords(List<KeywordSet> keywordSets, String language)
    throws InvalidMetadataException {
    DataCiteMetadata.Subjects subjects = FACTORY.createDataCiteMetadataSubjects();
    for (KeywordSet keywordSet : keywordSets) {
      for (String keyword : keywordSet.getKeywords()) {
        DataCiteMetadata.Subjects.Subject subject = FACTORY.createDataCiteMetadataSubjectsSubject();
        subject.setValue(keyword);
        if (StringUtils.isNotBlank(language)) {
          subject.setLang(language);
        }
        subject.setLang(language);
        // keyword thesaurus is either schemeURI or subjectScheme
        String thesaurus = keywordSet.getKeywordThesaurus();
        if (StringUtils.isNotBlank(thesaurus)) {
          if (thesaurus.startsWith(HTTP_PROTOCOL)) {
            try {
              URI schemeUri = new URI(keywordSet.getKeywordThesaurus());
              subject.setSchemeURI(schemeUri.toString());
            } catch (URISyntaxException e) {
              LOG.debug("Could not convert keyword thesaurus to URI: " + keywordSet.getKeywordThesaurus());
            }
          } else {
            subject.setSubjectScheme(thesaurus);
          }
        }
        subjects.getSubject().add(subject);
      }
    }
    return subjects;
  }

  /**
   * Convert list of EML TemporalCoverage into DataCite Dates. Only single date and date ranges can be converted.
   * Free text time periods like living time period cannot be stored in DataCite.
   * Also create DataCite CREATED and UPDATED Dates too.
   *
   * @param coverages Eml list of TemporalCoverage
   *
   * @return DataCite Dates list
   */
  protected static DataCiteMetadata.Dates convertEmlDates(Date created, List<TemporalCoverage> coverages) {
    DataCiteMetadata.Dates dates = FACTORY.createDataCiteMetadataDates();
    // created date
    if (created != null) {
      DataCiteMetadata.Dates.Date createdDate = FACTORY.createDataCiteMetadataDatesDate();
      createdDate.setValue(DATE_FORMAT.format(created));
      createdDate.setDateType(DateType.CREATED);
      dates.getDate().add(createdDate);
    }

    // updated date = now
    DataCiteMetadata.Dates.Date updatedDate = FACTORY.createDataCiteMetadataDatesDate();
    updatedDate.setValue(DATE_FORMAT.format(new Date()));
    updatedDate.setDateType(DateType.UPDATED);
    dates.getDate().add(updatedDate);

    // single date and date range temporal coverages
    for (TemporalCoverage coverage : coverages) {
      if (coverage.getType().equals(TemporalCoverageType.SINGLE_DATE)) {
        DataCiteMetadata.Dates.Date singleDate = FACTORY.createDataCiteMetadataDatesDate();
        singleDate.setValue(DATE_FORMAT.format(coverage.getStartDate()));
        singleDate.setDateType(DateType.VALID);
        dates.getDate().add(singleDate);
      } else if (coverage.getType().equals(TemporalCoverageType.DATE_RANGE)) {
        // construct range using RKMS-ISO8601 standard e.g. 2004-03-02/2005-06-02
        DataCiteMetadata.Dates.Date range = FACTORY.createDataCiteMetadataDatesDate();
        String start = DATE_FORMAT.format(coverage.getStartDate());
        String end = DATE_FORMAT.format(coverage.getEndDate());
        range.setValue(start + "/" + end);
        range.setDateType(DateType.VALID);
        dates.getDate().add(range);
      }
    }
    return dates;
  }

  private String convertDateToYearMonthDay(Date date) {

    return DATE_FORMAT.format(date);
  }

  /**
   * Convert a Eml UserId object into a DataCite NameIdentifier object. DataCite metadata schema (v4) requires the
   * scheme name if the identifier is used. Unfortunately the scheme name cannot be reliably and accurately derived
   * from the schemeURI (UserId.directory) therefore this method only supports the following schemes: ORCID and
   * ResearcherID.
   *
   * @param userId Eml UserId object
   *
   * @return DataCite NameIdentifier object or null if none could be created (e.g. because directory wasn't recognized)
   */
  protected static NameIdentifier convertEmlUserIdIntoCreatorNameIdentifier(
    UserId userId) {
    if (StringUtils.isNotBlank(userId.getIdentifier()) && StringUtils.isNotBlank(userId.getDirectory())) {
      String directory = StringUtils.trimToEmpty(userId.getDirectory()).toLowerCase();
      if (directory.contains(ORCID_NAME_IDENTIFIER_SCHEME.toLowerCase()) || directory
        .contains(RESEARCHERID_NAME_IDENTIFIER_SCHEME.toLowerCase())) {
        NameIdentifier nid = FACTORY.createNameIdentifier();
        nid.setValue(userId.getIdentifier());
        nid.setSchemeURI(userId.getDirectory());
        nid.setNameIdentifierScheme(
          (directory.contains(ORCID_NAME_IDENTIFIER_SCHEME.toLowerCase())) ? ORCID_NAME_IDENTIFIER_SCHEME
            : RESEARCHERID_NAME_IDENTIFIER_SCHEME);
        return nid;
      } else {
        LOG.debug("UserId has unrecognized directory (" + directory + "), only ORCID and ResearcherID are supported");
        return null;
      }
    }
    return null;
  }

  /**
   * Convert a Eml UserId object into a DataCite NameIdentifier object. DataCite metadata schema (v4) requires the
   * scheme name if the identifier is used. Unfortunately the scheme name cannot be reliably and accurately derived
   * from the schemeURI (UserId.directory) therefore this method only supports the following schemes: ORCID and
   * ResearcherID.
   *
   * @param userId Eml UserId object
   *
   * @return DataCite NameIdentifier object or null if none could be created (e.g. because directory wasn't recognized)
   */
  protected static NameIdentifier convertEmlUserIdIntoContributorNameIdentifier(
    UserId userId) {
    if (StringUtils.isNotBlank(userId.getIdentifier()) && StringUtils.isNotBlank(userId.getDirectory())) {
      String directory = StringUtils.trimToEmpty(userId.getDirectory()).toLowerCase();
      if (directory.contains(ORCID_NAME_IDENTIFIER_SCHEME.toLowerCase()) || directory
        .contains(RESEARCHERID_NAME_IDENTIFIER_SCHEME.toLowerCase())) {
        NameIdentifier nid = FACTORY.createNameIdentifier();
        nid.setValue(userId.getIdentifier());
        nid.setSchemeURI(userId.getDirectory());
        nid.setNameIdentifierScheme(
          (directory.contains(ORCID_NAME_IDENTIFIER_SCHEME.toLowerCase())) ? ORCID_NAME_IDENTIFIER_SCHEME
            : RESEARCHERID_NAME_IDENTIFIER_SCHEME);
        return nid;
      } else {
        LOG.debug("UserId has unrecognized directory (" + directory + "), only ORCID and ResearcherID are supported");
        return null;
      }
    }
    return null;
  }

  /**
   * Add RelatedIdentifier describing the DOI of the resource being replaced by the resource being registered.
   *
   * @param replaced DOI identifier of resource being replaced
   */
  public static void addIsNewVersionOfDOIRelatedIdentifier(@NotNull DataCiteMetadata metadata, @NotNull DOI replaced)
    throws InvalidMetadataException {
    DataCiteMetadata.RelatedIdentifiers.RelatedIdentifier rid =
      FACTORY.createDataCiteMetadataRelatedIdentifiersRelatedIdentifier();
    rid.setRelatedIdentifierType(RelatedIdentifierType.DOI);
    rid.setValue(replaced.getDoiName());
    rid.setRelationType(RelationType.IS_NEW_VERSION_OF);
    metadata.getRelatedIdentifiers().getRelatedIdentifier().add(rid);
  }

  /**
   * Add RelatedIdentifier describing the DOI of the resource replacing the resource being registered.
   *
   * @param replacing DOI identifier of resource replacing resource being registered
   */
  public static void addIsPreviousVersionOfDOIRelatedIdentifier(@NotNull DataCiteMetadata metadata,
    @NotNull DOI replacing) {
    DataCiteMetadata.RelatedIdentifiers.RelatedIdentifier rid =
      FACTORY.createDataCiteMetadataRelatedIdentifiersRelatedIdentifier();
    rid.setRelatedIdentifierType(RelatedIdentifierType.DOI);
    rid.setValue(replacing.getDoiName());
    rid.setRelationType(RelationType.IS_PREVIOUS_VERSION_OF);
    metadata.getRelatedIdentifiers().getRelatedIdentifier().add(rid);
  }
}
