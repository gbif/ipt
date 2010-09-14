package org.gbif.ipt.config;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.RegistrationManagerImpl;
import org.gbif.ipt.service.admin.impl.UserAccountManagerImpl;
import org.gbif.ipt.utils.InputStreamUtils;

import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A very simple utility class to encapsulate the basic layout of the data directory and to configure & persist the path
 * for that directory and make it available to the entire application.
 * 
 * @author markus
 * 
 */
@Singleton
public class DataDir {
  public static final String LOGGING_DIR = "logs";
  public static final String CONFIG_DIR = "config";
  public static final String RESOURCES_DIR = "resources";
  public static final String LUCENE_DIR = "lucene";
  public static final String TMP_DIR = "tmp";
  private static final String JETTY_DIR = "jetty";
  private int tmpCounter = 0;
  private Map<String, Integer> tmpPrefixCounter = new HashMap<String, Integer>();

  private static Logger log = Logger.getLogger(DataDir.class);

  protected File dataDir;
  private File dataDirSettingFile;
  private InputStreamUtils streamUtils = new InputStreamUtils();

  private DataDir() {
  }

  public static DataDir buildFromDataDirFile(File dataDir) {
    if (dataDir != null) {
      DataDir dd = new DataDir();
      // a datadir has been configured already. Lets see where that is
      log.info("IPT Data Directory configured at " + dataDir.getAbsolutePath());
      dd.dataDir = dataDir;
      dd.dataDirSettingFile = null;
      return dd;
    }
    return null;
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
            "Failed to read the datadir location settings file in WEB-INF at " + dataDirSettingFile.getAbsolutePath(),
            e);
      }
    } else {
      log.warn("Datadir location settings file in WEB-INF not found. Continue without data directory.");
    }
    return dd;
  }

  public static DataDir buildMock() {
    return new DataDir();
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
   * Constructs an absolute path to a file within the config folder of the data dir
   * 
   * @param path the relative path within the config folder
   * @return
   */
  public File configFile(String path) {
    return dataFile(CONFIG_DIR + "/" + path);
  }

  /**
   * Basic method to convert a relative path within the data dir to an absolute path on the filesystem
   * 
   * @param path the relative path within the data dir
   * @return
   */
  public File dataFile(String path) {
    if (dataDir == null) {
      throw new IllegalStateException("No data dir has been configured yet");
    }
    // if (path.startsWith("/")){
    // return new File(path);
    // }
    File f = new File(dataDir, path);
    assureParentExists(f);
    return f;
  }

  /**
   * @return true if a working data directory is configured
   */
  public boolean isConfigured() {
    if (dataDir != null && dataDir.exists()) {
      return true;
    }
    return false;
  }

  /**
   * Constructs an absolute path to a file within the logs folder of the data dir
   * 
   * @param path the relative path within the logs folder
   * @return
   */
  public File loggingFile(String path) {
    return dataFile(LOGGING_DIR + "/" + path);
  }

  /**
   * Constructs an absolute path to the main lucene folder in the data dir
   * 
   * @return
   */
  public File luceneDir() {
    return dataFile("lucene");
  }

  public File resourceDwcaFile(String resourceName) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/dwca.zip");
  }

  public File resourceEmlFile(String resourceName, @Nullable Integer version) {
    String fn;
    if (version == null) {
      fn = "eml.xml";
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
   * @return
   */
  public File resourceFile(String resourceName, String path) {
    return dataFile(RESOURCES_DIR + "/" + resourceName + "/" + path);
  }

  /**
   * Sets the path to the data directory for the entire application and persists it in the /WEB-INF folder. This method
   * does not reload any configuration though - so normally setting the dataDir should be done through the ConfigManager
   * which calls this method but also reloads all user configurations.
   * 
   * @See ConfigManager
   * @param dataDir
   * @return true if a new data dir was created, false when an existing was read
   * @throws InvalidConfigException
   */
  public boolean setDataDir(File dataDir) throws InvalidConfigException {
    if (dataDir == null) {
      throw new NullPointerException("DataDir file required");
    } else {
      boolean created = false;
      if (dataDir.exists() && !dataDir.isDirectory()) {
        throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_DATA_DIR, "DataDir "
            + dataDir.getAbsolutePath() + " is not a directory");
      } else {
        try {
          if (!dataDir.exists()) {
            // create new main data dir. Populate later
            FileUtils.forceMkdir(dataDir);
          }
          // test if we can write to the directory
          File testFile = new File(dataDir, "test.tmp");
          FileUtils.touch(testFile);
          this.dataDir = dataDir;
          // remove test file and persist location in WEB-INF
          testFile.delete();
          FileUtils.writeStringToFile(dataDirSettingFile, dataDir.getAbsolutePath());
          log.info("IPT DataDir location file in /WEB-INF changed to " + dataDir.getAbsolutePath());
          // check if this directory contains a config folder - if not copy empty default dir from classpath
          File configDir = configFile("");
          if (configDir.exists() && configDir.isDirectory()) {
            log.info("Reusing existing data dir.");
          } else {
            log.info("Creating new default data dir.");
            created = true;
            FileUtils.cleanDirectory(dataDir);
            // create config, resources and lucene directories
            File resourcesDir = dataFile(RESOURCES_DIR);
            File luceneDir = dataFile(LUCENE_DIR);
            File loggingDir = dataFile(LOGGING_DIR);
            File jettyDir = tmpFile(JETTY_DIR);
            FileUtils.forceMkdir(configDir);
            FileUtils.forceMkdir(resourcesDir);
            FileUtils.forceMkdir(luceneDir);
            FileUtils.forceMkdir(loggingDir);
            FileUtils.forceMkdir(jettyDir);
            // copy default config files
            org.gbif.ipt.utils.FileUtils.copyStreamToFile(streamUtils.classpathStream("/configDefault/ipt.properties"),
                configFile(AppConfig.DATADIR_PROPFILE));
            org.gbif.ipt.utils.FileUtils.copyStreamToFile(streamUtils.classpathStream("/configDefault/about.ftl"),
                configFile("about.ftl"));
            org.gbif.ipt.utils.FileUtils.copyStreamToFile(streamUtils.classpathStream("/configDefault/users.xml"),
                configFile(UserAccountManagerImpl.PERSISTENCE_FILE));
            org.gbif.ipt.utils.FileUtils.copyStreamToFile(
                streamUtils.classpathStream("/configDefault/registration.xml"),
                configFile(RegistrationManagerImpl.PERSISTENCE_FILE));
          }
        } catch (IOException e) {
          log.error("New DataDir " + dataDir.getAbsolutePath() + " not writable", e);
          throw new InvalidConfigException(InvalidConfigException.TYPE.NON_WRITABLE_DATA_DIR, "DataDir "
              + dataDir.getAbsolutePath() + " is not writable");
        }
      }
      return created;
    }
  }

  public File sourceFile(Resource resource, Source source) {
    if (resource == null) {
      return null;
    }
    return resourceFile(resource.getShortname(), "sources/" + source.getName() + ".txt");
  }

  public File tmpDir() {
    tmpCounter++;
    File dir = tmpFile("dir-" + tmpCounter);
    assureDirExists(dir);
    return dir;
  }

  public File tmpDir(String name) {
    tmpCounter++;
    File dir = tmpFile("dir-" + tmpCounter + "/" + name);
    assureDirExists(dir);
    return dir;
  }

  /**
   * Generate a new unique temporary filename inside the datadir based on an autoincremented counter.
   * 
   * @return
   */
  public File tmpFile() {
    tmpCounter++;
    return tmpFile("file-" + tmpCounter + ".tmp");
  }

  /**
   * Construct the absolute path for a given relative path within the /tmp subfolder.
   * This method doesnt generate a unique filename - it only assembles what was given.
   * Use one of the other tmpFile(...) methods to generate a unique file handler.
   * 
   * @return
   */
  public File tmpFile(String path) {
    return dataFile(TMP_DIR + "/" + path);
  }

  /**
   * Generate an unused temporary filename based on a filename prefix and suffix.
   * In between an autogenerated integer will be placed to assure a unique name
   * 
   * @param prefix
   * @param suffix
   * @return
   */
  public File tmpFile(String prefix, String suffix) {
    String counterKey = prefix + "||" + suffix;
    if (!tmpPrefixCounter.containsKey(prefix)) {
      tmpPrefixCounter.put(counterKey, 1);
    } else {
      tmpPrefixCounter.put(counterKey, tmpPrefixCounter.get(counterKey) + 1);
    }

    return tmpFile(prefix + tmpPrefixCounter.get(counterKey) + suffix);
  }
}
