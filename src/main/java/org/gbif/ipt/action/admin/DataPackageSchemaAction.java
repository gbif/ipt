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
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

public class DataPackageSchemaAction extends POSTAction {

  private static final long serialVersionUID = -535308367714585780L;

  private static final Logger LOG = LogManager.getLogger(DataPackageSchemaAction.class);

  private final DataPackageSchemaManager schemaManager;
  private final RegistryManager registryManager;
  private final ConfigWarnings configWarnings;

  @Setter
  @Getter
  private List<DataPackageSchema> latestDataSchemasVersions;
  @Getter
  private List<DataPackageSchema> schemas;
  @Getter
  private List<DataPackageSchema> newSchemas;
  @Setter
  private String schemaName;
  @Getter
  private DataPackageSchema dataPackageSchema;
  @Getter
  private String dataPackageSchemaRawData;
  // true if all installed data schemas use the latest version, false otherwise
  @Setter
  @Getter
  private boolean upToDate = true;
  @Getter
  private boolean iptReinstallationRequired = false;

  @Inject
  public DataPackageSchemaAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
                                 DataPackageSchemaManager schemaManager, RegistryManager registryManager,
                                 ConfigWarnings configWarnings) {
    super(textProvider, cfg, registrationManager);
    this.schemaManager = schemaManager;
    this.registryManager = registryManager;
    this.configWarnings = configWarnings;
  }

  @Override
  public String delete() throws Exception {
    try {
      schemaManager.uninstallSafely(id, schemaName);
      addActionMessage(getText("admin.dataPackages.delete.success", new String[] {id}));
    } catch (DeletionNotAllowedException e) {
      addActionWarning(getText("admin.dataPackages.delete.error", new String[] {id}));
      addActionExceptionWarning(e);
    }
    return SUCCESS;
  }

  /**
   * Update installed data schema to the latest version.
   * </br>
   * This involves migrating all associated resource mappings over to the new version.
   * </br>
   * If there are no associated resource mappings, the new version can simply be installed.
   *
   * @return struts2 result
   */
  public String update() throws Exception {
    try {
      LOG.info("Updating data schema {} to the latest version...", id);
      schemaManager.update(id);
      addActionMessage(getText("admin.dataPackages.update.success", new String[] {id}));
    } catch (Exception e) {
      LOG.error(e);
      addActionWarning(getText("admin.dataPackages.update.error", new String[] {e.getMessage()}), e);
    }
    return SUCCESS;
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

    // update each installed extension indicating whether it is the latest version or not
    // also update isUpdatable field
    updateComputableFields(schemas);

    // populate list of uninstalled data schemas, removing data schemas installed already, showing only latest versions
    newSchemas = getLatestDataSchemasVersions();
    List<String> installedSchemasIdentifiers = schemas.stream()
        .map(DataPackageSchema::getIdentifier)
        .collect(Collectors.toList());
    newSchemas.removeIf(ds -> installedSchemasIdentifiers.contains(ds.getIdentifier()));

    return SUCCESS;
  }

  @Override
  public String save() {
    try {
      Optional<DataPackageSchema> wrappedSchema = latestDataSchemasVersions.stream()
          .filter(ds -> ds.getIdentifier().equals(id))
          .findFirst();

      if (wrappedSchema.isPresent()) {
        schemaManager.install(wrappedSchema.get());
      } else {
        addActionWarning(getText("admin.dataPackages.install.error", new String[] {id}));
      }

      addActionMessage(getText("admin.dataPackages.install.success", new String[] {id}));
    } catch (Exception e) {
      LOG.error(e);
      addActionWarning(getText("admin.dataPackages.install.error", new String[] {id}), e);
    }
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();

    // load the latest data schema versions from Registry
    loadLatestDataSchemasVersions();

    if (id != null) {
      dataPackageSchema = schemaManager.get(id);
      dataPackageSchemaRawData = schemaManager.getRawData(dataPackageSchema.getName());
    }
  }

  /**
   * Reload the list of registered data schemas, loading only the latest data schema versions.
   */
  private void loadLatestDataSchemasVersions() {
    try {
      // list of all registered data schemas
      List<DataPackageSchema> all = registryManager.getLatestDataPackageSchemas();
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
      msg = getText("admin.dataPackages.couldnt.load", new String[] {cfg.getRegistryUrl()});
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
   * @param dataPackageSchemas unfiltered list of all registered data schemas
   *
   * @return filtered list of data schemas
   */
  protected List<DataPackageSchema> getLatestVersions(List<DataPackageSchema> dataPackageSchemas) {
    // sort data schemas by issued date, starting with latest issued
    List<DataPackageSchema> sorted = dataPackageSchemas.stream()
        .sorted(Comparator.comparing(DataPackageSchema::getIssued, Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toList());

    // populate list of the latest data schema versions
    Map<String, DataPackageSchema> dataPackageSchemasByIdentifier = new HashMap<>();
    if (!sorted.isEmpty()) {
      for (DataPackageSchema dataPackageSchema : sorted) {
        String identifier = dataPackageSchema.getIdentifier();
        if (identifier != null && !dataPackageSchemasByIdentifier.containsKey(identifier)) {
          dataPackageSchemasByIdentifier.put(identifier, dataPackageSchema);
        }
      }
    }

    return new ArrayList<>(dataPackageSchemasByIdentifier.values());
  }

  /**
   * Method used for
   * <ol>
   *   <li>updating each data schema's isLatest field</li>
   *   <li>updating each data schema's isUpdatable field</li>
   *   <li>for action logging (logging if at least one data schema is not up-to-date).</li>
   * </ol>
   *
   * Works by iterating through list of installed data schemas. Updates each one, indicating if it is the latest version
   * or not. Plus, updates boolean "upToDate", set to false if there is at least one data schema that is not up-to-date.
   */
  protected void updateComputableFields(List<DataPackageSchema> installedDataSchemas) {
    if (installedDataSchemas.isEmpty()) {
      return;
    }

    try {
      // complete list of registered data schemas (latest and non-latest versions)
      List<DataPackageSchema> registeredSchemas = registryManager.getLatestDataPackageSchemas();

      for (DataPackageSchema installedSchema : installedDataSchemas) {
        updateComputableFields(installedSchema, registeredSchemas);
      }
    } catch (RegistryException e) {
      // add startup error message about Registry error
      String msg = RegistryException.logRegistryException(e, this);
      configWarnings.addStartupError(msg);
      LOG.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = getText("admin.dataPackages.couldnt.load", new String[]{cfg.getRegistryUrl()});
      configWarnings.addStartupError(msg);
      LOG.error(msg);
    }
  }

  private void updateComputableFields(DataPackageSchema installedSchema, List<DataPackageSchema> registeredSchemas) {
    installedSchema.setLatest(true);

    for (DataPackageSchema registeredSchema : registeredSchemas) {
      if (isLatest(registeredSchema) && isSameIdentifier(installedSchema, registeredSchema)) {
        handleSchema(installedSchema, registeredSchema);
        break;
      }
    }
  }

  private void handleSchema(DataPackageSchema installedSchema, DataPackageSchema latestSchema) {
    String latestCompatibleVersion = registryManager.getLatestCompatibleSchemaVersion(installedSchema.getName(), installedSchema.getVersion());
    String installedVersion = installedSchema.getVersion();
    String latestVersion = latestSchema.getVersion();

    if (installedVersion.equals(latestCompatibleVersion) && latestCompatibleVersion.equals(latestVersion)) {
      LOG.debug("Installed data schema with identifier {} was issued {}. It's the latest available schema.",
          installedSchema.getIdentifier(), installedSchema.getIssued());
    } else if (installedVersion.equals(latestCompatibleVersion)) {
      handleCompatibleVersionMatchesInstalledButNotMatchLatest(installedSchema, latestCompatibleVersion);
    } else {
      handleCompatibleVersionNotMatchInstalled(installedSchema, latestCompatibleVersion);
    }
  }

  private void handleCompatibleVersionNotMatchInstalled(DataPackageSchema installedSchema, String latestCompatibleVersion) {
    upToDate = false;
    installedSchema.setLatest(false);
    installedSchema.setUpdatable(true);

    LOG.debug("Installed data schema with identifier {} was issued {}. A newer compatible version {} exists.",
        installedSchema.getIdentifier(), installedSchema.getIssued(), latestCompatibleVersion);
  }

  private void handleCompatibleVersionMatchesInstalledButNotMatchLatest(DataPackageSchema installedSchema, String latestCompatibleVersion) {
    DataPackageSchema latestCompatibleSchema = registryManager.getSchema(installedSchema.getName(), latestCompatibleVersion);
    Date latestCompatibleSchemaIssuedDate = latestCompatibleSchema.getIssued();

    upToDate = false;
    installedSchema.setLatest(false);

    if (latestCompatibleSchemaIssuedDate.after(installedSchema.getIssued())) {
      LOG.debug("Installed data schema with identifier {} was issued {}. " +
              "A newer compatible schema with the same version but newer issued date exists.",
          installedSchema.getIdentifier(), installedSchema.getIssued());
      installedSchema.setUpdatable(true);
    } else {
      LOG.debug("Installed data schema with identifier {} was issued {}. " +
              "Latest compatible version already installed. IPT update required",
          installedSchema.getIdentifier(), installedSchema.getIssued());
      iptReinstallationRequired = true;
    }
  }

  private boolean isSameIdentifier(DataPackageSchema first, DataPackageSchema second) {
    return first.getIdentifier().equalsIgnoreCase(second.getIdentifier());
  }

  private boolean isLatest(DataPackageSchema schema) {
    return schema.isLatest();
  }
}
