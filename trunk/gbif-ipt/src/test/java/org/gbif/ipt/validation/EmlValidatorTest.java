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
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.PhysicalData;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EmlValidatorTest {

  private EmlValidator validator;

  @Before
  public void before() {
    AppConfig mockCfg = mock(AppConfig.class);
    SimpleTextProvider mockTextProvider = mock(SimpleTextProvider.class);
    RegistrationManager mockRegistrationManager = mock(RegistrationManager.class);
    validator = new EmlValidator(mockCfg, mockRegistrationManager, mockTextProvider);
  }

  /*
  * Validate the integer
  */
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

  /*
   * Validate the URL
   */
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
  public void testPhysicalPart() {
    Eml eml = new Eml();
    PhysicalData data1 = new PhysicalData();
    data1.setCharset("UTF-8");
    data1.setDistributionUrl("http://download.org/excel/1");
    data1.setFormat("Excel");
    data1.setFormatVersion("9.0");
    data1.setName("Excel spreadsheet complete copy");
    eml.getPhysicalData().add(data1);
    assertTrue(validator.isValid(eml, "physical"));
    // change the format version to be invalid (non decimal)
    data1.setFormatVersion("9.0.27");
    eml.getPhysicalData().add(data1);
    assertFalse(validator.isValid(eml, "physical"));
  }
}
