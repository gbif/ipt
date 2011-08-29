/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
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

import org.gbif.ipt.struts2.converter.LatitudeFormatConverter;
import org.gbif.ipt.struts2.converter.LongitudeFormatConverter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit test for latitudeFormatConverter class.
 * 
 * @author julieth
 */
public class LatitudeFormatConverterTest {

  @Test
  public void testConvertFromString() {
    LatitudeFormatConverter latitudeFormat = new LatitudeFormatConverter();
    // The decimal will be in the range -90 to 90
    // The commas are converted in periods
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"-90"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"90"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"-1,1"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"1,1"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"-1.1"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"1.1"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"-0,"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"0"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {",7"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {".7"}, null)));
    assertNotNull((latitudeFormat.convertFromString(null, new String[] {"87,3"}, null)));
    // Values outside the range are nulls
    assertNull((latitudeFormat.convertFromString(null, new String[] {"abc"}, null)));
    assertNull((latitudeFormat.convertFromString(null, new String[] {";/&@#,."}, null)));
    assertNull((latitudeFormat.convertFromString(null, new String[] {"0,,56"}, null)));
    assertNull((latitudeFormat.convertFromString(null, new String[] {""}, null)));
    assertNull((latitudeFormat.convertFromString(null, new String[] {" "}, null)));
    assertNull((latitudeFormat.convertFromString(null, new String[] {"-90.01"}, null)));
    assertNull((latitudeFormat.convertFromString(null, new String[] {"90.01"}, null)));
  }

  @Test
  public void testConvertToStringTest() {
    // Converts Double to String
    LongitudeFormatConverter longitudeFormat = new LongitudeFormatConverter();
    assertEquals((longitudeFormat.convertToString(null, new Double("4.5"))), "4.5");
    assertEquals((longitudeFormat.convertToString(null, new Double("90.0"))), "90.0");
    assertEquals((longitudeFormat.convertToString(null, new Double("90.00"))), "90.0");
    assertEquals((longitudeFormat.convertToString(null, new Double("-90.00"))), "-90.0");
    assertEquals((longitudeFormat.convertToString(null, new Double("-89.9999999999"))), "-89.9999999999");
    assertEquals((longitudeFormat.convertToString(null, "")), null);
    assertEquals((longitudeFormat.convertToString(null, "abc")), null);
    assertEquals((longitudeFormat.convertToString(null, "4.5")), null);
  }
}
