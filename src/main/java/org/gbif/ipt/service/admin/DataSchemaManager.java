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
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.DataSchemaManagerImpl;

import java.util.List;

import com.google.inject.ImplementedBy;

@ImplementedBy(DataSchemaManagerImpl.class)
public interface DataSchemaManager {

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
}
