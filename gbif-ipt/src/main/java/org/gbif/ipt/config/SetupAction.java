/**
 * 
 */
package org.gbif.ipt.config;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig.REGISTRY_TYPE;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.validation.UserSupport;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.util.ServletContextAware;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;

/**
 * The Action responsible for all user input relating to the IPT configuration
 * 
 * @author tim
 */
public class SetupAction extends BaseAction implements ServletContextAware {
  private static final long serialVersionUID = 4726973323043063968L;
  @Inject
  protected ConfigManager configManager;
  @Inject
  protected UserAccountManager userManager;
  private UserSupport userValidation = new UserSupport();

  // action attributes to be set
  protected String dataDirPath;
  protected User user = new User();
  protected Boolean production;
  protected String baseURL;
  private ServletContext ctx;

  /**
   * Tries to guess the current baseURL on the running server from the context
   * 
   * @return baseURL as string
   */
  public String findBaseURL() {
    // try to detect the baseURL if not configured yet!
    String appBase = req.getScheme() + "://" + getHostname() + getPort() + req.getContextPath();
    log.info("Auto-Detected IPT BaseURL=" + appBase);
    return appBase;
  }

  @Override
  public String getBaseURL() {
    // try to detect default values if not yet configured
    if (StringUtils.trimToNull(baseURL) == null) {
      baseURL = findBaseURL();
    }
    return baseURL;
  }

  public String getDataDirPath() {
    return dataDirPath;
  }

  private String getHostname() {
    String host = req.getServerName();
    try {
      InetAddress addr = InetAddress.getLocalHost();
      // Get hostname
      host = addr.getHostName();
    } catch (UnknownHostException e) {
      // stick with localhost
    }
    return host;
  }

  private String getPort() {
    if ("http".equalsIgnoreCase(req.getScheme()) && req.getServerPort() != 80
        || "https".equalsIgnoreCase(req.getScheme()) && req.getServerPort() != 443) {
      return (":" + req.getServerPort());
    } else {
      return "";
    }
  }

  public User getUser() {
    return user;
  }

  public Boolean isProduction() {
    return production;
  }

  public void setBaseURL(String baseUrlVerbatim) {
    this.baseURL = baseUrlVerbatim;
  }

  public void setDataDirPath(String dataDirPath) {
    this.dataDirPath = dataDirPath;
  }

  public void setProduction(Boolean production) {
    this.production = production;
  }

  public void setServletContext(ServletContext context) {
    this.ctx = context;
  }

  /**
   * Method called when setting up the IPT for the very first time. There might not even be a logged in user, be careful
   * to not require an admin!
   * 
   * @return
   * @throws InvalidConfigException
   */
  public String setup() {
    if (isHttpPost() && dataDirPath != null) {
      File dd = new File(dataDirPath);
      try {
        boolean created = configManager.setDataDir(dd);
        if (created) {
          addActionMessage(getText("admin.config.setup.datadir.created"));
        } else {
          addActionMessage(getText("admin.config.setup.datadir.reused"));
        }
      } catch (InvalidConfigException e) {
        log.debug("Failed to setup datadir: " + e.getMessage(), e);
        addActionError(getText("admin.config.setup.datadir.error"));
        addActionError(e.getMessage());
      }
    }
    if (dataDir.isConfigured()) {
      // the data dir is already/now configured, skip the first setup step
      return SUCCESS;
    }
    return INPUT;
  }

  public String setup2() {
    // first check if the selected datadir contains an admin user already
    if (configManager.setupComplete()) {
      addActionMessage(getText("admin.config.setup2.existingFound"));
      return SUCCESS;
    }
    if (isHttpPost()) {
      // we have submitted the form
      try {
        user.setRole(Role.Admin);
        user.setLastLoginToNow();
        userManager.add(user);
        userManager.save();
        // set IPT type: registry URL
        if (production) {
          cfg.setRegistryType(REGISTRY_TYPE.PRODUCTION);
        } else {
          cfg.setRegistryType(REGISTRY_TYPE.DEVELOPMENT);
        }
        // set baseURL
        try {
          URL burl = new URL(baseURL);
          configManager.setBaseURL(burl);
        } catch (MalformedURLException e) {
        }
        // save config
        configManager.saveConfig();
        addActionMessage(getText("admin.config.setup2.success"));
        return SUCCESS;
      } catch (IOException e) {
        addActionError("Failed to setup admin account. Can't write user file: " + e.getMessage());
      } catch (AlreadyExistingException e) {
        addActionError(e.getMessage());
      } catch (InvalidConfigException e) {
        addActionError(e.getMessage());
      }
    }
    return INPUT;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public void validate() {
    if (user != null && production != null) {
      // we are in step2
      userValidation.validate(this, user);
      if (StringUtils.trimToNull(baseURL) == null) {
        addFieldError("baseURL", getText("validation.baseURL.required"));
      } else {
        try {
          URL burl = new URL(baseURL);
        } catch (MalformedURLException e) {
          addFieldError("baseURL", getText("validation.baseURL.invalid"));
        }
      }
    }
  }

}
