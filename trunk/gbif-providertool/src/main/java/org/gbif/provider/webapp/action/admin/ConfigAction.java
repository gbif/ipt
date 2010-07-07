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

import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.RegistryManager.RegistryException;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;

/**
 * TODO: Documentation.
 * 
 */
@SuppressWarnings("serial")
public class ConfigAction extends BasePostAction {

  private static final String GOOGLE_MAPS_LOCALHOST_KEY = "ABQIAAAAaLS3GE1JVrq3TRuXuQ68wBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQY-Unm8BwXJu9YioYorDsQkvdK0Q";

  @Qualifier("resourceManager")
  protected GenericResourceManager<Resource> resourceManager;

  @Autowired
  private GeoserverManager geoManager;

  @Autowired
  private RegistryManager registryManager;

  private String initialBaseUrl;

  public AppConfig getConfig() {
    return this.cfg;
  }

  @Override
  public String read() {
    initialBaseUrl = cfg.getBaseUrl();
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
    updateServices();
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
    String baseUrl = cfg.getBaseUrl();
    if (baseUrl == null || baseUrl.trim().length() == 0
        || baseUrl.contains("localhost")) {
      saveMessage("Invalid Base URL: When the IPT is registered, or when a "
          + "resource is created in the IPT, all the access points that will be "
          + "registered with GBIF will have a URL that starts with this, so it "
          + "needs to be a publicly visible URL on the internets.");
      cfg.setBaseUrl("Invalid URL: Cannot include localhost");
    }
    File f = new File(cfg.getDataDir());
    if (!f.isDirectory() || !f.canWrite()) {
      saveMessage(getText("config.check.iptDataDir"));
    }
    if (trimToNull(cfg.getGeoserverUrl()) == null
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
    if (trimToNull(cfg.getGoogleMapsApiKey()) == null
        || trimToEmpty(cfg.getGoogleMapsApiKey()).equalsIgnoreCase(
            GOOGLE_MAPS_LOCALHOST_KEY)) {
      saveMessage(getText("config.check.googleMapsApiKey"));
    }
  }

  private void updateServices() {
    String baseUrl = cfg.getBaseUrl();
    if (baseUrl == null || baseUrl.contains("localhost")
        || initialBaseUrl == null) {
      return;
    }
    if (initialBaseUrl.trim().equals(baseUrl.trim())) {
      return;
    }
    String resourceKey, orgKey, orgPasswd;
    for (Resource r : resourceManager.getAll()) {
      resourceKey = r.getMeta().getUddiID();
      orgKey = r.getOrgUuid();
      orgPasswd = r.getOrgPassword();
      if (trimToNull(resourceKey) == null || trimToNull(orgKey) == null
          || trimToNull(orgPasswd) == null) {
        continue;
      }
      GbrdsService.Builder builder;
      try {
        for (GbrdsService s : registryManager.listGbrdsServices(resourceKey).getResult()) {
          builder = GbrdsService.builder(s);
          switch (ServiceType.valueOf(s.getType())) {
            case EML:
              builder.accessPointURL(cfg.getEmlUrl(r.getGuid()));
            case DWC_ARCHIVE:
              builder.accessPointURL(cfg.getArchiveUrl(r.getGuid()));
            case TAPIR:
              builder.accessPointURL(cfg.getTapirEndpoint(r.getId()));
            case WFS:
              builder.accessPointURL(cfg.getWfsEndpoint(r.getId()));
            case WMS:
              builder.accessPointURL(cfg.getWmsEndpoint(r.getId()));
            case TCS_RDF:
              builder.accessPointURL(cfg.getArchiveTcsUrl(r.getGuid()));
            default:
              log.warn("Unsupported service type: " + s.getType());
          }
          GbrdsService gs = builder.resourceKey(resourceKey).organisationKey(
              orgKey).resourcePassword(orgPasswd).build();
          UpdateServiceResponse response = registryManager.updateGbrdsService(gs);
          if (response.getStatus() == HttpStatus.SC_OK) {
            saveMessage(getText("Updated service URL: "
                + gs.getAccessPointURL()));
          } else {
            saveMessage(getText("Unable to update service"));
          }
        }
      } catch (RegistryException e) {
        e.printStackTrace();
        String msg = String.format("Problem updating services: %s", e);
        saveMessage(msg);
      }
    }
  }
}