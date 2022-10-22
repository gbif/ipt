/*
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
import org.gbif.doi.metadata.datacite.ContributorType;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.metadata.datacite.DateType;
import org.gbif.doi.metadata.datacite.DescriptionType;
import org.gbif.doi.metadata.datacite.NameIdentifier;
import org.gbif.doi.metadata.datacite.RelatedIdentifierType;
import org.gbif.doi.metadata.datacite.RelationType;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.BBox;
import org.gbif.metadata.eml.BibliographicCitationSet;
import org.gbif.metadata.eml.Citation;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.GeospatialCoverage;
import org.gbif.metadata.eml.KeywordSet;
import org.gbif.metadata.eml.PhysicalData;
import org.gbif.metadata.eml.Point;
import org.gbif.metadata.eml.TemporalCoverage;
import org.gbif.metadata.eml.UserId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataCiteMetadataBuilderTest {

  @Test
  public void testBuilder() throws InvalidMetadataException {
    Resource resource = new Resource();
    resource.setMetadataVersion(new BigDecimal("2.0"));

    DOI doi = new DOI("10.5072/ipt12");
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);

    resource.setCoreType("Occurrence");
    resource.setRecordsPublished(7200);

    Organisation organisation = new Organisation();
    organisation.setName("Natural History Museum");
    organisation.setKey(UUID.randomUUID().toString());
    resource.setOrganisation(organisation);

    Eml eml = new Eml();
    Calendar cal = Calendar.getInstance();
    cal.set(2013, Calendar.JANUARY, 9);
    Date date = cal.getTime();
    eml.setDateStamp(date);

    resource.setEml(eml);

    eml.setTitle("Ants of New York State");
    List<String> description = new ArrayList<>();
    description.add("Comprehensive ants collection.");
    description.add("Mostly dried preserved.");
    eml.setDescription(description);
    eml.setMetadataLanguage("eng");
    eml.setLanguage("heb");
    Agent creator1 = new Agent();
    creator1.setLastName("Smith");
    creator1.setFirstName("Jim");
    List<UserId> userIds = new ArrayList<>();
    UserId userId1 = new UserId("http://orcid.org", "0000-0099-6824-9999");
    userIds.add(userId1);
    creator1.setUserIds(userIds);
    eml.addCreator(creator1);

    Agent creator2 = new Agent();
    creator2.setLastName("GBIF");
    List<UserId> userIds2 = new ArrayList<>();
    creator2.setUserIds(userIds2);
    eml.addCreator(creator2);

    // contact = contributor
    Agent contributor1 = new Agent();
    contributor1.setLastName("Love");
    contributor1.setFirstName("Brian");
    List<UserId> contributorUserIds = new ArrayList<>();
    UserId contributorUserId1 = new UserId("http://orcid.org", "0000-0099-6824-1234");
    contributorUserIds.add(contributorUserId1);
    contributor1.setUserIds(contributorUserIds);
    eml.addContact(contributor1);

    // metadataProvider = contributor
    Agent contributor2 = new Agent();
    contributor2.setLastName("Wong");
    contributor2.setFirstName("Markus");
    List<UserId> contributorUserIds2 = new ArrayList<>();
    contributor2.setUserIds(contributorUserIds2);
    eml.addMetadataProvider(contributor2);

    // associatedProvider = contributor
    Agent contributor3 = new Agent();
    contributor3.setPosition("Insects Curator");
    contributor3.setRole("curator");
    List<UserId> contributorUserIds3 = new ArrayList<>();
    contributor3.setUserIds(contributorUserIds3);
    eml.addAssociatedParty(contributor3);

    Agent contributor4 = new Agent();
    contributor4.setPosition("Programmer");
    contributor4.setRole("programmer");
    eml.addAssociatedParty(contributor4);
    // TODO add more associatedParties covering all roles

    // keywords
    KeywordSet keywordSet1 = new KeywordSet();
    keywordSet1.add("ants");
    keywordSet1.add("insects");
    keywordSet1.setKeywordThesaurus("http://www.vocab.com/collections");
    eml.addKeywordSet(keywordSet1);
    KeywordSet keywordSet2 = new KeywordSet();
    keywordSet2.add("New York");
    keywordSet2.setKeywordThesaurus("Locations on earth");
    eml.addKeywordSet(keywordSet2);

    // temporal coverages
    TemporalCoverage coverage1 = new TemporalCoverage();
    Calendar startCal = Calendar.getInstance();
    startCal.set(2010, Calendar.FEBRUARY, 14);
    Date startDate = startCal.getTime();
    coverage1.setStartDate(startDate);
    eml.addTemporalCoverage(coverage1);

    TemporalCoverage coverage2 = new TemporalCoverage();
    Calendar rangeStartCal = Calendar.getInstance();
    rangeStartCal.set(2009, Calendar.MARCH, 17);
    Date rangeStartDate = rangeStartCal.getTime();
    coverage2.setStartDate(rangeStartDate);
    Calendar rangeEndCal = Calendar.getInstance();
    rangeEndCal.set(2009, Calendar.MARCH, 18);
    Date rangeEndDate = rangeEndCal.getTime();
    coverage2.setEndDate(rangeEndDate);
    eml.addTemporalCoverage(coverage2);

    eml.addAlternateIdentifier("http://www.gbif.org/dataset/1234");
    eml.addAlternateIdentifier("http://www.ipt.gbif.org/resource?r=ants");

    BibliographicCitationSet bibliographicCitationSet = new BibliographicCitationSet();
    Citation citation1 = new Citation("Citation of first bibliographic citation", "http://doi.org/10.5072/bibcite1");
    Citation citation2 = new Citation("Citation of second bibliographic citation", "http://doi.org/10.5072/bibcite2");
    List<Citation> citations = new ArrayList<>();
    citations.add(citation1);
    citations.add(citation2);
    bibliographicCitationSet.setBibliographicCitations(citations);
    eml.setBibliographicCitationSet(bibliographicCitationSet);

    PhysicalData physicalData = new PhysicalData();
    physicalData.setDistributionUrl("http://box.excel/v1?r=ants");
    physicalData.setFormat("Excel");
    physicalData.setCharset("UTF-8");
    physicalData.setFormatVersion("1.0");
    physicalData.setName("Alternate format - Excel");
    eml.addPhysicalData(physicalData);

    eml.setIntellectualRights(
      "This work is licensed under <a href=\"http://creativecommons.org/publicdomain/zero/1.0/legalcode\">Creative Commons CCZero (CC0) 1.0 License</a>.");

    Point min = new Point();
    min.setLatitude(51.853298);
    min.setLongitude(-115.468750);
    Point max = new Point();
    max.setLatitude(51.973588);
    max.setLongitude(-112.653503);
    BBox bbox = new BBox(min, max);
    GeospatialCoverage coverage = new GeospatialCoverage();
    coverage.setBoundingCoordinates(bbox);
    coverage.setDescription("NE Calgary Region");
    eml.addGeospatialCoverage(coverage);

    DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);

    // add isNewVersionOf RelatedIdentifier
    DOI formerDoi = new DOI("10.5072/former");
    User manager = new User();
    manager.setEmail("jsmith@gbif.org");
    BigDecimal replacedVersion = new BigDecimal("1.0");
    VersionHistory historyVersion1 = new VersionHistory(replacedVersion, new Date(), PublicationStatus.PUBLIC);
    resource.addVersionHistory(historyVersion1);
    DataCiteMetadataBuilder.addIsNewVersionOfDOIRelatedIdentifier(dataCiteMetadata, formerDoi);

    // make assertions
    assertEquals("10.5072/ipt12", dataCiteMetadata.getIdentifier().getValue());
    assertEquals(DataCiteMetadataBuilder.DOI_IDENTIFIER_TYPE, dataCiteMetadata.getIdentifier().getIdentifierType());
    assertEquals("Ants of New York State", dataCiteMetadata.getTitles().getTitle().get(0).getValue());
    assertEquals("Natural History Museum", dataCiteMetadata.getPublisher().getValue());
    assertEquals("2013", dataCiteMetadata.getPublicationYear());
    assertEquals("eng", dataCiteMetadata.getTitles().getTitle().get(0).getLang());
    assertNull(dataCiteMetadata.getTitles().getTitle().get(0).getTitleType());

    // creator1
    assertEquals("Jim Smith", dataCiteMetadata.getCreators().getCreator().get(0).getCreatorName().getValue());
    assertEquals("0000-0099-6824-9999",
      dataCiteMetadata.getCreators().getCreator().get(0).getNameIdentifier().get(0).getValue());
    assertEquals("http://orcid.org",
      dataCiteMetadata.getCreators().getCreator().get(0).getNameIdentifier().get(0).getSchemeURI());
    assertEquals(DataCiteMetadataBuilder.ORCID_NAME_IDENTIFIER_SCHEME,
      dataCiteMetadata.getCreators().getCreator().get(0).getNameIdentifier().get(0).getNameIdentifierScheme());
    // creator2
    assertEquals("GBIF", dataCiteMetadata.getCreators().getCreator().get(1).getCreatorName().getValue());

    // contributor1
    assertEquals("Brian Love", dataCiteMetadata.getContributors().getContributor().get(0).getContributorName().getValue());
    assertEquals("0000-0099-6824-1234",
      dataCiteMetadata.getContributors().getContributor().get(0).getNameIdentifier().get(0).getValue());
    assertEquals("http://orcid.org",
      dataCiteMetadata.getContributors().getContributor().get(0).getNameIdentifier().get(0).getSchemeURI());
    assertEquals(DataCiteMetadataBuilder.ORCID_NAME_IDENTIFIER_SCHEME,
      dataCiteMetadata.getContributors().getContributor().get(0).getNameIdentifier().get(0).getNameIdentifierScheme());
    assertEquals(ContributorType.CONTACT_PERSON,
      dataCiteMetadata.getContributors().getContributor().get(0).getContributorType());
    // contributor2
    assertEquals("Markus Wong", dataCiteMetadata.getContributors().getContributor().get(1).getContributorName().getValue());
    assertEquals(ContributorType.DATA_CURATOR,
      dataCiteMetadata.getContributors().getContributor().get(1).getContributorType());
    // contributor3
    assertEquals("Insects Curator", dataCiteMetadata.getContributors().getContributor().get(2).getContributorName().getValue());
    assertEquals(ContributorType.DATA_CURATOR,
      dataCiteMetadata.getContributors().getContributor().get(2).getContributorType());
    assertEquals("Programmer", dataCiteMetadata.getContributors().getContributor().get(3).getContributorName().getValue());
    assertEquals(ContributorType.PRODUCER,
      dataCiteMetadata.getContributors().getContributor().get(3).getContributorType());

    // subjects
    assertEquals("ants", dataCiteMetadata.getSubjects().getSubject().get(0).getValue());
    assertEquals("eng", dataCiteMetadata.getSubjects().getSubject().get(0).getLang());
    assertEquals("http://www.vocab.com/collections", dataCiteMetadata.getSubjects().getSubject().get(0).getSchemeURI());
    assertEquals("insects", dataCiteMetadata.getSubjects().getSubject().get(1).getValue());
    assertEquals("http://www.vocab.com/collections", dataCiteMetadata.getSubjects().getSubject().get(1).getSchemeURI());
    assertEquals("eng", dataCiteMetadata.getSubjects().getSubject().get(1).getLang());
    assertEquals("New York", dataCiteMetadata.getSubjects().getSubject().get(2).getValue());
    assertEquals("Locations on earth", dataCiteMetadata.getSubjects().getSubject().get(2).getSubjectScheme());
    assertEquals("eng", dataCiteMetadata.getSubjects().getSubject().get(2).getLang());

    // dates: created, updated, then ones converted from temporal coverage
    assertNotNull(dataCiteMetadata.getDates().getDate().get(0).getValue());
    assertEquals(DateType.CREATED, dataCiteMetadata.getDates().getDate().get(0).getDateType());
    assertNotNull(dataCiteMetadata.getDates().getDate().get(1).getValue());
    assertEquals(DateType.UPDATED, dataCiteMetadata.getDates().getDate().get(1).getDateType());
    assertEquals("2010-02-14", dataCiteMetadata.getDates().getDate().get(2).getValue());
    assertEquals(DateType.VALID, dataCiteMetadata.getDates().getDate().get(2).getDateType());
    assertEquals("2009-03-17/2009-03-18", dataCiteMetadata.getDates().getDate().get(3).getValue());
    assertEquals(DateType.VALID, dataCiteMetadata.getDates().getDate().get(3).getDateType());

    // resource language
    assertEquals("heb", dataCiteMetadata.getLanguage());

    // ResourceType
    assertEquals("Occurrence", dataCiteMetadata.getResourceType().getValue());
    assertEquals("Dataset", dataCiteMetadata.getResourceType().getResourceTypeGeneral().value());

    // AlternateIds
    assertEquals("http://www.gbif.org/dataset/1234",
      dataCiteMetadata.getAlternateIdentifiers().getAlternateIdentifier().get(0).getValue());
    assertEquals(DataCiteMetadataBuilder.ALTERNATE_IDENTIFIER_TYPE_TEXT,
      dataCiteMetadata.getAlternateIdentifiers().getAlternateIdentifier().get(0).getAlternateIdentifierType());
    assertEquals("http://www.ipt.gbif.org/resource?r=ants",
      dataCiteMetadata.getAlternateIdentifiers().getAlternateIdentifier().get(1).getValue());
    assertEquals(DataCiteMetadataBuilder.ALTERNATE_IDENTIFIER_TYPE_TEXT,
      dataCiteMetadata.getAlternateIdentifiers().getAlternateIdentifier().get(1).getAlternateIdentifierType());

    assertEquals("2.0", dataCiteMetadata.getVersion());
    // Reference RelatedIdentifiers: bibliographic citations
    assertEquals(RelationType.REFERENCES,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(0).getRelationType());
    assertEquals("http://doi.org/10.5072/bibcite1",
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(0).getValue());
    assertEquals(RelatedIdentifierType.URL,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(0).getRelatedIdentifierType());
    assertEquals(RelationType.REFERENCES,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(1).getRelationType());
    assertEquals("http://doi.org/10.5072/bibcite2",
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(1).getValue());
    assertEquals(RelatedIdentifierType.URL,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(1).getRelatedIdentifierType());
    // isNewVariantOf RelatedIdentifier: physicalData
    assertEquals(RelationType.IS_VARIANT_FORM_OF,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(2).getRelationType());
    assertEquals("http://box.excel/v1?r=ants",
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(2).getValue());
    assertEquals(RelatedIdentifierType.URL,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(2).getRelatedIdentifierType());
    // isNewVersionOf RelatedIdentifier: set when replacing DOI (new major version published)
    assertEquals(RelationType.IS_NEW_VERSION_OF,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(3).getRelationType());
    assertEquals(formerDoi.getDoiName(),
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(3).getValue());
    assertEquals(RelatedIdentifierType.DOI,
      dataCiteMetadata.getRelatedIdentifiers().getRelatedIdentifier().get(3).getRelatedIdentifierType());

    // 3 different resource formats available for download. Each of these is versioned and
    assertEquals(DataCiteMetadataBuilder.DWC_FORMAT_NAME, dataCiteMetadata.getFormats().getFormat().get(0));
    assertEquals(DataCiteMetadataBuilder.EML_FORMAT_NAME, dataCiteMetadata.getFormats().getFormat().get(1));
    assertEquals(DataCiteMetadataBuilder.RTF_FORMAT_NAME, dataCiteMetadata.getFormats().getFormat().get(2));

    // Sizes: record #
    assertEquals("7200 " + DataCiteMetadataBuilder.RECORDS_NAME, dataCiteMetadata.getSizes().getSize().get(0));

    // Rights
    assertEquals("http://creativecommons.org/publicdomain/zero/1.0/legalcode",
      dataCiteMetadata.getRightsList().getRights().get(0).getRightsURI());
    assertEquals("Creative Commons CCZero (CC0) 1.0 License",
      dataCiteMetadata.getRightsList().getRights().get(0).getValue());

    // Abstract aka description
    assertEquals("Comprehensive ants collection.",
      dataCiteMetadata.getDescriptions().getDescription().get(0).getContent().get(0));
    assertEquals("Mostly dried preserved.",
      dataCiteMetadata.getDescriptions().getDescription().get(1).getContent().get(0));
    assertEquals(DescriptionType.ABSTRACT,
      dataCiteMetadata.getDescriptions().getDescription().get(0).getDescriptionType());
    assertEquals("eng", dataCiteMetadata.getDescriptions().getDescription().get(0).getLang());

    // GeoLocation
    assertEquals("Box[westBoundLongitude=-115.46875, eastBoundLongitude=-112.6535, southBoundLatitude=51.8533, northBoundLatitude=51.973587]",
        dataCiteMetadata.getGeoLocations().getGeoLocation().get(0).getGeoLocationPlaceOrGeoLocationPointOrGeoLocationBox().get(1).toString());
    assertEquals("NE Calgary Region",
      dataCiteMetadata.getGeoLocations().getGeoLocation().get(0).getGeoLocationPlaceOrGeoLocationPointOrGeoLocationBox().get(0));
  }

  /**
   * Publisher name is an empty string.
   */
  @Test
  public void testGetPublisherWithEmptyName() {
    Resource resource = new Resource();
    Organisation organisation = new Organisation();
    organisation.setName("");
    resource.setOrganisation(organisation);
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.getPublisher(resource));
  }

  /**
   * Publisher does not exist.
   */
  @Test
  public void testGetPublisherNotExisting() {
    Resource resource = new Resource();
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.getPublisher(resource));
  }

  /**
   * Publisher does not exist, because default organisation "No organisation" has been assigned to resource.
   */
  @Test
  public void testGetPublisherDefaultAssigned() {
    Resource resource = new Resource();
    Organisation o = new Organisation();
    o.setKey(Constants.DEFAULT_ORG_KEY.toString());
    o.setName("No organisation");
    resource.setOrganisation(o);
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.getPublisher(resource));
  }

  /**
   * Title is an empty string.
   */
  @Test
  public void testConvertEmlTitlesWithEmptyTitle() {
    Eml eml = new Eml();
    eml.setTitle("");
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.convertEmlTitles(eml));
  }


  /**
   * Scheme not recognized - only ORCID and ResearcherId are supported.
   */
  @Test
  public void testConvertEmlUserIdWithUnrecognizedScheme() {
    UserId userId1 = new UserId("http://unrecognized.org", "0000-0099-6824-9999");
    NameIdentifier id = DataCiteMetadataBuilder.convertEmlUserIdIntoCreatorNameIdentifier(userId1);
    assertNull(id);
  }

  /**
   * SchemeURI (UserId.directory) not provided. Null returned in this case, since the directory and identifier are both
   * required to output a NameIdentifier.
   */
  @Test
  public void testConvertEmlUserIdWithMissingSchemeURI() {
    UserId userId1 = new UserId("", "0000-0099-6824-9999");
    assertNull(DataCiteMetadataBuilder.convertEmlUserIdIntoCreatorNameIdentifier(userId1));
  }

  /**
   * First agent in list had an empty name.
   */
  @Test
  public void testConvertEmlCreatorsWithEmptyName() {
    Agent creator1 = new Agent();
    creator1.setLastName("");
    creator1.setFirstName("");
    List<Agent> creators = new ArrayList<>();
    creators.add(creator1);
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.convertEmlCreators(creators));
  }

  /**
   * Agents list was empty.
   */
  @Test
  public void testConvertEmlCreatorsWithEmptyList() {
    List<Agent> creators = new ArrayList<>();
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.convertEmlCreators(creators));
  }

  /**
   * First agent in list only had position name.
   */
  @Test
  public void testConvertEmlCreatorsWithPositionName() {
    Agent creator1 = new Agent();
    creator1.setPosition("President");
    List<Agent> creators = new ArrayList<>();
    creators.add(creator1);
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.convertEmlCreators(creators));
  }

  /**
   * First agent in list only had organization name.
   */
  @Test
  public void testConvertEmlCreatorsWithOrganisationName() throws InvalidMetadataException {
    Agent creator1 = new Agent();
    creator1.setOrganisation("NHM London");
    List<Agent> creators = new ArrayList<>();
    creators.add(creator1);
    DataCiteMetadata.Creators ls = DataCiteMetadataBuilder.convertEmlCreators(creators);
    DataCiteMetadata.Creators.Creator c = ls.getCreator().get(0);
    assertEquals("NHM London", c.getCreatorName().getValue());
  }

  /**
   * Scheme not recognized - only ORCID and ResearcherId are supported.
   */
  @Test
  public void testConvertEmlContributorUserIdWithUnrecognizedScheme() {
    UserId userId1 = new UserId("http://unrecognized.org", "0000-0099-6824-9999");
    NameIdentifier id = DataCiteMetadataBuilder.convertEmlUserIdIntoContributorNameIdentifier(userId1);
    assertNull(id);
  }

  /**
   * SchemeURI (UserId.directory) not provided. Null returned in this case, since the directory and identifier are both
   * required to output a NameIdentifier.
   */
  @Test
  public void testConvertEmlContributorUserIdWithMissingSchemeURI() {
    UserId userId1 = new UserId("", "0000-0099-6824-9999");
    assertNull(DataCiteMetadataBuilder.convertEmlUserIdIntoContributorNameIdentifier(userId1));
  }

  /**
   * First agent in list had an empty name, no organisation, and no position.
   */
  @Test
  public void testConvertEmlContributorWithEmptyName() {
    Agent contributor = new Agent();
    contributor.setLastName("");
    contributor.setFirstName("");
    List<Agent> contributors = new ArrayList<>();
    contributors.add(contributor);
    assertThrows(InvalidMetadataException.class, () -> DataCiteMetadataBuilder.convertEmlContributors(contributors));
  }
}
