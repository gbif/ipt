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
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.model.DataSchemaFile;
import org.gbif.ipt.model.factory.DataSchemaFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;

import com.google.inject.Inject;

import static org.gbif.utils.HttpUtil.success;

public class DataSchemaManagerImpl extends BaseManager implements DataSchemaManager  {

  private static final String CONFIG_FOLDER = ".dataSchemas";
  private static final String DATA_SCHEMA_FILE_SUFFIX = ".json";

  private final ConfigWarnings warnings;
  private final DataSchemaFactory factory;
  private final RegistryManager registryManager;
  private final HttpClient downloader;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private final BaseAction baseAction;

  @Inject
  public DataSchemaManagerImpl(AppConfig cfg, DataDir dataDir, ConfigWarnings warnings, DataSchemaFactory factory,
                               SimpleTextProvider textProvider, RegistrationManager registrationManager,
                               RegistryManager registryManager, HttpClient downloader) {
    super(cfg, dataDir);
    this.warnings = warnings;
    this.factory = factory;
    this.registryManager = registryManager;
    this.downloader = downloader;
    this.baseAction = new BaseAction(textProvider, cfg, registrationManager);
  }

  @Override
  public void installBaseSchemas() throws InvalidConfigException {
    List<DataSchema> schemas = getBaseDataSchemas();
    for (DataSchema schema : schemas) {
      install(schema);
    }
  }

  @Override
  public synchronized void install(DataSchema dataSchema) throws InvalidConfigException {
    Objects.requireNonNull(dataSchema);
    try {
      for (DataSchemaFile subSchema : dataSchema.getSubSchemas()) {
        File tmpFile = download(subSchema.getUrl());
        DataSchemaFile dataSchemaFile = loadFromFile(tmpFile);
        finishInstall(tmpFile, dataSchema.getIdentifier(), dataSchemaFile);
      }
    } catch (InvalidConfigException e) {
      throw e;
    } catch (Exception e) {
      String msg = baseAction.getText("admin.schemas.install.error", new String[] {dataSchema.getUrl().toString()});
      LOG.error(msg, e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_SCHEMA, msg, e);
    }
  }

  @Override
  public List<DataSchema> list() {
    return new ArrayList<>();
  }

  /**
   * Download a data schema into temporary file and return it.
   *
   * @param url URL of data schema to download
   *
   * @return temporary file data schema was downloaded to, or null if it failed to be downloaded
   */
  private File download(URL url) throws IOException {
    Objects.requireNonNull(url);
    String filename = org.gbif.ipt.utils.FileUtils
        .getSuffixedFileName(url.toString().replace(DATA_SCHEMA_FILE_SUFFIX, ""), DATA_SCHEMA_FILE_SUFFIX);
    File tmpFile = dataDir.tmpFile(filename);
    StatusLine statusLine = downloader.download(url, tmpFile);
    if (success(statusLine)) {
      LOG.info("Successfully downloaded data schema: " + url);
      return tmpFile;
    } else {
      String msg =
          "Failed to download data schema: " + url + ". Response=" + statusLine.getStatusCode();
      LOG.error(msg);
      throw new IOException(msg);
    }
  }

  /**
   * Return the latest versions of base schemas from the registry.
   *
   * @return list containing latest versions of data schemas
   */
  private List<DataSchema> getBaseDataSchemas() {
    List<DataSchema> baseDataSchemas = new ArrayList<>();
    try {
      for (DataSchema schema : registryManager.getDataSchemas()) {
        if (schema.getIdentifier() != null && AppConfig.getBaseDataSchemas().contains(schema.getIdentifier())) {
          if (schema.isLatest()) { // must be the latest version
            baseDataSchemas.add(schema);
          }
        }
      }
    } catch (RegistryException e) {
      // add startup error message about Registry error
      String msg = RegistryException.logRegistryException(e, baseAction);
      warnings.addStartupError(msg);
      LOG.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = baseAction.getText("admin.schemas.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      LOG.error(msg);
    }

    // throw exception if not all base data schemas could not be loaded
    if (AppConfig.getBaseDataSchemas().size() != baseDataSchemas.size()) {
      String msg = "Not all base data schemas were loaded!";
      LOG.error(msg);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_DIR, msg);
    }
    return baseDataSchemas;
  }

  /**
   * Reads a data schema file from file and returns it.
   *
   * @param localFile data schema file to read from
   *
   * @return data schema loaded from file
   *
   * @throws InvalidConfigException if data schema could not be loaded successfully
   */
  protected DataSchemaFile loadFromFile(File localFile) throws InvalidConfigException {
    Objects.requireNonNull(localFile);
    if (!localFile.exists()) {
      throw new IllegalStateException();
    }

    try {
      DataSchemaFile dataSchemaFile = factory.build(localFile);
      LOG.info("Successfully loaded data schema file " + dataSchemaFile.getName());
      return dataSchemaFile;
    } catch (IOException e) {
      LOG.error("Can't access local data schema file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_SCHEMA,
          "Can't access local data schema file");
    }
  }

  /**
   * Move and rename temporary file to final version.
   *
   * @param tmpFile   downloaded data schema file (in temporary location with temporary filename)
   * @param schemaIdentifier schema identifier
   * @param dataSchemaFile data schema file being installed
   *
   * @throws IOException if moving file fails
   */
  private void finishInstall(File tmpFile, String schemaIdentifier, DataSchemaFile dataSchemaFile) throws IOException {
    Objects.requireNonNull(tmpFile);
    Objects.requireNonNull(dataSchemaFile);
    Objects.requireNonNull(dataSchemaFile.getName());

    try {
      String schemaName = StringUtils.substringAfterLast(schemaIdentifier, "/");
      File installedFile = getDataSchemaFile(schemaIdentifier, schemaName, dataSchemaFile.getName());
      FileUtils.moveFile(tmpFile, installedFile);
    } catch (IOException e) {
      LOG.error("Installing data schema failed, while trying to move and rename data schema file: " + e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Retrieve data schema file by its unique identifier.
   *
   * @param schemaIdentifier schema identifier
   * @param schemaName schema name
   * @param subSchemaName sub schema name
   *
   * @return data schema file
   */
  private File getDataSchemaFile(String schemaIdentifier, String schemaName, String subSchemaName) {
    String filename = org.gbif.ipt.utils.FileUtils.getSuffixedFileName(schemaIdentifier + "_" + subSchemaName, DATA_SCHEMA_FILE_SUFFIX);
    return dataDir.configFile(CONFIG_FOLDER + "/" + schemaName + "/" + filename);
  }
}
