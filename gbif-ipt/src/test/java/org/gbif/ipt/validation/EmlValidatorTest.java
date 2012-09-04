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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.BBox;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.utils.file.FileUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EmlValidatorTest {

  private EmlValidator validator;
  private Eml eml;
  private Agent badAgent;

  @Before
  public void before() throws IOException, SAXException {
    AppConfig mockCfg = mock(AppConfig.class);
    SimpleTextProvider mockTextProvider = mock(SimpleTextProvider.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    // instance of EmlValidator using mock AppConfig, RegistrationManager, and SimpleTextProvider
    validator = new EmlValidator(mockCfg, mockRegistrationManager, mockTextProvider);

    // load sample eml
    eml = EmlFactory.build(FileUtils.classpathStream("data/eml.xml"));

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
    assertNull(EmlValidator.formatURL("- - - "));
    assertNull(EmlValidator.formatURL("//**##"));
    assertNull(EmlValidator.formatURL("      "));
    assertNull(EmlValidator.formatURL("ftp://ftp.gbif.org //h"));
    assertNotNull(EmlValidator.formatURL("www.gbif.com"));
    assertNotNull(EmlValidator.formatURL("torrent://www.gbif.org"));
    assertNotNull(EmlValidator.formatURL("ftp://ftp.gbif.org"));
    assertNotNull(EmlValidator.formatURL("http://www.gbif.org"));
  }

  @Test
  public void testBasicPart() {
    // valid
    assertTrue(validator.isValid(eml, "basic"));
  }

  @Test
  public void testBasicPartTitleMissing() {
    // invalid
    eml.setTitle(null);
    assertFalse(validator.isValid(eml, "basic"));
  }

  @Test
  public void testBasicPartDescriptionMissing() {
    // invalid
    eml.setDescription(null);
    assertFalse(validator.isValid(eml, "basic"));
    eml.setDescription("shrt");
    assertFalse(validator.isValid(eml, "basic"));
    // valid
    eml.setDescription("long_enough");
    assertTrue(validator.isValid(eml, "basic"));
  }

  @Test
  public void testBasicPartContactIncomplete() {
    // invalid
    eml.setContact(badAgent);
    assertFalse(validator.isValid(eml, "basic"));
    badAgent.setLastName("Smith");
    // valid
    assertTrue(validator.isValid(eml, "basic"));
  }

  @Test
  public void testBasicPartCreatorIncomplete() {
    // invalid
    eml.setResourceCreator(badAgent);
    assertFalse(validator.isValid(eml, "basic"));
    badAgent.setLastName("Smith");
    // valid
    assertTrue(validator.isValid(eml, "basic"));
  }

  @Test
  public void testBasicPartMetaProviderIncomplete() {
    // invalid
    eml.setMetadataProvider(badAgent);
    assertFalse(validator.isValid(eml, "basic"));
    badAgent.setLastName("Smith");
    // valid
    assertTrue(validator.isValid(eml, "basic"));
  }

  @Test
  public void testGeoPart() {
    // valid
    assertTrue(validator.isValid(eml, "geocoverage"));
  }

  @Test
  public void testGeoPartDescriptionIncomplete() {
    // invalid
    eml.getGeospatialCoverages().get(0).setDescription("");
    assertFalse(validator.isValid(eml, "geocoverage"));
  }

  @Test
  public void testGeoPartBoundingBoxIncomplete() {
    // invalid
    BBox box = new BBox();
    box.getMin().setLongitude(null);
    eml.getGeospatialCoverages().get(0).setBoundingCoordinates(box);
    assertFalse(validator.isValid(eml, "geocoverage"));
  }

  @Test
  public void testTaxCovPart() {
    // valid
    assertTrue(validator.isValid(eml, "taxcoverage"));
  }

  @Test
  public void testTaxCovPartScientificNameIncomplete() {
    // invalid
    eml.getTaxonomicCoverages().get(0).getTaxonKeywords().get(0).setScientificName(null);
    assertFalse(validator.isValid(eml, "taxcoverage"));
  }

  @Test
  public void testTempCovPart() {
    // valid
    assertTrue(validator.isValid(eml, "tempcoverage"));
  }

  @Test
  public void testTempCovPartRangeIncomplete() throws ParseException {
    // invalid
    eml.getTemporalCoverages().get(0).setStartDate(null);
    eml.getTemporalCoverages().get(0).setEndDate(null);
    assertFalse(validator.isValid(eml, "tempcoverage"));
  }

  @Test
  public void testKeywordsPart() {
    // valid
    assertTrue(validator.isValid(eml, "keywords"));
  }

  @Test
  public void testKeywordsPartThesaurusIncomplete() {
    // invalid
    eml.getKeywords().get(0).setKeywordThesaurus(null);
    assertFalse(validator.isValid(eml, "keywords"));
  }

  @Test
  public void testKeywordsPartKeywordListIncomplete() {
    // invalid
    eml.getKeywords().get(0).setKeywords(new ArrayList<String>());
    assertFalse(validator.isValid(eml, "keywords"));
  }

  @Test
  public void testPartiesPart() {
    // valid
    assertTrue(validator.isValid(eml, "parties"));
  }

  @Test
  public void testPartiesPartIncomplete() {
    // invalid
    eml.getAssociatedParties().clear();
    eml.getAssociatedParties().add(badAgent);
    assertFalse(validator.isValid(eml, "parties"));
  }

  @Test
  public void testProjectPart() {
    // valid
    assertTrue(validator.isValid(eml, "project"));
  }

  @Test
  public void testProjectPartTitleIncomplete() {
    // invalid
    eml.getProject().setTitle(null);
    assertFalse(validator.isValid(eml, "project"));
  }

  @Test
  public void testProjectPartPersonnelIncomplete() {
    // invalid
    eml.getProject().setPersonnel(badAgent);
    assertFalse(validator.isValid(eml, "project"));
  }

  @Test
  public void testMethodsPart() {
    // valid
    assertTrue(validator.isValid(eml, "methods"));
  }

  @Test
  public void testMethodsPartStudyExtentIncomplete() {
    // invalid
    eml.setSampleDescription("Non empty");
    eml.setStudyExtent("");
    assertFalse(validator.isValid(eml, "methods"));
  }

  @Test
  public void testMethodsPartSampleDescriptionIncomplete() {
    // invalid
    eml.setSampleDescription("");
    eml.setStudyExtent("Non empty");
    assertFalse(validator.isValid(eml, "methods"));
  }

  @Test
  public void testMethodsPartStepIncomplete() {
    // invalid
    eml.getMethodSteps().set(0, "");
    assertFalse(validator.isValid(eml, "methods"));
  }

  @Test
  public void testCitationsPart() {
    // valid
    assertTrue(validator.isValid(eml, "citations"));
  }

  @Test
  public void testCitationsPartCitationMissing() {
    // invalid
    eml.getCitation().setCitation("");
    assertFalse(validator.isValid(eml, "citations"));
    eml.getCitation().setCitation(null);
    assertFalse(validator.isValid(eml, "citations"));
  }

  @Test
  public void testCitationsPartBiblioCitationMissing() {
    // invalid
    eml.getBibliographicCitations().get(0).setCitation(null);
    assertFalse(validator.isValid(eml, "citations"));
  }

  @Test
  public void testCollectionPart() {
    // valid
    assertTrue(validator.isValid(eml, "collections"));
  }

  @Test
  public void testCollectionPartCollectionNameIncomplete() {
    // invalid
    eml.setCollectionName(null);
    assertFalse(validator.isValid(eml, "collections"));
  }

  @Test
  public void testCollectionPartCollectionIdIncomplete() {
    // invalid
    eml.setCollectionId(null);
    assertFalse(validator.isValid(eml, "collections"));
  }

  @Test
  public void testCollectionPartParentCollectionIdIncomplete() {
    // invalid
    eml.setParentCollectionId(null);
    assertFalse(validator.isValid(eml, "collections"));
  }

  @Test
  public void testCollectionCuratorialIncomplete() {
    // invalid - doesn't exceed min length 2
    eml.getJgtiCuratorialUnits().get(0).setUnitType("m");
    assertFalse(validator.isValid(eml, "collections"));
  }

  @Test
  public void testPhysicalPart() {
    // valid
    assertTrue(validator.isValid(eml, "physical"));
  }

  @Test
  public void testPhysicalPartDistributionUrlInvalid() {
    // invalid
    eml.getPhysicalData().get(0).setDistributionUrl("[hppt]");
    assertFalse(validator.isValid(eml, "physical"));
  }

  @Test
  public void testPhysicalPartNameIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setName(null);
    assertFalse(validator.isValid(eml, "physical"));
  }

  @Test
  public void testPhysicalCharSetIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setCharset(null);
    assertFalse(validator.isValid(eml, "physical"));
  }

  @Test
  public void testPhysicalDistributionIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setDistributionUrl(null);
    assertFalse(validator.isValid(eml, "physical"));
  }

  @Test
  public void testPhysicalFormatIncomplete() {
    // invalid
    eml.getPhysicalData().get(0).setFormat(null);
    assertFalse(validator.isValid(eml, "physical"));
  }

  @Test
  public void testPhysicalResourceHomepageInvalid() {
    // invalid
    eml.setHomepageUrl("[]");
    assertFalse(validator.isValid(eml, "physical"));
  }

  @Test
  public void testAdditionalPart() {
    // valid
    assertTrue(validator.isValid(eml, "additional"));
  }

  @Test
  public void testAdditionalPartAlternateIdIncomplete() {
    // invalid
    eml.getAlternateIdentifiers().set(0, "1");
    assertFalse(validator.isValid(eml, "additional"));
  }
}
