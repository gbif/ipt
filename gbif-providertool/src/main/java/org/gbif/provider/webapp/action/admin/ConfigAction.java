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

import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

/**
 * TODO: Documentation.
 * 
 */
public class ConfigAction extends BasePostAction {
  private static final String GOOGLE_MAPS_LOCALHOST_KEY = "ABQIAAAAaLS3GE1JVrq3TRuXuQ68wBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQY-Unm8BwXJu9YioYorDsQkvdK0Q";
  @Autowired
  private GeoserverManager geoManager;

  public AppConfig getConfig() {
    return this.cfg;
  }

  @Override
  public String read() {
    check();
    return SUCCESS;
  }

  @Override
  public String save() {
    if (cancel != null) {
      return "cancel";
    }
    this.cfg.save();
    cfg.reloadLogger();
    saveMessage(getText("config.updated"));
    check();
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

  private void check() {
    // tests
    File f = new File(cfg.getDataDir());
    if (!f.isDirectory() || !f.canWrite()) {
      saveMessage(getText("config.check.iptDataDir"));
    }
    if (StringUtils.trimToNull(cfg.getGeoserverUrl()) == null
        || !cfg.getGeoserverUrl().startsWith("http")) {
      saveMessage(getText("config.check.geoserverUrl"));
    }
    f = new File(cfg.getGeoserverDataDir());
    if (!f.isDirectory() || !f.canWrite()) {
      saveMessage(getText("config.check.geoserverDataDir"));
    }
    if (!geoManager.login(cfg.getGeoserverUser(), cfg.getGeoserverUser(),
        cfg.getGeoserverUrl())) {
      saveMessage(getText("config.check.geoserverLogin"));
    }
    if (StringUtils.trimToNull(cfg.getGoogleMapsApiKey()) == null
        || StringUtils.trimToEmpty(cfg.getGoogleMapsApiKey()).equalsIgnoreCase(
            GOOGLE_MAPS_LOCALHOST_KEY)) {
      saveMessage(getText("config.check.googleMapsApiKey"));
    }
  }
}