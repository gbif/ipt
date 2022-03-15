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
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

public class DataSchemaAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(DataSchemaAction.class);

  private final DataSchemaManager schemaManager;
  private final RegistryManager registryManager;
  private final ConfigWarnings configWarnings;

  private List<DataSchema> latestDataSchemasVersions;
  private List<DataSchema> schemas;
  private List<DataSchema> newSchemas;

  @Inject
  public DataSchemaAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                          DataSchemaManager schemaManager, RegistryManager registryManager,
                          ConfigWarnings configWarnings) {
    super(textProvider, cfg, registrationManager);
    this.schemaManager = schemaManager;
    this.registryManager = registryManager;
    this.configWarnings = configWarnings;
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

    // populate list of uninstalled data schemas, removing data schemas installed already, showing only latest versions
    newSchemas = getLatestDataSchemasVersions();
    for (DataSchema e : schemas) {
      newSchemas.remove(e);
    }

    return SUCCESS;
  }

  public List<DataSchema> getSchemas() {
    return schemas;
  }

  public List<DataSchema> getNewSchemas() {
    return newSchemas;
  }

  @Override
  public void prepare() {
    super.prepare();
    // ensure data schemas are always loaded
    schemaManager.load();

    // load the latest data schema versions from Registry
    loadLatestDataSchemasVersions();
  }

  /**
   * Reload the list of registered data schemas, loading only the latest data schema versions.
   */
  private void loadLatestDataSchemasVersions() {
    try {
      // list of all registered data schemas
      List<DataSchema> all = registryManager.getDataSchemas();
      if (!all.isEmpty()) {
        // list of latest data schema versions
        setLatestDataSchemasVersions(getLatestVersions(all));
      }
    } catch (RegistryException e) {
      // add startup error message that explains why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, this);
      configWarnings.addStartupError(msg);
      LOG.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = getText("admin.schemas.couldnt.load", new String[] {cfg.getRegistryUrl()});
      configWarnings.addStartupError(msg);
      LOG.error(msg);
    } finally {
      // initialize list as empty list if the list could not be populated
      if (getLatestDataSchemasVersions() == null) {
        setLatestDataSchemasVersions(new ArrayList<>());
      }
    }
  }

  /**
   * Filter a list of data schemas, returning the latest versions. The latest version of a data schema
   * is determined by its issued date.
   *
   * @param dataSchemas unfiltered list of all registered data schemas
   *
   * @return filtered list of data schemas
   */
  protected List<DataSchema> getLatestVersions(List<DataSchema> dataSchemas) {
    // sort data schemas by issued date, starting with latest issued
    List<DataSchema> sorted = dataSchemas.stream()
        .sorted(Comparator.comparing(DataSchema::getIssued, Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toList());

    // populate list of the latest data schema versions
    Map<String, DataSchema> dataSchemasByIdentifier = new HashMap<>();
    if (!sorted.isEmpty()) {
      for (DataSchema dataSchema : sorted) {
        String identifier = dataSchema.getIdentifier();
        if (identifier != null && !dataSchemasByIdentifier.containsKey(identifier)) {
          dataSchemasByIdentifier.put(identifier, dataSchema);
        }
      }
    }

    return new ArrayList<>(dataSchemasByIdentifier.values());
  }

  public List<DataSchema> getLatestDataSchemasVersions() {
    return latestDataSchemasVersions;
  }

  public void setLatestDataSchemasVersions(List<DataSchema> latestDataSchemasVersions) {
    this.latestDataSchemasVersions = latestDataSchemasVersions;
  }
}
