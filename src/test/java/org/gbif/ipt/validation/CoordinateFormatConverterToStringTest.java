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
package org.gbif.ipt.validation;

import org.gbif.ipt.struts2.converter.LongitudeFormatConverter;

import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for convertToString method in LatitudetudeFormatConverter and LongitudeFormatConverter classes.
 */
public class CoordinateFormatConverterToStringTest {

  public static Stream<Arguments> getTestParameters() {
    // Set of objects, each object contains: A expected value (String) and a value to test (double).
    // (expectedString, firstTestValue).
    return Stream.of(
        Arguments.of("0.0", 0.0),
        Arguments.of("-0.0", -0.0),
        Arguments.of("-180.0", -180.0),
        Arguments.of("32.025", 32.025)
    );
  }

  @ParameterizedTest
  @MethodSource("getTestParameters")
  public void convertToStringTest(String expectedString, Double firstTestValue) {
    // The object created can be of the LongitudeFormatConverter or LatitudeFornatConverter class.
    LongitudeFormatConverter longitudeFormat = new LongitudeFormatConverter();
    assertEquals(expectedString, longitudeFormat.convertToString(new HashMap<>(), firstTestValue));
  }
}
