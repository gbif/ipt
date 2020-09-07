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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.opensymphony.xwork2.conversion.TypeConversionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test for the convertFromString method in LatitudeFormatConverter class.
 */
@RunWith(value = Parameterized.class)
public class LatitudeFormatConverterTest {

  // Variables used in convertFromStringTest method
  private Double expectedDouble;
  private String[] firstTestValue;


  public LatitudeFormatConverterTest(Double expectedDouble, String[] firstTestValue) {
    this.expectedDouble = expectedDouble;
    this.firstTestValue = firstTestValue;
  }

  @Parameters
  public static Collection<Object[]> getTestParameters() {
    // Object list in which each object contains: A expected value (double) and an incoming value to test (String[]).
    // (expectedDouble, firstTestvalue).
    Collection<Object[]> list = new ArrayList<Object[]>();
    list.add(new Object[] {4.5, new String[] {"4.5"}});
    list.add(new Object[] {4.5, new String[] {"4,5"}});
    list.add(new Object[] {90.0, new String[] {"90.0"}});
    list.add(new Object[] {-90.0, new String[] {"-90.0"}});
    list.add(new Object[] {-89.99999, new String[] {"-89.99999"}});
    list.add(new Object[] {-0.0, new String[] {"-0"}});
    list.add(new Object[] {0.0, new String[] {"0.0"}});
    list.add(new Object[] {0.7, new String[] {",7"}});
    list.add(new Object[] {null, new String[] {""}});
    return list;
  }

  @Test
  public void convertFromStringTest() {
    LatitudeFormatConverter latitudeFormat = new LatitudeFormatConverter();
    // latitudeFormat.
    assertEquals(expectedDouble, latitudeFormat.convertFromString(new HashMap(), firstTestValue, null));
  }

  @Test(expected = TypeConversionException.class)
  public void convertFromStringTestTypeConvertionException() {
    LatitudeFormatConverter latitudeFormat = new LatitudeFormatConverter();
    // Fails if the value exceeds the minimum latitude
    assertNull(latitudeFormat.convertFromString(new HashMap(), new String[] {"-90.01"}, null));
    // Fails if the value exceeds the maximum latitude
    assertNull(latitudeFormat.convertFromString(new HashMap(), new String[] {"90.01"}, null));
    // Fails if the value is a String
    assertNull(latitudeFormat.convertFromString(new HashMap(), new String[] {"abc"}, null));
    assertNull(latitudeFormat.convertFromString(new HashMap(), new String[] {"@#$%"}, null));
    assertNull(latitudeFormat.convertFromString(new HashMap(), new String[] {" "}, null));
  }
}
