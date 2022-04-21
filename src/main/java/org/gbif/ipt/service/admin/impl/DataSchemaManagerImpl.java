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
import org.gbif.ipt.model.DataSubschema;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.factory.DataSchemaFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.http.StatusLine;

import com.google.inject.Inject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.gbif.utils.HttpUtil.success;

public class DataSchemaManagerImpl extends BaseManager implements DataSchemaManager  {

  private static final String CONFIG_FOLDER = ".dataSchemas";
  private static final String DATA_SCHEMA_FILE_SUFFIX = ".json";

  private final ConfigWarnings warnings;
  private final DataSchemaFactory factory;
  private final RegistryManager registryManager;
  private final ResourceManager resourceManager;
  private final HttpClient downloader;
  private final Gson gson;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private final BaseAction baseAction;

  private List<DataSchema> dataSchemas = new ArrayList<>();
  private Map<String, DataSchema> dataSchemasByIdentifiers = new HashMap<>();

  @Inject
  public DataSchemaManagerImpl(AppConfig cfg, DataDir dataDir, ConfigWarnings warnings, DataSchemaFactory factory,
                               SimpleTextProvider textProvider, RegistrationManager registrationManager,
                               RegistryManager registryManager, ResourceManager resourceManager, HttpClient downloader) {
    super(cfg, dataDir);
    this.warnings = warnings;
    this.factory = factory;
    this.registryManager = registryManager;
    this.resourceManager = resourceManager;
    this.downloader = downloader;
    this.baseAction = new BaseAction(textProvider, cfg, registrationManager);
    this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
  }

  @Override
  public void uninstallSafely(String schemaIdentifier, String schemaName) throws DeletionNotAllowedException {
    if (dataSchemasByIdentifiers.containsKey(schemaIdentifier)) {
      // check if it's used by some resources
      for (Resource r : resourceManager.list()) {
        if (r.getSchemaIdentifier() != null
            && r.getSchemaIdentifier().equals(schemaIdentifier) && !r.getDataSchemaMappings().isEmpty()) {
          LOG.warn("Schema mapped in resource " + r.getShortname());
          String msg = baseAction.getText("admin.schemas.delete.error.mapped", new String[] {r.getShortname()});
          throw new DeletionNotAllowedException(DeletionNotAllowedException.Reason.DATA_SCHEMA_MAPPED, msg);
        }
      }
      uninstall(schemaIdentifier, schemaName);
    } else {
      LOG.warn("Data schema not installed locally, can't delete " + schemaIdentifier);
    }
  }

  @Override
  public synchronized void update(String identifier) throws IOException, RegistryException {
    // identify installed data schema by identifier
    DataSchema installed = get(identifier);

    if (installed != null) {
      // verify there is a newer (latest) version
      DataSchema latestVersion = null;
      for (DataSchema ds : registryManager.getDataSchemas()) {
        // match by rowType and isLatest, plus the URL cannot be null in order to be installed
        if (ds.getIdentifier() != null && ds.getIdentifier().equalsIgnoreCase(identifier) && ds.isLatest()) {
          latestVersion = ds;
          break;
        }
      }

      boolean isNewVersion = false;
      if (latestVersion != null) {
        Date issued = installed.getIssued();
        Date issuedLatest = latestVersion.getIssued();
        if (issued == null && issuedLatest != null) {
          isNewVersion = true;
        } else if (issued != null && issuedLatest != null) {
          isNewVersion = (issuedLatest.compareTo(issued) > 0); // latest version must have newer issued date
        }
      }

      // TODO: 16/03/2022 manage affected resources
      if (isNewVersion && latestVersion.getUrl() != null) {
        // uninstall and install new version
        uninstall(identifier, latestVersion.getName());
        install(latestVersion);
      }
    }
  }

  @Override
  public DataSchema get(String identifier) {
    DataSchema result = dataSchemasByIdentifiers.get(identifier);

    // try name
    if (result == null) {
      if (dataSchemas.isEmpty()) {
        load();
      }

      for (DataSchema ds : dataSchemas) {
        if (identifier.equals(ds.getName()) || identifier.equals(ds.getIdentifier())) {
          result = ds;
          break;
        }
      }
    }

    return result;
  }

  /**
   * Uninstall data schema by its unique identifier.
   *
   * @param identifier identifier of data schema to uninstall
   * @param name name of data schema to uninstall
   */
  private void uninstall(String identifier, String name) {
    if (dataSchemasByIdentifiers.containsKey(identifier)) {
      dataSchemasByIdentifiers.remove(identifier);

      File f = getDataSchemaDirectory(name);
      if (f.exists()) {
        FileUtils.deleteQuietly(f);
      } else {
        LOG.warn("Data schema doesn't exist locally, can't delete " + identifier);
      }
    } else {
      LOG.warn("Data schema not installed locally, can't delete " + identifier);
    }
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
      String filename = org.gbif.ipt.utils.FileUtils
          .getSuffixedFileName("_" + dataSchema.getIdentifier().replace(DATA_SCHEMA_FILE_SUFFIX, ""), DATA_SCHEMA_FILE_SUFFIX);
      File tmpFileSchema = dataDir.tmpFile(filename);

      try (FileWriter fw = new FileWriter(tmpFileSchema)) {
        gson.toJson(dataSchema, fw);
      }
      finishInstallSchema(tmpFileSchema, dataSchema.getIdentifier(), dataSchema.getName());

      for (DataSubschema subSchema : dataSchema.getSubSchemas()) {
        File tmpFile = download(subSchema.getUrl());
        DataSubschema dataSubschema = loadSubschemaFromFile( tmpFile);
        finishInstallSubschema(tmpFile, dataSchema.getIdentifier(), dataSchema.getName(), dataSubschema);
      }

      // keep data schemas in local lookup: allowed one installed data schema per identifier
      dataSchemasByIdentifiers.put(dataSchema.getIdentifier(), dataSchema);
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
    if (dataSchemas.isEmpty()) {
      load();
    }
    return dataSchemas;
  }

  @Override
  public int load() {
    File dataSchemasDir = dataDir.configFile(CONFIG_FOLDER);
    int counter = 0;
    if (dataSchemasDir.isDirectory()) {
      String[] dataSchemaNames = dataSchemasDir.list((current, name) -> new File(current, name).isDirectory());
      FilenameFilter filter = new SuffixFileFilter(DATA_SCHEMA_FILE_SUFFIX, IOCase.INSENSITIVE);
      DataSchema dataSchema = null;
      List<DataSubschema> dataSubschemas;

      try {
        if (dataSchemaNames != null) {
          for (String dataSchemaDirectoryName : dataSchemaNames) {
            dataSubschemas = new ArrayList<>();
            File dataSchemaDirectory = new File(dataSchemasDir, dataSchemaDirectoryName);
            File[] files = dataSchemaDirectory.listFiles(filter);

            if (files != null) {
              for (File file : files) {
                if (file.getName().startsWith("_")) {
                  dataSchema = loadSchemaFromFile(file);
                  // keep data schema in local lookup
                  dataSchemasByIdentifiers.put(dataSchema.getIdentifier(), dataSchema);
                } else {
                  dataSubschemas.add(loadSubschemaFromFile(file));
                }
              }

              if (dataSchema != null) {
                dataSchema.setSubSchemas(dataSubschemas);
                dataSchemas.add(dataSchema);
              }
            }
            counter++;
          }
        }
      } catch (InvalidConfigException e) {
        // TODO: 14/03/2022 delete corrupted files
        LOG.error("Failed to load data schemas", e);
      }
    }
    return counter;
  }

  @Override
  public boolean isSchemaInstalled(String nameOrIdentifier) {
    boolean result = false;

    if (dataSchemas.isEmpty()) {
      load();
    }

    for (DataSchema dataSchema : dataSchemas) {
      if (dataSchema.getIdentifier().equals(nameOrIdentifier)
          || dataSchema.getName().equals(nameOrIdentifier)) {
        result = true;
        break;
      }
    }

    return result;
  }
  @Override
  public boolean isSchemaType(String nameOrIdentifier) {
    boolean result = false;

    List<DataSchema> dataSchemas = registryManager.getDataSchemas();
    for (DataSchema dataSchema : dataSchemas) {
      if (dataSchema.getIdentifier().equals(nameOrIdentifier)
          || dataSchema.getName().equals(nameOrIdentifier)) {
        result = true;
        break;
      }
    }

    return result;
  }

  @Override
  public String getSchemaIdentifier(String schemaName) {
    String result = null;

    List<DataSchema> dataSchemas = registryManager.getDataSchemas();
    for (DataSchema dataSchema : dataSchemas) {
      if (dataSchema.getName().equals(schemaName)) {
        result = dataSchema.getIdentifier();
        break;
      }
    }

    return result;
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
   * Reads a data schema from file and returns it.
   *
   * @param localFile data schema to read from
   *
   * @return data schema loaded from file
   *
   * @throws InvalidConfigException if data schema could not be loaded successfully
   */
  protected DataSchema loadSchemaFromFile(File localFile) throws InvalidConfigException {
    Objects.requireNonNull(localFile);
    if (!localFile.exists()) {
      throw new IllegalStateException();
    }

    try {
      DataSchema dataSchema = factory.buildSchema(localFile);
      LOG.info("Successfully loaded data schema file " + dataSchema.getIdentifier());
      return dataSchema;
    } catch (IOException e) {
      LOG.error("Can't access local data schema file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_SCHEMA,
          "Can't access local data schema file");
    }
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
  protected DataSubschema loadSubschemaFromFile(File localFile) throws InvalidConfigException {
    Objects.requireNonNull(localFile);
    if (!localFile.exists()) {
      throw new IllegalStateException();
    }

    try {
      DataSubschema dataSubschema = factory.buildSubschema(localFile);
      LOG.info("Successfully loaded data subschema file " + dataSubschema.getName());
      return dataSubschema;
    } catch (IOException e) {
      LOG.error("Can't access local data schema file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_SCHEMA,
          "Can't access local data schema file");
    }
  }

  /**
   * Move and rename temporary file to final version.
   *
   * @param tmpFile   downloaded data schema (in temporary location with temporary filename)
   * @param schemaIdentifier schema identifier
   * @param schemaName schema name
   *
   * @throws IOException if moving file fails
   */
  private void finishInstallSchema(File tmpFile, String schemaIdentifier, String schemaName) throws IOException {
    Objects.requireNonNull(tmpFile);

    try {
      File installedFile = getDataSchema(schemaIdentifier, schemaName);
      FileUtils.moveFile(tmpFile, installedFile);
    } catch (IOException e) {
      LOG.error("Installing data schema failed, while trying to move and rename data schema file: " + e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Move and rename temporary file to final version.
   *
   * @param tmpFile   downloaded data schema file (in temporary location with temporary filename)
   * @param schemaIdentifier schema identifier
   * @param schemaName schema name
   * @param dataSubschema data schema file being installed
   *
   * @throws IOException if moving file fails
   */
  private void finishInstallSubschema(File tmpFile, String schemaIdentifier, String schemaName, DataSubschema dataSubschema) throws IOException {
    Objects.requireNonNull(tmpFile);
    Objects.requireNonNull(dataSubschema);
    Objects.requireNonNull(dataSubschema.getName());

    try {
      File installedFile = getDataSchemaFile(schemaIdentifier, schemaName, dataSubschema.getName());
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

  /**
   * Retrieve data schema file by its unique identifier.
   *
   * @param schemaIdentifier schema identifier
   * @param schemaName schema name
   *
   * @return data schema file
   */
  private File getDataSchema(String schemaIdentifier, String schemaName) {
    String filename = "_" + org.gbif.ipt.utils.FileUtils.getSuffixedFileName(schemaIdentifier, DATA_SCHEMA_FILE_SUFFIX);
    return dataDir.configFile(CONFIG_FOLDER + "/" + schemaName + "/" + filename);
  }

  /**
   * Get data schema directory.
   *
   * @param schemaName schema name
   *
   * @return data schema directory
   */
  private File getDataSchemaDirectory(String schemaName) {
    return dataDir.configFile(CONFIG_FOLDER + "/" + schemaName);
  }
}
