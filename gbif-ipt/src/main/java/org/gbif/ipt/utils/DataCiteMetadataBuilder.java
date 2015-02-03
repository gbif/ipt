package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.metadata.datacite.ObjectFactory;
import org.gbif.doi.metadata.datacite.RelatedIdentifierType;
import org.gbif.doi.metadata.datacite.RelationType;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.ipt.model.Resource;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.UserId;

import java.util.Calendar;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

public class DataCiteMetadataBuilder {

  public static final String DOI_IDENTIFIER_TYPE = "DOI";
  public static final String ORCID_NAME_IDENTIFIER_SCHEME = "ORCID";
  public static final String RESEARCHERID_NAME_IDENTIFIER_SCHEME = "ResearcherID";

  private static final Logger LOG = Logger.getLogger(DataCiteMetadataBuilder.class);
  private static final ObjectFactory FACTORY = new ObjectFactory();

  public static DataCiteMetadata createDataCiteMetadata(DOI doi, Resource resource) throws InvalidMetadataException {

    DataCiteMetadata dataCiteMetadata = FACTORY.createDataCiteMetadata();

    // add DOI (mandatory)
    DataCiteMetadata.Identifier identifier = getDOIIdentifier(doi);
    dataCiteMetadata.setIdentifier(identifier);

    // add list of titles (mandatory)
    DataCiteMetadata.Titles titles = convertEmlTitles(resource.getEml());
    dataCiteMetadata.setTitles(titles);

    // add list of creators (mandatory)
    DataCiteMetadata.Creators creators = convertEmlCreators(resource.getEml().getCreators());
    dataCiteMetadata.setCreators(creators);

    // publisher (mandatory)
    String publisher = getPublisher(resource);
    dataCiteMetadata.setPublisher(publisher);

    // publication year (mandatory)
    String publicationYear = getPublicationYear(resource.getEml());
    dataCiteMetadata.setPublicationYear(publicationYear);

    // version (optional according to DataCite, mandatory and thus never null according to IPT)
    dataCiteMetadata.setVersion(resource.getEmlVersion().toPlainString());

    // relatedIdentifiers (recommended)
    DataCiteMetadata.RelatedIdentifiers rids = FACTORY.createDataCiteMetadataRelatedIdentifiers();
    dataCiteMetadata.setRelatedIdentifiers(rids);

    return dataCiteMetadata;
  }

  /**
   * Retrieve the DOI identifier assigned to the resource. DataCite metadata schema (v3) requires the identifier.
   *
   * @param doi DOI identifier assigned to resource
   *
   * @return DataCite identifier of type DOI
   */
  protected static DataCiteMetadata.Identifier getDOIIdentifier(@NotNull DOI doi) throws InvalidMetadataException {
    DataCiteMetadata.Identifier identifier = FACTORY.createDataCiteMetadataIdentifier();
    identifier.setValue(doi.getDoiName());
    identifier.setIdentifierType(DOI_IDENTIFIER_TYPE);
    return identifier;
  }

  /**
   * Retrieve the publisher from the IPT resource, equal to the name of the publishing organisation. DataCite schema
   * (v3) requires the publisher.
   *
   * @param resource IPT resource
   *
   * @return the publisher name
   *
   * @throws InvalidMetadataException if mandatory publisher cannot be retrieved
   */
  @VisibleForTesting
  protected static String getPublisher(Resource resource) throws InvalidMetadataException {
    if (resource.getOrganisation() != null && !Strings.isNullOrEmpty(resource.getOrganisation().getName())) {
      return resource.getOrganisation().getName();
    } else {
      throw new InvalidMetadataException("DataCite schema (v3) requires the publisher");
    }
  }

  /**
   * Retrieve the publication year from Eml dateStamp, which stores the date the resource was created. DataCite schema
   * (v3) requires the publication year.
   *
   * @param eml EML
   *
   * @return the publication year
   *
   * @throws InvalidMetadataException if mandatory publication year cannot be retrieved
   */
  @VisibleForTesting
  protected static String getPublicationYear(Eml eml) throws InvalidMetadataException {
    if (eml.getDateStamp() != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(eml.getDateStamp());
      return String.valueOf(cal.get(Calendar.YEAR));
    } else {
      throw new InvalidMetadataException("DataCite schema (v3) requires the publication year");
    }
  }

  /**
   * Convert list of EML creators into DataCite creators. DataCite metadata schema (v3.0) requires at least one creator.
   *
   * @param agents EML agents list
   *
   * @return DataCite creators list
   *
   * @throws org.gbif.doi.service.InvalidMetadataException if mandatory number of creators cannot be created/returned
   */
  @VisibleForTesting
  protected static DataCiteMetadata.Creators convertEmlCreators(List<Agent> agents) throws InvalidMetadataException {
    DataCiteMetadata.Creators creators = FACTORY.createDataCiteMetadataCreators();
    if (!agents.isEmpty() && !Strings.isNullOrEmpty(agents.get(0).getFullName())) {
      for (Agent agent: agents) {
        DataCiteMetadata.Creators.Creator creator = FACTORY.createDataCiteMetadataCreatorsCreator();
        // name is mandatory, in order of priority:
        // 1. try agent name
        if (!Strings.isNullOrEmpty(agent.getFullName())) {
          creator.setCreatorName(agent.getFullName());
          // identifier is optional, however, there can only be one and the name of the identifier scheme is mandatory
          if (!agent.getUserIds().isEmpty()) {
            for (UserId userId : agent.getUserIds()) {
              DataCiteMetadata.Creators.Creator.NameIdentifier nid = convertEmlUserId(userId);
              if (nid != null) {
                creator.setNameIdentifier(nid);
                break;
              }
            }
          }
          // affiliation is optional
          if (!Strings.isNullOrEmpty(agent.getOrganisation())) {
            creator.getAffiliation().add(agent.getOrganisation());
          }
        }
        // 2. try organisation name
        else if (!Strings.isNullOrEmpty(agent.getOrganisation())) {
          creator.setCreatorName(agent.getOrganisation());
        }
        // 3. try position name
        else if (!Strings.isNullOrEmpty(agent.getPosition())) {
          creator.setCreatorName(agent.getPosition());
          // affiliation is optional
          if (!Strings.isNullOrEmpty(agent.getOrganisation())) {
            creator.getAffiliation().add(agent.getOrganisation());
          }
        }
        // otherwise if no name, organisation name, or position name found, throw exception
        else {
          throw new InvalidMetadataException(
            "DataCite schema (v3) requires creator have a name! Check creator/agent: " + agent.toString());
        }
        // add to list
        creators.getCreator().add(creator);
      }
      return creators;
    } else {
      throw new InvalidMetadataException("DataCite schema (v3) requires at least one creator");
    }
  }

  /**
   * Convert list of EML titles into DataCite titles. Only the title is mandatory, the type is optional.
   * EML version 1.1 only contains a single title, with language derived from metadata language. DataCite metadata
   * schema (v3) requires at least one title.
   *
   * @param eml Eml
   *
   * @return DataCite titles list
   *
   * @throws org.gbif.doi.service.InvalidMetadataException if mandatory number of titles cannot be created/returned
   */
  protected static DataCiteMetadata.Titles convertEmlTitles(Eml eml) throws InvalidMetadataException {
    if (!Strings.isNullOrEmpty(eml.getTitle())) {
      DataCiteMetadata.Titles titles = FACTORY.createDataCiteMetadataTitles();
      DataCiteMetadata.Titles.Title primary = FACTORY.createDataCiteMetadataTitlesTitle();
      primary.setValue(eml.getTitle());
      primary.setLang(eml.getMetadataLanguage());
      titles.getTitle().add(primary);
      return titles;
    } else {
      throw new InvalidMetadataException("DataCite schema (v3) requires at least one title");
    }
  }

  /**
   * Convert a Eml UserId object into a DataCite NameIdentifier object. DataCite metadata schema (v3) requires the
   * scheme name if the identifier is used. Unfortunately the scheme name cannot be reliably and accurately derived
   * from the schemeURI (UserId.directory) therefore this method only supports the following schemes: ORCID and
   * ResearcherID.
   *
   * @param userId Eml UserId object
   *
   * @return DataCite NameIdentifier object or null if none could be created (e.g. because directory wasn't recognized)
   */
  @VisibleForTesting
  protected static DataCiteMetadata.Creators.Creator.NameIdentifier convertEmlUserId(UserId userId) {
    if (!Strings.isNullOrEmpty(userId.getIdentifier()) && !Strings.isNullOrEmpty(userId.getDirectory())) {
      String directory = Strings.nullToEmpty(userId.getDirectory()).toLowerCase();
      if (directory.contains(ORCID_NAME_IDENTIFIER_SCHEME.toLowerCase()) ||
          directory.contains(RESEARCHERID_NAME_IDENTIFIER_SCHEME.toLowerCase())) {
        DataCiteMetadata.Creators.Creator.NameIdentifier nid = FACTORY.createDataCiteMetadataCreatorsCreatorNameIdentifier();
        nid.setValue(userId.getIdentifier());
        nid.setSchemeURI(userId.getDirectory());
        nid.setNameIdentifierScheme((directory.contains(ORCID_NAME_IDENTIFIER_SCHEME.toLowerCase())) ?
          ORCID_NAME_IDENTIFIER_SCHEME : RESEARCHERID_NAME_IDENTIFIER_SCHEME);
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
