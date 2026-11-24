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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourcePublicationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.PublicationFailureEmailUtils;
import org.gbif.ipt.utils.URLUtils;

import jakarta.inject.Inject;
import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

/**
 * The IPT settings page (admin/config.ftl).
 * <p>
 * The form starts from the saved settings and the posted values replace them. Save checks every field first and
 * changes nothing while any of them is wrong; only the proxy and the public URL, which are tested over the network,
 * can still fail after that, and a failed public URL puts the previous proxy back.
 */
public class ConfigAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = 4726973323043063968L;

  private static final Logger LOG = LogManager.getLogger(ConfigAction.class);

  private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

  private static final String KEEP_ALL = "all";
  private static final String KEEP_LAST = "last";

  /**
   * The interface languages, each in its own language, in the order the page lists them.
   */
  private static final Map<String, String> LANGUAGES;

  static {
    Map<String, String> languages = new LinkedHashMap<>();
    languages.put("en", "English");
    languages.put("es", "Español");
    languages.put("fr", "Français");
    languages.put("pt", "Português");
    languages.put("ru", "Русский");
    languages.put("ja", "日本語");
    languages.put("zh", "繁體中文");
    LANGUAGES = Collections.unmodifiableMap(languages);
  }

  protected ConfigManager configManager;
  private final ResourceManager resourceManager;
  private final ResourcePublicationManager resourcePublicationManager;

  // the form: the saved settings (prepare), replaced by the posted values
  private String baseUrl;
  private String proxy;
  private Boolean debug;
  private String analyticsKey;
  private String adminEmail;
  private String latitude;
  private String longitude;
  private Boolean archivalMode;
  private String archivalKeep;
  private String archivalLimit;
  private String defaultLocale;
  private String mailSmtpHost;
  private String mailSmtpPort;
  private String mailSmtpUsername;
  private String mailSmtpPassword;
  private boolean mailSmtpPasswordRemove;
  private Boolean mailSmtpStartTlsEnable;

  @Inject
  public ConfigAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ConfigManager configManager,
      ResourceManager resourceManager,
      ResourcePublicationManager resourcePublicationManager) {
    super(textProvider, cfg, registrationManager);
    this.configManager = configManager;
    this.resourceManager = resourceManager;
    this.resourcePublicationManager = resourcePublicationManager;
  }

  @Override
  public void prepare() {
    super.prepare();
    baseUrl = cfg.getBaseUrl();
    proxy = cfg.getProxy();
    debug = cfg.debug();
    analyticsKey = cfg.getAnalyticsKey();
    adminEmail = cfg.getAdminEmail();
    latitude = cfg.getLatitude() == null ? null : cfg.getLatitude().toString();
    longitude = cfg.getLongitude() == null ? null : cfg.getLongitude().toString();
    archivalMode = cfg.isArchivalMode();
    Integer limit = cfg.getArchivalLimit();
    archivalKeep = limit == null || limit <= 0 ? KEEP_ALL : KEEP_LAST;
    archivalLimit = limit == null || limit <= 0 ? null : limit.toString();
    defaultLocale = cfg.getDefaultLocale();
    mailSmtpHost = cfg.getMailSmtpHost();
    mailSmtpPort = cfg.getMailSmtpPort();
    mailSmtpUsername = cfg.getMailSmtpUsername();
    mailSmtpStartTlsEnable = cfg.isMailSmtpStartTlsEnabled();
  }

  @Override
  public String save() {
    LOG.info("Changing the IPT configuration");

    // 1. check everything, change nothing
    URL newBaseUrl = checkBaseUrl();
    checkProxy();
    checkEmail();
    Integer limit = checkArchiving();
    Double[] location = checkLocation();
    if (hasFieldErrors()) {
      return INPUT;
    }

    // 2. the settings tested over the network: the proxy first, since the public URL is tested through it
    String oldProxy = cfg.getProxy();
    boolean proxyChanged = !Objects.equals(StringUtils.trimToNull(proxy), StringUtils.trimToNull(oldProxy));
    if (proxyChanged) {
      try {
        configManager.setProxy(proxy);
      } catch (InvalidConfigException e) {
        addFieldError("proxy", getText(e.getMessage()));
        return INPUT;
      }
    }

    boolean baseUrlChanged = !Objects.equals(baseUrl, cfg.getBaseUrl());
    if (baseUrlChanged) {
      LOG.info("Changing the installation baseURL from [{}] to [{}]", cfg.getBaseUrl(), baseUrl);
      try {
        configManager.setBaseUrl(newBaseUrl);
      } catch (InvalidConfigException e) {
        if (URLUtils.isLocalhost(newBaseUrl) && cfg.getRegistryType() != AppConfig.REGISTRY_TYPE.DEVELOPMENT) {
          addFieldError("baseUrl", getText("admin.config.baseUrl.invalidBaseURL"));
        } else {
          addFieldError("baseUrl", getText("admin.config.baseUrl.inaccessible"));
        }
        restoreProxy(proxyChanged, oldProxy);
        return INPUT;
      }
    }

    // 3. the rest; only writing the file can still fail
    configManager.setAdminEmail(adminEmail);
    configManager.setMailSettings(mailSmtpHost, mailSmtpPort, mailSmtpUsername, newMailPassword(),
        mailSmtpStartTlsEnable);
    try {
      if (archivalMode != null) {
        configManager.setArchivalMode(archivalMode);
      }
      if (archivalKeep != null) {
        configManager.setArchivalLimit(limit);
      }
      if (debug != null && debug != cfg.debug()) {
        configManager.setDebugMode(debug);
      }
      configManager.setAnalyticsKey(analyticsKey);
      configManager.setIptLocation(location[0], location[1]);
      configManager.setDefaultLocale(defaultLocale);
      configManager.saveConfig();
    } catch (InvalidConfigException e) {
      LOG.error("Couldn't save the IPT settings", e);
      addActionError(getText("admin.config.save.error"));
      return INPUT;
    }

    if (StringUtils.startsWithIgnoreCase(analyticsKey, "UA-")) {
      addActionWarning(getText("admin.config.analyticsKey.ua"));
    }

    if (baseUrlChanged) {
      // any public resource's URL (alternative identifier) changes with it
      updateAllAlternateIdentifiersForIPTURLToResource();
      LOG.info("Installation baseURL successfully changed to [{}]", baseUrl);
      addActionMessage(getText("admin.config.baseUrl.changed"));
      addActionMessage(getText("admin.user.login"));
      addActionMessage(getText("admin.config.baseUrl.changed.reminder"));
      session.remove(Constants.SESSION_USER);
      if (URLUtils.isLocalhost(newBaseUrl)) {
        addActionWarning(getText("admin.config.error.localhostURL"));
      } else if (URLUtils.isHostName(newBaseUrl)) {
        // the machine name: make sure it is visible on the Internet
        addActionWarning(getText("admin.config.baseUrl.sameHostName"));
      }
      return HOME;
    }

    addActionMessage(getText("admin.config.success"));
    return SUCCESS;
  }

  /**
   * @return the posted public URL, or null with a field error if it isn't an http(s) URL
   */
  private URL checkBaseUrl() {
    if (baseUrl == null) {
      addFieldError("baseUrl", getText("validation.required", new String[] {getText("admin.config.baseUrl")}));
      return null;
    }
    URL url = httpUrl(baseUrl);
    if (url == null) {
      addFieldError("baseUrl", getText("admin.config.error.invalidBaseURL"));
    }
    return url;
  }

  private void checkProxy() {
    if (proxy != null && httpUrl(proxy) == null) {
      addFieldError("proxy", getText("admin.config.error.invalidProxyURL"));
    }
  }

  private void checkEmail() {
    if (adminEmail != null && !EMAIL_VALIDATOR.isValid(adminEmail)) {
      addFieldError("adminEmail", getText("validation.email.invalid"));
    }
    if (mailSmtpPort != null) {
      Integer port = wholeNumber(mailSmtpPort);
      if (port == null || port < 1 || port > 65535) {
        addFieldError("mailSmtpPort", getText("admin.config.mailSmtpPort.invalid"));
      }
    }
  }

  /**
   * @return the number of versions to keep, null for all
   */
  private Integer checkArchiving() {
    if (Boolean.FALSE.equals(archivalMode) && getDoiAccountOrganisation() != null) {
      addFieldError("archivalMode", getText("admin.error.invalidConfiguration.doiAccount.activated"));
    }
    if (!KEEP_LAST.equals(archivalKeep)) {
      return null;
    }
    Integer limit = archivalLimit == null ? null : wholeNumber(archivalLimit);
    if (limit == null || limit < 1) {
      addFieldError("archivalLimit", getText("admin.config.archivalLimit.invalid"));
      return null;
    }
    return limit;
  }

  /**
   * @return latitude and longitude, both null for no location
   */
  private Double[] checkLocation() {
    Double[] location = new Double[2];
    if (latitude == null && longitude == null) {
      return location;
    }
    if (latitude == null || longitude == null) {
      addFieldError(latitude == null ? "latitude" : "longitude", getText("admin.config.server.location.both"));
      return location;
    }
    location[0] = decimal(latitude);
    location[1] = decimal(longitude);
    if (location[0] == null || location[0] < -90 || location[0] > 90) {
      addFieldError("latitude", getText("admin.config.server.latitude.invalid"));
    }
    if (location[1] == null || location[1] < -180 || location[1] > 180) {
      addFieldError("longitude", getText("admin.config.server.longitude.invalid"));
    }
    return location;
  }

  private String newMailPassword() {
    if (mailSmtpPassword != null) {
      return mailSmtpPassword;
    }
    return mailSmtpPasswordRemove ? null : cfg.getMailSmtpPassword();
  }

  private void restoreProxy(boolean proxyChanged, String oldProxy) {
    if (!proxyChanged) {
      return;
    }
    try {
      configManager.setProxy(oldProxy);
    } catch (InvalidConfigException e) {
      LOG.warn("Couldn't put the previous proxy [{}] back", oldProxy, e);
    }
  }

  private static URL httpUrl(String value) {
    try {
      URL url = new URI(value).toURL();
      if (("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()))
          && StringUtils.isNotBlank(url.getHost())) {
        return url;
      }
    } catch (MalformedURLException | IllegalArgumentException | URISyntaxException e) {
      // not a URL
    }
    return null;
  }

  private static Integer wholeNumber(String value) {
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Double decimal(String value) {
    try {
      double d = Double.parseDouble(value);
      return Double.isFinite(d) ? d : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Updates all public resource's alternative identifier for the IPT URL to the resource. This identifier should only
   * exist for the resource, if its visibility is public. Any time the baseURL changes, all resources will need this
   * identifier to be updated.
   */
  private void updateAllAlternateIdentifiersForIPTURLToResource() {
    List<Resource> resources = resourceManager.list(PublicationStatus.PUBLIC);
    resources.addAll(resourceManager.list(PublicationStatus.REGISTERED));
    if (!resources.isEmpty()) {
      LOG.debug("Updating all public resources' IPT URL to resource alternate identifier");
    }
    for (Resource resource : resources) {
      resourcePublicationManager.updateAlternateIdentifierForIPTURLToResource(resource);
    }
  }

  // --- the page's facts

  public String getVersion() {
    return cfg.getVersion();
  }

  public boolean isProductionMode() {
    return cfg.getRegistryType() == AppConfig.REGISTRY_TYPE.PRODUCTION;
  }

  public String getRegistryUrl() {
    return cfg.getRegistryUrl();
  }

  public String getDataDir() {
    return cfg.getDataDir().dataFile("").getAbsolutePath();
  }

  public String getLogDir() {
    return cfg.getDataDir().loggingDir().getAbsolutePath();
  }

  /**
   * @return the saved public URL, to tell a changed one apart
   */
  public String getSavedBaseUrl() {
    return cfg.getBaseUrl();
  }

  /**
   * @return the organisation whose DOI account is active (archiving can't be turned off then), or null
   */
  public Organisation getDoiAccountOrganisation() {
    return registrationManager.findPrimaryDoiAgencyAccount();
  }

  public boolean isMailPasswordSaved() {
    return StringUtils.isNotBlank(cfg.getMailSmtpPassword());
  }

  /**
   * @return true if publication failure emails go out with the saved settings
   */
  public boolean isFailureEmailsOn() {
    return PublicationFailureEmailUtils.isConfigured(cfg);
  }

  public Map<String, String> getLanguages() {
    return LANGUAGES;
  }

  // --- the form

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getProxy() {
    return proxy;
  }

  public Boolean getDebug() {
    return debug;
  }

  public String getAnalyticsKey() {
    return analyticsKey;
  }

  public String getAdminEmail() {
    return adminEmail;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public Boolean getArchivalMode() {
    return archivalMode;
  }

  public String getArchivalKeep() {
    return archivalKeep;
  }

  public String getArchivalLimit() {
    return archivalLimit;
  }

  public String getDefaultLocale() {
    return defaultLocale;
  }

  public String getMailSmtpHost() {
    return mailSmtpHost;
  }

  public String getMailSmtpPort() {
    return mailSmtpPort;
  }

  public String getMailSmtpUsername() {
    return mailSmtpUsername;
  }

  public boolean isMailSmtpPasswordRemove() {
    return mailSmtpPasswordRemove;
  }

  public Boolean getMailSmtpStartTlsEnable() {
    return mailSmtpStartTlsEnable;
  }

  @StrutsParameter
  public void setBaseUrl(String baseUrl) {
    // without trailing slashes, as AppConfig returns it: "https://example.org/ipt/" is not a new URL
    this.baseUrl = StringUtils.stripEnd(StringUtils.trimToNull(baseUrl), "/");
  }

  @StrutsParameter
  public void setProxy(String proxy) {
    this.proxy = StringUtils.trimToNull(proxy);
  }

  @StrutsParameter
  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  @StrutsParameter
  public void setAnalyticsKey(String analyticsKey) {
    this.analyticsKey = StringUtils.trimToNull(analyticsKey);
  }

  @StrutsParameter
  public void setAdminEmail(String adminEmail) {
    this.adminEmail = StringUtils.trimToNull(adminEmail);
  }

  @StrutsParameter
  public void setLatitude(String latitude) {
    this.latitude = StringUtils.trimToNull(latitude);
  }

  @StrutsParameter
  public void setLongitude(String longitude) {
    this.longitude = StringUtils.trimToNull(longitude);
  }

  @StrutsParameter
  public void setArchivalMode(Boolean archivalMode) {
    this.archivalMode = archivalMode;
  }

  /**
   * @param archivalKeep "all" or "last" (the number in archivalLimit)
   */
  @StrutsParameter
  public void setArchivalKeep(String archivalKeep) {
    this.archivalKeep = KEEP_LAST.equals(archivalKeep) ? KEEP_LAST : KEEP_ALL;
  }

  @StrutsParameter
  public void setArchivalLimit(String archivalLimit) {
    this.archivalLimit = StringUtils.trimToNull(archivalLimit);
  }

  @StrutsParameter
  public void setDefaultLocale(String defaultLocale) {
    this.defaultLocale = defaultLocale;
  }

  @StrutsParameter
  public void setMailSmtpHost(String mailSmtpHost) {
    this.mailSmtpHost = StringUtils.trimToNull(mailSmtpHost);
  }

  @StrutsParameter
  public void setMailSmtpPort(String mailSmtpPort) {
    this.mailSmtpPort = StringUtils.trimToNull(mailSmtpPort);
  }

  @StrutsParameter
  public void setMailSmtpUsername(String mailSmtpUsername) {
    this.mailSmtpUsername = StringUtils.trimToNull(mailSmtpUsername);
  }

  @StrutsParameter
  public void setMailSmtpPassword(String mailSmtpPassword) {
    this.mailSmtpPassword = StringUtils.trimToNull(mailSmtpPassword);
  }

  @StrutsParameter
  public void setMailSmtpPasswordRemove(boolean mailSmtpPasswordRemove) {
    this.mailSmtpPasswordRemove = mailSmtpPasswordRemove;
  }

  @StrutsParameter
  public void setMailSmtpStartTlsEnable(Boolean mailSmtpStartTlsEnable) {
    this.mailSmtpStartTlsEnable = mailSmtpStartTlsEnable;
  }
}
