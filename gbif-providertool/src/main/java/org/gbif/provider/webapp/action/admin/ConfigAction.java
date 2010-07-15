/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.webapp.action.admin;

import com.google.common.collect.ImmutableSet;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;

/**
 * Action support for editing IPT configurations.
 * 
 */
@SuppressWarnings("serial")
public class ConfigAction extends BasePostAction {

  /**
   * This helper class is designed to make this action testable via unit tests.
   * 
   */
  static class Helper {

    private static final String GOOGLE_MAPS_LOCALHOST_KEY = "ABQIAAAAaLS3GE1JVrq3TRuXuQ68wBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQY-Unm8BwXJu9YioYorDsQkvdK0Q";

    static boolean checkDataDir(String dir) {
      if (nullOrEmpty(dir)) {
        return false;
      }
      File f = new File(dir);
      boolean isDir = f.isDirectory();
      boolean canWrite = f.canWrite();
      return isDir && canWrite;
    }

    static boolean checkGeoServerCreds(String user, String pass, String url,
        GeoserverManager m) {
      if (nullOrEmpty(user) || nullOrEmpty(pass) || nullOrEmpty(url)
          || m == null) {
        return false;
      }
      // FIXME: This hangs when GS URL is not localhost:
      // return !m.login(user, pass, url);
      return true;
    }

    static boolean checkMapsKey(String key) {
      return !nullOrEmpty(key)
          && !trimToEmpty(key).equalsIgnoreCase(GOOGLE_MAPS_LOCALHOST_KEY);
    }

    static boolean nullOrEmpty(String val) {
      return val == null || val.trim().length() == 0;
    }

  }

  @Autowired
  @Qualifier("resourceManager")
  protected GenericResourceManager<Resource> resourceManager;

  @Autowired
  private GeoserverManager geoManager;

  @Autowired
  private RegistryManager registry;

  public AppConfig getConfig() {
    return this.cfg;
  }

  @Override
  public String read() {
    ImmutableSet<String> errors = checkForErrors();
    if (!errors.isEmpty()) {
      for (String e : errors) {
        saveMessage(e);
      }
    }
    return SUCCESS;
  }

  @Override
  public String save() {
    if (cancel != null) {
      return "cancel";
    }

    // Checks for errors:
    ImmutableSet<String> errors = checkForErrors();
    if (!errors.isEmpty()) {
      for (String e : errors) {
        saveMessage(e);
      }
      return SUCCESS;
    }

    // Saves app config:
    cfg.save();
    saveMessage("Success: IPT configuration saved");
    cfg.reloadLogger();

    // Updates RSS service URL for the IPT resource if the IPT org exists:
    String orgKey = cfg.getOrg().getUddiID();
    if (registry.orgExists(orgKey)) {
      String orgPass = cfg.getOrgPassword();
      String error = registry.updateIptRssServiceUrl(orgKey, orgPass,
          cfg.getAtomFeedURL());
      if (error != null) {
        saveMessage(error);
      } else {
        saveMessage("Success: Updated the IPT RSS service URL");
      }
    }

    // Updates all service URLs for all resources:
    errors = registry.updateServiceUrls(resourceManager.getAll());
    for (String msg : errors) {
      saveMessage(msg);
    }
    if (errors.isEmpty()) {
      saveMessage("Success: Updated all resource service URLs");
    }

    return SUCCESS;
  }

  public void setConfig(AppConfig cfg) {
    this.cfg = cfg;
  }

  public String updateGeoserver() throws Exception {
    try {
      geoManager.updateCatalog();
      saveMessage(getText("config.geoserverUpdated"));
    } catch (IOException e) {
      saveMessage(getText("config.geoserverNotUpdated"));
    }
    return SUCCESS;
  }

  private ImmutableSet<String> checkForErrors() {
    ImmutableSet.Builder<String> b = ImmutableSet.builder();

    if (registry.isLocalhost(cfg.getBaseUrl())
        || registry.isLocalhost(cfg.getGeoserverUrl())) {
      b.add("Warning: Invalid base URLs for IPT or Geoserver");
    }
    if (!Helper.checkDataDir(cfg.getDataDir())) {
      b.add("Warning: Invalid data directory");
    }
    if (!Helper.checkDataDir(cfg.getGeoserverDataDir())) {
      b.add("Warning: Invalid Geoserver data directory");
    }
    if (!Helper.checkMapsKey(cfg.getGoogleMapsApiKey())) {
      b.add("Warning: Invalid Google Maps API key");
    }
    if (!Helper.checkGeoServerCreds(cfg.getGeoserverUser(),
        cfg.getGeoserverUser(), cfg.getGeoserverUrl(), geoManager)) {
      b.add("Warning: Invalid Geoserver credentials");
    }
    return b.build();
  }
}