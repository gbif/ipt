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
package org.gbif.ipt.model.voc;

import org.apache.commons.lang3.StringUtils;

/**
 * This enumeration represents the set of camtrap metadata sections in the IPT. Each entry represents a section, and the
 * name of the section is the name used in URL.
 */
public enum CamtrapMetadataSection implements DataPackageMetadataSection {

  BASIC_SECTION("basic"),
  GEOGRAPHIC_SECTION ("geographic"),
  TAXONOMIC_SECTION ("taxonomic"),
  TEMPORAL_SECTION ("temporal"),
  KEYWORDS_SECTION ("keywords"),
  PROJECT_SECTION ("project"),
  CITATION_SECTION("citation"),
  OTHER_SECTION ("other");

  /**
   * Section name. Used for example in URL.
   */
  private final String name;

  /**
   * Returns a CamtrapMetadataSection from the section name.
   *
   * @param section the section name
   *
   * @return CamtrapMetadataSection or null if not found from the section name
   */
  public static CamtrapMetadataSection fromName(String section) {
    for (CamtrapMetadataSection s : CamtrapMetadataSection.values()) {
      if (s.name.equalsIgnoreCase(StringUtils.trimToEmpty(section))) {
        return s;
      }
    }
    return null;
  }

  CamtrapMetadataSection(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
