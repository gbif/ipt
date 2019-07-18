package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.URLUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Action responsible for all user input relating to the IPT configuration.
 */
public class ConfigAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(ConfigAction.class);

  private static final long serialVersionUID = 4726973323043063968L;

  protected ConfigManager configManager;
  private final ResourceManager resourceManager;

  // these are transient properties that are set on a per request basis
  // getters and setters are called by the Struts2 interceptors based on the
  // http request submitted
  protected String baseUrl;
  protected String proxy;
  protected Boolean debug;
  protected Boolean analyticsGbif;
  protected String analyticsKey;
  protected Double latitude;
  protected Double longitude;
  protected Boolean archivalMode;

  @Inject
  public ConfigAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ConfigManager configManager, ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.configManager = configManager;
    this.resourceManager = resourceManager;
  }

  public Boolean getAnalyticsGbif() {
    return cfg.isGbifAnalytics();
  }

  public String getAnalyticsKey() {
    return cfg.getAnalyticsKey();
  }

  public String getBaseUrl() {
    return cfg.getBaseUrl();
  }

  public String getDataDir() {
    return cfg.getDataDir().dataFile("").getAbsolutePath();
  }

  public Boolean getDebug() {
    return cfg.debug();
  }

  public Double getLatitude() {
    return cfg.getLatitude();
  }

  public String getLogDir() {
    return cfg.getDataDir().loggingDir().getAbsolutePath();
  }

  public Double getLongitude() {
    return cfg.getLongitude();
  }

  public String getProxy() {
    return cfg.getProxy();
  }

  public String getRegistryUrl() {
    return cfg.getRegistryUrl();
  }

  /**
   * Check if the IPT is configured to use archival mode.
   * 
   * @return is in archival mode
   */
  public Boolean getArchivalMode() {
    return cfg.isArchivalMode();
  }

  /**
   * This is called when the new configuration is submitted.
   * 
   * @return SUCCESS if it is valid, or failure with a message if the entered configuration is invalid
   */
  @Override
  public String save() {
    LOG.info("Changing the IPT configuration");
    boolean baseUrlChanged = false;
    // base URL
    if (!stringEquals(baseUrl, cfg.getBaseUrl())) {
      LOG.info("Changing the installation baseURL from [" + cfg.getBaseUrl() + "] to [" + baseUrl + "]");
      try {
        URL burl = new URL(baseUrl);
        configManager.setBaseUrl(burl);

        // ensure any public resource URL (alternative identifiers) are updated also
        updateAllAlternateIdentifiersForIPTURLToResource();

        LOG.info("Installation baseURL successfully changed to[" + baseUrl + "]");
        addActionMessage(getText("admin.config.baseUrl.changed"));
        addActionMessage(getText("admin.user.login"));
        addActionMessage(getText("admin.config.baseUrl.changed.reminder"));
        session.remove(Constants.SESSION_USER);
        if (URLUtils.isLocalhost(burl)) {
          addActionWarning(getText("admin.config.error.localhostURL"));
        } else if (URLUtils.isHostName(burl)) {
          // warn the base URL is same as machine name so user checks it is visible on the Internet
          LOG.info("Machine name used in base URL");
          addActionWarning(getText("admin.config.baseUrl.sameHostName"));
        }
        baseUrlChanged = true;
      } catch (MalformedURLException e) {
        addActionError(getText("admin.config.error.invalidBaseURL"));
        return INPUT;
      } catch (InvalidConfigException e) {
        if (e.getType() == InvalidConfigException.TYPE.INVALID_BASE_URL) {
          addActionError(getText("admin.config.baseUrl.invalidBaseURL") + " " + baseUrl);
        } else if (e.getType() == InvalidConfigException.TYPE.INACCESSIBLE_BASE_URL) {
          addActionError(getText("admin.config.baseUrl.inaccessible") + " " + baseUrl);
        } else {
          addActionError(getText("admin.error.invalidConfiguration", new String[] {e.getMessage()}));
        }
        return INPUT;
      }
    }

    // http proxy

    try {
      configManager.setProxy(proxy);
    } catch (InvalidConfigException e) {
      addActionError(getText(e.getMessage()) + " " + proxy);
      return INPUT;
    }

    // ipt debug mode
    if (debug != null) {
      try {
        configManager.setDebugMode(debug);
      } catch (InvalidConfigException e) {
        addActionError(getText("admin.config.debug.error"));
        return INPUT;
      }
    }

    // ipt archival mode
    if (archivalMode != null) {
      try {
        configManager.setArchivalMode(archivalMode);
      } catch (InvalidConfigException e) {
        if (e.getType() == InvalidConfigException.TYPE.DOI_REGISTRATION_ALREADY_ACTIVATED) {
          addActionError(getText("admin.error.invalidConfiguration.doiAccount.activated"));
        } else {
          addActionError(getText("admin.config.archival.error"));
        }
        return INPUT;
      }
    }

    // allow gbif analytics
    if (analyticsGbif != null) {
      try {
        configManager.setGbifAnalytics(analyticsGbif);
      } catch (InvalidConfigException e) {
        addActionError(getText("admin.config.analyticsGbif.error"));
        return INPUT;
      }
    }

    // google analyticsKey
    if (analyticsKey != null) {
      try {
        configManager.setAnalyticsKey(analyticsKey);
      } catch (InvalidConfigException e) {
        addActionError(getText("admin.config.analyticsKey.error"));
        return INPUT;
      }
    }

    // IPT lat/lon
    try {
      configManager.setIptLocation(latitude, longitude);
    } catch (InvalidConfigException e) {
      addActionError(getText("admin.config.server.location.error"));
      return INPUT;
    }

    try {
      configManager.saveConfig();
    } catch (InvalidConfigException e) {
      LOG.error("couldnt write config settings", e);
      addActionError(getText("admin.config.save.error"));
      return INPUT;
    }
    if (baseUrlChanged) {
      return HOME;
    }

    return SUCCESS;
  }

  public void setAnalyticsGbif(Boolean analyticsGbif) {
    this.analyticsGbif = analyticsGbif;
  }

  public void setAnalyticsKey(String analyticsKey) {
    this.analyticsKey = analyticsKey;
  }

  // Getters / Setters follow
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public void setProxy(String proxy) {
    this.proxy = StringUtils.trimToNull(proxy);
  }

  public void setArchivalMode(Boolean archivalMode) {
    this.archivalMode = archivalMode;
  }

  /**
   * Updates all public resource's alternative identifier for the IPT URL to the resource. This identifier should only
   * exist for the resource, if its visibility is public. Any time the baseURL changes, all resources will need this
   * identifier to be updated.
   */
  private void updateAllAlternateIdentifiersForIPTURLToResource() {
    // collect all public resources
    List<Resource> resources = resourceManager.list(PublicationStatus.PUBLIC);
    resources.addAll(resourceManager.list(PublicationStatus.REGISTERED));
    // log
    if (!resources.isEmpty()) {
      LOG.debug("Updating all public resources' IPT URL to resource alternate identifier");
    }
    // update resource IPT URLs
    for (Resource resource : resources) {
      resourceManager.updateAlternateIdentifierForIPTURLToResource(resource);
    }
  }
}
