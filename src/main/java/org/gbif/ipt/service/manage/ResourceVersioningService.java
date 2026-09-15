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
package org.gbif.ipt.service.manage;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;

import java.math.BigDecimal;

public interface ResourceVersioningService {

  /**
   * Convert an integer version number to major_version.minor_version version number. Please note IPTs before v2.2 used
   * integer-based version numbers.
   * <p>
   * This is only used by Darwin Core Archive resources, Data Package resources (Frictionless Data) use
   * an integer-based version number.
   *
   * @param resource resource
   * @return converted version number, or null if no conversion happened
   */
  BigDecimal convertVersion(Resource resource);

  /**
   * Update a resource's version and rename its eml, rtf, and dwca versioned files to have the new version also.
   *
   * @param resource   resource to update
   * @param oldVersion old version number
   * @param newVersion new version number
   * @return resource whose version number and files' version numbers have been updated
   */
  Resource updateResourceVersion(Resource resource, BigDecimal oldVersion, BigDecimal newVersion);

  /**
   * Rename a resource's dwca.zip to have the last published version, e.g. dwca-18.0.zip
   *
   * @param resource resource to update
   * @param version  last published version number
   */
  void renameDwcaToIncludeVersion(Resource resource, BigDecimal version);

  /**
   * Construct VersionHistory for the last published version of a resource if the resource has been published but had no
   * VersionHistory. Please note IPTs before v2.2 had no list of VersionHistory.
   *
   * @param resource resource
   * @return VersionHistory, or null if no VersionHistory needed to be created.
   */
  VersionHistory constructVersionHistoryForLastPublishedVersion(Resource resource);
}
