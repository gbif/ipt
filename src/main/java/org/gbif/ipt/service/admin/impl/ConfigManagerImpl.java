/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.LoggingConfigFactory;
import org.gbif.ipt.config.LoggingConfiguration;
import org.gbif.ipt.config.PublishingMonitor;
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
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.URLUtils;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ConfigManagerImpl extends BaseManager implements ConfigManager {

  private final UserAccountManager userManager;
  private final ResourceManager resourceManager;
  private final ExtensionManager extensionManager;
  private final VocabulariesManager vocabManager;
  private final RegistrationManager registrationManager;
  private final ConfigWarnings warnings;
  private final HttpClient client;
  private final PublishingMonitor publishingMonitor;
  private static final String PATH_TO_CSS = "/styles/main.css";

  private static final int DEFAULT_TO = 4000; // Default time out
  private static final String DEPRECATED_VOCAB_PERSISTENCE_FILE = "vocabularies.xml";

  @Inject
  public ConfigManagerImpl(DataDir dataDir, AppConfig cfg, UserAccountManager userManager,
                           ResourceManager resourceManager, ExtensionManager extensionManager,
                           VocabulariesManager vocabManager, RegistrationManager registrationManager,
                           ConfigWarnings warnings, HttpClient client, PublishingMonitor
    publishingMonitor) {
    super(cfg, dataDir);
    this.userManager = userManager;
    this.resourceManager = resourceManager;
    this.extensionManager = extensionManager;
    this.vocabManager = vocabManager;
    this.registrationManager = registrationManager;
    this.warnings = warnings;
    this.client = client;
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
   * @param currentProxyHost the actual proxy.
   * @param proxy    a URL with the format http://proxy.my-institution.com:8080.
   * @throws InvalidConfigException If it can not connect to the proxy host or if the port number is no integer or if
   *                                the proxy URL is not with the valid format http://proxy.my-institution.com:8080
   */
  @SuppressWarnings("UnusedReturnValue")
  private boolean changeProxy(HttpHost currentProxyHost, String proxy) {
    try {
      HttpHost proxyHost = URLUtils.getHost(proxy);
      String testUrl = cfg.getRegistryUrl();
      boolean proxyWorks;
      try {
        // prepare custom configuration with proxy and timeouts
        LOG.info("Testing new proxy by fetching " + testUrl + " with 4 second timeout");
        RequestConfig rs = RequestConfig.custom()
            .setProxy(proxyHost)
            .setConnectTimeout(DEFAULT_TO)
            .setSocketTimeout(DEFAULT_TO)
            .build();

        ExtendedResponse response = client.get(testUrl, rs);

        LOG.info("Proxy response is "+ response.getStatusLine());
        proxyWorks = (HttpServletResponse.SC_OK == response.getStatusLine().getStatusCode());
      } catch (Exception e) {
        proxyWorks = false;
        LOG.warn("Proxy failed because", e);
      }

      if (proxyWorks) {
        LOG.info("Proxy tested and working.");
        client.setProxy(proxyHost);
      } else {
        throwConfigException(
            "Proxy could not be validated (tried to retrieve " + testUrl + ")",
            currentProxyHost,
            "admin.config.error.connectionRefused");
      }
    } catch (NumberFormatException e) {
      throwConfigException(e.getClass().getSimpleName() + " encountered", currentProxyHost, "admin.config.error.invalidPort");
    } catch (MalformedURLException e) {
      throwConfigException(e.getClass().getSimpleName() + " encountered", currentProxyHost, "admin.config.error.invalidProxyURL");
    }
    return true;
  }

  private void throwConfigException(String logMessage, HttpHost currentProxyHost, String message) {
    if (currentProxyHost != null) {
      LOG.info(logMessage + " , reverting to previous proxy setting on HTTP client: " + currentProxyHost);
    }
    throw new InvalidConfigException(TYPE.INVALID_PROXY, message);
  }

  /**
   * Returns the local host name.
   */
  @Override
  public String getHostName() {
    return URLUtils.getHostName();
  }

  @Override
  public boolean isBaseURLValid() {
    try {
      URL baseURL = new URL(cfg.getProperty(AppConfig.BASEURL));
      String proxyUrl = cfg.getProperty(AppConfig.PROXY);
      HttpHost proxyHost = StringUtils.trimToNull(proxyUrl) != null ? URLUtils.getHost(proxyUrl) : null;
      return isValidBaseUrl(baseURL, proxyHost);
    } catch (MalformedURLException e) {
      LOG.error("MalformedURLException encountered while validating baseURL");
    }

    return false;
  }

  /**
   * Update configuration singleton from config file in data dir.
   */
  @Override
  public void loadDataDirConfig() throws InvalidConfigException {
    LOG.info("Reading DATA DIRECTORY: " + dataDir.getDataDir().getAbsolutePath());

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

  @Override
  public void saveConfig() throws InvalidConfigException {
    try {
      cfg.saveConfig();
    } catch (IOException e) {
      LOG.debug("Cant save IPT configuration: " + e.getMessage(), e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant save IPT configuration: " + e.getMessage());
    }
  }

  @Override
  public void setAnalyticsKey(String key) throws InvalidConfigException {
    cfg.setProperty(AppConfig.ANALYTICS_KEY, StringUtils.trimToEmpty(key));
  }

  @Override
  public void setBaseUrl(URL baseURL) throws InvalidConfigException {
    boolean validate = true;

    String proxyUrl = cfg.getProperty(AppConfig.PROXY);
    HttpHost proxyHost;

    try {
      proxyHost = StringUtils.trimToNull(proxyUrl) != null ? URLUtils.getHost(proxyUrl) : null;
    } catch (MalformedURLException e) {
      throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL, "Wrong Proxy configuration");
    }

    if (URLUtils.isLocalhost(baseURL)) {
      LOG.info("Localhost used in base URL");

      // validate if localhost URL is configured only in developer mode.
      // use cfg registryType vs cfg devMode since it takes into account devMode from pom and production from setupPage
      if (cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.DEVELOPMENT) {
        if (proxyHost != null) {
          // if local URL is configured, the IPT should do the validation without a proxy.
          validate = false;
          if (!isValidBaseUrl(baseURL, proxyHost)) {
            throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL, "No IPT found at new base URL");
          }
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

    if (validate && !isValidBaseUrl(baseURL, proxyHost)) {
      throw new InvalidConfigException(TYPE.INACCESSIBLE_BASE_URL, "No IPT found at new base URL");
    }

    // store in properties file
    LOG.info("Updating the baseURL to: " + baseURL);
    cfg.setProperty(AppConfig.BASEURL, baseURL.toString());
  }

  @Override
  public void setConfigProperty(String key, String value) {
    cfg.setProperty(key, value);
  }

  @Override
  public boolean setDataDir(File dataDir) throws InvalidConfigException {
    boolean created = this.dataDir.setDataDir(dataDir);
    loadDataDirConfig();
    return created;
  }

  @Override
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
  @Override
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

  @Override
  public void setGbifAnalytics(boolean useGbifAnalytics) throws InvalidConfigException {
    cfg.setProperty(AppConfig.ANALYTICS_GBIF, Boolean.toString(useGbifAnalytics));
  }

  @Override
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
   * @param proxy a URL with the format http://proxy.my-institution.com:8080.
   */
  @Override
  public void setProxy(String proxy) throws InvalidConfigException {
    String newProxyValue = StringUtils.trimToNull(proxy);

    if (newProxyValue == null) {
      // new proxy value is null, so proxy property is supposed to be removed
      LOG.info("No proxy entered, so removing proxy setting on http client");
      client.removeProxy();
    } else {
      // save the current proxy
      HttpHost currentProxy = null;
      String proxyProperty = cfg.getProperty(AppConfig.PROXY);
      if (StringUtils.trimToNull(proxyProperty) != null) {
        try {
          URL currentProxyUrl = new URL(proxyProperty);
          currentProxy = new HttpHost(currentProxyUrl.getHost(), currentProxyUrl.getPort());
        } catch (MalformedURLException e) {
          // This exception should not be shown, the currentProxyUrl was validated before being saved.
          LOG.info("the proxy URL is invalid", e);
        }
      }

      // new proxy property is provided
      // first case: before setup (current proxy is null)
      // second case: after setup (current proxy is not null)
      changeProxy(currentProxy, newProxyValue);
    }

    // store in properties file
    cfg.setProperty(AppConfig.PROXY, newProxyValue);
  }

  @Override
  public boolean setupComplete() {
    return dataDir.isConfigured() && cfg.getRegistryType() != null && !userManager.list(Role.Admin).isEmpty();
  }

  /**
   * It validates if the there is a connection with the baseURL, it executes a request using the baseURL.
   *
   * @param baseURL a URL to validate.
   * @param proxy a proxy host
   *
   * @return true if the response to the request has a status code equal to 200.
   */
  public boolean isValidBaseUrl(URL baseURL, HttpHost proxy) {
    if (baseURL == null) {
      return false;
    }
    try {
      String testURL = baseURL + PATH_TO_CSS;
      LOG.info("Validating BaseURL with get request (having 4 second timeout) to: " + testURL);

      RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
          .setConnectTimeout(DEFAULT_TO)
          .setSocketTimeout(DEFAULT_TO);

      if (proxy != null) {
        requestConfigBuilder.setProxy(proxy);
      }

      ExtendedResponse response = client.get(testURL, requestConfigBuilder.build());

      return HttpServletResponse.SC_OK == response.getStatusLine().getStatusCode();
    } catch (ClientProtocolException e) {
      LOG.info("Protocol error connecting to new base URL [" + baseURL + "]", e);
    } catch (IOException e) {
      LOG.info("IO error connecting to new base URL [" + baseURL + "]", e);
    } catch (Exception e) {
      LOG.info("Unknown error connecting to new base URL [" + baseURL + "]", e);
    }
    return false;
  }

  @Override
  public void setAdminEmail(String adminEmail) {
    cfg.setProperty(AppConfig.ADMIN_EMAIL, adminEmail);
  }
}
