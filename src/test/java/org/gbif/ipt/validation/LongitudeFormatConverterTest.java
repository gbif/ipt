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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.opensymphony.xwork2.conversion.TypeConversionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for convertFromString method in LongitudeFormatConverter class.
 */
public class LongitudeFormatConverterTest {

  public static Stream<Arguments> getTestParameters() {
    // Set of objects, each object contains: A expected value (double) and a value to test (String values[]).
    // (expectedDouble, firstTestvalue).
    return Stream.of(
        Arguments.of(-180.0, new String[] {"-180"}),
        Arguments.of(180.0, new String[] {"180"}),
        Arguments.of(-0.0, new String[] {"-0"}),
        Arguments.of(0.0, new String[] {"0"}),
        Arguments.of(0.7, new String[] {",7"}),
        Arguments.of(-1.1, new String[] {"-1.1"}),
        Arguments.of(-1.1, new String[] {"-1,1"}),
        Arguments.of(null, new String[] {""})
    );
  }

  @ParameterizedTest
  @MethodSource("getTestParameters")
  public void convertFromStringTest(Double expectedDouble, String[] firstTestValue) {
    LongitudeFormatConverter longitudeFormat = new LongitudeFormatConverter();
    assertEquals(expectedDouble, longitudeFormat.convertFromString(new HashMap<>(), firstTestValue, null));
  }

  @Test
  public void convertFromStringTestTypeConversionException() {
    LongitudeFormatConverter longitudeFormat = new LongitudeFormatConverter();
    // Fails if the value exceeds the minimum longitude
    assertThrows(TypeConversionException.class,
        () -> longitudeFormat.convertFromString(new HashMap<>(), new String[] {"-180.01"}, null));
    // Fails if the value exceeds the maximum longitude
    assertThrows(TypeConversionException.class,
        () -> longitudeFormat.convertFromString(new HashMap<>(), new String[] {"180.01"}, null));
    // Fails if the value is a String
    assertThrows(TypeConversionException.class,
        () -> longitudeFormat.convertFromString(new HashMap<>(), new String[] {"abc"}, null));
    assertThrows(TypeConversionException.class,
        () -> longitudeFormat.convertFromString(new HashMap<>(), new String[] {"@#$%"}, null));
    assertThrows(TypeConversionException.class,
        () -> longitudeFormat.convertFromString(new HashMap<>(), new String[] {" "}, null));
  }
}
