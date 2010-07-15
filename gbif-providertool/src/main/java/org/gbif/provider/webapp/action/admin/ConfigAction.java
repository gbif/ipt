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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.ListServicesResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    static String updateIptRssService(String key, String password,
        String rssUrl, RegistryManager registry) {
      // Checks if the resource exists:
      if (!registry.resourceExists(key)) {
        return null;
      }

      // Checks credentials:
      OrgCredentials creds;
      creds = OrgCredentials.with(key, password);
      if (creds == null) {
        return "Warning: Unable to update RSS service because of invalid credentials";
      }

      // Checks for localhost in the RSS URL:
      if (registry.isLocalhost(rssUrl)) {
        return "Warning: Unable to update RSS service because of invalid base URL";
      }

      // Looks for the RSS service in the GBRDS:
      ListServicesResponse lsr = registry.listServices(key);
      if (lsr.getStatus() != HttpStatus.SC_OK) {
        return "Warning: Unable to get services";
      }
      List<GbrdsService> results = lsr.getResult();
      GbrdsService rss = null;
      for (GbrdsService s : results) {
        if (ServiceType.fromCode(s.getType()) == ServiceType.RSS) {
          rss = s;
        }
      }
      if (rss == null) {
        return "Warning: An RSS service does not exist for the IPT";
      }

      // Updates the service URL:
      rss = GbrdsService.builder(rss).accessPointURL(rssUrl).build();
      UpdateServiceResponse usr = null;
      try {
        usr = registry.updateService(rss, creds);
      } catch (BadCredentialsException e) {
        return "Warning: Unable to update RSS service because of bad credentials: "
            + creds;
      }
      if (usr.getStatus() != HttpStatus.SC_OK) {
        return "Warning: Updating RSS service returned HTTP status "
            + usr.getStatus();
      }

      return null;
    }

    static ImmutableSet<String> updateServices(List<Resource> resources,
        RegistryManager registry) {
      checkNotNull(resources, "Resource list is null");
      checkNotNull(registry, "Registry is null");
      ImmutableSet.Builder<String> errors = ImmutableSet.builder();

      // Updates all service URLs for all IPT resources:
      for (Resource r : resources) {
        // Checks if a GBRDS resource exists for current IPT resource:
        String resourceKey = r.getMeta().getUddiID();
        if (!registry.resourceExists(resourceKey)) {
          errors.add("Warning: No GBRDS resource for IPT resource " + r.getId());
          continue;
        }

        // Checks credentials:
        String key = r.getOrgUuid();
        String password = r.getOrgPassword();
        OrgCredentials creds = OrgCredentials.with(key, password);
        if (creds == null) {
          errors.add("Warning: Invalid credentials: " + creds);
          continue;
        }

        // Gets services from GBRDS:
        ListServicesResponse lsr = registry.listServices(resourceKey);
        if (lsr.getStatus() != HttpStatus.SC_OK) {
          errors.add("Warninig: Unable to list services for " + resourceKey);
          continue;
        }

        // Updates each service URL:
        List<GbrdsService> services = lsr.getResult();
        for (GbrdsService s : services) {
          ServiceType type = ServiceType.fromCode(s.getType());
          String url = registry.getServiceUrl(type, r);
          GbrdsService gs = GbrdsService.builder(s).accessPointURL(url).build();
          try {
            UpdateServiceResponse usr = registry.updateService(gs, creds);
            if (usr.getStatus() != HttpStatus.SC_OK) {
              errors.add("Warning: Unable to update service " + gs.getKey());
            }
          } catch (BadCredentialsException e) {
            continue;
          }
        }
      }

      return errors.build();
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

    // Updates RSS service URL for the IPT resource:
    String error = Helper.updateIptRssService(cfg.getOrg().getUddiID(),
        cfg.getOrgPassword(), cfg.getAtomFeedURL(), registry);
    if (error != null) {
      saveMessage(error);
    } else {
      saveMessage("Success: Updated the IPT RSS service URL");
    }

    // Updates service URLs for all services:
    errors = Helper.updateServices(resourceManager.getAll(), registry);
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