/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
package org.gbif.ipt.validation;

import org.gbif.api.vocabulary.Language;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.MetadataSection;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.BBox;
import org.gbif.metadata.eml.Collection;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.MaintenanceUpdateFrequency;
import org.gbif.metadata.eml.UserId;
import org.gbif.utils.file.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmlValidatorTest {

  private EmlValidator validator;
  private Resource resource;
  private Eml eml;
  private Agent badAgent;
  private BaseAction action;
  private Organisation organisation;

  @BeforeEach
  public void before() throws IOException, SAXException, ParserConfigurationException {
    AppConfig mockCfg = mock(AppConfig.class);
    SimpleTextProvider mockTextProvider = mock(SimpleTextProvider.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    // instance of EmlValidator using mock AppConfig, RegistrationManager, and SimpleTextProvider
    validator = new EmlValidator(mockCfg, mockRegistrationManager, mockTextProvider);
    // instance of BaseAction
    action = new BaseAction(mockTextProvider, mockCfg, mockRegistrationManager);

    // load sample eml, and set on resource
    resource = new Resource();
    organisation = new Organisation();
    organisation.setName("NHM");
    UUID organisationKey = UUID.randomUUID();
    organisation.setKey(organisationKey.toString());
    // mock organisation lookup by key
    when(mockRegistrationManager.get(any(UUID.class))).thenReturn(organisation);
    resource.setOrganisation(organisation);

    resource.setCoreType(Resource.CoreRowType.CHECKLIST.toString());
    resource.setUpdateFrequency(MaintenanceUpdateFrequency.ANNUALLY.toString());

    eml = EmlFactory.build(FileUtils.classpathStream("data/eml.xml"));
    resource.setEml(eml);

    // create incomplete Agent (no last name, org name, or position)
    badAgent = new Agent();
    badAgent.setFirstName("John");
  }

  @Test
  public void testInteger() {
    assertFalse(EmlValidator.isValidInteger("0.1"));
    assertFalse(EmlValidator.isValidInteger("1,1"));
    assertFalse(EmlValidator.isValidInteger("gbif"));
    assertFalse(EmlValidator.isValidInteger("0-0"));
    assertFalse(EmlValidator.isValidInteger("."));
    assertFalse(EmlValidator.isValidInteger(" "));
    assertFalse(EmlValidator.isValidInteger("1 1"));
    assertFalse(EmlValidator.isValidInteger("12 alpha"));
    assertTrue(EmlValidator.isValidInteger("0"));
    assertTrue(EmlValidator.isValidInteger("-1"));
    assertTrue(EmlValidator.isValidInteger("123445556"));
  }

  @Test
  public void testPhone() {
    assertTrue(EmlValidator.isValidPhoneNumber("4916213056"));
    assertTrue(EmlValidator.isValidPhoneNumber("49 162 130 5624 - 0"));
    assertTrue(EmlValidator.isValidPhoneNumber("0049 (162) 130 5624 - 0"));
    assertTrue(EmlValidator.isValidPhoneNumber("+49 (162) 130 5624 - 0"));
    assertTrue(EmlValidator.isValidPhoneNumber("001/432/4342321233"));
    assertTrue(EmlValidator.isValidPhoneNumber("+49 (30) 567-9876 ext 55"));
    assertTrue(EmlValidator.isValidPhoneNumber("+49 (30) 999-0000 Ext. 55"));
    assertTrue(EmlValidator.isValidPhoneNumber("3210049,33"));
    assertTrue(EmlValidator.isValidPhoneNumber("32134214."));
    // bad numbers
    assertFalse(EmlValidator.isValidPhoneNumber("675343545 && 788789977"));
    assertFalse(EmlValidator.isValidPhoneNumber("*45 2117 8990"));
  }

  @Test
  public void testURL() {
    assertNull(EmlValidator.formatURL(null));
    assertNull(EmlValidator.formatURL("- - - "));
    assertNull(EmlValidator.formatURL("//**##"));
    assertNull(EmlValidator.formatURL("      "));
    assertNull(EmlValidator.formatURL("ftp://ftp.gbif.org //h"));
    assertNotNull(EmlValidator.formatURL("www.gbif.com"));
    assertEquals("http://www.gbif.com", EmlValidator.formatURL("www.gbif.com"));
    assertEquals("http://gbif.com", EmlValidator.formatURL("gbif.com"));
    assertNotNull(EmlValidator.formatURL("torrent://www.gbif.org"));
    assertNotNull(EmlValidator.formatURL("ftp://ftp.gbif.org"));
    assertNotNull(EmlValidator.formatURL("http://www.gbif.org"));
    assertNotNull(EmlValidator.formatURL("hhttp://ipt.sibcolombia.net/iavh/resource.do?r=flora_tame_arauca_2013"));
  }

  @Test
  public void testURI() {
    assertFalse(EmlValidator.isWellFormedURI(null));
    assertFalse(EmlValidator.isWellFormedURI("http://ipt.bio.aq/resource.do?r=fossil_ diversity_ of_ the_ Early_ Cr"));
    assertFalse(EmlValidator.isWellFormedURI("//**##"));
    assertFalse(EmlValidator.isWellFormedURI("      "));
    assertFalse(EmlValidator.isWellFormedURI("ftp://ftp.gbif.org //h"));
    assertTrue(EmlValidator.isWellFormedURI("www.gbif.com"));
    assertTrue(EmlValidator.isWellFormedURI("torrent://www.gbif.org"));
    assertTrue(EmlValidator.isWellFormedURI("ftp://ftp.gbif.org"));
    assertTrue(EmlValidator.isWellFormedURI("http://www.gbif.org"));
  }

  @Test
  public void testBasicPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  @Test
  public void testBasicPartTitleMissing() {
    // invalid
    eml.setTitle(null);
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  @Test
  public void testBasicPartDescriptionMissing() {
    // invalid
    List<String> description = new ArrayList<>();
    eml.setDescription(description);
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    description.add("shrt");
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // valid
    description.clear();
    description.add("long_enough");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  @Test
  public void testBasicPartIntellectualRightsMissing() {
    // invalid
    eml.setIntellectualRights("");
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // valid
    eml.setIntellectualRights("CC-BY");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    eml.setIntellectualRights(
      "This work is licensed under a <a href=\"http://creativecommons.org/licenses/by/4.0/legalcode\">Creative Commons Attribution (CC-BY) 4.0 License</a>.");
    assertTrue(validator.isValid(resource, MetadataSection.METHODS_SECTION));
  }

  @Test
  public void testBasicPartPublishingOrganisationMissing() {
    // invalid
    resource.setOrganisation(null);
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // valid
    resource.setOrganisation(organisation);
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  @Test
  public void testBasicPartCoreTypeMissing() {
    // invalid
    resource.setCoreType(null);
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // valid
    resource.setCoreType(Resource.CoreRowType.OCCURRENCE.toString());
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  @Test
  public void testBasicPartUpdateFrequencyMissing() {
    // valid, because will set to default "unkown"
    eml.setUpdateFrequency(null);
    resource.setUpdateFrequency(null);
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    assertEquals("unkown", eml.getUpdateFrequency().getIdentifier());

    // valid, because will reuse auto-publishing interval as update frequency
    eml.setUpdateFrequency(null);
    resource.setUpdateFrequency(MaintenanceUpdateFrequency.DAILY.getIdentifier());
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    assertEquals("daily", eml.getUpdateFrequency().getIdentifier());
  }

  @Test
  public void testBasicPartDefaultsSet() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    eml.setLanguage(null);
    eml.setMetadataLanguage(null);
    eml.setUpdateFrequency(null);
    resource.setUpdateFrequency(null);
    resource.setEml(eml);
    resource.setCoreType(Resource.CoreRowType.OCCURRENCE.toString());
    // still valid
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // defaults correct?
    assertEquals("eng", resource.getEml().getLanguage());
    assertEquals("eng", resource.getEml().getMetadataLanguage());
    assertEquals("unkown", eml.getUpdateFrequency().getIdentifier());
  }

  /**
   * Tests that aN EML file with coordinates that use ',' as decimal separator is interpreted incorrectly as world bbox.
   */
  @Test
  public void testDecimalGeographicIssues() {
    try {
      Eml emlWithIssues = EmlFactory.build(FileUtils.classpathStream("data/emlGeographicIssues.xml"));
      assertEquals(emlWithIssues.getGeospatialCoverages().get(0).getBoundingCoordinates(), BBox.newWorldInstance());
    } catch (IOException | SAXException e) {
      fail();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  /**
   * The Basic section requires at least one contact. This test ensures the validation for contact is working
   * checking that each contact has a last name and if a user id has been specified, that it contains both parts:
   * directory and identifier.
   */
  @Test
  public void testBasicPartContactIncomplete() {
    // invalid
    eml.addContact(badAgent);
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    badAgent.setLastName("Smith");
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));

    // no user ids to begin
    assertTrue(badAgent.getUserIds().isEmpty());

    // set invalid user id, because it doesn't contain directory
    UserId invalidId = new UserId("", "1234-5678-9101-1213");
    badAgent.getUserIds().add(invalidId);
    assertFalse(badAgent.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // make user id valid by setting its directory
    badAgent.getUserIds().get(0).setDirectory("http://orcid.org");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));

    // clear user ids
    badAgent.getUserIds().clear();
    assertTrue(badAgent.getUserIds().isEmpty());

    // this time, set invalid user id, because it doesn't contain identifier
    invalidId = new UserId("http://orcid.org", "");
    badAgent.getUserIds().add(invalidId);
    assertFalse(badAgent.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // make user id valid by setting its identifier
    badAgent.getUserIds().get(0).setIdentifier("1234-5678-9101-1213");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  /**
   * The Basic section requires at least one creator. This test ensures the validation for creator is working
   * checking that each creator has a last name and if a user id has been specified, that it contains both parts:
   * directory and identifier.
   */
  @Test
  public void testBasicPartCreatorIncomplete() {
    // invalid
    eml.addCreator(badAgent);
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    badAgent.setLastName("Smith");
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));

    // no user ids to begin
    assertTrue(badAgent.getUserIds().isEmpty());

    // set invalid user id, because it doesn't contain directory
    UserId invalidId = new UserId("", "1234-5678-9101-1213");
    badAgent.getUserIds().add(invalidId);
    assertFalse(badAgent.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // make user id valid by setting its directory
    badAgent.getUserIds().get(0).setDirectory("http://orcid.org");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));

    // clear user ids
    badAgent.getUserIds().clear();
    assertTrue(badAgent.getUserIds().isEmpty());

    // this time, set invalid user id, because it doesn't contain identifier
    invalidId = new UserId("http://orcid.org", "");
    badAgent.getUserIds().add(invalidId);
    assertFalse(badAgent.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // make user id valid by setting its identifier
    badAgent.getUserIds().get(0).setIdentifier("1234-5678-9101-1213");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  /**
   * The Basic section requires at least one metadata provider. This test ensures the validation for metadata provider
   * is working checking that each metadata provider has a last name and if a user id has been specified, that it
   * contains both parts: directory and identifier.
   */
  @Test
  public void testBasicPartMetaProviderIncomplete() {
    // invalid
    eml.addMetadataProvider(badAgent);
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    badAgent.setLastName("Smith");
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));

    // no user ids to begin
    assertTrue(badAgent.getUserIds().isEmpty());

    // set invalid user id, because it doesn't contain directory
    UserId invalidId = new UserId("", "1234-5678-9101-1213");
    badAgent.getUserIds().add(invalidId);
    assertFalse(badAgent.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // make user id valid by setting its directory
    badAgent.getUserIds().get(0).setDirectory("http://orcid.org");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));

    // clear user ids
    badAgent.getUserIds().clear();
    assertTrue(badAgent.getUserIds().isEmpty());

    // this time, set invalid user id, because it doesn't contain identifier
    invalidId = new UserId("http://orcid.org", "");
    badAgent.getUserIds().add(invalidId);
    assertFalse(badAgent.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.BASIC_SECTION));
    // make user id valid by setting its identifier
    badAgent.getUserIds().get(0).setIdentifier("1234-5678-9101-1213");
    assertTrue(validator.isValid(resource, MetadataSection.BASIC_SECTION));
  }

  @Test
  public void testGeoPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.GEOGRAPHIC_COVERAGE_SECTION));
  }

  @Test
  public void testGeoPartDescriptionIncomplete() {
    // invalid
    eml.getGeospatialCoverages().get(0).setDescription("");
    assertFalse(validator.isValid(resource, MetadataSection.GEOGRAPHIC_COVERAGE_SECTION));
  }

  @Test
  public void testGeoPartBoundingBoxIncomplete() {
    // invalid
    BBox box = new BBox();
    box.getMin().setLongitude(null);
    eml.getGeospatialCoverages().get(0).setBoundingCoordinates(box);
    assertFalse(validator.isValid(resource, MetadataSection.GEOGRAPHIC_COVERAGE_SECTION));
  }

  @Test
  public void testTaxCovPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.TAXANOMIC_COVERAGE_SECTION));
  }

  @Test
  public void testTaxCovPartScientificNameIncomplete() {
    // invalid
    eml.getTaxonomicCoverages().get(0).getTaxonKeywords().get(0).setScientificName(null);
    assertFalse(validator.isValid(resource, MetadataSection.TAXANOMIC_COVERAGE_SECTION));
  }

  @Test
  public void testTempCovPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.TEMPORAL_COVERAGE_SECTION));
  }

  @Test
  public void testTempCovPartRangeIncomplete() {
    // invalid
    eml.getTemporalCoverages().get(0).setStartDate(null);
    eml.getTemporalCoverages().get(0).setEndDate(new Date());
    assertFalse(validator.isValid(resource, MetadataSection.TEMPORAL_COVERAGE_SECTION));
  }

  @Test
  public void testTempCovPartSingleDateIncomplete() {
    // 1st is empty, but next 3 aren't = INVALID
    eml.getTemporalCoverages().get(0).setStartDate(null);
    eml.getTemporalCoverages().get(0).setEndDate(null);
    eml.getTemporalCoverages().get(0).setFormationPeriod(null);
    eml.getTemporalCoverages().get(0).setLivingTimePeriod(null);
    assertFalse(validator.isValid(resource, MetadataSection.TEMPORAL_COVERAGE_SECTION));
  }

  @Test
  public void testKeywordsPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.KEYWORDS_SECTION));
  }

  @Test
  public void testKeywordsPartThesaurusIncomplete() {
    // invalid
    eml.getKeywords().get(0).setKeywordThesaurus(null);
    assertFalse(validator.isValid(resource, MetadataSection.KEYWORDS_SECTION));
  }

  @Test
  public void testKeywordsPartKeywordListIncomplete() {
    // invalid
    eml.getKeywords().get(0).setKeywords(new ArrayList<>());
    assertFalse(validator.isValid(resource, MetadataSection.KEYWORDS_SECTION));
  }

  @Test
  public void testPartiesPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.PARTIES_SECTION));
  }

  @Test
  public void testPartiesPartIncomplete() {
    // invalid
    eml.getAssociatedParties().clear();
    eml.getAssociatedParties().add(badAgent);
    assertFalse(validator.isValid(resource, MetadataSection.PARTIES_SECTION));
  }

  @Test
  public void testPartiesPartFirstPartyIncomplete() {
    // add emtpy party at top, with remaining 13 valid parties
    eml.getAssociatedParties().add(0, new Agent());
    assertFalse(validator.isValid(resource, MetadataSection.PARTIES_SECTION));
  }

  @Test
  public void testProjectPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.PROJECT_SECTION));
  }

  @Test
  public void testProjectPartTitleIncomplete() {
    // invalid
    eml.getProject().setTitle(null);
    assertFalse(validator.isValid(resource, MetadataSection.PROJECT_SECTION));
  }

  @Test
  public void testProjectPartPersonnelMissing() {
    assertNotNull(eml.getProject().getTitle());
    eml.getProject().getPersonnel().clear();
    assertTrue(eml.getProject().getPersonnel().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.PROJECT_SECTION));
  }

  @Test
  public void testProjectPartPersonnelNameIncomplete() {
    assertNotNull(eml.getProject().getTitle());
    eml.getProject().getPersonnel().clear();
    assertTrue(eml.getProject().getPersonnel().isEmpty());
    // invalid, because agent is missing last name
    eml.getProject().addProjectPersonnel(badAgent);
    assertFalse(eml.getProject().getPersonnel().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.PROJECT_SECTION));
  }

  /**
   * The Project section requires at least one personnel. This test ensures the validation for personnel is working
   * checking that each personnel has a last name and if a user id has been specified, that it contains both parts:
   * directory and identifier.
   */
  @Test
  public void testProjectPartPersonnelUseridIncomplete() {
    assertNotNull(eml.getProject().getTitle());
    assertEquals(1, eml.getProject().getPersonnel().size());
    Agent personnel = eml.getProject().getPersonnel().get(0);
    // no user ids to begin
    assertTrue(personnel.getUserIds().isEmpty());

    // set invalid user id, because it doesn't contain directory
    UserId invalidId = new UserId("", "1234-5678-9101-1213");
    personnel.getUserIds().add(invalidId);
    assertFalse(personnel.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.PROJECT_SECTION));
    // make user id valid by setting its directory
    personnel.getUserIds().get(0).setDirectory("http://orcid.org");
    assertTrue(validator.isValid(resource, MetadataSection.PROJECT_SECTION));

    // clear user ids
    personnel.getUserIds().clear();
    assertTrue(personnel.getUserIds().isEmpty());

    // this time, set invalid user id, because it doesn't contain identifier
    invalidId = new UserId("http://orcid.org", "");
    personnel.getUserIds().add(invalidId);
    assertFalse(personnel.getUserIds().isEmpty());
    assertFalse(validator.isValid(resource, MetadataSection.PROJECT_SECTION));
    // make user id valid by setting its identifier
    personnel.getUserIds().get(0).setIdentifier("1234-5678-9101-1213");
    assertTrue(validator.isValid(resource, MetadataSection.PROJECT_SECTION));
  }

  @Test
  public void testMethodsPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.METHODS_SECTION));
  }

  @Test
  public void testMethodsPartStudyExtentIncomplete() {
    // invalid
    eml.setSampleDescription("Non empty");
    eml.setStudyExtent("");
    assertFalse(validator.isValid(resource, MetadataSection.METHODS_SECTION));
  }

  @Test
  public void testMethodsPartSampleDescriptionIncomplete() {
    // invalid
    eml.setSampleDescription("");
    eml.setStudyExtent("Non empty");
    assertFalse(validator.isValid(resource, MetadataSection.METHODS_SECTION));
  }

  @Test
  public void testMethodsPartStepIncomplete() {
    // invalid
    eml.getMethodSteps().set(0, "");
    assertFalse(validator.isValid(resource, MetadataSection.METHODS_SECTION));
  }

  @Test
  public void testCitationsPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
  }

  @Test
  public void testCitationsPartCitationMissing() {
    // invalid
    eml.getCitation().setCitation("");
    assertFalse(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
    eml.getCitation().setCitation(null);
    assertFalse(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
  }

  @Test
  public void testCitationIdentifiersInvalid() {
    // valid identifiers 2 - 100
    eml.getCitation().setIdentifier(RandomStringUtils.randomAlphabetic(2));
    assertTrue(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
    eml.getCitation().setIdentifier(RandomStringUtils.randomAlphabetic(100));
    assertTrue(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
    eml.getCitation().setIdentifier(RandomStringUtils.randomAlphabetic(75));
    assertTrue(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
    eml.getCitation().setIdentifier(RandomStringUtils.randomAlphabetic(110));
    assertTrue(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
    // invalid identifiers less than 2, or greater than 100
    eml.getCitation().setIdentifier("a");
    assertFalse(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
    eml.getCitation().setIdentifier(RandomStringUtils.randomAlphabetic(210));
    assertFalse(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
  }

  @Test
  public void testCitationsPartBiblioCitationMissing() {
    // invalid
    eml.getBibliographicCitations().get(0).setCitation(null);
    assertFalse(validator.isValid(resource, MetadataSection.CITATIONS_SECTION));
  }

  @Test
  public void testCollectionPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.COLLECTIONS_SECTION));
  }

  @Test
  public void testCollectionPartCollectionNameIncomplete() {
    // invalid
    Collection collection = new Collection();
    collection.setCollectionName(null);
    eml.addCollection(collection);
    assertFalse(validator.isValid(resource, MetadataSection.COLLECTIONS_SECTION));
  }

  @Test
  public void testCollectionPartCollectionIdIncompleteButValid() {
    // valid
    Collection collection = new Collection();
    collection.setCollectionName("Birds");
    collection.setCollectionId(null);
    eml.addCollection(collection);
    assertTrue(validator.isValid(resource, MetadataSection.COLLECTIONS_SECTION));
  }

  @Test
  public void testCollectionPartParentCollectionIdIncompleteButValid() {
    // valid
    Collection collection = new Collection();
    collection.setCollectionName("Birds");
    collection.setParentCollectionId(null);
    eml.addCollection(collection);
    assertTrue(validator.isValid(resource, MetadataSection.COLLECTIONS_SECTION));
  }

  @Test
  public void testCollectionCuratorialIncomplete() {
    // invalid - doesn't exceed min length 2
    eml.getJgtiCuratorialUnits().get(0).setUnitType("m");
    assertFalse(validator.isValid(resource, MetadataSection.COLLECTIONS_SECTION));
  }

  @Test
  public void testPhysicalPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.PHYSICAL_SECTION));
  }

  @Test
  public void testPhysicalPartDistributionUrlInvalid() {
    // invalid
    eml.getPhysicalData().get(0).setDistributionUrl("[hppt]");
    assertFalse(validator.isValid(resource, MetadataSection.PHYSICAL_SECTION));
  }

  @Test
  public void testPhysicalPartNameIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setName(null);
    assertFalse(validator.isValid(resource, MetadataSection.PHYSICAL_SECTION));
  }

  @Test
  public void testPhysicalCharSetIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setCharset(null);
    assertFalse(validator.isValid(resource, MetadataSection.PHYSICAL_SECTION));
  }

  @Test
  public void testPhysicalDistributionIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setDistributionUrl(null);
    assertFalse(validator.isValid(resource, MetadataSection.PHYSICAL_SECTION));
  }

  @Test
  public void testPhysicalFormatIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setFormat(null);
    assertFalse(validator.isValid(resource, MetadataSection.PHYSICAL_SECTION));
  }

  @Test
  public void testPhysicalResourceHomepageInvalid() {
    // invalid
    eml.setDistributionUrl("[]");
    assertFalse(validator.isValid(resource, MetadataSection.PHYSICAL_SECTION));
  }

  @Test
  public void testAdditionalPart() {
    // valid
    assertTrue(validator.isValid(resource, MetadataSection.ADDITIONAL_SECTION));
  }

  @Test
  public void testAdditionalPartAlternateIdIncomplete() {
    // invalid
    eml.getAlternateIdentifiers().set(0, "1");
    assertFalse(validator.isValid(resource, MetadataSection.ADDITIONAL_SECTION));
  }

  /**
   * All sections of new Eml instance are empty, except the basic mandatory elements. Validation is skipped for each
   * section other than the basic metadata section, and the document is valid.
   */
  @Test
  public void testAreAllSectionsValid() {
    Resource resource = new Resource();
    Eml empty = new Eml();
    resource.setEml(empty);

    // populate basic mandatory elements
    Organisation organisation = new Organisation();
    organisation.setName("NHM");
    organisation.setKey(UUID.randomUUID().toString());
    resource.setOrganisation(organisation);

    resource.setCoreType(Resource.CoreRowType.CHECKLIST.toString());
    resource.setUpdateFrequency(MaintenanceUpdateFrequency.ANNUALLY.toString());

    empty.setTitle("Title");
    empty.addDescriptionPara("Description");
    empty.setMetadataLanguage(Language.FRENCH.getIso3LetterCode());
    empty.setLanguage(Language.SPANISH.getIso3LetterCode());
    empty.setIntellectualRights("CC-BY");

    Agent agent = new Agent();
    agent.setLastName("Smith");
    empty.addContact(agent);
    empty.addCreator(agent);
    empty.addMetadataProvider(agent);

    assertTrue(validator.areAllSectionsValid(action, resource));
  }
}
