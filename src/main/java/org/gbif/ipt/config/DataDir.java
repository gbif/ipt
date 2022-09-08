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
package org.gbif.ipt.config;

import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.utils.InputStreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

/**
 * A very simple utility class to encapsulate the basic layout of the data directory and to configure & persist the
 * path for that directory and make it available to the entire application.
 */
@Singleton
public class DataDir {

  public static final String LOGGING_DIR = "logs";
  public static final String CONFIG_DIR = "config";
  public static final String RESOURCES_DIR = "resources";
  public static final String TMP_DIR = "tmp";
  public static final String PERSISTENCE_FILENAME = "resource.xml";
  public static final String INFERRED_METADATA_FILENAME = "inferredMetadata.xml";
  public static final String EML_XML_FILENAME = "eml.xml";
  public static final String DWCA_FILENAME = "dwca.zip";
  public static final String PUBLICATION_LOG_FILENAME = "publication.log";
  private static final Random RANDOM = new Random();

  private static final Logger LOG = LogManager.getLogger(DataDir.class);

  protected File dataDir;
  private File dataDirSettingFile;
  private InputStreamUtils streamUtils = new InputStreamUtils();

  private DataDir() {
  }

  public enum DirStatus {
    NOT_EXIST, NO_ACCESS, READ_ONLY, READ_WRITE
  }

  /**
   * Build and configure new IPT Data Directory instance from location settings file.
   *
   * @param dataDirSettingFile location settings file specifying location of existing IPT data directory
   *
   * @return IPT Data Directory instance
   */
  public static DataDir buildFromLocationFile(File dataDirSettingFile) {
    DataDir dd = new DataDir();
    dd.dataDirSettingFile = dataDirSettingFile;
    if (dataDirSettingFile != null && dataDirSettingFile.exists()) {
      String dataDirPath = null;
      try {
        dataDirPath = StringUtils.trimToNull(FileUtils.readFileToString(dataDirSettingFile, "UTF-8"));
        if (dataDirPath != null) {
          LOG.info("IPT Data Directory configured at " + dataDirPath);
          dd.dataDir = new File(dataDirPath);
        }
      } catch (IOException e) {
        LOG.error(
          "Failed to read the IPT Data Directory location settings file in WEB-INF at " + dataDirSettingFile.getAbsolutePath(), e);
      }
    } else {
      LOG.warn("IPT Data Directory location settings file in WEB-INF not found. Continue without data directory.");
    }
    return dd;
  }

  /**
   * Build and configure new IPT Data Directory instance from specified path.
   *
   * @param dataDirPath location of existing IPT Data Directory
   *
   * @return IPT Data Directory instance
   */
  public static DataDir buildFromString(String dataDirPath) {
    DataDir dd = new DataDir();
    if (dataDirPath != null) {
      LOG.info("IPT Data Directory configured at " + dataDirPath);
      dd.dataDir = new File(dataDirPath);
    }
    return dd;
  }

  private void assureDirExists(File f) {
    if (f != null && !f.exists()) {
      f.mkdirs();
    }
  }

  private void assureParentExists(File f) {
    if (f != null && !f.getParentFile().exists()) {
      f.getParentFile().mkdirs();
    }
  }

  protected void clearTmp() throws IOException {
    File tmpDir = tmpFile("");
    FileUtils.forceMkdir(tmpDir);
    FileUtils.cleanDirectory(tmpDir);
    LOG.debug("Cleared temporary folder");
  }

  /**
   * Constructs an absolute path to a file within the config folder of the data dir.
   *
   * @param path the relative path within the config folder
   */
  public File configFile(String path) {
    return dataFile(CONFIG_DIR + "/" + path);
  }

  private void createDefaultDir() throws IOException {
    // create config, resources
    File configDir = dataFile(CONFIG_DIR);
    File resourcesDir = dataFile(RESOURCES_DIR);
    File loggingDir = dataFile(LOGGING_DIR);
    FileUtils.forceMkdir(configDir);
    FileUtils.forceMkdir(resourcesDir);
    FileUtils.forceMkdir(loggingDir);
    // copy default config files
    InputStream input = streamUtils.classpathStream("configDefault/ipt.properties");
    if (input == null) {
      throw new InvalidConfigException(TYPE.CONFIG_WRITE,
        "Cannot read required classpath resources to create new data dir!");
    }
    org.gbif.ipt.utils.FileUtils.copyStreamToFile(input, configFile(AppConfig.DATADIR_PROPFILE));

    input = streamUtils.classpathStream("configDefault/about2.ftl");
    if (input == null) {
      throw new InvalidConfigException(TYPE.CONFIG_WRITE,
        "Cannot read required classpath resources to create new data dir!");
    }
    org.gbif.ipt.utils.FileUtils.copyStreamToFile(input, configFile("about2.ftl"));

    LOG.info("Creating new default data dir");
  }

  /**
   * Basic method to convert a relative path within the data dir to an absolute path on the filesystem.
   *
   * @param path the relative path within the data dir
   */
  public File dataFile(String path) {
    if (dataDir == null) {
      throw new IllegalStateException("No data dir has been configured yet");
    }
    File f = new File(dataDir, path);
    assureParentExists(f);
    return f;
  }

  /**
   * @return true if a working data directory is configured
   */
  public boolean isConfigured() {
    return dataDir != null && dataDir.isDirectory() && dataDir.list().length > 0;
  }

  /**
   * @return true if a working data directory is configured, but is not yet set up
   */
  public boolean isConfiguredButEmpty() {
    return dataDir != null && dataDir.isDirectory() && dataDir.list().length == 0;
  }

  /**
   * Constructs an absolute path to the config folder of the data dir.
   */
  public File configDir() {
    return dataFile(CONFIG_DIR);
  }

  /**
   * Constructs an absolute path to the tmp folder of the data dir.
   */
  public File tmpRootDir() {
    return dataFile(TMP_DIR);
  }

  /**
   * Constructs an absolute path to the resources folder of the data dir.
   */
  public File resourcesDir() {
    return dataFile(RESOURCES_DIR);
  }

  /**
   * Constructs an absolute path to the logs folder of the data dir.
   */
  public File loggingDir() {
    return dataFile(LOGGING_DIR);
  }

  /**
   * Constructs an absolute path to a file within the logs folder of the data dir.
   *
   * @param path the relative path within the logs folder
   */
  public File loggingFile(String path) {
    return dataFile(LOGGING_DIR + "/" + path);
  }

  private void persistLocation() throws IOException {
    // persist location in WEB-INF
    FileUtils.writeStringToFile(dataDirSettingFile, dataDir.getAbsolutePath(), StandardCharsets.UTF_8);
    LOG.info("IPT DataDir location file in /WEB-INF changed to " + dataDir.getAbsolutePath());
  }

  /**
   * Retrieves published DwC-A file for a specific version of a resource.
   *
   * @param resourceName resource short name
   * @param version      version
   *
   * @return DwC-A file having specific version
   */
  public File resourceDwcaFile(@NotNull String resourceName, @NotNull BigDecimal version) {
    String fn = "dwca-" + version.toPlainString() + ".zip";
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + fn);
  }

  /**
   * Retrieves DwC-A file for a resource.
   *
   * @param resourceName resource short name
   *
   * @return DwC-A file having specific version
   */
  public File resourceDwcaFile(@NotNull String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + DWCA_FILENAME);
  }

  /**
   * Retrieves published EML file for a specific version of a resource.
   *
   * @param resourceName resource short name
   * @param version      version
   *
   * @return EML file having specific version
   */
  public File resourceEmlFile(@NotNull String resourceName, @NotNull BigDecimal version) {
    String fn = "eml-" + version.toPlainString() + ".xml";
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + fn);
  }

  /**
   * Retrieves EML file for a resource.
   *
   * @param resourceName resource short name
   *
   * @return interim EML file for resource
   */
  public File resourceEmlFile(@NotNull String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + EML_XML_FILENAME);
  }

  public File resourceFile(Resource resource, String path) {
    if (resource == null) {
      return null;
    }
    return resourceFile(resource.getShortname(), path);
  }

  /**
   * Constructs an absolute path to a file within a resource folder inside the data dir
   *
   * @param path the relative path within the individual resource folder
   */
  public File resourceFile(String resourceName, String path) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + path);
  }

  /**
   * Constructs an absolute path to a resource.xml file inside the data dir.
   *
   * @param resourceName resource name
   * @return absolute path to the resource.xml
   */
  public File resourceFile(String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + PERSISTENCE_FILENAME);
  }

  /**
   * Constructs an absolute path to a resource.xml file inside the data dir.
   *
   * @param resource resource
   * @return absolute path to the resource.xml
   */
  public File resourceFile(Resource resource) {
    return resource == null ? null : dataFile(RESOURCES_DIR + "/" + resource.getShortname() + "/" + PERSISTENCE_FILENAME);
  }

  /**
   * Constructs an absolute path to a inferredMetadata.xml file inside the data dir.
   *
   * @param resourceName resource name
   * @return absolute path to the inferredMetadata.xml
   */
  public File resourceInferredMetadataFile(String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + INFERRED_METADATA_FILENAME);
  }

  /**
   * @param suffix the logo file suffix, indicating the format. E.g. jpeg or gif
   */
  public File resourceLogoFile(String resourceName, String suffix) {
    if (suffix == null) {
      suffix = "jpeg";
    }
    suffix = suffix.toLowerCase();
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/logo." + suffix);
  }

  /**
   * @param suffix the logo file suffix, indicating the format. E.g. jpeg or gif
   */
  public File appLogoFile(String suffix) {
    if (suffix == null) {
      suffix = "jpeg";
    }
    suffix = suffix.toLowerCase();
    return dataFile(CONFIG_DIR + "/.uiSettings/logos/logo." + suffix);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void removeLogoFile() {
    File logosDirectory = new File(dataDir, CONFIG_DIR + "/.uiSettings/logos");
    File[] logoFiles = logosDirectory.listFiles();
    if (logoFiles != null) {
      Stream.of(logoFiles)
          .filter(file -> !file.isDirectory())
          .filter(file -> file.getName().startsWith("logo"))
          .forEach(File::delete);
    }
  }

  public File resourcePublicationLogFile(String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + PUBLICATION_LOG_FILENAME);
  }

  /**
   * Retrieves published RTF file for a specific version of a resource.
   *
   * @param resourceName resource short name
   * @param version      version
   *
   * @return RTF file having specific version, defaulting to the latest published version if no version specified
   */
  public File resourceRtfFile(@NotNull String resourceName, @NotNull BigDecimal version) {
    String fn = resourceName + "-" + version.toPlainString() + ".rtf";
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + fn);
  }

  /**
   * Sets the path to the data directory for the entire application and persists it in the /WEB-INF folder. This method
   * does not reload any configuration though - so normally setting the dataDir should be done through the
   * ConfigManager
   * which calls this method but also reloads all user configurations.
   *
   * @return true if a new data dir was created, false when an existing was read
   */
  public boolean setDataDir(File dataDir) throws InvalidConfigException {
    if (dataDir == null) {
      throw new NullPointerException("DataDir file required");
    } else {

      this.dataDir = dataDir;
      File configDir = configFile("");

      if (dataDir.exists() && (!dataDir.isDirectory() || dataDir.list().length > 0)) {
        // EXISTING file or directory with content: make sure its an IPT datadir - otherwise break
        if (dataDir.isDirectory()) {
          // check if this directory contains a config folder - if not copy empty default dir from classpath
          if (!configDir.exists() || !configDir.isDirectory()) {
            this.dataDir = null;
            throw new InvalidConfigException(TYPE.INVALID_DATA_DIR,
              "DataDir " + dataDir.getAbsolutePath() + " exists already and is no IPT data dir.");
          }
          LOG.info("Reusing existing data dir {}", dataDir);
          // persist location in WEB-INF
          try {
            persistLocation();
          } catch (IOException e) {
            LOG.error("Cant persist datadir location in WEBINF webapp folder", e);
          }
          return false;
        } else {
          this.dataDir = null;
          throw new InvalidConfigException(TYPE.INVALID_DATA_DIR,
            "DataDir " + dataDir.getAbsolutePath() + " is not a directory");
        }

      } else {
        // NEW datadir
        LOG.info("Setting up new data directory {}", dataDir);
        try {
          // create new main data dir. Populate later
          FileUtils.forceMkdir(dataDir);
          // test if we can write to the directory
          File testFile = new File(dataDir, "test.tmp");
          FileUtils.touch(testFile);
          // remove test file
          testFile.delete();
          // create new default data dir
          createDefaultDir();
          if (dataDirSettingFile != null) {
            // all works fine - persist location in WEB-INF if that is how it is recorded
            persistLocation();
          }
          return true;
        } catch (IOException e) {
          LOG.error("New DataDir " + dataDir.getAbsolutePath() + " not writable", e);
          this.dataDir = null;
          throw new InvalidConfigException(InvalidConfigException.TYPE.NON_WRITABLE_DATA_DIR,
            "DataDir " + dataDir.getAbsolutePath() + " is not writable");
        }
      }
    }
  }

  public File sourceFile(Resource resource, FileSource source) {
    if (resource == null) {
      return null;
    }
    return resourceFile(resource.getShortname(), "sources/" + source.getName() + source.getPreferredFileSuffix());
  }

  public File sourceLogFile(String resourceName, String sourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/sources/" + sourceName + ".log");
  }

  /**
   * Return a temporary directory with randomly-generated number added to name to uniquely identifier it.
   *
   * @return temporary directory
   */
  public File tmpDir() {
    String random = String.valueOf(RANDOM.nextLong());
    File dir = tmpFile("dir" + random);
    assureDirExists(dir);
    return dir;
  }

  /**
   * Construct the absolute path for a given relative path within the temporary folder. This method doesn't generate a
   * unique filename - it only uses the path provided.
   *
   * @param path relative file path within temporary folder
   *
   * @return temporary file within relative file path
   */
  public File tmpFile(String path) {
    return dataFile(TMP_DIR + "/" + path);
  }

  /**
   * Generates an unused temporary filename, using an auto-generated number in the name. The File handle gets returned.
   *
   * @param prefix file name prefix, e.g. "dwca"
   * @param suffix file name suffix, e.g. ".zip"
   *
   * @return temporary File handle with unique filename
   */
  public File tmpFile(String prefix, String suffix) {
    String random = String.valueOf(RANDOM.nextInt());
    return tmpFile(prefix + random + suffix);
  }

  /**
   * Datadir disk space
   */

  public long getDataDirTotalSpace() {
    return dataDir.getTotalSpace();
  }

  public long getDataDirUsableSpace() {
    return dataDir.getUsableSpace();
  }

  /**
   * Get directory read/write status
   */
  public DirStatus getDirectoryReadWriteStatus(File dir) {
    // No folder
    if (dir == null || !dir.exists()) {
      return DirStatus.NOT_EXIST;
    }
    // No access
    else if (!dir.canRead()) {
      return DirStatus.NO_ACCESS;
    }
    // Read only
    else if (dir.canRead() && !dir.canWrite()) {
      return DirStatus.READ_ONLY;
    }
    // Read / Write
    else {
      return DirStatus.READ_WRITE;
    }
  }

  /**
   * Get sub directories read/write status
   * If one sub directory has not Read/Write status, its status is returned
   */
  public DirStatus getSubDirectoriesReadWriteStatus(File dir) {
    File[] files = dir.listFiles();
    if (files != null) {
      for (File subDir : files) {
        DirStatus status = getDirectoryReadWriteStatus(subDir);
        if (status != DirStatus.READ_WRITE) {
          return status;
        }
      }
      return DirStatus.READ_WRITE;
    }
    else {
      return DirStatus.NOT_EXIST;
    }
  }

  public File getDataDir() {
    return dataDir;
  }
}
