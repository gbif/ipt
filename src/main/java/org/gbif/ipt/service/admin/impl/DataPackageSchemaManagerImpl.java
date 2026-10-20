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
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.apache.http.StatusLine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import jakarta.inject.Inject;

import static org.gbif.ipt.service.InvalidConfigException.TYPE.INVALID_DATA_SCHEMA;
import static org.gbif.utils.HttpUtil.success;

public class DataPackageSchemaManagerImpl extends BaseManager implements DataPackageSchemaManager {

  private static final String CONFIG_FOLDER = ".dataPackages";
  private static final String DATA_SCHEMA_FILE_SUFFIX = ".json";
  private static final Pattern VALID_SCHEMA_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

  private final ConfigWarnings warnings;
  private final DataSchemaFactory factory;
  private final RegistryManager registryManager;
  private final HttpClient downloader;
  private final Gson gson;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private final BaseAction baseAction;

  // installed data schemas, replaced as a whole on every change so that readers always see a consistent state;
  // null until loaded from the data dir
  private volatile Installed installed;
  private final Map<String, Set<String>> alternativeIdentifiers = new HashMap<>();

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

  /**
   * Snapshot of the installed data schemas, with the raw content of their main schema files by schema name. Never
   * modified: changes create a new one.
   */
  private record Installed(List<DataPackageSchema> schemas, Map<String, String> rawContentByName) {

    DataPackageSchema byIdentifier(String identifier) {
      return schemas.stream()
          .filter(schema -> Objects.equals(schema.getIdentifier(), identifier))
          .findFirst()
          .orElse(null);
    }

    /**
     * @return snapshot with the schema, replacing the installed one with the same identifier and the replaced one
     */
    Installed with(DataPackageSchema schema, String rawContent, DataPackageSchema replaced) {
      Installed result = without(schema);
      if (replaced != null) {
        result = result.without(replaced);
      }
      List<DataPackageSchema> resultSchemas = new ArrayList<>(result.schemas);
      resultSchemas.add(schema);
      Map<String, String> resultRawContent = new HashMap<>(result.rawContentByName);
      resultRawContent.put(schema.getName(), rawContent);
      return new Installed(List.copyOf(resultSchemas), Map.copyOf(resultRawContent));
    }

    Installed without(DataPackageSchema schema) {
      List<DataPackageSchema> resultSchemas = schemas.stream()
          .filter(installedSchema -> !Objects.equals(installedSchema.getIdentifier(), schema.getIdentifier()))
          .toList();
      Map<String, String> resultRawContent = new HashMap<>(rawContentByName);
      resultRawContent.remove(schema.getName());
      return new Installed(resultSchemas, Map.copyOf(resultRawContent));
    }
  }

  /**
   * @return the installed data schemas, loaded from the data dir on first use
   */
  private Installed installed() {
    Installed current = installed;
    if (current == null) {
      synchronized (this) {
        if (installed == null) {
          load();
        }
        current = installed;
      }
    }
    return current;
  }

  @Override
  public synchronized void uninstallSafely(String identifier) {
    DataPackageSchema schema = get(identifier);
    if (schema == null) {
      LOG.warn("Data schema not installed locally, can't delete {}", identifier);
      return;
    }

    // the folder of the installed schema, never one derived from the request
    File folder = getDataSchemaDirectory(schema.getName());
    if (folder.exists()) {
      // moved away first, so a failure leaves the schema installed rather than half deleted
      File removed = new File(dataDir.tmpDir(), folder.getName());
      try {
        FileUtils.moveDirectory(folder, removed);
      } catch (IOException e) {
        throw new InvalidConfigException(INVALID_DATA_SCHEMA,
            "Failed to remove data package schema " + schema.getIdentifier() + ": " + e.getMessage(), e);
      }
      FileUtils.deleteQuietly(removed.getParentFile());
    } else {
      LOG.warn("Data schema doesn't exist locally {}", schema.getIdentifier());
    }
    installed = installed().without(schema);
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

    Installed current = installed();
    return Optional.ofNullable(current.byIdentifier(identifier))
        .or(() -> getWithoutProtocol(current, identifier))
        .or(() -> getByAlternativeIdentifiers(current, identifier))
        .or(() -> getByName(current, identifier))
        .orElse(null);
  }

  @Override
  public String getVersion(String identifier) {
    DataPackageSchema schema = get(identifier);
    return schema != null ? schema.getVersion() : null;
  }

  private Optional<DataPackageSchema> getWithoutProtocol(Installed current, String identifier) {
    return current.schemas().stream()
        .filter(schema -> org.gbif.ipt.utils.StringUtils.equalsWithoutProtocol(schema.getIdentifier(), identifier))
        .findFirst();
  }

  private Optional<DataPackageSchema> getByAlternativeIdentifiers(Installed current, String identifier) {
    Optional<String> baseIdentifier = alternativeIdentifiers.entrySet().stream()
        .filter(e -> e.getValue().contains(identifier))
        .map(Map.Entry::getKey)
        .findFirst();

    return baseIdentifier.map(current::byIdentifier);
  }

  private Optional<DataPackageSchema> getByName(Installed current, String identifier) {
    return current.schemas().stream()
        .filter(schema -> identifier.equals(schema.getName()))
        .findFirst();
  }

  @Override
  public String getRawData(String name) {
    return name != null ? installed().rawContentByName().get(name) : null;
  }

  @Override
  public void installSupportedDataPackageSchemas() throws InvalidConfigException {
    List<String> failed = new ArrayList<>();
    // one failing schema must not stop the others
    for (DataPackageSchema schema : getSupportedDataSchemas()) {
      try {
        install(schema);
      } catch (InvalidConfigException e) {
        failed.add(schema.getIdentifier());
      }
    }

    if (!failed.isEmpty()) {
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
          "Failed to install data package schemas: " + String.join(", ", failed));
    }
  }

  @Override
  public synchronized void install(DataPackageSchema dataPackageSchema) throws InvalidConfigException {
    Objects.requireNonNull(dataPackageSchema);
    install(dataPackageSchema, installed().byIdentifier(dataPackageSchema.getIdentifier()));
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
      Objects.requireNonNull(dataPackageSchema.getIdentifier(), "Data package schema identifier is required");
      Objects.requireNonNull(dataPackageSchema.getName(), "Data package schema name is required");
      // the name is the schema folder's
      if (!VALID_SCHEMA_NAME.matcher(dataPackageSchema.getName()).matches()) {
        throw new IllegalArgumentException("Data package schema name is not a valid folder name: "
            + dataPackageSchema.getName());
      }

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

      if (replaced != null && !Objects.equals(replaced.getName(), dataPackageSchema.getName())) {
        FileUtils.deleteQuietly(getDataSchemaDirectory(replaced.getName()));
      }
      // allowed one installed data schema per identifier
      installed = installed().with(dataPackageSchema, rawContent, replaced);
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
    return installed().schemas();
  }

  /**
   * Loads the installed data schemas from the data dir, replacing the ones in memory. A schema that can't be loaded
   * is skipped, with a startup warning, so it doesn't prevent loading the others.
   *
   * @return number of data schemas that have been loaded successfully
   */
  @Override
  public synchronized int load() {
    List<DataPackageSchema> schemas = new ArrayList<>();
    Map<String, String> rawContentByName = new HashMap<>();

    File[] schemaFolders = dataDir.configFile(CONFIG_FOLDER).listFiles(File::isDirectory);
    if (schemaFolders != null) {
      Arrays.sort(schemaFolders);
      for (File schemaFolder : schemaFolders) {
        try {
          File mainSchemaFile = getMainSchemaFile(schemaFolder);
          DataPackageSchema schema = loadSchemaFolder(schemaFolder, mainSchemaFile);
          if (schemas.stream().anyMatch(s -> s.getIdentifier().equals(schema.getIdentifier())
              || s.getName().equals(schema.getName()))) {
            throw new InvalidConfigException(INVALID_DATA_SCHEMA,
                "Data package schema " + schema.getIdentifier() + " is installed more than once");
          }
          schemas.add(schema);
          rawContentByName.put(schema.getName(), Files.readString(mainSchemaFile.toPath(), StandardCharsets.UTF_8));
        } catch (Exception e) {
          String msg = "Failed to load data package schema from " + schemaFolder.getAbsolutePath() + ": " + e.getMessage();
          LOG.error(msg, e);
          warnings.addStartupError(msg);
        }
      }
    }

    installed = new Installed(List.copyOf(schemas), Map.copyOf(rawContentByName));
    return schemas.size();
  }

  /**
   * Loads a data schema and its table schemas from its folder.
   *
   * @param schemaFolder data schema folder
   * @param mainSchemaFile main schema file in the folder
   * @return data schema
   * @throws InvalidConfigException if the data schema or one of its table schemas is missing or invalid
   */
  private DataPackageSchema loadSchemaFolder(File schemaFolder, File mainSchemaFile) throws InvalidConfigException {
    DataPackageSchema schema = loadSchemaFromFile(mainSchemaFile);
    if (schema.getIdentifier() == null || schema.getName() == null) {
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
          "Data package schema file has no identifier or name: " + mainSchemaFile.getName());
    }
    // its files are looked up in the folder named after it, e.g. for publishing
    if (!schemaFolder.getName().equals(schema.getName())) {
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
          "Data package schema " + schema.getName() + " is installed in folder " + schemaFolder.getName());
    }

    Set<DataPackageTableSchema> tableSchemas = new LinkedHashSet<>();
    if (schema.getTableSchemas() != null) {
      for (DataPackageTableSchema tableSchema : schema.getTableSchemas()) {
        tableSchemas.add(loadTableSchemaFromFile(getInstalledTableSchemaFile(schemaFolder, schema, tableSchema)));
      }
    }
    schema.setTableSchemas(tableSchemas);
    return schema;
  }

  /**
   * Finds the installed file of a table schema, named like on install, or after the table schema identifier by older
   * IPT versions.
   */
  private File getInstalledTableSchemaFile(File schemaFolder, DataPackageSchema schema,
                                           DataPackageTableSchema tableSchema) throws InvalidConfigException {
    List<String> fileNames = new ArrayList<>();
    if (tableSchema.getName() != null) {
      fileNames.add(getTableSchemaFileName(schema.getIdentifier(), tableSchema.getName()));
    }
    if (tableSchema.getIdentifier() != null) {
      fileNames.add(IptFileUtils.getSuffixedFileName(tableSchema.getIdentifier(), DATA_SCHEMA_FILE_SUFFIX));
    }

    return fileNames.stream()
        .map(fileName -> new File(schemaFolder, fileName))
        .filter(File::isFile)
        .findFirst()
        .orElseThrow(() -> new InvalidConfigException(INVALID_DATA_SCHEMA,
            "Table schema file was not found: " + String.join(" or ", fileNames)));
  }

  @Override
  public synchronized void installOrUpdateDefaults() {
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
    DataPackageSchema installedDataSchema = installed().byIdentifier(supportedDataPackageSchema.getIdentifier());

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
   * Finds the main schema file in a data schema folder: the JSON file starting with an underscore.
   *
   * @param schemaFolder data schema folder
   * @return main schema file
   */
  private File getMainSchemaFile(File schemaFolder) throws InvalidConfigException {
    File[] files = schemaFolder.listFiles(
        (dir, name) -> name.startsWith("_") && Strings.CI.endsWith(name, DATA_SCHEMA_FILE_SUFFIX));
    if (files == null || files.length == 0) {
      throw new InvalidConfigException(INVALID_DATA_SCHEMA, "Main schema file was not found");
    }
    if (files.length > 1) {
      // which one is installed is unknown
      throw new InvalidConfigException(INVALID_DATA_SCHEMA, "More than one main schema file: "
          + Arrays.stream(files).map(File::getName).sorted().collect(Collectors.joining(", ")));
    }
    return files[0];
  }

  @Override
  public boolean isSchemaInstalled(String nameOrIdentifier) {
    return installed().schemas().stream()
        .anyMatch(schema -> schema.getIdentifier().equals(nameOrIdentifier) || schema.getName().equals(nameOrIdentifier));
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

    if (supportedDataPackageSchemas.isEmpty()) {
      String msg = "No supported data package schemas could be loaded";
      LOG.error(msg);
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_DIR, msg);
    }

    // the supported schemas depend on the registry type, e.g. some are for development only
    Set<String> loadedNames = supportedDataPackageSchemas.stream()
        .map(DataPackageSchema::getName)
        .collect(Collectors.toSet());
    List<String> missingNames = cfg.getSupportedDataSchemaNamesWithVersions().keySet().stream()
        .filter(name -> !loadedNames.contains(name))
        .sorted()
        .toList();
    if (!missingNames.isEmpty()) {
      LOG.warn("Supported data package schemas could not be loaded: {}", String.join(", ", missingNames));
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
    try {
      DataPackageSchema dataPackageSchema = factory.buildSchema(localFile);
      if (dataPackageSchema == null) {
        throw new InvalidConfigException(INVALID_DATA_SCHEMA, "Data package schema file is empty: " + localFile.getName());
      }
      LOG.info("Successfully loaded data package schema file {}", dataPackageSchema.getIdentifier());
      return dataPackageSchema;
    } catch (IOException | JsonParseException e) {
      LOG.error("Can't read local data package schema file ({})", localFile.getAbsolutePath(), e);
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
          "Can't read local data package schema file " + localFile.getName() + ": " + e.getMessage(), e);
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
    try {
      DataPackageTableSchema tableSchema = factory.buildTableSchema(localFile);
      if (tableSchema == null) {
        throw new InvalidConfigException(INVALID_DATA_SCHEMA, "Table schema file is empty: " + localFile.getName());
      }
      LOG.info("Successfully loaded table schema file {}", tableSchema.getName());
      return tableSchema;
    } catch (IOException | JsonParseException e) {
      LOG.error("Can't read local table schema file ({})", localFile.getAbsolutePath(), e);
      throw new InvalidConfigException(INVALID_DATA_SCHEMA,
          "Can't read local table schema file " + localFile.getName() + ": " + e.getMessage(), e);
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
