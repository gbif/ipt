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

import org.gbif.ipt.utils.CoordinateUtils;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for decToDms method in CoordinateUtils class.
 *
 * @author julieth
 */
@RunWith(value = Parameterized.class)
public class CoordinateUtilsTest {

  // Variables used in decToDms method
  private String expectedString;
  private Double firstDoubleValue;
  private String secondStringValue;
  private static String degreeSign = "\u00B0";

  public CoordinateUtilsTest(String expectedString, Double firstDoubleValue, String secondStringValue) {
    this.expectedString = expectedString;
    this.firstDoubleValue = firstDoubleValue;
    this.secondStringValue = secondStringValue;
  }

  @Parameters
  public static Collection<Object[]> getTestParameters() {
    // Set of objects, each object contains: A expected value (String), a first value to test (double) and a second
    // value to test (String)
    // (expectedString, firstDoubleValue, secondStringValue).

    Collection<Object[]> list = new ArrayList<Object[]>();
    // LATITUDE test
    list.add(new Object[] {"10" + degreeSign + "3'0''S", -10.05, CoordinateUtils.LATITUDE});
    list.add(new Object[] {"10" + degreeSign + "3'0''N", 10.05, CoordinateUtils.LATITUDE});
    list.add(new Object[] {"0" + degreeSign + "0'0''N", 0.0, CoordinateUtils.LATITUDE});
    list.add(new Object[] {"1" + degreeSign + "0'0''N", 1.0, CoordinateUtils.LATITUDE});
    list.add(new Object[] {"176" + degreeSign + "11'60''N", 176.20, CoordinateUtils.LATITUDE});
    list.add(new Object[] {"0" + degreeSign + "58'48''N", .98, CoordinateUtils.LATITUDE});
    // LONGITUDE test
    list.add(new Object[] {"10" + degreeSign + "3'0''W", -10.05, CoordinateUtils.LONGITUDE});
    list.add(new Object[] {"10" + degreeSign + "3'0''E", 10.05, CoordinateUtils.LONGITUDE});
    list.add(new Object[] {"176" + degreeSign + "11'60''E", 176.20, CoordinateUtils.LONGITUDE});
    list.add(new Object[] {"0" + degreeSign + "0'0''E", 0.0, CoordinateUtils.LONGITUDE});
    list.add(new Object[] {"1" + degreeSign + "0'0''E", 1.0, CoordinateUtils.LONGITUDE});
    list.add(new Object[] {"0" + degreeSign + "58'48''E", .98, CoordinateUtils.LONGITUDE});

    list.add(new Object[] {"", 1.0, ""});
    list.add(new Object[] {"", 1.0, null});

    return list;
  }

  @Test
  public void decToDmsTest() {
    assertEquals(expectedString, CoordinateUtils.decToDms(firstDoubleValue, secondStringValue));
  }
}
