/**
 * 
 */
package org.gbif.ipt.config;

import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.ipt.utils.LogFileAppender;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A skeleton implementation for the time being....
 * 
 * @author tim
 */
@Singleton
public class ConfigManagerImpl extends BaseManager implements ConfigManager {
  private InputStreamUtils streamUtils;
  private UserAccountManager userManager;
  private ResourceManager resourceManager;
  private ExtensionManager extensionManager;
  private VocabulariesManager vocabManager;
  private RegistrationManager registrationManager;
  private ConfigWarnings warnings;

  @Inject
  public ConfigManagerImpl(DataDir dataDir, AppConfig cfg, InputStreamUtils streamUtils,
      UserAccountManager userManager, ResourceManager resourceManager, ExtensionManager extensionManager,
      VocabulariesManager vocabManager, RegistrationManager registrationManager, ConfigWarnings warnings) {
    super(cfg, dataDir);
    this.streamUtils = streamUtils;
    this.userManager = userManager;
    this.resourceManager = resourceManager;
    this.extensionManager = extensionManager;
    this.vocabManager = vocabManager;
    this.registrationManager = registrationManager;
    this.warnings = warnings;
    if (dataDir.isConfigured()) {
      log.info("IPT DataDir configured - loading its configuration");
      try {
        loadDataDirConfig();
      } catch (InvalidConfigException e) {
        log.error("Configuration problems existing. Watch your data dir! " + e.getMessage(), e);
      }
    } else {
      log.debug("IPT DataDir not configured - no configuration loaded");
    }
  }

  /**
   * Update configuration singleton from config file in data dir
   * 
   * @return true if successful
   * @throws InvalidConfigException
   */
  public void loadDataDirConfig() throws InvalidConfigException {
    log.info("Reading DATA DIRECTORY: " + dataDir.dataDir.getAbsolutePath());

    log.info("Loading IPT config ...");
    cfg.loadConfig();

    log.info("Reloading log4j settings ...");
    reloadLogger();

    log.info("Loading user accounts ...");
    userManager.load();

    log.info("Loading vocabularies ...");
    vocabManager.load();

    log.info("Loading dwc extensions ...");
    extensionManager.load();

    log.info("Loading registration configuration...");
    registrationManager.load();

    log.info("Loading resource configurations ...");
    resourceManager.load();

  }

  private void reloadLogger() {
    LogFileAppender.LOGDIR = dataDir.loggingDir().getAbsolutePath();
    log.info("Setting logging dir to " + LogFileAppender.LOGDIR);

    InputStream log4j;
    // use different log4j settings files for production or debug mode
    if (cfg.debug()) {
      log4j = streamUtils.classpathStream("log4j.xml");
    } else {
      log4j = streamUtils.classpathStream("log4j-production.xml");
    }
    LogManager.resetConfiguration();
    DOMConfigurator domConfig = new DOMConfigurator();
    try {
      domConfig.doConfigure(log4j, LogManager.getLoggerRepository());
      log.info("Reloaded log4j for " + (cfg.debug() ? "debugging" : "production"));
    } catch (Error e) {
      log.error("Failed to reload log4j configuration for " + (cfg.debug() ? "debugging" : "production"), e);
    }
  }

  public void saveConfig() throws InvalidConfigException {
    try {
      cfg.saveConfig();
    } catch (IOException e) {
      log.debug("Cant save IPT configuration: " + e.getMessage(), e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant save IPT configuration: " + e.getMessage());
    }
  }

  public void setAnalyticsKey(String key) throws InvalidConfigException {
    cfg.setProperty(AppConfig.ANALYTICS_KEY, StringUtils.trimToEmpty(key));
  }

  /**
   * @see org.gbif.ipt.service.admin.ConfigManager#setBaseURL(java.net.URL)
   */
  public void setBaseURL(URL baseURL) throws InvalidConfigException {
    log.info("Updating the baseURL to: " + baseURL);

    if (baseURL.getHost().equalsIgnoreCase("localhost") || baseURL.getHost().equalsIgnoreCase("127.0.0.1")) {
      log.warn("Localhost used as base url, IPT will not be visible to the outside!");
    }

    // store in properties file
    cfg.setProperty(AppConfig.BASEURL, baseURL.toString());
  }

  public void setConfigProperty(String key, String value) {
    cfg.setProperty(key, value);
  }

  public boolean setDataDir(File dataDir) throws InvalidConfigException {
    boolean created = this.dataDir.setDataDir(dataDir);
    loadDataDirConfig();
    return created;
  }

  public void setDebugMode(boolean debug) throws InvalidConfigException {
    cfg.setProperty(AppConfig.DEBUG, Boolean.toString(debug));
    reloadLogger();
  }

  public void setGbifAnalytics(boolean useGbifAnalytics) throws InvalidConfigException {
    cfg.setProperty(AppConfig.ANALYTICS_GBIF, Boolean.toString(useGbifAnalytics));
  }

  public void setIptLocation(Double lat, Double lon) throws InvalidConfigException {
    if ((lat > 90.0 || lat < -90.0) || (lon > 180.0 || lon < -180.0)) {
      log.warn("IPT Lat/Lon is not a valid coordinate");
      lat = null;
      lon = null;
    }
    if (lat != null && lon != null) {
      cfg.setProperty(AppConfig.IPT_LATITUDE, Double.toString(lat));
      cfg.setProperty(AppConfig.IPT_LONGITUDE, Double.toString(lon));
    } else {
      cfg.setProperty(AppConfig.IPT_LATITUDE, "");
      cfg.setProperty(AppConfig.IPT_LONGITUDE, "");
    }

  }

  public boolean setupComplete() {
    if (dataDir.isConfigured()) {
      if (cfg.getRegistryType() != null && !userManager.list(Role.Admin).isEmpty()) {
        return true;
      }
    }
    return false;
  }

}
