package org.gbif.ipt.config;

import com.sun.jersey.json.impl.provider.entity.JSONArrayProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.LoggerContext;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.RegistrationManagerImpl;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.ipt.utils.URLUtils;
import org.gbif.utils.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * A skeleton implementation for the time being.
 */
@Singleton
public class ConfigManagerImpl extends BaseManager implements ConfigManager {

  private final InputStreamUtils streamUtils;
  private final UserAccountManager userManager;
  private final ResourceManager resourceManager;
  private final ExtensionManager extensionManager;
  private final VocabulariesManager vocabManager;
  private final RegistrationManager registrationManager;
  private final ConfigWarnings warnings;
  private final DefaultHttpClient client;
  private final HttpUtil http;
  private final PublishingMonitor publishingMonitor;
  private static final String PATH_TO_CSS = "/styles/main.css";

  private static final int DEFAULT_TO = 4000; // Default time out
  private static final String DEPRECATED_VOCAB_PERSISTENCE_FILE = "vocabularies.xml";

  @Inject
  public ConfigManagerImpl(DataDir dataDir, AppConfig cfg, InputStreamUtils streamUtils,
    UserAccountManager userManager,
    ResourceManager resourceManager, ExtensionManager extensionManager, VocabulariesManager vocabManager,
    RegistrationManager registrationManager, ConfigWarnings warnings, DefaultHttpClient client, PublishingMonitor
    publishingMonitor) {
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
    this.publishingMonitor = publishingMonitor;
    if (dataDir.isConfigured()) {
      LOG.info("IPT DataDir configured - loading its configuration");
      try {
        loadDataDirConfig();
      } catch (InvalidConfigException e) {
        LOG.error("Configuration problems existing. Watch your data dir! " + e.getMessage(), e);
      }
    } else {
      LOG.debug("IPT DataDir not configured - no configuration loaded");
    }
  }

  /**
   * It Creates a HttpHost object with the string given by the user and verifies if there is a connection with this
   * host. If there is a connection with this host, it changes the current proxy host with this host. If not it keeps
   * the current proxy.
   *
   * @param hostTemp the actual proxy.
   * @param proxy    an URL with the format http://proxy.my-institution.com:8080.
   * @throws InvalidConfigException If it can not connect to the proxy host or if the port number is no integer or if
   *                                the proxy URL is not with the valid format http://proxy.my-institution.com:8080
   */
  private boolean changeProxy(HttpHost hostTemp, String proxy) {
    try {
      HttpHost host = URLUtils.getHost(proxy);
      client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);

      String testUrl = cfg.getRegistryUrl();
      boolean proxyWorks;
      try {
        LOG.info("Testing new proxy by fetching "+testUrl+" with 4 second timeout");
        HttpResponse response = http.executeGetWithTimeout(new HttpGet(testUrl), DEFAULT_TO);

        LOG.info("Proxy response is "+ response.getStatusLine());
        proxyWorks = (HttpServletResponse.SC_OK == response.getStatusLine().getStatusCode());
      } catch (Exception e) {
        proxyWorks = false;
        LOG.warn("Proxy failed because", e);
      }

      if (proxyWorks) {
        LOG.info("Proxy tested and working.");
      } else {
        if (hostTemp != null) {
          LOG.info("Proxy could not be validated (tried to retrieve " + testUrl + "), reverting to previous proxy setting on HTTP client: " + hostTemp.toString());
          client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hostTemp);
        }
        throw new InvalidConfigException(TYPE.INVALID_PROXY, "admin.config.error.connectionRefused");
      }

    } catch (NumberFormatException e) {
      if (hostTemp != null) {
        LOG.info("NumberFormatException encountered, reverting to previous proxy setting on HTTP client: " + hostTemp.toString());
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hostTemp);
      }
      throw new InvalidConfigException(TYPE.INVALID_PROXY, "admin.config.error.invalidPort");
    } catch (MalformedURLException e) {
      if (hostTemp != null) {
        LOG.info("MalformedURLException encountered, reverting to previous proxy setting on HTTP client: " + hostTemp.toString());
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hostTemp);
      }
      throw new InvalidConfigException(TYPE.INVALID_PROXY, "admin.config.error.invalidProxyURL");
    }
    return true;
  }

  /**
   * Returns the local host name.
   */
  public String getHostName() {
    return URLUtils.getHostName();
  }

  public boolean isBaseURLValid() {
    try {
      URL baseURL = new URL(cfg.getProperty(AppConfig.BASEURL));
      return validateBaseURL(baseURL);
    } catch (MalformedURLException e) {
      LOG.error("MalformedURLException encountered while validating baseURL");
    }

    return false;
  }

  /**
   * Update configuration singleton from config file in data dir.
   */
  public void loadDataDirConfig() throws InvalidConfigException {
    LOG.info("Reading DATA DIRECTORY: " + dataDir.dataDir.getAbsolutePath());

    LOG.info("Loading IPT config ...");
    cfg.loadConfig();

    LOG.info("Reloading log4j settings ...");
    reloadLogger();

    if (cfg.getProxy() != null) {
      LOG.info("Configuring http proxy ...");
      try {
        setProxy(cfg.getProxy());
      } catch (InvalidConfigException e) {
        warnings.addStartupError(e);
      }
    }

    LOG.info("Loading user accounts ...");
    userManager.load();

    LOG.info("Loading vocabularies ...");
    vocabManager.load();

    LOG.info("Ensure latest versions of default vocabularies are installed...");
    vocabManager.installOrUpdateDefaults();

    File vocabDir = dataDir.configFile(VocabulariesManagerImpl.CONFIG_FOLDER);
    File deprecatedVocabFile = new File(vocabDir, DEPRECATED_VOCAB_PERSISTENCE_FILE);
    if (deprecatedVocabFile.exists()) {
      LOG.info("Perform 1-time event: delete deprecated vocabularies.xml file");
      FileUtils.deleteQuietly(deprecatedVocabFile);
    }

    LOG.info("Loading extensions ...");
    extensionManager.load();

    if (!dataDir.configFile(RegistrationManagerImpl.PERSISTENCE_FILE_V2).exists()) {
      LOG.info("Perform 1-time event: migrate registration.xml into registration2.xml with passwords encrypted");
      registrationManager.encryptRegistration();
    }

    LOG.info("Loading registration configuration...");
    registrationManager.load();

    LOG.info("Loading resource configurations ...");
    // default creator used to populate missing resource creator when loading resources
    User defaultCreator = (userManager.list(Role.Admin).isEmpty()) ? null : userManager.list(Role.Admin).get(0);
    File resourcesDir = dataDir.dataFile(DataDir.RESOURCES_DIR);
    checkResourcesDirAtStartup(resourcesDir);
    resourceManager.load(resourcesDir, defaultCreator);

    // start publishing monitor
    LOG.info("Starting Publishing Monitor...");
    publishingMonitor.start();
  }

  private void checkResourcesDirAtStartup(File resourcesDir) {
    DataDir.DirStatus status = dataDir.getDirectoryReadWriteStatus(resourcesDir);
    switch (status) {
      case NOT_EXIST:
        // No folder /resources
        LOG.error("Resources directory does not exist: " + resourcesDir);
        warnings.addStartupError("Resources directory does not exist: " + resourcesDir);
        break;
      case NO_ACCESS:
        // No access to folder /resources
        LOG.error("Resources directory cannot be read. Please check access rights for: " + resourcesDir);
        warnings.addStartupError("Resources directory cannot be read. Please check access rights for: " + resourcesDir);
        break;
      case READ_ONLY:
        // No write access to folder /resources
        LOG.error("Resources directory cannot be written. Please check access rights for: " + resourcesDir);
        warnings.addStartupError("Resources directory cannot be written. Please check access rights for: " + resourcesDir);
        break;
      case READ_WRITE:
        // Write access to folder /resources
        // Check subdirectories
        File[] files = resourcesDir.listFiles();
        if (files != null) {
          for (File subResourceDir : files) {
            DataDir.DirStatus subStatus = dataDir.getDirectoryReadWriteStatus(subResourceDir);
            switch (subStatus) {
              case NOT_EXIST:
              case NO_ACCESS:
                // No access to sub folders of /resources
                LOG.error("At least one resource directory cannot be read. Please check access rights for: " + subResourceDir);
                warnings.addStartupError("At least one resource directory cannot be read. Please check access rights for: " + subResourceDir);
                break;
              case READ_ONLY:
                // No write access to sub folders of /resources
                LOG.error("At least one resource directory cannot be written. Please check access rights for: " + subResourceDir);
                warnings.addStartupError("At least one resource directory cannot be written. Please check access rights for: " + subResourceDir);
                break;
            }
          }
        }
        break;
    }
  }

  private void reloadLogger() {
    LoggingConfiguration.logDirectory = dataDir.loggingDir().getAbsolutePath()+"/";
    LOG.info("Changing logging directory to {}", LoggingConfiguration.logDirectory);

    LoggingConfigFactory.useDebug = cfg.debug();

    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration newConfig = LoggingConfigFactory.newConfigurationBuilder().build(true);
    context.setConfiguration(newConfig);
    context.reconfigure();

    LOG.info("Reloaded Log4J2 for {}", (cfg.debug() ? "debugging" : "production"));
    LOG.info("Logging to {}", LoggingConfiguration.logDirectory);
    LOG.info("IPT Data Directory: {}", dataDir.dataFile(".").getAbsolutePath());
  }

  public void saveConfig() throws InvalidConfigException {
    try {
      cfg.saveConfig();
    } catch (IOException e) {
      LOG.debug("Cant save IPT configuration: " + e.getMessage(), e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant save IPT configuration: " + e.getMessage());
    }
  }

  public void setAnalyticsKey(String key) throws InvalidConfigException {
    cfg.setProperty(AppConfig.ANALYTICS_KEY, StringUtils.trimToEmpty(key));
  }

  public void setBaseUrl(URL baseURL) throws InvalidConfigException {
    boolean validate = true;
    if (URLUtils.isLocalhost(baseURL)) {
      LOG.info("Localhost used in base URL");

      // validate if localhost URL is configured only in developer mode.
      // use cfg registryType vs cfg devMode since it takes into account devMode from pom and production from setupPage
      if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.DEVELOPMENT) {
        HttpHost hostTemp = (HttpHost) client.getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY);
        if (hostTemp != null) {
          // if local URL is configured, the IPT should do the validation without a proxy.
          setProxy(null);
          validate = false;
          if (!validateBaseURL(baseURL)) {
            setProxy(hostTemp.toString());
            throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL, "No IPT found at new base URL");
          }
          setProxy(hostTemp.toString());
        }
      } else {
        // we want to allow baseURL equal the machine name in production mode, but not localhost
        if (!baseURL.getHost().equalsIgnoreCase(this.getHostName())) {
          // local URL is not permitted in production mode.
          throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL,
            "Localhost base URL not permitted in production mode, since the IPT will not be visible to the outside!");
        }
      }
    }

    if (validate && !validateBaseURL(baseURL)) {
      throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL, "No IPT found at new base URL");
    }

    // store in properties file
    LOG.info("Updating the baseURL to: " + baseURL);
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

  /**
   * Turn archival mode on or off. If being turned off, there can be no associated organisations in the IPT that
   * has its DOI registration agency account activated to start registering DOIs for datasets.
   *
   * @param archivalMode true to turn on, false to turn off
   */
  public void setArchivalMode(boolean archivalMode) throws InvalidConfigException {
    if (!archivalMode && registrationManager.findPrimaryDoiAgencyAccount() != null) {
      throw new InvalidConfigException(TYPE.DOI_REGISTRATION_ALREADY_ACTIVATED,
        "Cannot turn off archival mode since" + "DOI registration has been activated");
    }
    cfg.setProperty(AppConfig.ARCHIVAL_MODE, Boolean.toString(archivalMode));
  }

  /**
   * Define archival limit.
   */
  @Override
  public void setArchivalLimit(Integer archivalLimit) throws InvalidConfigException {
    if ((archivalLimit == null) || (archivalLimit == 0)){
      cfg.setProperty(AppConfig.ARCHIVAL_LIMIT, "");
    }
    else {
      cfg.setProperty(AppConfig.ARCHIVAL_LIMIT, Integer.toString(archivalLimit));
    }
  }

  public void setGbifAnalytics(boolean useGbifAnalytics) throws InvalidConfigException {
    cfg.setProperty(AppConfig.ANALYTICS_GBIF, Boolean.toString(useGbifAnalytics));
  }

  public void setIptLocation(Double lat, Double lon) throws InvalidConfigException {
    if (lat == null || lon == null) {
      cfg.setProperty(AppConfig.IPT_LATITUDE, "");
      cfg.setProperty(AppConfig.IPT_LONGITUDE, "");
    } else {
      if (lat > 90.0 || lat < -90.0 || (lon > 180.0 || lon < -180.0)) {
        LOG.warn("IPT Lat/Lon is not a valid coordinate");
        throw new InvalidConfigException(TYPE.FORMAT_ERROR, "IPT Lat/Lon is not a valid coordinate");
      }
      cfg.setProperty(AppConfig.IPT_LATITUDE, Double.toString(lat));
      cfg.setProperty(AppConfig.IPT_LONGITUDE, Double.toString(lon));
    }

  }

  /**
   * It validates if is the first time that the user saves a proxy, if this is true, the proxy is saved normally (the
   * first time that the proxy is saved is in the setup page), if not (the second time that the user saves a proxy is
   * in
   * the config page), it validates if this proxy is the same as current proxy, if this is true, nothing changes, if
   * not, it removes the current proxy and save the new proxy.
   *
   * @param proxy an URL with the format http://proxy.my-institution.com:8080.
   */
  public void setProxy(String proxy) throws InvalidConfigException {
    proxy = StringUtils.trimToNull(proxy);
    // save the current proxy
    HttpHost hostTemp = null;
    if (StringUtils.trimToNull(cfg.getProperty(AppConfig.PROXY)) != null) {
      try {
        URL urlTemp = new URL(cfg.getProperty(AppConfig.PROXY));
        hostTemp = new HttpHost(urlTemp.getHost(), urlTemp.getPort());
      } catch (MalformedURLException e) {
        // This exception should not be shown, the urlTemp was validated before being saved.
        LOG.info("the proxy URL is invalid", e);
      }
    }

    if (proxy == null) {
      // remove proxy from http client
      // Suddenly the client didn't have proxy host.
      LOG.info("No proxy entered, so removing proxy setting on http client");
      client.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
    } else {
      // Changing proxy host
      if (hostTemp == null) {
        // First time, before Setup
        changeProxy(null, proxy);
      } else {
        // After Setup
        // Validating if the current proxy is the same proxy given by the user
        if (hostTemp.toString().equals(proxy)) {
          changeProxy(hostTemp, proxy);
        } else {
          // remove proxy from http client
          LOG.info("A change of proxy detected so starting by removing proxy setting on http client");
          client.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
          changeProxy(hostTemp, proxy);
        }
      }
    }
    // store in properties file
    cfg.setProperty(AppConfig.PROXY, proxy);
  }


  public boolean setupComplete() {
    return dataDir.isConfigured() && cfg.getRegistryType() != null && !userManager.list(Role.Admin).isEmpty();
  }

  /**
   * It validates if the there is a connection with the baseURL, it executes a request using the baseURL.
   *
   * @param baseURL a URL to validate.
   *
   * @return true if the response to the request has a status code equal to 200.
   */
  public boolean validateBaseURL(URL baseURL) {
    if (baseURL == null) {
      return false;
    }
    try {
      String testURL = baseURL.toString() + PATH_TO_CSS;
      LOG.info("Validating BaseURL with get request (having 4 second timeout) to: " + testURL);
      HttpResponse response = http.executeGetWithTimeout(new HttpGet(testURL), DEFAULT_TO);
      return HttpServletResponse.SC_OK == response.getStatusLine().getStatusCode();
    } catch (ClientProtocolException e) {
      LOG.info("Protocol error connecting to new base URL [" + baseURL.toString() + "]", e);
    } catch (IOException e) {
      LOG.info("IO error connecting to new base URL [" + baseURL.toString() + "]", e);
    } catch (Exception e) {
      LOG.info("Unknown error connecting to new base URL [" + baseURL.toString() + "]", e);
    }
    return false;
  }

}
