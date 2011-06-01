package org.gbif.ipt.config;

import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.utils.InputStreamUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

@Singleton
public class AppConfig {
  protected enum REGISTRY_TYPE {
    PRODUCTION, DEVELOPMENT
  }

  protected static final String DATADIR_PROPFILE = "ipt.properties";
  private static final String CLASSPATH_PROPFILE = "application.properties";
  public static final String BASEURL = "ipt.baseURL";
  public static final String PROXY = "proxy";
  public static final String DEBUG = "debug";
  public static final String ANALYTICS_GBIF = "analytics.gbif";
  public static final String ANALYTICS_KEY = "analytics.key";
  public static final String IPT_LATITUDE = "location.lat";
  public static final String IPT_LONGITUDE = "location.lon";
  private static final String PRODUCTION_TYPE_LOCKFILE = ".gbifreg";;
  private Properties properties = new Properties();
  private Logger log = Logger.getLogger(this.getClass());
  private DataDir dataDir;
  private REGISTRY_TYPE type;

  private AppConfig() {
  }

  @Inject
  public AppConfig(DataDir dataDir) throws InvalidConfigException {
    this.dataDir = dataDir;
    // also loaded via ConfigManager constructor if datadir was linked at startup already
    // If it wasnt, this is the only place to load at least the default classpath config settings
    loadConfig();
  }

  public static AppConfig buildMock() {
    AppConfig mock = new AppConfig();
    mock.dataDir = DataDir.buildMock();
    return mock;
  }

  public boolean debug() {
    return "true".equalsIgnoreCase(properties.getProperty(DEBUG));
  }

  public boolean devMode() {
    return !("false".equalsIgnoreCase(properties.getProperty("dev.devmode")));
  }

  public String getAnalyticsKey() {
    return properties.getProperty(ANALYTICS_KEY);
  }

  public String getBaseURL() {
    String base = properties.getProperty(BASEURL);
    while (base != null && base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base;
  }

  public DataDir getDataDir() {
    return dataDir;
  }

  public Double getLatitude() {
    try {
      String val = properties.getProperty(IPT_LATITUDE);
      if (val != null) {
        return Double.valueOf(val);
      }
    } catch (NumberFormatException e) {
    }
    return null;
  }

  public Double getLongitude() {
    try {
      String val = properties.getProperty(IPT_LONGITUDE);
      if (val != null) {
        return Double.valueOf(val);
      }
    } catch (NumberFormatException e) {
    }
    return null;
  }

  public int getMaxThreads() {
    try {
      return Integer.parseInt(getProperty("dev.maxthreads"));
    } catch (NumberFormatException e) {
      return 3;
    }
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  public String getProxy() {
    return properties.getProperty(PROXY);
  }

  public REGISTRY_TYPE getRegistryType() {
    return type;
  }

  private File getRegistryTypeLockFile() {
    return dataDir.configFile(PRODUCTION_TYPE_LOCKFILE);
  }

  public String getRegistryUrl() {
    if (REGISTRY_TYPE.PRODUCTION == type) {
      return getProperty("dev.registry.url");
    }
    return getProperty("dev.registrydev.url");
  }

  public String getResourceArchiveUrl(String shortname) {
    return getBaseURL() + "/archive.do?" + Constants.REQ_PARAM_RESOURCE + "=" + shortname;
  }

  public String getResourceEmlUrl(String shortname) {
    return getBaseURL() + "/eml.do?" + Constants.REQ_PARAM_RESOURCE + "=" + shortname;
  }

  public String getResourceUrl(String shortname) {
    return getBaseURL() + "/resource.do?" + Constants.REQ_PARAM_RESOURCE + "=" + shortname;
  }
  
  public String getResourceLogoUrl(String shortname) {
	    return getBaseURL() + "/logo.do?" + Constants.REQ_PARAM_RESOURCE + "=" + shortname;
  }

  public String getVersion() {
    return properties.getProperty("dev.version");
  }

  public boolean hasLocation() {
    if (getLongitude() != null && getLatitude() != null) {
      return true;
    }
    return false;
  }

  public boolean isGbifAnalytics() {
    return "true".equalsIgnoreCase(properties.getProperty(ANALYTICS_GBIF));
  }

  /**
   * Deprecated in favor over @See getRegistryType()
   * 
   * @return true if the datadir is linked to the production registry
   */
  @Deprecated
  public boolean isTestInstallation() {
    return REGISTRY_TYPE.DEVELOPMENT == type;
  }

  protected void loadConfig() throws InvalidConfigException {
    InputStreamUtils streamUtils = new InputStreamUtils();
    InputStream configStream = streamUtils.classpathStream(CLASSPATH_PROPFILE);
    try {
      Properties props = new Properties();
      if (configStream != null) {
        props.load(configStream);
        log.debug("Loaded default configuration from application.properties in classpath");
      } else {
        log.error("Could not load default configuration from application.properties in classpath");
      }
      if (dataDir.dataDir != null && dataDir.dataDir.exists()) {
        // read user configuration from data dir if it exists
        File userCfgFile = new File(dataDir.dataDir, "config/" + DATADIR_PROPFILE);
        if (userCfgFile.exists()) {
          try {
            props.load(new FileInputStream(userCfgFile));
            log.debug("Loaded user configuration from " + userCfgFile.getAbsolutePath());
          } catch (IOException e) {
            log.warn("DataDir configured, but failed to load user configuration from " + userCfgFile.getAbsolutePath(),
                e);
          }
        } else {
          log.warn("DataDir configured, but user configuration doesnt exist: " + userCfgFile.getAbsolutePath());
        }
        // check if this datadir is a production or test installation
        // we use a hidden file to indicate the production type
        readRegistryLock();
      }
      // without error replace existing config with new one
      this.properties = props;
    } catch (IOException e) {
      log.error("Failed to load the default application configuration from application.properties", e);
    }
  }

  /**
   * 
   */
  private void readRegistryLock() throws InvalidConfigException {
    // set lock file if not yet existing
    File lockFile = getRegistryTypeLockFile();
    if (!lockFile.exists()) {
      log.warn("DataDir is not locked to a registry yet !!!");
    } else {
      String regTypeAsString;
      try {
        regTypeAsString = StringUtils.trimToNull(FileUtils.readFileToString(lockFile));
        this.type = REGISTRY_TYPE.valueOf(regTypeAsString);
      } catch (IOException e) {
        log.error("Cannot read datadir registry lock", e);
        throw new InvalidConfigException(TYPE.INVALID_DATA_DIR, "Cannot read datadir registry lock");
      }
    }
  }

  /**
   * @throws IOException
   */
  protected void saveConfig() throws IOException {
    // save property config file
    File userCfgFile = new File(dataDir.dataDir, "config/" + DATADIR_PROPFILE);
    if (userCfgFile.exists()) {
    }
    OutputStream out = new FileOutputStream(userCfgFile);

    Properties props = (Properties) properties.clone();
    Enumeration<?> e = props.propertyNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      if (key.startsWith("dev.")) {
        props.remove(key);
      }
    }
    props.store(out, "IPT configuration, last saved " + new Date().toString());
    out.close();
  }

  public void setProperty(String key, String value) {
    properties.setProperty(key, StringUtils.trimToEmpty(value));
  }

  protected void setRegistryType(REGISTRY_TYPE type) throws InvalidConfigException {
    if (type == null) {
      throw new NullPointerException("Registry type cannot be null");
    }
    if (this.type != null) {
      if (this.type != type) {
        throw new InvalidConfigException(TYPE.DATADIR_ALREADY_REGISTERED, "The datadir is already designated as "
            + this.type);
      } else {
        // already contains the same information. Dont do anything
        return;
      }
    }
    // set lock file if not yet existing
    File lockFile = getRegistryTypeLockFile();
    try {
      Writer lock = new FileWriter(lockFile, false);
      lock.write(type.name());
      lock.close();
      this.type = type;
      log.info("Locked DataDir to registry of type " + type);
    } catch (IOException e) {
      log.error("Cannot lock the datadir to registry type " + type, e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cannot lock the datadir to registry type " + type);
    }
  }
}