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
package org.gbif.ipt.config;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig.REGISTRY_TYPE;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.URLUtils;
import org.gbif.ipt.validation.UserValidator;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the IPT configuration.
 */
public class SetupAction extends BaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(SetupAction.class);

  private static final long serialVersionUID = 4726973323043063968L;

  private final ConfigManager configManager;
  private final AppConfig cfg;
  private final UserAccountManager userManager;
  private final DataDir dataDir;
  private final ExtensionManager extensionManager;
  private final HttpClient client;

  private final UserValidator userValidation = new UserValidator();

  // action attributes to be set
  protected String dataDirPath;
  protected User user = new User();
  private String password2;
  protected String modeSelected;
  protected String baseURL;
  protected String proxy;
  // can't pass a literal boolean to ftl, using int instead...
  protected Integer ignoreUserValidation = 0;
  private boolean setupDefaultAdministrator = false;
  private boolean setupPublicUrl = false;

  private static final String MODE_DEVELOPMENT = "Test";
  private static final String MODE_PRODUCTION = "Production";
  private static final List<String> MODES = Arrays.asList(MODE_DEVELOPMENT, MODE_PRODUCTION);

  @Inject
  public SetupAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager regManager,
                     ConfigManager configManager, UserAccountManager userManager, DataDir dataDir,
                     ExtensionManager extensionManager, HttpClient client) {
    super(textProvider, cfg, regManager);
    this.cfg = cfg;
    this.configManager = configManager;
    this.userManager = userManager;
    this.dataDir = dataDir;
    this.extensionManager = extensionManager;
    this.client = client;
  }

  public List<String> getModes() {
    return MODES;
  }

  public String continueHome() {
    return SUCCESS;
  }

  @Override
  public String getBaseURL() {
    if (StringUtils.isBlank(baseURL)) {
      // try to detect default values if not yet configured
      if (StringUtils.trimToNull(cfg.getBaseUrl()) == null) {
        Enumeration<String> headerNames = req.getHeaderNames();
        if (headerNames != null) {
          LOG.debug("Dumping request headers used to detect initial baseURL");
          while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            LOG.debug("» {}: {}", header, req.getHeader(header));
          }
        }

        // Try to guess the current baseURL on the running server from the context
        baseURL = req.getRequestURL().toString().replaceAll(req.getServletPath(), "");
        if ("https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"))) {
          baseURL = baseURL.replaceFirst("^http://", "https://");
        }

        LOG.info("Auto-Detected IPT BaseURL=" + baseURL);
      } else {
        baseURL = cfg.getBaseUrl();
      }
    }
    return baseURL;
  }

  public String getDataDirPath() {
    return dataDirPath;
  }

  public Integer getIgnoreUserValidation() {
    return this.ignoreUserValidation;
  }

  public String getPassword2() {
    return password2;
  }

  public String getProxy() {
    return proxy;
  }

  public User getUser() {
    return user;
  }

  /**
   * If the config is in debug mode, then production settings are not possible.
   *
   * @return true if production setting is allowed
   */
  public boolean isProductionSettingAllowed() {
    return !cfg.debug();
  }

  public void setBaseURL(String baseUrlVerbatim) {
    this.baseURL = baseUrlVerbatim;
  }

  public void setDataDirPath(String dataDirPath) {
    this.dataDirPath = dataDirPath;
  }

  public void setIgnoreUserValidation(Integer ignoreUserValidation) {
    this.ignoreUserValidation = ignoreUserValidation;
  }

  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  public void setProxy(String proxy) {
    this.proxy = proxy;
  }

  public void setSetupDefaultAdministrator(boolean setupDefaultAdministrator) {
    this.setupDefaultAdministrator = setupDefaultAdministrator;
  }

  public void setSetupPublicUrl(boolean setupPublicUrl) {
    this.setupPublicUrl = setupPublicUrl;
  }

  public String setupDataDirectory() {
    if ((dataDir.dataDir != null && (!dataDir.dataDir.exists() || dataDir.isConfiguredButEmpty()))
        || (isHttpPost() && dataDirPath != null)) {

      LOG.info("Set up data directory {}", dataDir.dataDir);

      File dd = dataDirPath != null ? new File(dataDirPath.trim()) : dataDir.dataDir;
      try {
        if (StringUtils.isEmpty(dataDirPath) && dataDir.dataDir == null) {
          addFieldError("dataDirPath",
              getText("validation.required", new String[] {getText("admin.config.setup.datadir")}));
        } else if (dd.isAbsolute()) {
          boolean created = configManager.setDataDir(dd);
          if (created) {
            addActionMessage(getText("admin.config.setup.datadir.created"));
          } else {
            addActionMessage(getText("admin.config.setup.datadir.reused"));
          }
        } else {
          addFieldError("dataDirPath", getText("admin.config.setup.datadir.absolute"));
        }
      } catch (InvalidConfigException e) {
        LOG.warn("Failed to setup datadir: " + e.getMessage(), e);
        if (e.getType() == InvalidConfigException.TYPE.NON_WRITABLE_DATA_DIR) {
          addActionError(getText("admin.config.setup.datadir.writable", new String[] {dataDirPath}));
        } else {
          addActionError(getText("admin.config.setup.datadir.error"));
        }
      } catch (RegistryException e) {
        String msg = RegistryException.logRegistryException(e, this);
        LOG.warn("Failed to contact the GBIF Registry (" + msg + "): " + e.getMessage(), e);
        addActionError(msg);
      }
    }

    if (dataDir.isConfigured()) {
      // the data dir is already/now configured, skip the first setup step
      LOG.info("Skipping setup data directory step");
      return SUCCESS;
    }

    return INPUT;
  }

  public String setupDefaultAdministrator() {
    // check if everything is already configured
    if (configManager.setupComplete() && configManager.isBaseURLValid()) {
      addActionMessage(getText("admin.config.setup2.existingFound"));
      return SUCCESS;
    }

    // check if a data directory exists
    if (!dataDir.isConfigured()) {
      addActionWarning(getText("admin.config.setup2.datadir.notExist"));
      return ERROR;
    }

    // check if the selected datadir contains an admin user already
    List<User> admins = userManager.list(Role.Admin);
    if (!isHttpPost() && admins != null && !admins.isEmpty()) {
      return SUCCESS;
    }

    // if validation passed - redirect to next step
    if (isHttpPost()) {
      user.setRole(Role.Admin);

      try {
        // now create the user
        if (ignoreUserValidation == 0) {
          // confirm password
          userManager.create(user);
          user.setLastLoginToNow();
          userManager.save();

          userManager.setSetupUser(user);

          // login as new admin
          session.put(Constants.SESSION_USER, user);

          return SUCCESS;
        }
      } catch (IOException e) {
        LOG.error(e);
        addActionError(getText("admin.config.setup2.failed", new String[] {e.getMessage()}));
      } catch (AlreadyExistingException e) {
        addFieldError("user.email", getText("admin.config.setup2.nonadmin"));
      }
    }

    return INPUT;
  }

  public String setupMode() {
    // check if everything is already configured
    if (configManager.setupComplete() && configManager.isBaseURLValid()) {
      return SUCCESS;
    }

    // check if the selected datadir contains an admin user already
    List<User> admins = userManager.list(Role.Admin);
    if (!isHttpPost() && admins.isEmpty()) {
      return ERROR;
    } else if (isHttpPost()) {
      if (getModeSelected() == null) {
        addFieldError("modeSelected", getText("admin.config.setup2.nomode"));
        return INPUT;
      } else {
        // set IPT type: registry URL
        if (getModeSelected().equalsIgnoreCase(MODE_PRODUCTION) && !cfg.devMode()) {
          cfg.setRegistryType(REGISTRY_TYPE.PRODUCTION);
          LOG.info("Production mode has been selected");
        } else {
          cfg.setRegistryType(REGISTRY_TYPE.DEVELOPMENT);
          LOG.info("Test mode has been selected");
        }

        return SUCCESS;
      }
    }

    return INPUT;
  }

  public String setupPublicUrl() {
    // check if everything but public URL is already configured (it must be for this step)
    if (configManager.setupComplete()) {
      if (configManager.isBaseURLValid()) {
        return SUCCESS;
      }
    } else {
      return ERROR;
    }

    // check if mode was set
    REGISTRY_TYPE registryType = cfg.getRegistryType();
    if (registryType == null) {
      return ERROR;
    }

    // form has been submitted
    if (isHttpPost()) {
      try {
        URL burl = null;
        if (ignoreUserValidation == 0) {
          try {
            burl = new URL(baseURL);
          } catch (MalformedURLException e) {
            // checked in validate() already
          }

          // set IPT type: registry URL
          if (registryType.name().equalsIgnoreCase(MODE_PRODUCTION) && !cfg.devMode()) {
            if (URLUtils.isLocalhost(burl)) {
              addFieldError("baseURL", getText("admin.config.baseUrl.invalidBaseURL"));
              // get the correct baseURL from context
              baseURL = req.getRequestURL().toString().replaceAll(req.getServletPath(), "");
              return INPUT;
            } else if (URLUtils.isHostName(burl)) {
              // warn the base URL is same as machine name so user ensures it is visible on the Internet
              LOG.info("Machine name used in base URL");
              addActionWarning(getText("admin.config.baseUrl.sameHostName"));
            }
          }
        }

        // set baseURL, this has to be before the validation with the proxy
        // will try to get local CSS file with this base URL and if it fails throws an InvalidConfigException
        configManager.setBaseUrl(burl);

        // set proxy
        try {
          configManager.setProxy(proxy);
        } catch (InvalidConfigException e) {
          addFieldError("proxy", getText(e.getMessage()) + " " + proxy);
          return INPUT;
        }

        // save config
        configManager.saveConfig();

        addActionMessage(getText("admin.config.setup2.success"));
        addActionMessage(getText("admin.config.setup2.next"));

        return SUCCESS;
      } catch (InvalidConfigException e) {
        if (e.getType() == TYPE.INACCESSIBLE_BASE_URL) {
          addFieldError("baseURL", getText("admin.config.baseUrl.inaccessible") + " " + baseURL);
        } else {
          LOG.error(e);
          addActionError(
              getTextWithDynamicArgs("admin.config.setup2.already.registered", cfg.getRegistryType().toString()));
        }
      } catch (RegistryException e) {
        String msg = RegistryException.logRegistryException(e, this);
        LOG.warn("Failed to contact the GBIF Registry (" + msg + "): " + e.getMessage(), e);
        addActionError(msg);
      }
    }

    return INPUT;
  }

  public String setupInstallationComplete() {
    // check if everything is already configured (it must be for this step)
    if (!configManager.setupComplete() || !configManager.isBaseURLValid()) {
      return ERROR;
    }

    // install or update the latest version of all default vocabularies
    // (Done in loadDataDirConfig method).
    try {
      configManager.loadDataDirConfig();
      session.put(Constants.SESSION_USER, userManager.getSetupUser());
    } catch (InvalidConfigException e) {
      String msg = getText("admin.vocabulary.couldnt.install.defaults", new String[]{e.getMessage()});
      LOG.error(msg, e);
      addActionWarning(msg, e);
    } catch (RegistryException e) {
      String msg = RegistryException.logRegistryException(e, this);
      LOG.warn("Failed to contact the GBIF Registry (" + msg + "): " + e.getMessage(), e);
      addActionError(msg);
      addActionExceptionWarning(e);
    }

    List<Extension> extensions = extensionManager.listCore();
    if (extensions.isEmpty()) {
      try {
        // install core type extensions
        extensionManager.installCoreTypes();
      } catch (InvalidConfigException e) {
        LOG.error(e);
        addActionWarning(getText("admin.extension.couldnt.install.coreTypes"), e);
      }
    }

    // install default organisation "No organisation" used to indicate resource has no publishing organisation
    if (registrationManager.getIpt() == null || getDefaultOrganisation() == null) {
      try {
        registrationManager.addAssociatedOrganisation(createDefaultOrganisation());
        registrationManager.save();
      } catch (Exception e) {
        LOG.error(e);
        addActionWarning(getText("admin.error.invalidConfiguration", new String[]{e.getMessage()}), e);
      }
    }
    return INPUT;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public void validate() {
    if (setupDefaultAdministrator) {
      if (ignoreUserValidation == 0 && user != null) {
        userValidation.validate(this, user);
        if (StringUtils.trimToNull(user.getPassword()) != null && !user.getPassword().equals(password2)) {
          addFieldError("password2", getText("validation.password2.wrong"));
        }
      }
    } else if (setupPublicUrl) {
      if (StringUtils.trimToNull(baseURL) == null) {
        addFieldError("baseURL", getText("validation.baseURL.required"));
      } else if (!URLUtils.isURLValid(baseURL)) {
        addFieldError("baseURL", getText("validation.baseURL.invalid") + " " + baseURL);
      } else {
        try {
          new URL(baseURL);
        } catch (MalformedURLException e) {
          addFieldError("baseURL", getText("validation.baseURL.invalid") + " " + baseURL);
        }
      }

      if (StringUtils.trimToNull(proxy) != null) {
        if (!URLUtils.isURLValid(proxy)) {
          addFieldError("proxy", getText("admin.config.proxy.error") + " " + proxy);
        } else {
          try {
            HttpHost host = URLUtils.getHost(proxy);
            if (!client.verifyHost(host)) {
              addFieldError("proxy", getText("admin.config.error.connectionRefused") + " " + proxy);
            }
          } catch (MalformedURLException e) {
            addFieldError("proxy", getText("admin.config.error.invalidProxyURL") + " " + proxy);
          }
        }
      }
    }
  }

  public String getModeSelected() {
    if (cfg != null && cfg.devMode()) {
      return MODE_DEVELOPMENT;
    }
    return modeSelected;
  }

  /**
   * The mode the IPT will run in: test or production.
   *
   * @param modeSelected mode that has been selected to run the IPT in
   */
  public void setModeSelected(String modeSelected) {
    this.modeSelected = modeSelected;
  }

  /**
   * Construct and return a default organisation. This can be used for example, to populate the IPT with at least
   * one organisation
   *
   * @return default organisation
   */
  private Organisation createDefaultOrganisation() {
    Organisation organisation = new Organisation();
    String name = getText("eml.publishingOrganisation.none");
    organisation.setName(name);
    organisation.setAlias(name);
    organisation.setCanHost(true);
    organisation.setDescription("Installed by default, used to indicate resource is not published by any organisation");
    organisation.setKey(Constants.DEFAULT_ORG_KEY.toString());
    organisation.setPassword("password");
    return organisation;
  }
}
