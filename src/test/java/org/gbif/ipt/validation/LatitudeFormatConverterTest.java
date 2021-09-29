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

import java.util.HashMap;
import java.util.stream.Stream;

import com.opensymphony.xwork2.conversion.TypeConversionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for the convertFromString method in LatitudeFormatConverter class.
 */
public class LatitudeFormatConverterTest {

  public static Stream<Arguments> getTestParameters() {
    // Object list in which each object contains: A expected value (double) and an incoming value to test (String[]).
    // (expectedDouble, firstTestvalue).
    return Stream.of(
        Arguments.of(4.5, new String[] {"4.5"}),
        Arguments.of(4.5, new String[] {"4,5"}),
        Arguments.of(90.0, new String[] {"90.0"}),
        Arguments.of(-90.0, new String[] {"-90.0"}),
        Arguments.of(-89.99999, new String[] {"-89.99999"}),
        Arguments.of(-0.0, new String[] {"-0"}),
        Arguments.of(0.0, new String[] {"0.0"}),
        Arguments.of(0.7, new String[] {",7"}),
        Arguments.of(null, new String[] {""})
    );
  }

  @ParameterizedTest
  @MethodSource("getTestParameters")
  public void convertFromStringTest(Double expectedDouble, String[] firstTestValue) {
    LatitudeFormatConverter latitudeFormat = new LatitudeFormatConverter();
    // latitudeFormat.
    assertEquals(expectedDouble, latitudeFormat.convertFromString(new HashMap<>(), firstTestValue, null));
  }

  @Test
  public void convertFromStringTestTypeConvertionException() {
    LatitudeFormatConverter latitudeFormat = new LatitudeFormatConverter();
    // Fails if the value exceeds the minimum latitude
    assertThrows(TypeConversionException.class,
        () -> latitudeFormat.convertFromString(new HashMap<>(), new String[]{"-90.01"}, null));
    // Fails if the value exceeds the maximum latitude
    assertThrows(TypeConversionException.class,
        () -> latitudeFormat.convertFromString(new HashMap<>(), new String[]{"90.01"}, null));
    // Fails if the value is a String
    assertThrows(TypeConversionException.class,
        () -> latitudeFormat.convertFromString(new HashMap<>(), new String[]{"abc"}, null));
    assertThrows(TypeConversionException.class,
        () -> latitudeFormat.convertFromString(new HashMap<>(), new String[]{"@#$%"}, null));
    assertThrows(TypeConversionException.class,
        () -> latitudeFormat.convertFromString(new HashMap<>(), new String[]{" "}, null));
  }
}
