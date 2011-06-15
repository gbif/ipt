/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Action responsible for all user input relating to the IPT configuration
 * 
 * @author tim
 */
public class ConfigAction extends POSTAction {
  private static final long serialVersionUID = 4726973323043063968L;
  @Inject
  protected ConfigManager configManager;

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

  public Boolean getAnalyticsGbif() {
    return cfg.isGbifAnalytics();
  }

  public String getAnalyticsKey() {
    return cfg.getAnalyticsKey();
  }

  public String getBaseUrl() {
    return cfg.getBaseURL();
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
   * This is called when the new configuration is submitted
   * 
   * @return SUCCESS if it is valid, or failure with a message if the entered configuration is invalid
   */
  @Override
  public String save() {
    log.info("Changing the IPT configuration");
    boolean baseUrlChanged = false;
    // base URL
    if (!stringEquals(baseUrl, cfg.getBaseURL())) {
      log.info("Changing the installation baseURL from [" + cfg.getBaseURL() + "] to [" + baseUrl + "]");
      try {
        URL burl = new URL(baseUrl);
        configManager.setBaseURL(burl);
        log.info("Installation baseURL successfully changed to[" + baseUrl + "]");
        addActionMessage(getText("admin.config.baseUrl.changed"));
        addActionMessage(getText("admin.user.login"));
        session.remove(Constants.SESSION_USER);
        if (burl.getHost().equalsIgnoreCase("localhost") || burl.getHost().equalsIgnoreCase("127.0.0.1")
            || burl.getHost().equalsIgnoreCase(configManager.getHostName())) {
          addActionWarning(getText("admin.config.error.localhostURL"));
        }
        baseUrlChanged = true;
      } catch (MalformedURLException e) {
        addActionError(getText("admin.config.error.invalidBaseURL"));
        return INPUT;
      } catch (InvalidConfigException e) {
        if (e.getType() == InvalidConfigException.TYPE.INVALID_BASE_URL) {
          addActionError(getText("admin.config.baseUrl.invalidBaseURL"));
        } else if (e.getType() == InvalidConfigException.TYPE.INACCESSIBLE_BASE_URL) {
          addActionError(getText("admin.config.baseUrl.inaccessible"));
        } else {
          addActionError(getText("admin.error.invalidConfiguration", new String[]{e.getMessage()}));
        }
        return INPUT;
      }
    }

    // http proxy

    try {
      configManager.setProxy(proxy);
    } catch (InvalidConfigException e) {
      addActionError(getText("admin.config.error.invalidProxyURL"));
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
      log.error("couldnt write config settings", e);
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

}
