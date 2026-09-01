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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LicenseConverterUtilsTest {

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n", "  \t \n "})
  @DisplayName("Blank or null input returns empty string")
  void testFullLicenseToShort_blankInput(String input) {
    assertEquals("", LicenseConverterUtils.fullLicenseToShort(input));
  }

  @Test
  @DisplayName("CC0 simple text")
  void testFullLicenseToShort_ccZero_exactPhrase() {
    assertEquals("CC0-1.0", LicenseConverterUtils.fullLicenseToShort("Public Domain (CC0 1.0)"));
  }

  @Test
  @DisplayName("CC-BY simple text")
  void testFullLicenseToShort_ccBy_exactPhrase() {
    assertEquals("CC-BY-4.0",
        LicenseConverterUtils.fullLicenseToShort("Creative Commons Attribution (CC-BY) 4.0 License"));
  }

  @Test
  @DisplayName("CC-BY-NC simple text")
  void testFullLicenseToShort_ccByNc_exactPhrase() {
    assertEquals("CC-BY-NC-4.0",
        LicenseConverterUtils.fullLicenseToShort("Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License"));
  }

  @Test
  @DisplayName("CC-BY from intellectual rights")
  void testFullLicenseToShort_ccBy_fromIntellectualRights() {
    String fullLicense = "This work is licensed under a "
        + "<a href=\"http://creativecommons.org/licenses/by/4.0/legalcode\">"
        + "Creative Commons Attribution (CC-BY 4.0) License</a>.";

    assertEquals("CC-BY-4.0", LicenseConverterUtils.fullLicenseToShort(fullLicense));
  }

  @Test
  @DisplayName("CC-BY-NC from intellectual rights")
  void testFullLicenseToShort_ccByNc_fromIntellectualRights() {
    String fullLicense = "This work is licensed under a "
        + "<a href=\"http://creativecommons.org/licenses/by-nc/4.0/legalcode\">"
        + "Creative Commons Attribution Non Commercial (CC-BY-NC 4.0) License</a>.";

    assertEquals("CC-BY-NC-4.0", LicenseConverterUtils.fullLicenseToShort(fullLicense));
  }

  @Test
  @DisplayName("CC0 from intellectual rights")
  void testFullLicenseToShort_fromIntellectualRights() {
    String fullLicense = "This work is licensed under a "
        + "<a href=\"http://creativecommons.org/publicdomain/zero/1.0/legalcode\">"
        + "Public Domain (CC0 1.0)</a>.";

    assertEquals("CC0-1.0", LicenseConverterUtils.fullLicenseToShort(fullLicense));
  }

  @Test
  @DisplayName("CC-BY license code appears without version number")
  void testFullLicenseToShort_ccByNoVersion() {
    assertEquals("CC-BY-4.0",
        LicenseConverterUtils.fullLicenseToShort("Licensed under CC-BY"));
  }

  @Test
  @DisplayName("CC0 license code appears without version number")
  void testFullLicenseToShort_ccZeroNoVersion() {
    assertEquals("CC0-1.0", LicenseConverterUtils.fullLicenseToShort("Released under CC0"));
  }

  @Test
  @DisplayName("CC-BY-NC must not be misclassified as CC-BY, since CC-BY-NC contains 'CC-BY'")
  void testFullLicenseToShort_ccByNcNotConfusedWithCcBy() {
    String fullLicense = "Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License";
    assertEquals("CC-BY-NC-4.0", LicenseConverterUtils.fullLicenseToShort(fullLicense));
  }

  @Test
  @DisplayName("Compact CC-BY-NC form without full descriptive text still resolves to NC variant")
  void testFullLicenseToShort_ccByNcCompactForm() {
    assertEquals("CC-BY-NC-4.0", LicenseConverterUtils.fullLicenseToShort("CC-BY-NC 4.0"));
  }

  @Test
  @DisplayName("Lowercase 'cc-by-nc' is not recognized due to case-sensitive matching")
  void testFullLicenseToShort_ccByNcLowercase() {
    assertEquals("", LicenseConverterUtils.fullLicenseToShort("cc-by-nc 4.0"));
  }

  @Test
  @DisplayName("Lowercase license codes are not recognized (documents case-sensitive behavior)")
  void testFullLicenseToShort_caseSensitive_lowercase() {
    assertEquals("", LicenseConverterUtils.fullLicenseToShort("public domain (cc0 1.0)"));
  }

  @Test
  @DisplayName("Mixed case license codes are not recognized")
  void testFullLicenseToShort_caseSensitive_mixedCase() {
    assertEquals("", LicenseConverterUtils.fullLicenseToShort("Cc-By 4.0 License"));
  }

  @Test
  @DisplayName("Unrelated text unrecognized")
  void testFullLicenseToShort_unrelatedText() {
    assertEquals("", LicenseConverterUtils.fullLicenseToShort("All rights reserved. No license granted."));
  }

  @ParameterizedTest
  @ValueSource(strings = {"GPL-3.0", "MIT License", "Apache License 2.0", "BSD 3-Clause"})
  @DisplayName("Non-Creative-Commons licenses are not recognized")
  void testFullLicenseToShort_nonCcLicenses(String input) {
    assertEquals("", LicenseConverterUtils.fullLicenseToShort(input));
  }

  @Test
  @DisplayName("When CC0 and CC-BY both appear, CC0 should win due to check order")
  void testFullLicenseToShort_multipleMatches_cc0TakesPriority() {
    String fullLicense = "Formerly Public Domain (CC0 1.0), now Creative Commons Attribution (CC-BY) 4.0 License";
    assertEquals("CC0-1.0", LicenseConverterUtils.fullLicenseToShort(fullLicense));
  }
}
