/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.validation;

import org.gbif.ipt.utils.CoordinateUtils;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for decToDms method in CoordinateUtils class.
 */
public class CoordinateUtilsTest {

  private static String degreeSign = "\u00B0";

  public static Stream<Arguments> getTestParameters() {
    // Set of objects, each object contains: A expected value (String), a first value to test (double) and a second
    // value to test (String)
    // (expectedString, firstDoubleValue, secondStringValue).
    return Stream.of(
        // LATITUDE test
        Arguments.of("10" + degreeSign + "3'0''S", -10.05, CoordinateUtils.LATITUDE),
        Arguments.of("10" + degreeSign + "3'0''N", 10.05, CoordinateUtils.LATITUDE),
        Arguments.of("0" + degreeSign + "0'0''N", 0.0, CoordinateUtils.LATITUDE),
        Arguments.of("1" + degreeSign + "0'0''N", 1.0, CoordinateUtils.LATITUDE),
        Arguments.of("176" + degreeSign + "11'60''N", 176.20, CoordinateUtils.LATITUDE),
        Arguments.of("0" + degreeSign + "58'48''N", .98, CoordinateUtils.LATITUDE),
        // LONGITUDE test
        Arguments.of("10" + degreeSign + "3'0''W", -10.05, CoordinateUtils.LONGITUDE),
        Arguments.of("10" + degreeSign + "3'0''E", 10.05, CoordinateUtils.LONGITUDE),
        Arguments.of("176" + degreeSign + "11'60''E", 176.20, CoordinateUtils.LONGITUDE),
        Arguments.of("0" + degreeSign + "0'0''E", 0.0, CoordinateUtils.LONGITUDE),
        Arguments.of("1" + degreeSign + "0'0''E", 1.0, CoordinateUtils.LONGITUDE),
        Arguments.of("0" + degreeSign + "58'48''E", .98, CoordinateUtils.LONGITUDE),

        Arguments.of("", 1.0, ""),
        Arguments.of("", 1.0, null)
    );
  }

  @ParameterizedTest
  @MethodSource("getTestParameters")
  public void decToDmsTest(String expectedString, Double firstDoubleValue, String secondStringValue) {
    assertEquals(expectedString, CoordinateUtils.decToDms(firstDoubleValue, secondStringValue));
  }
}
