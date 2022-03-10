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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class DataSchemaAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(DataSchemaAction.class);

  private final DataSchemaManager schemaManager;

  private List<DataSchema> schemas;

  @Inject
  public DataSchemaAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                          DataSchemaManager schemaManager) {
    super(textProvider, cfg, registrationManager);
    this.schemaManager = schemaManager;
  }

  /**
   * Handles the population of installed and uninstalled schemas on the "Data schemas" page.
   * This method always tries to pick up newly registered schemas from the Registry.
   *
   * @return struts2 result
   */
  public String list() {
    // retrieve all data schemas that have been installed already
    schemas = schemaManager.list();

    return SUCCESS;
  }
}
