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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface DataPackageSchemaManager {

  /**
   * Uninstalls a data schema, removing its files. Callers must make sure no resource uses it.
   *
   * @param identifier identifier or name of the installed data schema, see {@link #get(String)}
   */
  void uninstall(String identifier);

  /**
   * Update an installed data schema to the latest version.
   */
  DataPackageSchema update(String identifier) throws IOException;

  /**
   * Get the version an installed data schema would be updated to, with its table schemas, without installing it.
   *
   * @param identifier identifier or name of the installed data schema
   * @return the newer version, or null if there is none or the data schema isn't installed
   */
  DataPackageSchema getUpdate(String identifier) throws IOException;

  /**
   * Get a locally installed data schema by its identifier or name.
   *
   * @return data schema for that identifier/name or null if not installed
   */
  DataPackageSchema get(String identifier);

  /**
   * All identifiers {@link #get(String)} resolves to an installed data schema: its identifier, previous identifiers,
   * alternative identifiers and name. Apart from the name, they also match with another protocol (http/https).
   *
   * @return accepted identifiers of the data schema
   */
  Set<String> getAcceptedIdentifiers(DataPackageSchema schema);

  /**
   * Get a locally installed data schema's version by its identifier or name.
   *
   * @return version of the schema by identifier/name or null if not installed
   */
  String getVersion(String identifier);

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

  /**
   * Get the locally installed file of a table schema.
   *
   * @param schema data schema the table schema belongs to
   * @param tableSchemaName table schema name
   *
   * @return table schema file, which doesn't exist if the table schema isn't installed
   */
  File getTableSchemaFile(DataPackageSchema schema, String tableSchemaName);
}
