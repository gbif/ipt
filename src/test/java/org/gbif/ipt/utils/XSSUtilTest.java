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
package org.gbif.ipt.utils;

import java.util.stream.Stream;

import org.gbif.ipt.IptBaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XSSUtilTest extends IptBaseTest {

  /**
   * Additional requests to test can be added here.
   */
  public static Stream<Arguments> data() {
    return Stream.of(
        // contain xss
        Arguments.of(
            "http://localhost:8090/ipt/about.do?request_locale=\"'><script>alert(6227)</script>&email=\"'><script>alert(6227)</script>&password=\"'><script>alert(6227)</script>&login-submit=\"'><script>alert(6227)</script>",
            true,
            "http://localhost:8090/ipt/about.do?request_locale=\"'>&email=\"'>&password=\"'>&login-submit=\"'>"),
        Arguments.of(
            "http://www.gbif.org/dataset/search?q=\"><script>prompt('XSSPOSED')</script>",
            true,
            "http://www.gbif.org/dataset/search?q=\">"),
        Arguments.of(
            "http://www.gbif.org/index.php?name=<script>window.onload = function() {var link=document.getElementsByTagName(\"a\");link[0].href=\"http://not-real-xssattackexamples.com/\";}</script>",
            true,
            "http://www.gbif.org/index.php?name="),
        Arguments.of(
            "http://www.gbif.org/dataset/search?q=<iframe>frame it</iframe>",
            true,
            "http://www.gbif.org/dataset/search?q="),
        Arguments.of(
            "http://www.gbif.org/login.do?email=\"><img src=x onerror=prompt(/XSS/)>",
            true,
            "http://www.gbif.org/login.do?email=\">"),
        Arguments.of(
            "http://gbif.org/login.do?email=\"><img src=x onmouseover=prompt(/XSS/)>",
            true,
            "http://gbif.org/login.do?email=\">"),
        // do not contain xss
        Arguments.of(
            "http://gbif.org/login.do?email=sven@gbif.org&password=xyz",
            false,
            "http://gbif.org/login.do?email=sven@gbif.org&password=xyz"),
        Arguments.of("http://www.gbif.org/ipt", false, null),
        Arguments.of("http://api.gbif.org/v1/occurrence/search?year=1800", false, null)
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testContainsXSS(String value, boolean containsXSS, String stripXSSValue) {
    assertEquals(containsXSS, XSSUtil.containsXSS(value));

    if (containsXSS) {
      assertEquals(stripXSSValue, XSSUtil.stripXSS(value));
    }
  }
}
