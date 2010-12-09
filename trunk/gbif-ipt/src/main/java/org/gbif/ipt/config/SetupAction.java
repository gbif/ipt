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
import org.gbif.ipt.validation.UserValidator;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * The Action responsible for all user input relating to the IPT configuration
 * 
 * @author tim
 */
public class SetupAction extends BaseAction {
  private static final long serialVersionUID = 4726973323043063968L;
  @Inject
  protected ConfigManager configManager;
  @Inject
  protected UserAccountManager userManager;
  @Inject
  private DataDir dataDir;
  private UserValidator userValidation = new UserValidator();

  // action attributes to be set
  protected String dataDirPath;
  protected User user = new User();
  private String password2;
  protected Boolean production;
  protected String baseURL;
  protected String proxy;
  private boolean setup2 = false;

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

  public String getPassword2() {
    return password2;
  }

  private String getPort() {
    if ("http".equalsIgnoreCase(req.getScheme()) && req.getServerPort() != 80
        || "https".equalsIgnoreCase(req.getScheme()) && req.getServerPort() != 443) {
      return (":" + req.getServerPort());
    } else {
      return "";
    }
  }

  public String getProxy() {
    return proxy;
  }

  public User getUser() {
    return user;
  }

  public Boolean isProduction() {
    return production;
  }

  /**
   * If the config is in debug mode, then production settings are not possible
   * 
   * @return true if production setting is allowed
   */
  public boolean isProductionSettingAllowed() {
    return !cfg.debug();
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
  }

  public void setBaseURL(String baseUrlVerbatim) {
    this.baseURL = baseUrlVerbatim;
  }

  public void setDataDirPath(String dataDirPath) {
    this.dataDirPath = dataDirPath;
  }

  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  public void setProduction(Boolean production) {
    this.production = production;
  }

  public void setProxy(String proxy) {
    this.proxy = proxy;
  }

  public void setSetup2(boolean setup2) {
    this.setup2 = setup2;
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
	      File dd = new File(dataDirPath.trim());
	      try {
	    	  if(dd.isAbsolute()) {
	    		  boolean created = configManager.setDataDir(dd);
	    		  if (created) {
	    			  addActionMessage(getText("admin.config.setup.datadir.created"));
	    		  } else {
	    			  addActionMessage(getText("admin.config.setup.datadir.reused"));
	    		  }
	    	  } else {
	    		  addActionError(getText("admin.config.setup.datadir.absolute", new String[] {dataDirPath}));
	    	  }
	      } catch (InvalidConfigException e) {
	    	  log.warn("Failed to setup datadir: " + e.getMessage(), e);
	    	  if(e.getType() == InvalidConfigException.TYPE.NON_WRITABLE_DATA_DIR) {
	    		  addActionError(getText("admin.config.setup.datadir.writable", new String[] {dataDirPath}));
	    	  } else {
	    		  addActionError(getText("admin.config.setup.datadir.error"));
	    	  }
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
        if (userValidation.validate(this, user)) {
          // confirm password
          userManager.create(user);
          user.setLastLoginToNow();
          userManager.save();
          // login as new admin
          session.put(Constants.SESSION_USER, user);
        }

        // when in dev mode, production is disabled in the form
        if (production == null) {
          production = false;
        }

        // set IPT type: registry URL
        if (production && !cfg.devMode()) {
          cfg.setRegistryType(REGISTRY_TYPE.PRODUCTION);
        } else {
          cfg.setRegistryType(REGISTRY_TYPE.DEVELOPMENT);
        }
        // set baseURL
        try {
          URL burl = new URL(baseURL);
          configManager.setBaseURL(burl);
        } catch (MalformedURLException e) {
          // checked in validate() already
        }
        // set proxy
        try {
          configManager.setProxy(proxy);
        } catch (InvalidConfigException e) {
          addFieldError("proxy", getText("admin.config.proxy.error"));
        }
        // save config
        configManager.saveConfig();
        addActionMessage(getText("admin.config.setup2.success"));
        addActionMessage(getText("admin.config.setup2.next"));
        userManager.setSetupUser(user);
        return SUCCESS;
      } catch (IOException e) {
        log.error(e);
        addActionError(getText("admin.config.setup2.failed", new String[]{e.getMessage()}));
      } catch (AlreadyExistingException e) {
        addFieldError("user.email", "User exists as non admin user already");
      } catch (InvalidConfigException e) {
        log.error(e);
        addActionError(e.getType().toString() + ": " + e.getMessage());
      }
    }
    return INPUT;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public void validate() {
    if (user != null && setup2) {
      // we are in step2
      userValidation.validate(this, user);
      if (StringUtils.trimToNull(user.getPassword()) != null && !user.getPassword().equals(password2)) {
        addFieldError("password2", getText("validation.password2.wrong"));
      }
      if (StringUtils.trimToNull(baseURL) == null) {
        addFieldError("baseURL", getText("validation.baseURL.required"));
      } else {
        try {
          URL burl = new URL(baseURL);
        } catch (MalformedURLException e) {
        }
      }
    }
  }

}
