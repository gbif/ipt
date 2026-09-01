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

import org.apache.commons.lang3.StringUtils;

public final class LicenseConverterUtils {

  private static final String CC_ZERO_SHORT = "CC0-1.0";
  private static final String CC_ZERO_KEY = "CC0";
  private static final String CC_BY_SHORT = "CC-BY-4.0";
  private static final String CC_BY_KEY = "CC-BY";
  private static final String CC_BY_NC_SHORT = "CC-BY-NC-4.0";
  private static final String CC_BY_NC_KEY = "CC-BY-NC";

  private LicenseConverterUtils() {
  }

  /**
   * Converts a full Creative Commons license name to its short identifier.
   * Returns an empty string if the license is blank or not recognized.
   */
  public static String fullLicenseToShort(String fullLicense) {
    if (StringUtils.isBlank(fullLicense)) {
      return "";
    }

    if (fullLicense.contains(CC_ZERO_KEY)) {
      return CC_ZERO_SHORT;
    }
    if (fullLicense.contains(CC_BY_NC_KEY)) {
      return CC_BY_NC_SHORT;
    }
    if (fullLicense.contains(CC_BY_KEY)) {
      return CC_BY_SHORT;
    }

    return "";
  }
}
