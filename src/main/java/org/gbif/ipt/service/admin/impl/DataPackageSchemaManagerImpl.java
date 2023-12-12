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
import org.gbif.ipt.config.SupportedDatapackageType;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.factory.DataSchemaFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.service.InvalidConfigException.TYPE.INVALID_DATA_SCHEMA;
import static org.gbif.utils.HttpUtil.success;

@Singleton
public class DataPackageSchemaManagerImpl extends BaseManager implements DataPackageSchemaManager {

  private static final String CONFIG_FOLDER = ".dataPackages";
  private static final String DATA_SCHEMA_FILE_SUFFIX = ".json";

  private final ConfigWarnings warnings;
  private final DataSchemaFactory factory;
  private final RegistryManager registryManager;
  private final ResourceManager resourceManager;
  private final HttpClient downloader;
  private final Gson gson;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private final BaseAction baseAction;

  private List<DataPackageSchema> dataPackageSchemas = new ArrayList<>();
  private Map<String, DataPackageSchema> dataPackageSchemasByIdentifiers = new HashMap<>();

  @Inject
  public DataPackageSchemaManagerImpl(AppConfig cfg, DataDir dataDir, ConfigWarnings warnings, DataSchemaFactory factory,
                                      SimpleTextProvider textProvider, RegistrationManager registrationManager,
                                      RegistryManager registryManager, ResourceManager resourceManager, HttpClient downloader) {
    super(cfg, dataDir);
    this.warnings = warnings;
    this.factory = factory;
    this.registryManager = registryManager;
    this.resourceManager = resourceManager;
    this.downloader = downloader;
    this.baseAction = new BaseAction(textProvider, cfg, registrationManager);
    this.gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
  }

  @Override
  public void uninstallSafely(String schemaIdentifier, String schemaName) throws DeletionNotAllowedException {
    if (dataPackageSchemasByIdentifiers.containsKey(schemaIdentifier)) {
      // check if it's used by some resources
      for (Resource r : resourceManager.list()) {
        if (r.getSchemaIdentifier() != null
            && r.getSchemaIdentifier().equals(schemaIdentifier) && !r.getDataPackageMappings().isEmpty()) {
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
    DataPackageSchema installed = get(identifier);

    if (installed != null) {
      // verify there is a newer version
      DataPackageSchema latestCompatibleSchema = null;
      String latestCompatibleSchemaVersion = registryManager.getLatestCompatibleSchemaVersion(installed.getName(), installed.getVersion());

      if (latestCompatibleSchemaVersion != null) {
        latestCompatibleSchema = registryManager.getSchema(installed.getName(), latestCompatibleSchemaVersion);
      }

      boolean isNewVersion = false;
      if (latestCompatibleSchema != null) {
        Date issued = installed.getIssued();
        Date issuedLatest = latestCompatibleSchema.getIssued();

        if (issued == null && issuedLatest != null) {
          isNewVersion = true;
        } else if (issued != null && issuedLatest != null) {
          // the latest version must have newer issued date
          isNewVersion = (issuedLatest.compareTo(issued) > 0);
        }
      }

      if (isNewVersion && latestCompatibleSchema.getUrl() != null) {
        // uninstall and install new version
        uninstall(identifier, latestCompatibleSchema.getName());
        install(latestCompatibleSchema);

        updateResourcesAfterSchemaUpdate(latestCompatibleSchema);
      }
    }
  }

  private void updateResourcesAfterSchemaUpdate(DataPackageSchema newlyInstalledSchema) {
    resourceManager.list()
        .stream()
        .filter(Resource::isDataPackage)
        .forEach(res -> updateResourceAfterSchemaUpdate(res, newlyInstalledSchema));
  }

  private void updateResourceAfterSchemaUpdate(Resource resource, DataPackageSchema newlyInstalledSchema) {
    // update metadata profile
    if (CAMTRAP_DP.equals(resource.getCoreType())) {
      if (resource.getDataPackageMetadata() instanceof CamtrapMetadata) {
        CamtrapMetadata metadata = (CamtrapMetadata) resource.getDataPackageMetadata();
        metadata.setProfile(newlyInstalledSchema.getProfile());
      }
    }

    // update schema in mappings
    resource.getDataPackageMappings().forEach(m -> m.setDataPackageSchema(newlyInstalledSchema));
  }

  @Override
  public DataPackageSchema get(String identifier) {
    DataPackageSchema result = dataPackageSchemasByIdentifiers.get(identifier);

    // try name
    if (result == null) {
      if (dataPackageSchemas.isEmpty()) {
        load();
      }

      for (DataPackageSchema ds : dataPackageSchemas) {
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
    if (dataPackageSchemasByIdentifiers.containsKey(identifier)) {
      dataPackageSchemasByIdentifiers.remove(identifier);
      dataPackageSchemas.removeIf(d -> StringUtils.equals(d.getIdentifier(), identifier));

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
  public void installSupportedSchemas() throws InvalidConfigException {
    List<DataPackageSchema> schemas = getSupportedDataSchemas();
    for (DataPackageSchema schema : schemas) {
      install(schema);
    }
  }

  @Override
  public synchronized void install(DataPackageSchema dataPackageSchema) throws InvalidConfigException {
    Objects.requireNonNull(dataPackageSchema);
    try {
      String filename = org.gbif.ipt.utils.FileUtils
          .getSuffixedFileName("_" + dataPackageSchema.getIdentifier().replace(DATA_SCHEMA_FILE_SUFFIX, ""), DATA_SCHEMA_FILE_SUFFIX);
      File tmpFileSchema = dataDir.tmpFile(filename);

      try (FileWriter fw = new FileWriter(tmpFileSchema)) {
        gson.toJson(dataPackageSchema, fw);
      }
      finishInstallSchema(tmpFileSchema, dataPackageSchema.getIdentifier(), dataPackageSchema.getName());

      Set<DataPackageTableSchema> tableSchemas = new LinkedHashSet<>();
      for (DataPackageTableSchema ts : dataPackageSchema.getTableSchemas()) {
        File tmpFile = download(ts.getUrl());
        DataPackageTableSchema tableSchema = loadTableSchemaFromFile(tmpFile);
        finishInstallTableSchema(tmpFile, dataPackageSchema.getIdentifier(), dataPackageSchema.getName(), tableSchema);
        tableSchemas.add(tableSchema);
      }

      dataPackageSchema.setTableSchemas(tableSchemas);

      // keep data schemas in local lookup: allowed one installed data schema per identifier
      dataPackageSchemasByIdentifiers.put(dataPackageSchema.getIdentifier(), dataPackageSchema);
      dataPackageSchemas.add(dataPackageSchema);
    } catch (InvalidConfigException e) {
      throw e;
    } catch (Exception e) {
      String msg = baseAction.getText("admin.schemas.install.error", new String[] {dataPackageSchema.getUrl().toString()});
      LOG.error(msg, e);

      // clean directory if installation failed
      try {
        File schemaConfigFolder = dataDir.configFile(CONFIG_FOLDER + "/" + dataPackageSchema.getName());
        FileUtils.cleanDirectory(schemaConfigFolder);
      } catch (IOException ioe) {
        LOG.error("Failed to clean directory for schema " + dataPackageSchema.getName(), e);
      }

      throw new InvalidConfigException(INVALID_DATA_SCHEMA, msg, e);
    }
  }

  @Override
  public List<DataPackageSchema> list() {
    if (dataPackageSchemas.isEmpty()) {
      load();
    }
    return dataPackageSchemas;
  }

  @Override
  public int load() {
    File dataPackageSchemasDir = dataDir.configFile(CONFIG_FOLDER);
    int counter = 0;
    if (dataPackageSchemasDir.isDirectory()) {
      String[] dataSchemaNames = dataPackageSchemasDir.list((current, name) -> new File(current, name).isDirectory());
      FilenameFilter filter = new SuffixFileFilter(DATA_SCHEMA_FILE_SUFFIX, IOCase.INSENSITIVE);
      DataPackageSchema dataPackageSchema;
      Set<DataPackageTableSchema> tableSchemas;

      try {
        if (dataSchemaNames != null) {
          dataPackageSchemasByIdentifiers.clear();
          dataPackageSchemas.clear();
          for (String dataSchemaDirectoryName : dataSchemaNames) {
            tableSchemas = new LinkedHashSet<>();
            File dataSchemaDirectory = new File(dataPackageSchemasDir, dataSchemaDirectoryName);
            File[] files = dataSchemaDirectory.listFiles(filter);

            if (files != null) {
              File mainSchemaFile = getMainSchemaFileFromFiles(files);
              dataPackageSchema = loadSchemaFromFile(mainSchemaFile);
              // keep data schema in local lookup
              dataPackageSchemasByIdentifiers.put(dataPackageSchema.getIdentifier(), dataPackageSchema);

              for (DataPackageTableSchema tableSchema : dataPackageSchema.getTableSchemas()) {
                // TODO: 02/03/2023 HTTP vs HTTPS concerns
                String filename = org.gbif.ipt.utils.FileUtils
                    .getSuffixedFileName(tableSchema.getIdentifier(), DATA_SCHEMA_FILE_SUFFIX);
                File tableSchemaFile = getTableSchemaConfigFileByName(files, filename);
                tableSchemas.add(loadTableSchemaFromFile(tableSchemaFile));
              }

              dataPackageSchema.setTableSchemas(tableSchemas);
              dataPackageSchemas.add(dataPackageSchema);
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
  public void installOrUpdateDefaults() {
    List<DataPackageSchema> supportedDataPackageSchemas = registryManager.getSupportedDataSchemas();

    for (DataPackageSchema supportedDataPackageSchema : supportedDataPackageSchemas) {
      if (!isSchemaInstalled(supportedDataPackageSchema.getIdentifier())) {
        // schema is not installed, install it
        LOG.info("Missing default schema {}. Installing", supportedDataPackageSchema.getIdentifier());
        install(supportedDataPackageSchema);
      } else {
        // schema is installed, make sure the proper version used
        DataPackageSchema installedDataSchema = dataPackageSchemasByIdentifiers.get(supportedDataPackageSchema.getIdentifier());

        String installedDataSchemaVersion = installedDataSchema.getVersion();
        String dataSchemaName = installedDataSchema.getName();
        String dataSchemaIdentifier = installedDataSchema.getIdentifier();
        Date installedSchemaIssuedDate = installedDataSchema.getIssued();
        Date supportedSchemaIssuedDate = supportedDataPackageSchema.getIssued();

        // version must not match
        // installed schema must be older than supported one (do not downgrade)
        if (!installedDataSchemaVersion.equals(supportedDataPackageSchema.getVersion())
            && installedSchemaIssuedDate.compareTo(supportedSchemaIssuedDate) < 0) {
          LOG.info("Schema {} uses outdated version {}. Updating to {}",
              supportedDataPackageSchema.getIdentifier(), installedDataSchemaVersion, supportedDataPackageSchema.getVersion());
          uninstall(dataSchemaIdentifier, dataSchemaName);
          install(supportedDataPackageSchema);
        }
      }
    }
  }

  /**
   * Find and get a table schema file from an array of files.
   *
   * @param files array of files
   * @param filename table schema file name
   * @return table schema file from an array
   */
  private File getTableSchemaConfigFileByName(File[] files, String filename) {
    for (File file : files) {
      if (filename.equals(file.getName())) {
        return file;
      }
    }

    throw new InvalidConfigException(INVALID_DATA_SCHEMA, "Table schema file was not found: " + filename);
  }

  /**
   * Find and get main schema file from array of files (it starts with underscore character).
   *
   * @param files array of files
   * @return main schema file
   */
  private File getMainSchemaFileFromFiles(File[] files) {
    for (File file : files) {
      if (file.getName().startsWith("_")) {
        return file;
      }
    }

    throw new InvalidConfigException(INVALID_DATA_SCHEMA, "Main schema file was not found!");
  }

  @Override
  public boolean isSchemaInstalled(String nameOrIdentifier) {
    boolean result = false;

    if (dataPackageSchemas.isEmpty()) {
      load();
    }

    for (DataPackageSchema dataPackageSchema : dataPackageSchemas) {
      if (dataPackageSchema.getIdentifier().equals(nameOrIdentifier)
          || dataPackageSchema.getName().equals(nameOrIdentifier)) {
        result = true;
        break;
      }
    }

    return result;
  }
  @Override
  public boolean isSchemaType(String nameOrIdentifier) {
    boolean result = false;

    List<DataPackageSchema> dataPackageSchemas = registryManager.getLatestDataPackageSchemas();
    for (DataPackageSchema dataPackageSchema : dataPackageSchemas) {
      if (dataPackageSchema.getIdentifier().equals(nameOrIdentifier)
          || dataPackageSchema.getName().equals(nameOrIdentifier)) {
        result = true;
        break;
      }
    }

    return result;
  }

  @Override
  public String getSchemaIdentifier(String schemaName) {
    String result = null;

    List<DataPackageSchema> dataPackageSchemas = registryManager.getLatestDataPackageSchemas();
    for (DataPackageSchema dataPackageSchema : dataPackageSchemas) {
      if (dataPackageSchema.getName().equals(schemaName)) {
        result = dataPackageSchema.getIdentifier();
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
   * Return the supported versions of schemas from the registry.
   *
   * @return list containing latest versions of data schemas
   */
  private List<DataPackageSchema> getSupportedDataSchemas() {
    List<DataPackageSchema> supportedDataPackageSchemas = new ArrayList<>();
    try {
      for (DataPackageSchema schema : registryManager.getSupportedDataSchemas()) {
        if (schema.getIdentifier() != null) {
            supportedDataPackageSchemas.add(schema);
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

    // throw exception if all supported data schemas could not be loaded
    if (SupportedDatapackageType.values().length != supportedDataPackageSchemas.size()) {
      String msg = "Not all supported data schemas were loaded!";
      LOG.error(msg);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_DIR, msg);
    }
    return supportedDataPackageSchemas;
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
  protected DataPackageSchema loadSchemaFromFile(File localFile) throws InvalidConfigException {
    Objects.requireNonNull(localFile);
    if (!localFile.exists()) {
      throw new IllegalStateException();
    }

    try {
      DataPackageSchema dataPackageSchema = factory.buildSchema(localFile);
      LOG.info("Successfully loaded data schema file " + dataPackageSchema.getIdentifier());
      return dataPackageSchema;
    } catch (IOException e) {
      LOG.error("Can't access local data schema file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
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
  protected DataPackageTableSchema loadTableSchemaFromFile(File localFile) throws InvalidConfigException {
    Objects.requireNonNull(localFile);
    if (!localFile.exists()) {
      throw new IllegalStateException();
    }

    try {
      DataPackageTableSchema tableSchema = factory.buildTableSchema(localFile);
      LOG.info("Successfully loaded table schema file " + tableSchema.getName());
      return tableSchema;
    } catch (IOException e) {
      LOG.error("Can't access local data schema file (" + localFile.getAbsolutePath() + ")", e);
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
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
   * @param tableSchema data schema file being installed
   *
   * @throws IOException if moving file fails
   */
  private void finishInstallTableSchema(File tmpFile, String schemaIdentifier, String schemaName, DataPackageTableSchema tableSchema) throws IOException {
    Objects.requireNonNull(tmpFile);
    Objects.requireNonNull(tableSchema, "Table schema must not be null");
    Objects.requireNonNull(tableSchema.getName(), "Table schema name is required");

    try {
      File installedFile = getTableSchemaFile(schemaIdentifier, schemaName, tableSchema.getName());
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
   * @param tableSchemaName table schema name
   *
   * @return data schema file
   */
  private File getTableSchemaFile(String schemaIdentifier, String schemaName, String tableSchemaName) {
    String filename = org.gbif.ipt.utils.FileUtils.getSuffixedFileName(schemaIdentifier + "_" + tableSchemaName, DATA_SCHEMA_FILE_SUFFIX);
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
