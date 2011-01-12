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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.util.XmlFileUtils;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.TemplateException;

/**
 * TODO: Documentation.
 * 
 */
public class GeoserverManagerImpl extends HttpBaseManager implements
    GeoserverManager {
  private static final String FEATURE_TYPE_TEMPLATE = "/WEB-INF/geoserver/featureTypeInfo.ftl";
  private static final String SEED_TEMPLATE = "/WEB-INF/geoserver/seed.ftl";
  private static final String CATALOG_TEMPLATE = "/WEB-INF/geoserver/catalog.ftl";

  public String buildFeatureTypeDescriptor(OccurrenceResource resource) {
    try {
      return FreeMarkerTemplateUtils.processTemplateIntoString(
          freemarker.getTemplate(FEATURE_TYPE_TEMPLATE), resource);
    } catch (IOException e) {
      log.error("Freemarker IO template error", e);
    } catch (TemplateException e) {
      log.error("Freemarker template exception", e);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.impl.GeoserverManager#login(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public boolean login(String username, String password, String geoserverURL) {
    setCredentials(getGeoserverHost(), cfg.getGeoserverUser(),
        cfg.getGeoserverPass());
    NameValuePair[] data = {
        new NameValuePair("username", username),
        new NameValuePair("password", password)};
    String result = executePost(geoserverURL + "/admin/loginSubmit.do", data,
        false);
    return result != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.impl.GeoserverManager#reloadCatalog()
   */
  public void reloadCatalog() throws IOException {
    String username = cfg.getGeoserverUser();
    String password = cfg.getGeoserverPass();
    String geoserverURL = cfg.getGeoserverUrl();

    // LOGIN
    boolean login = login(username, password, geoserverURL);
    if (login) {
      log.debug("login to geoserver succeeded");
    } else {
      log.warn("Failed to log into geoserver");
    }

    // RELOAD
    String result = executeGet(geoserverURL + "/admin/loadFromXML.do", false);
    if (result != null) {
      log.info("Reloaded geoserver catalog");
    } else {
      log.warn("Failed to reload catalog");
    }

    // RELOAD GeoWebCache
    NameValuePair[] data = {new NameValuePair("reload_configuration", "1")};
    executePost(geoserverURL + "/gwc/rest/reload", data, true);
    // do same call again. Sme weird bug in geowebcache requires to try this 2
    // times - also in the web forms!
    result = executePost(geoserverURL + "/gwc/rest/reload", data, true);
    if (result != null) {
      log.info("Reloaded geowebcache");
    } else {
      log.warn("Failed to reload geowebcache");
    }

    // LOGOUT
    result = executeGet(geoserverURL + "/admin/logout.do", false);
    if (result != null) {
      log.debug("logged out");
    } else {
      log.warn("Failed to logout of geoserver");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.impl.GeoserverManager#removeFeatureType(org.gbif
   * .provider.model.OccurrenceResource)
   */
  public void removeFeatureType(OccurrenceResource resource) throws IOException {
    if (cfg.getGeoserverDataDirFile() == null
        || !cfg.getGeoserverDataDirFile().exists()) {
      log.error("Cannot update geoserver configuration. Geoserver datadir not set correctly!");
      throw new IOException("Geoserver datadir configured wrongly");
    }
    File ftDir = new File(cfg.getGeoserverDataDirFile(), String.format(
        "featureTypes/ipt_resource%s", resource.getId()));
    if (ftDir.exists()) {
      FileUtils.deleteDirectory(ftDir);
      log.info("Removed geoserver feature type for resource "
          + resource.getId());
    }
    try {
      this.reloadCatalog();
    } catch (IOException e) {
      log.error("Cannot reload geoserver catalog. Geoserver not running or URL & login credentials not set correctly?");
      throw new IOException("Cannot reload geoserver catalog");
    }
    // remember new feature hashcode
    resource.setFeatureHash(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.impl.GeoserverManager#updateCatalog()
   */
  public void updateCatalog() throws IOException {
    if (cfg.getGeoserverDataDirFile() == null
        || !cfg.getGeoserverDataDirFile().exists()) {
      log.error("Cannot update geoserver configuration. Geoserver datadir not set correctly!");
      throw new IOException("Geoserver datadir configured wrongly");
    }
    // create new catalog file
    try {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("cfg", cfg);
      String catalog = FreeMarkerTemplateUtils.processTemplateIntoString(
          freemarker.getTemplate(CATALOG_TEMPLATE), data);
      log.info("Updating geoserver catalog");
      File fti = new File(cfg.getGeoserverDataDirFile(), "catalog.xml");
      if (fti.exists()) {
        fti.delete();
      }
      fti.createNewFile();
      Writer out = XmlFileUtils.startNewUtf8XmlFile(fti);
      out.write(catalog);
      out.close();
      try {
        this.reloadCatalog();
      } catch (IOException e) {
        log.error("Cannot reload geoserver catalog. Geoserver not running or URL & login credentials not set correctly?");
        throw new IOException("Cannot reload geoserver catalog");
      }
    } catch (IOException e) {
      log.error("Freemarker IO template error", e);
    } catch (TemplateException e) {
      log.error("Freemarker template exception", e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.impl.GeoserverManager#updateFeatureType(org.gbif
   * .provider.model.OccurrenceResource)
   */
  public void updateFeatureType(OccurrenceResource resource) throws IOException {
    // create new featuretype description
    String featureTypeInfo = this.buildFeatureTypeDescriptor(resource);
    // commpare hashcode with previous one
    if (featureTypeInfo != null
        && (resource.getFeatureHash() == null || !resource.getFeatureHash().equals(
            featureTypeInfo.hashCode()))) {
      log.info("Updating geoserver feature type");
      if (cfg.getGeoserverDataDirFile() == null
          || !cfg.getGeoserverDataDirFile().exists()) {
        log.error("Cannot update geoserver configuration. Geoserver datadir not set correctly!");
        throw new IOException("Geoserver datadir configured wrongly");
      }
      File fti = new File(cfg.getGeoserverDataDirFile(), String.format(
          "featureTypes/ipt_resource%s/info.xml", resource.getId()));
      if (fti.exists()) {
        fti.delete();
      } else {
        FileUtils.forceMkdir(fti.getParentFile());
      }
      fti.createNewFile();
      Writer out = XmlFileUtils.startNewUtf8XmlFile(fti);
      out.write(featureTypeInfo);
      out.close();
      try {
        this.reloadCatalog();
      } catch (IOException e) {
        log.error("Cannot reload geoserver catalog. Geoserver not running or URL & login credentials not set correctly?");
        throw new IOException("Cannot reload geoserver catalog");
      }
      // remember new feature hashcode
      resource.setFeatureHash(featureTypeInfo.hashCode());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.impl.GeoserverManager#updateGeowebcache(org.gbif
   * .provider.model.OccurrenceResource)
   */
  public void updateGeowebcache(OccurrenceResource resource) {
    // http://localhost:8081/geoserver/gwc/rest/seed/gbif:resource9
    // http://geoserver.org/display/GEOSDOC/5.+GWC+-+GeoWebCache
    log.debug("Seeding geowebcache for resource " + resource.getId());
    try {
      String seedrequest = FreeMarkerTemplateUtils.processTemplateIntoString(
          freemarker.getTemplate(FEATURE_TYPE_TEMPLATE), resource);
      setCredentials(getGeoserverHost(), cfg.getGeoserverUser(),
          cfg.getGeoserverPass());

      // post seed request, which is an xml doc
      String result = executePost(String.format("%s/gwc/rest/seed/%s.xml",
          cfg.getGeoserverUrl(), resource.getLayerName()), seedrequest,
          "text/xml", true);
      if (result == null) {
        log.warn("Failed to seed geowebcache for resource " + resource.getId());
      }
    } catch (IOException e) {
      log.error("Freemarker IO template error", e);
    } catch (TemplateException e) {
      log.error("Freemarker template exception", e);
    }
  }

  private String getGeoserverHost() {
    try {
      URI geoURI = new URI(cfg.getGeoserverUrl());
      return geoURI.getHost();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }
}
