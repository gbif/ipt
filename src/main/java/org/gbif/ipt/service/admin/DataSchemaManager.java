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

import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.DataSchemaManagerImpl;

import java.io.IOException;
import java.util.List;

import com.google.inject.ImplementedBy;

@ImplementedBy(DataSchemaManagerImpl.class)
public interface DataSchemaManager {

  /**
   * Safely remove an installed data schema by its unique identifier, making sure no mappings to this data schema exist.
   *
   * @param identifier of installed data schema to remove
   * @param name of installed data schema to remove
   *
   * @throws DeletionNotAllowedException if at least one mapping to this data schema exists preventing deletion
   */
  void uninstallSafely(String identifier, String name) throws DeletionNotAllowedException;

  /**
   * Update an installed data schema to the latest version.
   */
  void update(String identifier) throws IOException;

  /**
   * Get a locally installed data schema by its identifier or name.
   *
   * @return data schema for that identifier/name or null if not installed
   */
  DataSchema get(String identifier);

  /**
   * Install base data schemas.
   */
  void installBaseSchemas() throws InvalidConfigException;

  /**
   * Downloads a data schema to the local cache and installs it for mapping. If the file is already locally existing
   * overwrite the older copy.
   */
  void install(DataSchema schema) throws InvalidConfigException;

  /**
   * List all installed data schemas.
   *
   * @return list of installed IPT data schemas
   */
  List<DataSchema> list();

  /**
   * Load all installed data schemas from the data dir.
   *
   * @return number of data schemas that have been loaded successfully
   */
  int load();

  /**
   * Check whether schema is installed
   */
  boolean isSchemaInstalled(String nameOrIdentifier);
}
