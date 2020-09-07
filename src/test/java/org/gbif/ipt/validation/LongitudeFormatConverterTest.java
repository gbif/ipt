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

import com.opensymphony.xwork2.conversion.TypeConversionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test for convertFromString method in LongitudeFormatConverter class.
 *
 * @author julieth
 */
@RunWith(value = Parameterized.class)
public class LongitudeFormatConverterTest {

  // Variables used in convertFromStringTest method
  private Double expectedDouble;
  private String[] firstTestValue;


  public LongitudeFormatConverterTest(Double expectedDouble, String[] firstTestValue) {
    this.expectedDouble = expectedDouble;
    this.firstTestValue = firstTestValue;
  }

  @Parameters
  public static Collection<Object[]> getTestParameters() {
    // Set of objects, each object contains: A expected value (double) and a value to test (String values[]).
    // (expectedDouble, firstTestvalue).
    Collection<Object[]> list = new ArrayList<Object[]>();
    list.add(new Object[] {-180.0, new String[] {"-180"}});
    list.add(new Object[] {180.0, new String[] {"180"}});
    list.add(new Object[] {-0.0, new String[] {"-0"}});
    list.add(new Object[] {0.0, new String[] {"0"}});
    list.add(new Object[] {0.7, new String[] {",7"}});
    list.add(new Object[] {-1.1, new String[] {"-1.1"}});
    list.add(new Object[] {-1.1, new String[] {"-1,1"}});
    list.add(new Object[] {null, new String[] {""}});
    return list;
  }

  @Test
  public void convertFromStringTest() {
    LongitudeFormatConverter longitudeFormat = new LongitudeFormatConverter();
    assertEquals(expectedDouble, longitudeFormat.convertFromString(new HashMap(), firstTestValue, null));
  }

  @Test(expected = TypeConversionException.class)
  public void convertFromStringTestTypeConversionException() {
    LongitudeFormatConverter longitudeFormat = new LongitudeFormatConverter();
    // Fails if the value exceeds the minimum longitude
    assertNull(longitudeFormat.convertFromString(new HashMap(), new String[] {"-180.01"}, null));
    // Fails if the value exceeds the maximum longitude
    assertNull(longitudeFormat.convertFromString(new HashMap(), new String[] {"180.01"}, null));
    // Fails if the value is a String
    assertNull(longitudeFormat.convertFromString(new HashMap(), new String[] {"abc"}, null));
    assertNull(longitudeFormat.convertFromString(new HashMap(), new String[] {"@#$%"}, null));
    assertNull(longitudeFormat.convertFromString(new HashMap(), new String[] {" "}, null));
  }

}
