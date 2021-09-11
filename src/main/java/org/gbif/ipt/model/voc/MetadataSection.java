package org.gbif.ipt.model.voc;

import org.apache.commons.lang3.StringUtils;

/**
 * This enumeration represents the set of metadata sections in the IPT. Each entry represents a section, and the
 * name of the section is the name used in URL.
 */
public enum MetadataSection {
  BASIC_SECTION("basic"),
  GEOGRAPHIC_COVERAGE_SECTION ("geocoverage"),
  TAXANOMIC_COVERAGE_SECTION ("taxcoverage"),
  TEMPORAL_COVERAGE_SECTION ("tempcoverage"),
  PROJECT_SECTION ("project"),
  METHODS_SECTION ("methods"),
  CITATIONS_SECTION ("citations"),
  COLLECTIONS_SECTION ("collections"),
  PHYSICAL_SECTION ("physical"),
  KEYWORDS_SECTION ("keywords"),
  ADDITIONAL_SECTION ("additional"),
  PARTIES_SECTION ("parties");

  /**
   * Section name. Used for example in URL.
   */
  private final String name;

  /**
   * Returns a MetadataSection from the section name.
   *
   * @param section the section name
   *
   * @return MetadataSection or null if not found from the section name
   */
  public static MetadataSection fromName(String section) {
    for (MetadataSection s : MetadataSection.values()) {
      if (s.name.equalsIgnoreCase(StringUtils.trimToEmpty(section))) {
        return s;
      }
    }
    return null;
  }

  MetadataSection(String name) {
    this.name = name;
  }


  public String getName() {
    return name;
  }
}
