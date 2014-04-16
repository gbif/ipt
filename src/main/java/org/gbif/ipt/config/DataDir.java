package org.gbif.ipt.config;

import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.utils.InputStreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javax.annotation.Nullable;

import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

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
  public static final String EML_XML_FILENAME = "eml.xml";
  public static final String DWCA_FILENAME = "dwca.zip";
  public static final String PUBLICATION_LOG_FILENAME = "publication.log";
  private static final Random RANDOM = new Random();

  private static Logger log = Logger.getLogger(DataDir.class);

  protected File dataDir;
  private File dataDirSettingFile;
  private InputStreamUtils streamUtils = new InputStreamUtils();

  private DataDir() {
  }

  public static DataDir buildFromLocationFile(File dataDirSettingFile) {
    DataDir dd = new DataDir();
    dd.dataDirSettingFile = dataDirSettingFile;
    if (dataDirSettingFile != null && dataDirSettingFile.exists()) {
      // a datadir has been configured already. Lets see where that is
      String dataDirPath = null;
      try {
        dataDirPath = StringUtils.trimToNull(FileUtils.readFileToString(dataDirSettingFile));
        if (dataDirPath != null) {
          log.info("IPT Data Directory configured at " + dataDirPath);
          dd.dataDir = new File(dataDirPath);
        }
      } catch (IOException e) {
        log.error(
          "Failed to read the datadir location settings file in WEB-INF at " + dataDirSettingFile.getAbsolutePath(), e);
      }
    } else {
      log.warn("Datadir location settings file in WEB-INF not found. Continue without data directory.");
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
    log.debug("Cleared temporary folder");
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

    input = streamUtils.classpathStream("configDefault/about.ftl");
    if (input == null) {
      throw new InvalidConfigException(TYPE.CONFIG_WRITE,
        "Cannot read required classpath resources to create new data dir!");
    }
    org.gbif.ipt.utils.FileUtils.copyStreamToFile(input, configFile("about.ftl"));

    log.info("Creating new default data dir");
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
    return dataDir != null && dataDir.exists();
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
    FileUtils.writeStringToFile(dataDirSettingFile, dataDir.getAbsolutePath());
    log.info("IPT DataDir location file in /WEB-INF changed to " + dataDir.getAbsolutePath());
  }

  /**
   * Retrieves DwC-A file for the latest published version of the resource.
   *
   * @param resourceName resource short name
   *
   * @return latest published version of DwC-A file
   */
  public File resourceDwcaFile(String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + DWCA_FILENAME);
  }

  /**
   * Retrieves published DwC-A file for a resource.
   * Specific versions can also be requested depending on the parameter "version". If no specific version is
   * requested, the latest published version (dwca.zip) is used.
   *
   * @param resourceName resource short name
   * @param version      version
   *
   * @return DwC-A file having specific version, defaulting to latest published version if no version specified
   */
  public File resourceDwcaFile(String resourceName, @Nullable Integer version) {
    String fn;
    if (version == null) {
      fn = DWCA_FILENAME;
    } else {
      fn = "dwca-" + version + ".zip";
    }
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + fn);
  }

  /**
   * Retrieves file that stores information about how many records were published for a version of a resource.
   * If no specific version is requested, null is returned.
   *
   * @param resourceName resource short name
   * @param version      version
   *
   * @return file for version, or null if no version number was specified
   */
  public File resourceCountFile(String resourceName, @Nullable Integer version) {
    String fn;
    if (version == null) {
      return null;
    } else {
      fn = ".recordspublished-" + version;
    }
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + fn);
  }

  /**
   * Retrieves file for the latest published version of the EML file representing the resource metadata in XML format.
   * Specific versions can also be resolved depending on the parameter "version". If no specific version is
   * requested, or if the version requested is 0, the interim eml.xml without a version number is used.
   *
   * @param resourceName resource short name
   * @param version      version
   *
   * @return EML file having specific version, defaulting to interim eml.xml without a version number if none specified
   */
  public File resourceEmlFile(String resourceName, @Nullable Integer version) {
    String fn;
    if (version == null) {
      fn = EML_XML_FILENAME;
    } else {
      fn = "eml-" + version + ".xml";
    }
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + fn);
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
   * @param suffix the logo file suffix, indicating the format. E.g. jpeg or gif
   */
  public File resourceLogoFile(String resourceName, String suffix) {
    if (suffix == null) {
      suffix = "jpeg";
    }
    suffix = suffix.toLowerCase();
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/logo." + suffix);
  }

  public File resourcePublicationLogFile(String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + PUBLICATION_LOG_FILENAME);
  }

  /**
   * Retrieves file for the latest published version of the RFT file representing the EML metadata in RTF format.
   *
   * @param resourceName resource short name
   *
   * @return latest published version of RTF file
   */
  public File resourceRtfFile(String resourceName) {
    String fn = resourceName + ".rtf";
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + fn);
  }

  /**
   * Retrieves file for the latest published version of the RFT file representing the EML metadata in RTF format.
   * Specific versions can also be resolved depending on the parameter "version". If no specific version is
   * requested, the latest published version is used.
   *
   * @param resourceName resource short name
   * @param version      version
   *
   * @return RTF file having specific version, defaulting to latest published version if no version specified
   */
  public File resourceRtfFile(String resourceName, @Nullable Integer version) {
    String fn;
    if (version == null) {
      fn = resourceName + ".rtf";
    } else {
      fn = resourceName + "-" + version + ".rtf";
    }
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
          log.info("Reusing existing data dir.");
          // persist location in WEB-INF
          try {
            persistLocation();
          } catch (IOException e) {
            log.error("Cant persist datadir location in WEBINF webapp folder", e);
          }
          return false;
        } else {
          this.dataDir = null;
          throw new InvalidConfigException(TYPE.INVALID_DATA_DIR,
            "DataDir " + dataDir.getAbsolutePath() + " is not a directory");
        }

      } else {
        // NEW datadir
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
          // all works fine - persist location in WEB-INF
          persistLocation();
          return true;
        } catch (IOException e) {
          log.error("New DataDir " + dataDir.getAbsolutePath() + " not writable", e);
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
}
