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

import org.gbif.ipt.action.portal.OrganizedTaxonomicCoverage;
import org.gbif.ipt.action.portal.OrganizedTaxonomicKeywords;

import com.thoughtworks.xstream.XStream;

/**
 * Limits the classes XStream may instantiate when reading XML.
 * <p>
 * Since 1.4.18 XStream starts with an allowlist of safe JDK types (primitives, String, Date, UUID, URL, URI, File,
 * Locale, collections, maps, numbers, enums...). This adds the IPT's own model classes to it.
 * <p>
 * Never use {@code AnyTypePermission.ANY}: resource.xml can come from an uploaded "IPT Resource Folder", and without
 * an allowlist crafted XML can instantiate arbitrary classes on the classpath (deserialization gadgets, possibly
 * remote code execution).
 */
public final class XStreamSecurity {

  static final String[] ALLOWED_TYPES = {
      // Resource, User, Organisation, Registration, sources, mappings, inferred metadata...
      "org.gbif.ipt.model.**",
      // DOI
      "org.gbif.api.model.common.*",
      // EML classes
      "org.gbif.metadata.**",
      // Term implementations
      "org.gbif.dwc.terms.**"
  };

  /**
   * Persisted classes outside the packages above, e.g. inferred taxonomic metadata (inferredMetadata.xml).
   */
  static final Class<?>[] ALLOWED_CLASSES = {
      OrganizedTaxonomicCoverage.class,
      OrganizedTaxonomicKeywords.class
  };

  private XStreamSecurity() {
  }

  /**
   * Adds the IPT's model classes to XStream's default allowlist of types.
   *
   * @param xstream XStream instance, created without changing its default security
   * @return the same XStream instance
   */
  public static XStream restrict(XStream xstream) {
    xstream.allowTypesByWildcard(ALLOWED_TYPES);
    xstream.allowTypes(ALLOWED_CLASSES);
    return xstream;
  }
}
