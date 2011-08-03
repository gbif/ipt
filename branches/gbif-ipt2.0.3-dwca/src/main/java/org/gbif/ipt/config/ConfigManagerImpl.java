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
import org.gbif.utils.HttpUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

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
  private DefaultHttpClient client;
  private HttpUtil http;
  private final String pathToCss = "/styles/main.css";

  @Inject
  public ConfigManagerImpl(DataDir dataDir, AppConfig cfg, InputStreamUtils streamUtils,
      UserAccountManager userManager, ResourceManager resourceManager, ExtensionManager extensionManager,
      VocabulariesManager vocabManager, RegistrationManager registrationManager, ConfigWarnings warnings,
      DefaultHttpClient client) {
    super(cfg, dataDir);
    this.streamUtils = streamUtils;
    this.userManager = userManager;
    this.resourceManager = resourceManager;
    this.extensionManager = extensionManager;
    this.vocabManager = vocabManager;
    this.registrationManager = registrationManager;
    this.warnings = warnings;
    this.client = client;
    this.http = new HttpUtil(client);
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
   * Returns the local host name
   */
  public String getHostName() {
    String hostName = "";
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      log.info("No IP address for the local hostname could be found", e);
    }
    return hostName;
  }

  public boolean isBaseURLValid() {
    try {
      URL baseURL = new URL(cfg.getProperty(AppConfig.BASEURL));
      return validateBaseURL(baseURL);
    } catch (MalformedURLException e) {

    }

    return false;
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

    if (cfg.getProxy() != null) {
      log.info("Configuring http proxy ...");
      try {
        setProxy(cfg.getProxy());
      } catch (InvalidConfigException e) {
        warnings.addStartupError(e);
      }
    }

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
      log.info("Logging to " + LogFileAppender.LOGDIR);
      log.info("IPT Data Directory: " + dataDir.dataFile(".").getAbsolutePath());
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

    if (baseURL.getHost().equalsIgnoreCase("localhost") || baseURL.getHost().equalsIgnoreCase("127.0.0.1")
        || baseURL.getHost().equalsIgnoreCase(this.getHostName())) {
      log.warn("Localhost used as base url, IPT will not be visible to the outside!");
    }

    boolean validate = true;
    // validate if localhost URL is configured only in developer mode.
    if (cfg.devMode()) {
      HttpHost hostTemp = (HttpHost) client.getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY);
      if (hostTemp != null) {
        if (baseURL.getHost().equals("localhost") || baseURL.getHost().equals("127.0.0.1")
            || baseURL.getHost().equalsIgnoreCase(this.getHostName())) {
          // if local URL is configured, the IPT should do the validation without a proxy.
          setProxy(null);
          validate = false;
          if (!validateBaseURL(baseURL)) {
            setProxy(hostTemp.toString());
            throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL, "No IPT found at new base URL");
          }
          setProxy(hostTemp.toString());
        }
      }
    }
    // for production mode, the validation should be made using proxy. (local URL are not permitted)
    if (validate) {
      if (!validateBaseURL(baseURL)) {
        throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL, "No IPT found at new base URL");
      }
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
    if (lat != null && lon != null) {
      if ((lat > 90.0 || lat < -90.0) || (lon > 180.0 || lon < -180.0)) {
        log.warn("IPT Lat/Lon is not a valid coordinate");
        throw new InvalidConfigException(TYPE.FORMAT_ERROR, "IPT Lat/Lon is not a valid coordinate");
      }
      cfg.setProperty(AppConfig.IPT_LATITUDE, Double.toString(lat));
      cfg.setProperty(AppConfig.IPT_LONGITUDE, Double.toString(lon));
    } else {
      cfg.setProperty(AppConfig.IPT_LATITUDE, "");
      cfg.setProperty(AppConfig.IPT_LONGITUDE, "");
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.admin.ConfigManager#setProxy(java.lang.String)
   * 
   * Receive an URL with the format scheme://site:port, if don't, a MalformedURLException is thrown.
   */
  public void setProxy(String proxy) throws InvalidConfigException {
    proxy = StringUtils.trimToNull(proxy);
    if (proxy == null) {
      // remove proxy from http client
      log.info("Removing proxy setting");
      client.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
    } else {
      try {
        URL url = new URL(proxy);
        HttpHost host = null;
        if (proxy.contains(":")) {
          host = new HttpHost(url.getHost(), url.getPort());
        } else {
          host = new HttpHost(url.getHost());
        }
        // test that host really exists
        if (!http.verifyHost(host)) {
          throw new InvalidConfigException(TYPE.INVALID_PROXY, "Cannot connect to proxy host");
        }
        log.info("Updating the proxy setting to: " + proxy);
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
      } catch (NumberFormatException e) {
        throw new InvalidConfigException(TYPE.INVALID_PROXY, "port number is no integer");
      } catch (MalformedURLException e) {
        throw new InvalidConfigException(TYPE.INVALID_PROXY, "host URL is no valid");
      }
    }

    // store in properties file
    cfg.setProperty(AppConfig.PROXY, proxy);
  }

  public boolean setupComplete() {
    if (dataDir.isConfigured()) {
      if (cfg.getRegistryType() != null && !userManager.list(Role.Admin).isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private boolean validateBaseURL(URL baseURL) {
    if (baseURL == null) {
      return false;
    }
    // ensure there is an ipt listening at the target
    boolean valid = false;
    try {
      HttpGet get = new HttpGet(baseURL.toString() + pathToCss);
      HttpResponse response = http.executeGetWithTimeout(get, 4000);
      valid = (response.getStatusLine().getStatusCode() == 200);
    } catch (ClientProtocolException e) {
      log.info("Protocol error connecting to new base URL [" + baseURL.toString() + "]", e);
    } catch (IOException e) {
      log.info("IO error connecting to new base URL [" + baseURL.toString() + "]", e);
    } catch (Exception e) {
      log.info("Unknown error connecting to new base URL [" + baseURL.toString() + "]", e);
    }

    return valid;
  }

}
