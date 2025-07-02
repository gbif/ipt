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
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.service.InvalidConfigException;

import java.io.IOException;
import java.util.List;

public interface DataPackageSchemaManager {

  /**
   * Safely remove an installed data schema by its unique identifier, making sure no mappings to this data schema exist.
   *
   * @param identifier of installed data schema to remove
   * @param name of installed data schema to remove
   */
  void uninstallSafely(String identifier, String name);

  /**
   * Update an installed data schema to the latest version.
   */
  DataPackageSchema update(String identifier) throws IOException;

  /**
   * Get a locally installed data schema by its identifier or name.
   *
   * @return data schema for that identifier/name or null if not installed
   */
  DataPackageSchema get(String identifier);

  /**
   * Get locally installed data schema's raw data by its identifier or name.
   *
   * @return data schema for that identifier/name or null if not installed
   */
  String getRawData(String identifier);

  /**
   * Install base data package schemas.
   */
  void installSupportedDataPackageSchemas() throws InvalidConfigException;

  /**
   * Downloads a data schema to the local cache and installs it for mapping. If the file is already locally existing
   * overwrite the older copy.
   */
  void install(DataPackageSchema schema) throws InvalidConfigException;

  /**
   * List all installed data schemas.
   *
   * @return list of installed IPT data schemas
   */
  List<DataPackageSchema> list();

  /**
   * Load all installed data schemas from the data dir.
   *
   * @return number of data schemas that have been loaded successfully
   */
  int load();

  /**
   * Install or update the latest versions of all default schemas.
   */
  void installOrUpdateDefaults();

  /**
   * Check whether schema is installed
   */
  boolean isSchemaInstalled(String nameOrIdentifier);

  /**
   * Check whether identifier/name is schema identifier/name or not
   */
  boolean isSchemaType(String nameOrIdentifier);

  /**
   * Returns schema identifier by schema name
   */
  String getSchemaIdentifier(String schemaName);
}
