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
import static org.gbif.provider.model.voc.ServiceType.DWC_ARCHIVE;
import static org.gbif.provider.model.voc.ServiceType.EML;
import static org.gbif.provider.model.voc.ServiceType.TAPIR;
import static org.gbif.provider.model.voc.ServiceType.TCS_RDF;
import static org.gbif.provider.model.voc.ServiceType.WFS;
import static org.gbif.provider.model.voc.ServiceType.WMS;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.provider.webapp.action.admin.ConfigAction.Helper.UrlProvider;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
@SuppressWarnings("serial")
public class ConfigAction extends BasePostAction {

  /**
   * This helper class is designed to make this action testable via unit tests.
   * 
   */
  static class Helper {

    /**
     * Interface for classes that provide a service URL given a
     * {@link ServiceType} and {@link Resource}.
     */
    static interface UrlProvider {
      String getUrl(ServiceType type, Resource resource);
    }

    private static final String GOOGLE_MAPS_LOCALHOST_KEY = "ABQIAAAAaLS3GE1JVrq3TRuXuQ68wBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQY-Unm8BwXJu9YioYorDsQkvdK0Q";

    static boolean checkBaseUrl(String url) {
      return !nullOrEmpty(url) && !url.contains("localhost");
    }

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

    static boolean checkGeoServerUrl(String url) {
      return !nullOrEmpty(url) && url.startsWith("http");
    }

    static boolean checkMapsKey(String key) {
      return !nullOrEmpty(key)
          && !trimToEmpty(key).equalsIgnoreCase(GOOGLE_MAPS_LOCALHOST_KEY);
    }

    static OrgCredentials getCreds(String key, String pass) {
      try {
        return OrgCredentials.with(key, pass);
      } catch (Exception e) {
        return null;
      }
    }

    static boolean nullOrEmpty(String val) {
      return val == null || val.trim().length() == 0;
    }

    static UpdateServiceResponse udpateIptRssService(OrgCredentials creds,
        String iptResourceKey, String rssUrl, RegistryManager rm) {
      if (creds == null || rm == null || nullOrEmpty(iptResourceKey)
          || nullOrEmpty(rssUrl)) {
        return null;
      }
      for (GbrdsService s : rm.listGbrdsServices(iptResourceKey).getResult()) {
        ServiceType st = ServiceType.fromCode(s.getType());
        if (st != null && st.equals(ServiceType.RSS)) {
          GbrdsService gs = GbrdsService.builder().key(s.getKey()).resourceKey(
              iptResourceKey).accessPointURL(rssUrl).type(st.getCode()).build();
          try {
            return rm.updateGbrdsService(gs, creds);
          } catch (BadCredentialsException e) {
            return null;
          }
        }
      }
      return null;
    }

    static boolean updateResourceServices(List<Resource> resources,
        UrlProvider up, RegistryManager rm) {
      boolean allServicesUpdated = true;
      OrgCredentials creds;
      String resourceKey;
      for (Resource r : resources) {
        resourceKey = r.getMeta().getUddiID();
        creds = getCreds(r.getOrgUuid(), r.getOrgPassword());
        if (creds == null) {
          continue;
        }
        GbrdsService.Builder builder;
        ServiceType serviceType;
        for (GbrdsService s : rm.listGbrdsServices(resourceKey).getResult()) {
          builder = GbrdsService.builder(s);
          serviceType = ServiceType.fromCode(s.getType());
          if (serviceType == null) {
            allServicesUpdated = false;
            continue;
          }
          switch (serviceType) {
            case EML:
              builder.accessPointURL(up.getUrl(EML, r));
              break;
            case DWC_ARCHIVE:
              builder.accessPointURL(up.getUrl(DWC_ARCHIVE, r));
              break;
            case TAPIR:
              builder.accessPointURL(up.getUrl(TAPIR, r));
              break;
            case WFS:
              builder.accessPointURL(up.getUrl(WFS, r));
              break;
            case WMS:
              builder.accessPointURL(up.getUrl(WMS, r));
              break;
            case TCS_RDF:
              builder.accessPointURL(up.getUrl(TCS_RDF, r));
              break;
          }
          GbrdsService gs = builder.resourceKey(resourceKey).build();
          if (!rm.updateGbrdsService(gs, creds).getResult()) {
            allServicesUpdated = false;
          }
        }
      }
      return allServicesUpdated;
    }
  }

  private static class UrlProviderImpl implements UrlProvider {
    final AppConfig cfg;

    UrlProviderImpl(AppConfig cfg) {
      this.cfg = cfg;
    }

    public String getUrl(ServiceType type, Resource resource) {
      switch (type) {
        case EML:
          return cfg.getEmlUrl(resource.getGuid());
        case DWC_ARCHIVE:
          return cfg.getArchiveUrl(resource.getGuid());
        case TAPIR:
          return cfg.getTapirEndpoint(resource.getId());
        case WFS:
          return cfg.getWfsEndpoint(resource.getId());
        case WMS:
          return cfg.getWmsEndpoint(resource.getId());
        case TCS_RDF:
          return cfg.getArchiveTcsUrl(resource.getGuid());
        default:
          return null;
      }
    }
  }

  private UrlProvider urlProvider = new UrlProviderImpl(cfg);

  @Autowired
  @Qualifier("resourceManager")
  protected GenericResourceManager<Resource> resourceManager;

  @Autowired
  private GeoserverManager geoManager;

  @Autowired
  private RegistryManager registryManager;

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
    cfg.save();
    cfg.reloadLogger();
    saveMessage(getText("config.updated"));
    updateServices();
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
    if (!Helper.checkBaseUrl(cfg.getBaseUrl())) {
      String msg = "Invalid URL: Cannot include localhost";
      saveMessage(getText(msg));
      cfg.setBaseUrl(msg);
    }
    if (!Helper.checkDataDir(cfg.getDataDir())) {
      saveMessage(getText("config.check.iptDataDir"));
    }
    if (!Helper.checkGeoServerUrl(cfg.getGeoserverUrl())) {
      saveMessage(getText("config.check.geoserverUrl"));
    }
    if (!Helper.checkDataDir(cfg.getGeoserverDataDir())) {
      saveMessage(getText("config.check.geoserverDataDir"));
    }
    if (!Helper.checkMapsKey(cfg.getGoogleMapsApiKey())) {
      saveMessage(getText("config.check.googleMapsApiKey"));
    }
    if (!Helper.checkGeoServerCreds(cfg.getGeoserverUser(),
        cfg.getGeoserverUser(), cfg.getGeoserverUrl(), geoManager)) {
      saveMessage(getText("config.check.geoserverLogin"));
    }
  }

  private void updateServices() {
    if (!Helper.checkBaseUrl(cfg.getBaseUrl())) {
      saveMessage("Warning: Unable to update GBRDS services because IPT base URL contains localhost");
      return;
    }
    // Updates the IPT RSS service in the GBRDS:
    OrgCredentials creds;
    creds = Helper.getCreds(cfg.getOrg().getUddiID(), cfg.getOrgPassword());
    String resourceKey = cfg.getIpt().getUddiID();
    String rssUrl = cfg.getAtomFeedURL();
    if (Helper.udpateIptRssService(creds, resourceKey, rssUrl, registryManager).getResult()) {
      saveMessage("Updated IPT RSS service");
    } else {
      saveMessage("Warning: Unable to update IPT RSS service");
    }
    // Updates all service URLs for all IPT resources:
    if (Helper.updateResourceServices(resourceManager.getAll(), urlProvider,
        registryManager)) {
      saveMessage("Updated all service URLs");
    } else {
      saveMessage("Warning: Some service URLs not updated");
    }
  }
}