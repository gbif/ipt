/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;

import com.google.inject.Inject;

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
  protected Boolean debug;
  protected Boolean analyticsGbif;
  protected String analyticsKey;

  public Boolean getAnalyticsGbif() {
    return cfg.isGbifAnalytics();
  }

  public String getAnalyticsKey() {
    return cfg.getAnalyticsKey();
  }

  public String getBaseUrl() {
    return cfg.getBaseURL();
  }

  public Boolean getDebug() {
    return cfg.debug();
  }

  /**
   * This is called when the new configuration is submitted
   * 
   * @return SUCCESS if it is valid, or failure with a message if the entered configuration is invalid
   */
  @Override
  public String save() {
    log.info("Changing the IPT configuration");
    // base URL
    if (!stringEquals(baseUrl, cfg.getBaseURL())) {
      log.info("Changing the installation baseURL from[" + cfg.getBaseURL() + "] to[" + baseUrl + "]");
      try {
        configManager.setBaseURL(new URL(baseUrl));
        log.info("Installation baseURL successfully changed to[" + baseUrl + "]");
        addActionMessage(getText("admin.config.baseUrl.changed"));
      } catch (MalformedURLException e) {
        addActionError(getText("admin.config.error.invalidURL"));
        return INPUT;
      } catch (InvalidConfigException e) {
        if (e.getType() == InvalidConfigException.TYPE.INVALID_BASE_URL) {
          addActionError(getText("admin.config.baseUrl.invalidBaseURL"));
        } else {
          addActionError(getText("admin.error.invalidConfiguration", new String[]{e.getMessage()}));
        }
        return INPUT;
      }
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

    try {
      configManager.saveConfig();
    } catch (InvalidConfigException e) {
      log.error("couldnt write config settings", e);
      addActionError(getText("admin.config.save.error"));
      return INPUT;
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

}
