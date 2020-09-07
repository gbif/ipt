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

import org.gbif.ipt.struts2.converter.LongitudeFormatConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for convertToString method in LatitudetudeFormatConverter and LongitudeFormatConverter classes.
 *
 * @author julieth
 */
@RunWith(value = Parameterized.class)
public class CoordinateFormatConverterToStringTest {

  // Variables used in convertToStringTest method
  private String expectedString;
  private Double firstTestValue;

  public CoordinateFormatConverterToStringTest(String expectedString, Double firstTestValue) {
    this.expectedString = expectedString;
    this.firstTestValue = firstTestValue;
  }

  @Parameters
  public static Collection<Object[]> getTestParameters() {
    // Set of objects, each object contains: A expected value (String) and a value to test (double).
    // (expectedString, firstTestValue).
    Collection<Object[]> list = new ArrayList<Object[]>();
    list.add(new Object[] {"0.0", 0.0});
    list.add(new Object[] {"-0.0", -0.0});
    list.add(new Object[] {"-180.0", -180.0});
    list.add(new Object[] {"32.025", 32.025});
    return list;
  }

  @Test
  public void convertToStringTest() {
    // The object created can be of the LongitudeFormatConverter or LatitudeFornatConverter class.
    LongitudeFormatConverter longitudeFormat = new LongitudeFormatConverter();
    assertEquals(expectedString, longitudeFormat.convertToString(new HashMap(), firstTestValue));
  }
}
