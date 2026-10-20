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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.SupportedDataPackageType;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.factory.DataSchemaFactory;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.IptFileUtils;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.Strings;
import org.apache.http.StatusLine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.inject.Inject;

import static org.gbif.ipt.service.InvalidConfigException.TYPE.INVALID_DATA_SCHEMA;
import static org.gbif.utils.HttpUtil.success;

public class DataPackageSchemaManagerImpl extends BaseManager implements DataPackageSchemaManager {

  private static final String CONFIG_FOLDER = ".dataPackages";
  private static final String DATA_SCHEMA_FILE_SUFFIX = ".json";

  private final ConfigWarnings warnings;
  private final DataSchemaFactory factory;
  private final RegistryManager registryManager;
  private final HttpClient downloader;
  private final Gson gson;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private final BaseAction baseAction;

  private List<DataPackageSchema> dataPackageSchemas = new ArrayList<>();
  private Map<String, DataPackageSchema> dataPackageSchemasByIdentifiers = new HashMap<>();
  private Map<String, String> dataPackageSchemaRawContentByName = new HashMap<>();
  private Map<String, Set<String>> alternativeIdentifiers = new HashMap<>();

  @Inject
  public DataPackageSchemaManagerImpl(
      AppConfig cfg,
      DataDir dataDir,
      ConfigWarnings warnings,
      DataSchemaFactory factory,
      SimpleTextProvider textProvider,
      RegistrationManager registrationManager,
      RegistryManager registryManager,
      HttpClient downloader) {
    super(cfg, dataDir);
    this.warnings = warnings;
    this.factory = factory;
    this.registryManager = registryManager;
    this.downloader = downloader;
    this.baseAction = new BaseAction(textProvider, cfg, registrationManager);
    this.gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
    // TODO: store in properties somewhere
    this.alternativeIdentifiers.put(Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER, Set.of(Constants.DATA_PACKAGE_DWC_DP_IDENTIFIER_2));
  }

  @Override
  public void uninstallSafely(String schemaIdentifier, String schemaName) {
    if (dataPackageSchemasByIdentifiers.containsKey(schemaIdentifier)) {
      uninstall(schemaIdentifier, schemaName);
    } else {
      LOG.warn("Data schema not installed locally, can't delete {}", schemaIdentifier);
    }
  }

  @Override
  public synchronized DataPackageSchema update(String identifier) throws IOException, RegistryException {
    // identify installed data schema by identifier
    DataPackageSchema installed = get(identifier);
    DataPackageSchema latestCompatibleSchema = null;

    if (installed != null) {
      // verify there is a newer version
      String latestCompatibleSchemaVersion = registryManager.getLatestCompatibleSchemaVersion(installed.getName(), installed.getVersion());

      if (latestCompatibleSchemaVersion != null) {
        latestCompatibleSchema = registryManager.getSchema(installed.getName(), latestCompatibleSchemaVersion);
      }

      boolean isNewVersion = latestCompatibleSchema != null
          && isIssuedLater(latestCompatibleSchema.getIssued(), installed.getIssued());

      if (isNewVersion && latestCompatibleSchema.getUrl() != null) {
        // replaces the installed version only once the new one is installed
        install(latestCompatibleSchema, installed);
      } else {
        return null;
      }
    }

    return latestCompatibleSchema;
  }

  @Override
  public DataPackageSchema get(String identifier) {
    if (identifier == null) {
      return null;
    }

    // load data packages if empty
    if (dataPackageSchemas.isEmpty()) {
      load();
    }

    return Optional.ofNullable(dataPackageSchemasByIdentifiers.get(identifier))
        .or(() -> getWithoutProtocol(identifier))
        .or(() -> getByAlternativeIdentifiers(identifier))
        .or(() -> getByName(identifier))
        .orElse(null);
  }

  @Override
  public String getVersion(String identifier) {
    if (identifier == null) {
      return null;
    }

    // load data packages if empty
    if (dataPackageSchemas.isEmpty()) {
      load();
    }

    return Optional.ofNullable(dataPackageSchemasByIdentifiers.get(identifier))
        .or(() -> getWithoutProtocol(identifier))
        .or(() -> getByAlternativeIdentifiers(identifier))
        .or(() -> getByName(identifier))
        .map(DataPackageSchema::getVersion)
        .orElse(null);
  }

  private Optional<DataPackageSchema> getWithoutProtocol(String identifier) {
    return dataPackageSchemasByIdentifiers.entrySet().stream()
        .filter(e -> org.gbif.ipt.utils.StringUtils.equalsWithoutProtocol(e.getKey(), identifier))
        .map(Map.Entry::getValue)
        .findFirst();
  }

  private Optional<DataPackageSchema> getByAlternativeIdentifiers(String identifier) {
    Optional<String> baseIdentifier = alternativeIdentifiers.entrySet().stream()
        .filter(e -> e.getValue().contains(identifier))
        .map(Map.Entry::getKey)
        .findFirst();

    return baseIdentifier.map(i -> dataPackageSchemasByIdentifiers.get(i));
  }

  private Optional<DataPackageSchema> getByName(String identifier) {
    for (DataPackageSchema ds : dataPackageSchemas) {
      if (identifier.equals(ds.getName()) || identifier.equals(ds.getIdentifier())) {
        return Optional.of(ds);
      }
    }

    return Optional.empty();
  }

  @Override
  public String getRawData(String name) {
    return dataPackageSchemaRawContentByName.get(name);
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
      dataPackageSchemaRawContentByName.remove(name);
      dataPackageSchemas.removeIf(d -> Strings.CS.equals(d.getIdentifier(), identifier));

      File f = getDataSchemaDirectory(name);
      if (f.exists()) {
        FileUtils.deleteQuietly(f);
      } else {
        LOG.warn("Data schema doesn't exist locally, can't delete {}", identifier);
      }
    } else {
      LOG.warn("Data schema not installed locally, can't delete {}", identifier);
    }
  }

  @Override
  public void installSupportedDataPackageSchemas() throws InvalidConfigException {
    List<DataPackageSchema> schemas = getSupportedDataSchemas();
    for (DataPackageSchema schema : schemas) {
      install(schema);
    }
  }

  @Override
  public synchronized void install(DataPackageSchema dataPackageSchema) throws InvalidConfigException {
    Objects.requireNonNull(dataPackageSchema);
    install(dataPackageSchema, dataPackageSchemasByIdentifiers.get(dataPackageSchema.getIdentifier()));
  }

  /**
   * Installs a data package schema, replacing an installed one (e.g. its previous version). All files are downloaded
   * and read into a staging folder first, which then replaces the schema folder: if anything fails, the installed
   * schema stays as it was, on disk and in memory.
   *
   * @param dataPackageSchema data schema to install
   * @param replaced installed data schema it replaces, or null
   */
  private synchronized void install(DataPackageSchema dataPackageSchema, DataPackageSchema replaced)
      throws InvalidConfigException {
    File stagingFolder = dataDir.tmpDir();
    try {
      // the schema as described by the registry, with the identifiers and URLs of its table schemas
      File mainSchemaFile = new File(stagingFolder, getDataSchemaFileName(dataPackageSchema.getIdentifier()));
      try (Writer writer = Files.newBufferedWriter(mainSchemaFile.toPath(), StandardCharsets.UTF_8)) {
        gson.toJson(dataPackageSchema, writer);
      }
      String rawContent = Files.readString(mainSchemaFile.toPath(), StandardCharsets.UTF_8);

      Set<DataPackageTableSchema> tableSchemas = new LinkedHashSet<>();
      for (DataPackageTableSchema ts : dataPackageSchema.getTableSchemas()) {
        File downloaded = download(ts.getUrl(), new File(stagingFolder, "download" + DATA_SCHEMA_FILE_SUFFIX));
        DataPackageTableSchema tableSchema = loadTableSchemaFromFile(downloaded);
        Objects.requireNonNull(tableSchema, "Table schema is empty: " + ts.getUrl());
        Objects.requireNonNull(tableSchema.getName(), "Table schema name is required: " + ts.getUrl());
        FileUtils.moveFile(downloaded,
            new File(stagingFolder, getTableSchemaFileName(dataPackageSchema.getIdentifier(), tableSchema.getName())));
        tableSchemas.add(tableSchema);
      }

      replaceFolder(getDataSchemaDirectory(dataPackageSchema.getName()), stagingFolder);
      dataPackageSchema.setTableSchemas(tableSchemas);

      // keep data schemas in local lookup: allowed one installed data schema per identifier
      if (replaced != null) {
        removeFromLookup(replaced);
        if (!Objects.equals(replaced.getName(), dataPackageSchema.getName())) {
          FileUtils.deleteQuietly(getDataSchemaDirectory(replaced.getName()));
        }
      }
      removeFromLookup(dataPackageSchema);
      dataPackageSchemasByIdentifiers.put(dataPackageSchema.getIdentifier(), dataPackageSchema);
      dataPackageSchemaRawContentByName.put(dataPackageSchema.getName(), rawContent);
      dataPackageSchemas.add(dataPackageSchema);
    } catch (Exception e) {
      String msg = baseAction.getText("admin.dataPackages.install.error",
          new String[] {Objects.toString(dataPackageSchema.getUrl(), dataPackageSchema.getIdentifier())});
      LOG.error(msg, e);
      throw new InvalidConfigException(INVALID_DATA_SCHEMA, msg, e);
    } finally {
      FileUtils.deleteQuietly(stagingFolder);
    }
  }

  /**
   * Replaces a folder with another one. The folder is moved aside first, and moved back if the replacement fails.
   *
   * @param folder folder to replace, may not exist
   * @param replacement folder replacing it
   */
  private void replaceFolder(File folder, File replacement) throws IOException {
    // set aside in the temp folder, not among the schema folders, in case it's left behind
    File backup = null;
    if (folder.exists()) {
      backup = new File(dataDir.tmpDir(), folder.getName());
      FileUtils.moveDirectory(folder, backup);
    }

    try {
      FileUtils.moveDirectory(replacement, folder);
    } catch (IOException e) {
      if (backup != null) {
        FileUtils.deleteQuietly(folder);
        FileUtils.moveDirectory(backup, folder);
      }
      throw e;
    }

    if (backup != null) {
      FileUtils.deleteQuietly(backup.getParentFile());
    }
  }

  private void removeFromLookup(DataPackageSchema schema) {
    dataPackageSchemasByIdentifiers.remove(schema.getIdentifier());
    dataPackageSchemaRawContentByName.remove(schema.getName());
    dataPackageSchemas.removeIf(d -> Strings.CS.equals(d.getIdentifier(), schema.getIdentifier()));
  }

  /**
   * @return true if the candidate has an issued date later than the installed one, or the only one
   */
  private static boolean isIssuedLater(Date candidate, Date installed) {
    if (candidate == null) {
      return false;
    }
    return installed == null || candidate.after(installed);
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
          dataPackageSchemaRawContentByName.clear();
          dataPackageSchemas.clear();
          for (String dataSchemaDirectoryName : dataSchemaNames) {
            tableSchemas = new LinkedHashSet<>();
            File dataSchemaDirectory = new File(dataPackageSchemasDir, dataSchemaDirectoryName);
            File[] files = dataSchemaDirectory.listFiles(filter);

            if (files != null) {
              File mainSchemaFile = getMainSchemaFileFromFiles(files);

              String dataPackageRawContent = "Failed to read the data package schema file";
              try {
                dataPackageRawContent = new String(Files.readAllBytes(mainSchemaFile.toPath()), StandardCharsets.UTF_8);
              } catch (IOException e) {
                LOG.error("Failed to read the data package schema file {}", mainSchemaFile, e);
              }

              dataPackageSchema = loadSchemaFromFile(mainSchemaFile);
              // keep data schema in local lookup
              dataPackageSchemasByIdentifiers.put(dataPackageSchema.getIdentifier(), dataPackageSchema);
              dataPackageSchemaRawContentByName.put(dataPackageSchema.getName(), dataPackageRawContent);

              for (DataPackageTableSchema tableSchema : dataPackageSchema.getTableSchemas()) {
                // TODO: 02/03/2023 HTTP vs HTTPS concerns
                String filename = IptFileUtils
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
    try {
      List<DataPackageSchema> supportedDataPackageSchemas = registryManager.getSupportedDataSchemas();

      for (DataPackageSchema supportedDataPackageSchema : supportedDataPackageSchemas) {
        // one failing schema must not stop the others
        try {
          installOrUpdateDefault(supportedDataPackageSchema);
        } catch (Exception e) {
          LOG.error("Failed to install/update default data package schema {}",
              supportedDataPackageSchema.getIdentifier(), e);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to install/update default data package schemas", e);
    }
  }

  private void installOrUpdateDefault(DataPackageSchema supportedDataPackageSchema) {
    DataPackageSchema installedDataSchema = dataPackageSchemasByIdentifiers.get(supportedDataPackageSchema.getIdentifier());

    if (installedDataSchema == null) {
      LOG.info("Missing default data package schema {}. Installing", supportedDataPackageSchema.getIdentifier());
      install(supportedDataPackageSchema, null);
    } else if (!Objects.equals(installedDataSchema.getVersion(), supportedDataPackageSchema.getVersion())
        // do not downgrade
        && isIssuedLater(supportedDataPackageSchema.getIssued(), installedDataSchema.getIssued())) {
      LOG.info("Schema {} uses outdated version {}. Updating to {}", supportedDataPackageSchema.getIdentifier(),
          installedDataSchema.getVersion(), supportedDataPackageSchema.getVersion());
      install(supportedDataPackageSchema, installedDataSchema);
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
   * @param tmpFile file to download to
   *
   * @return temporary file data schema was downloaded to
   * @throws IOException if it failed to be downloaded
   */
  private File download(URL url, File tmpFile) throws IOException {
    Objects.requireNonNull(url);
    StatusLine statusLine = downloader.download(url, tmpFile);
    if (success(statusLine)) {
      LOG.info("Successfully downloaded data schema: {}", url);
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
      msg = baseAction.getText("admin.dataPackages.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      LOG.error(msg);
    }

    // throw exception if all supported data schemas could not be loaded
    if (SupportedDataPackageType.values().length != supportedDataPackageSchemas.size()) {
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
      LOG.info("Successfully loaded data package schema file {}", dataPackageSchema.getIdentifier());
      return dataPackageSchema;
    } catch (IOException e) {
      LOG.error("Can't access local data package schema file ({})", localFile.getAbsolutePath(), e);
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
      LOG.info("Successfully loaded table schema file {}", tableSchema.getName());
      return tableSchema;
    } catch (IOException e) {
      LOG.error("Can't access local data schema file ({})", localFile.getAbsolutePath(), e);
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
          "Can't access local data schema file");
    }
  }

  @Override
  public File getTableSchemaFile(DataPackageSchema schema, String tableSchemaName) {
    return getTableSchemaFile(schema.getIdentifier(), schema.getName(), tableSchemaName);
  }

  /**
   * Retrieve a data schema file by its unique identifier.
   *
   * @param schemaIdentifier schema identifier
   * @param schemaName schema name
   * @param tableSchemaName table schema name
   *
   * @return data schema file
   */
  private File getTableSchemaFile(String schemaIdentifier, String schemaName, String tableSchemaName) {
    return dataDir.configFile(CONFIG_FOLDER + "/" + schemaName + "/" + getTableSchemaFileName(schemaIdentifier, tableSchemaName));
  }

  private static String getTableSchemaFileName(String schemaIdentifier, String tableSchemaName) {
    return IptFileUtils.getSuffixedFileName(schemaIdentifier + "_" + tableSchemaName, DATA_SCHEMA_FILE_SUFFIX);
  }

  /**
   * Retrieve a data schema file by its unique identifier.
   *
   * @param schemaIdentifier schema identifier
   *
   * @return data schema file
   */
  private static String getDataSchemaFileName(String schemaIdentifier) {
    return "_" + IptFileUtils.getSuffixedFileName(schemaIdentifier, DATA_SCHEMA_FILE_SUFFIX);
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
