package org.gbif.ipt.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.gson.annotations.Since;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.utils.InputStreamUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

@Singleton
public class AppConfig {

  public enum REGISTRY_TYPE {
    PRODUCTION, DEVELOPMENT
  }

  protected static final String DATADIR_PROPFILE = "ipt.properties";
  static final String CLASSPATH_PROPFILE = "application.properties";
  public static final String BASEURL = "ipt.baseURL";
  @Since(2.1)
  public static final String CORE_ROW_TYPES = "ipt.core_rowTypes";
  public static final String CORE_ROW_ID_TERMS = "ipt.core_idTerms";
  public static final String PROXY = "proxy";
  public static final String DEBUG = "debug";
  public static final String ARCHIVAL_MODE = "archivalMode";
  public static final String ARCHIVAL_LIMIT = "archivalLimit";
  public static final String ANALYTICS_GBIF = "analytics.gbif";
  public static final String ANALYTICS_KEY = "analytics.key";
  public static final String IPT_LATITUDE = "location.lat";
  public static final String IPT_LONGITUDE = "location.lon";
  public static final String DEV_VERSION = "dev.version";
  private static final String PRODUCTION_TYPE_LOCKFILE = ".gbifreg";
  private Properties properties = new Properties();
  private static final Logger LOG = LogManager.getLogger(AppConfig.class);
  private DataDir dataDir;
  private REGISTRY_TYPE type;

  // to support compatibility with historical data directories, we default to the original hard coded
  // types that were scattered across the code.
  private static final List<String> DEFAULT_CORE_ROW_TYPES =
    ImmutableList.of(Constants.DWC_ROWTYPE_OCCURRENCE, Constants.DWC_ROWTYPE_TAXON, Constants.DWC_ROWTYPE_EVENT);

  // mapping of the id to the term that is the row ID
  private static final Map<String, String> DEFAULT_CORE_ROW_TYPES_ID_TERMS = Maps.newHashMap((ImmutableMap
    .of(Constants.DWC_ROWTYPE_OCCURRENCE, Constants.DWC_OCCURRENCE_ID,
      Constants.DWC_ROWTYPE_TAXON, Constants.DWC_TAXON_ID,
      Constants.DWC_ROWTYPE_EVENT, Constants.DWC_EVENT_ID)));

  private static List<String> coreRowTypes = DEFAULT_CORE_ROW_TYPES;
  private static Map<String, String> coreRowTypeIdTerms = DEFAULT_CORE_ROW_TYPES_ID_TERMS;

  private AppConfig() {
  }

  @Inject
  public AppConfig(DataDir dataDir) throws InvalidConfigException {
    this.dataDir = dataDir;
    // also loaded via ConfigManager constructor if datadir was linked at startup already
    // If it wasn't, this is the only place to load at least the default classpath config settings
    loadConfig();

  }

  /**
   * Returns the term to use as the ID for core.
   *
   * @return The expected field for the given core.
   */
  public static String coreIdTerm(String rowType) {
    if (coreRowTypeIdTerms.containsKey(rowType)) {
      return coreRowTypeIdTerms.get(rowType);
    } else {
      throw new IllegalArgumentException("IPT is not configured correctly to support rowType[" + rowType
                                         + "].  Hint: are you missing mappings for the row type and id term in the properties?");
    }
  }

  /**
   * Returns the core types that the application is configured to support.
   * Exposed with static accessor to allow model objects to access this without the need for dependency
   * injection. This is set during
   */
  public static List<String> getCoreRowTypes() {
    return coreRowTypes;
  }

  /**
   * @return true if the row type is suitable for use as a core.
   */
  public static boolean isCore(String rowType) {
    return coreRowTypes.contains(rowType);
  }

  public boolean debug() {
    return "true".equalsIgnoreCase(properties.getProperty(DEBUG));
  }

  public boolean devMode() {
    return !"false".equalsIgnoreCase(properties.getProperty("dev.devmode"));
  }

  public String getAnalyticsKey() {
    return properties.getProperty(ANALYTICS_KEY);
  }

  public String getBaseUrl() {
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
      if (!Strings.isNullOrEmpty(val)) {
        return Double.valueOf(val);
      }
    } catch (NumberFormatException e) {
      LOG.warn("IPT latitude was invalid: " + e.getMessage());
    }
    return null;
  }

  public Double getLongitude() {
    try {
      String val = properties.getProperty(IPT_LONGITUDE);
      if (!Strings.isNullOrEmpty(val)) {
        return Double.valueOf(val);
      }
    } catch (NumberFormatException e) {
      LOG.warn("IPT longitude was invalid: " +e.getMessage());
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

  /**
   * @return the GBIF Data Portal base URL, different depending on the IPT's mode
   */
  public String getPortalUrl() {
    if (REGISTRY_TYPE.PRODUCTION == type) {
      return getProperty("dev.portal.url");
    }
    return getProperty("dev.portaldev.url");
  }

  /**
   * @return DataCite base URL, depends on the IPT's mode
   */
  public String getDataCiteUrl() {
    if (REGISTRY_TYPE.PRODUCTION == type) {
      return getProperty("dev.datacite.url");
    }
    return getProperty("dev.datacitedev.url");
  }

  /**
   * @return String URI to resource's last published darwin core archive (no version number)
   */
  @NotNull
  public String getResourceArchiveUrl(@NotNull String shortname) {
    Preconditions.checkNotNull(getBaseUrl());

    return UriBuilder.fromPath(getBaseUrl()).path(Constants.REQ_PATH_DWCA)
      .queryParam(Constants.REQ_PARAM_RESOURCE, shortname).build().toString();
  }

  /**
   * @return String URI to resource's last published EML file (no version number)
   */
  @NotNull
  public String getResourceEmlUrl(@NotNull String shortname) {
    Preconditions.checkNotNull(getBaseUrl());

    return UriBuilder.fromPath(getBaseUrl()).path(Constants.REQ_PATH_EML)
      .queryParam(Constants.REQ_PARAM_RESOURCE, shortname).build().toString();
  }

  /**
   * @return String URI to resource's logo (no version number)
   */
  @NotNull
  public String getResourceLogoUrl(@NotNull String shortname) {
    Preconditions.checkNotNull(getBaseUrl());

    return UriBuilder.fromPath(getBaseUrl()).path(Constants.REQ_PATH_LOGO)
      .queryParam(Constants.REQ_PARAM_RESOURCE, shortname).build().toString();
  }

  /**
   * @return String URI to resource default homepage (no version number)
   */
  @NotNull
  public String getResourceUrl(@NotNull String shortname) {
    return getResourceUri(shortname).toString();
  }

  /**
   * @return URI to resource default homepage (no version number) used in DOI registration
   */
  @NotNull
  public URI getResourceUri(@NotNull String shortname) {
    Preconditions.checkNotNull(getBaseUrl());

    return UriBuilder.fromPath(getBaseUrl()).path(Constants.REQ_PATH_RESOURCE)
      .queryParam(Constants.REQ_PARAM_RESOURCE, shortname).build();
  }

  /**
   * @return String URI used as resource EML GUID, similar to resource homepage URI but with id param versus r param
   */
  @NotNull
  public String getResourceGuid(@NotNull String shortname) {
    Preconditions.checkNotNull(getBaseUrl());

    return UriBuilder.fromPath(getBaseUrl()).path(Constants.REQ_PATH_RESOURCE)
      .queryParam(Constants.REQ_PARAM_ID, shortname).build().toString();
  }

  /**
   * @return URI to resource homepage for a specific version of resource used in DOI registration
   */
  @NotNull
  public URI getResourceVersionUri(@NotNull String shortname, @NotNull BigDecimal version) {
    Preconditions.checkNotNull(getBaseUrl());

    return UriBuilder.fromPath(getBaseUrl()).path(Constants.REQ_PATH_RESOURCE)
      .queryParam(Constants.REQ_PARAM_RESOURCE, shortname)
      .queryParam(Constants.REQ_PARAM_VERSION, version.toPlainString()).build();
  }

  /**
   * Called from citations metadata page.
   *
   * @return URI to resource homepage for a specific version of resource used in DOI registration
   */
  @NotNull
  public String getResourceVersionUri(@NotNull String shortname, @NotNull String version) {
    Preconditions.checkNotNull(getBaseUrl());

    return getResourceVersionUri(shortname, new BigDecimal(version)).toString();
  }

  public String getVersion() {
    return properties.getProperty(DEV_VERSION);
  }

  public boolean hasLocation() {
    if (getLongitude() != null && getLatitude() != null) {
      return true;
    }
    return false;
  }

  /**
   * Checks whether the IPT has been configured to use archival mode.
   *
   * @return whether the IPT is used in archival mode
   */
  public boolean isArchivalMode() {
    return "true".equalsIgnoreCase(properties.getProperty(ARCHIVAL_MODE));
  }

  /**
   * Return the number of archive versions to keep for each resource
   */
  public Integer getArchivalLimit() {
    try {
      String val = properties.getProperty(ARCHIVAL_LIMIT);
      if (!Strings.isNullOrEmpty(val)) {
        return Integer.valueOf(val);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Archival limit was invalid: " +e.getMessage());
    }
    return null;
  }

  public boolean isGbifAnalytics() {
    return "true".equalsIgnoreCase(properties.getProperty(ANALYTICS_GBIF));
  }

  /**
   * @return true if the datadir is linked to the test registry, false otherwise
   *
   */
  public boolean isTestInstallation() {
    return REGISTRY_TYPE.DEVELOPMENT == type;
  }

  /**
   * Load application configuration from application properties file (application.properties) and from
   * user configuration file (ipt.properties), which includes populating core configuration.
   */
  protected void loadConfig() throws InvalidConfigException {
    InputStreamUtils streamUtils = new InputStreamUtils();
    InputStream configStream = streamUtils.classpathStream(CLASSPATH_PROPFILE);
    // load default configuration from application.properties
    try {
      Properties props = new Properties();
      if (configStream == null) {
        LOG.error("Could not load default configuration from application.properties in classpath");
      } else {
        props.load(configStream);
        LOG.debug("Loaded default configuration from application.properties in classpath");
      }
      if (dataDir.dataDir != null && dataDir.dataDir.exists()) {
        // load user configuration properties from data dir ipt.properties (if it exists)
        File userCfgFile = new File(dataDir.dataDir, "config/" + DATADIR_PROPFILE);
        if (userCfgFile.exists()) {
          FileInputStream fis = null;
          try {
            fis = new FileInputStream(userCfgFile);
            props.load(fis);
            LOG.debug("Loaded user configuration from " + userCfgFile.getAbsolutePath());
          } catch (IOException e) {
            LOG.warn("DataDir configured, but failed to load ipt.properties from " + userCfgFile.getAbsolutePath(), e);
          } finally {
            if (fis != null) {
              try {
                fis.close();
              } catch (IOException e) {
                LOG.debug("Failed to close input stream on ipt.properties file");
              }
            }
          }
        } else {
          LOG.warn("DataDir configured, but user configuration doesnt exist: " + userCfgFile.getAbsolutePath());
        }
        // check if this datadir is a production or test installation
        // we use a hidden file to indicate the production type
        readRegistryLock();
      }
      // without error replace existing config with new one
      this.properties = props;

      // populates the cores supported
      populateCoreConfiguration();

    } catch (IOException e) {
      LOG.error("Failed to load the default application configuration from application.properties", e);
    }
  }

  /**
   * Reads the configuration and populates the cores supported and mapping between core and the ID term to use
   * for the core.
   */
  private void populateCoreConfiguration() {
    String cores = properties.getProperty(CORE_ROW_TYPES);
    String ids = properties.getProperty(CORE_ROW_ID_TERMS);
    if (cores != null && ids != null) {
      LOG.info("Using custom core mapping");
      List<String> configCores = Lists.newArrayList(Splitter.on('|').trimResults().omitEmptyStrings().split(cores));
      List<String> configIDs = Lists.newArrayList(Splitter.on('|').trimResults().omitEmptyStrings().split(ids));

      if (configCores.size() == configIDs.size()) {
        coreRowTypes = Lists.newArrayList(DEFAULT_CORE_ROW_TYPES);
        coreRowTypes.addAll(configCores);

        coreRowTypeIdTerms = Maps.newHashMap(DEFAULT_CORE_ROW_TYPES_ID_TERMS);
        for (int i = 0; i < configCores.size(); i++) {
          coreRowTypeIdTerms.put(configCores.get(i), configIDs.get(i));
        }
        LOG.info("IPT configured to support cores and id terms: " + coreRowTypeIdTerms);
        return;

      } else {
        LOG.error("Invalid configuration of [" + CORE_ROW_TYPES + "," + CORE_ROW_ID_TERMS
                  + "].  Should have same number of elements - using defaults");
      }
    }

    coreRowTypes = DEFAULT_CORE_ROW_TYPES;
    coreRowTypeIdTerms = DEFAULT_CORE_ROW_TYPES_ID_TERMS;
  }

  /**
   * Reads registry lock file and determines what registry the DataDir is locked to.
   */
  private void readRegistryLock() throws InvalidConfigException {
    File lockFile = getRegistryTypeLockFile();
    if (lockFile.exists()) {
      try {
        LOG.info("Reading registry lock file to determine if the DataDir is locked to a registry yet.");
        String regTypeAsString = StringUtils.trimToEmpty(FileUtils.readFileToString(lockFile, "UTF-8"));
        this.type = REGISTRY_TYPE.valueOf(regTypeAsString);
        LOG.info("DataDir is locked to registry type: " + type.toString());
      } catch (IllegalArgumentException e) {
        LOG.error("Cannot interpret registry lock file contents!", e);
        throw new InvalidConfigException(TYPE.INVALID_DATA_DIR, "Cannot interpret registry lock file contents!");
      } catch (IOException e) {
        LOG.error("Cannot read registry lock file!", e);
        throw new InvalidConfigException(TYPE.INVALID_DATA_DIR, "Cannot read registry lock file!");
      }
    } else {
      LOG.warn("Registry lock file not found meaning the DataDir is NOT locked to a registry yet!");
    }
  }

  protected void saveConfig() throws IOException {
    // save property config file
    OutputStream out = null;
    try {
      File userCfgFile = new File(dataDir.dataDir, "config/" + DATADIR_PROPFILE);
      // if (userCfgFile.exists()) {
      // }
      out = new FileOutputStream(userCfgFile);

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
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  public void setProperty(String key, String value) {
    properties.setProperty(key, StringUtils.trimToEmpty(value));
  }

  protected void setRegistryType(REGISTRY_TYPE newType) throws InvalidConfigException {
    Preconditions.checkNotNull(newType, "Registry type cannot be null");
    if (this.type != null) {
      if (this.type == newType) {
        // already contains the same information. Dont do anything
        return;
      } else {
        throw new InvalidConfigException(TYPE.DATADIR_ALREADY_REGISTERED,
          "The datadir is already designated as " + this.type);
      }
    }
    try {
      writeRegistryLockFile(newType);
      this.type = newType;
    } catch (IOException e) {
      LOG.error("Cannot lock the datadir to registry type " + newType, e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cannot lock the datadir to registry type " + newType);
    }
  }

  private void writeRegistryLockFile(REGISTRY_TYPE registryType) throws IOException {
    Closer closer = Closer.create();
    try {
      // set lock file if not yet existing
      File lockFile = getRegistryTypeLockFile();
      Writer lock = closer.register(new FileWriter(lockFile, false));
      lock.write(registryType.name());
      lock.flush();
      LOG.info("Locked DataDir to registry of type " + registryType);
    } finally {
      closer.close();
    }
  }
}
