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
package org.gbif.provider.util;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ProviderCfg;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ProviderCfgManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class AppConfig {
  private static final int DEFAULT_FULL_TEXT_SEARCH_HIT_COUNT = 10;

  // need some static fields to create static methods that can be used outside
  // of spring managed contexts, e.g. for hibernate objects
  // fields are managed by regular instance setters thanks to singleton
  private static String baseURL;

  private static String dataDIR;

  private static File webappDIR;

  private static String registryURL;

  private static String gbifAnalyticsKey;

  public static File getEmlFile(Long resourceId) {
    return getEmlFile(resourceId, 0);
  }

  public static File getEmlFile(Long resourceId, int version) {
    String ver = "";
    if (version > 0) {
      ver = "-" + version;
    }
    File eml = new File(getResourceDataDir(resourceId), String.format(
        "metadata/eml%s.xml", ver));
    return eml;
  }

  public static String getExtensionDefinitionsUrl() {
    return String.format("%s/ipt/extensions.json", registryURL);
  }

  public static String getGbifAnalyticsKey() {
    return gbifAnalyticsKey;
  }

  public static File getMetadataFile(Long resourceId) {
    return getResourceDataFile(resourceId, "metadata/metadata.xml");
  }

  // OTHER UTILITY METHODS, MOSTLY DEFINING PATHS & URLs

  public static String getRegistryNodeUrl() {
    return String.format("%s/node", registryURL);
  }

  // REGISTRY URLS
  public static String getRegistryOrgUrl() {
    return String.format("%s/organisation", registryURL);
  }

  public static String getRegistryResourceUrl() {
    return String.format("%s/resource", registryURL);
  }

  public static String getRegistryServiceUrl() {
    return String.format("%s/service", registryURL);
  }

  public static File getResourceCacheDir(Long resourceId) {
    File dir = new File(getWebappDir(), "cache/" + resourceId.toString());
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  public static File getResourceCacheFile(Long resourceId, String relPath) {
    if (relPath.startsWith("/")) {
      relPath = relPath.substring(1);
    }
    return new File(getResourceCacheDir(resourceId), relPath);
  }

  public static File getResourceDataDir(Long resourceId) {
    if (resourceId == null) {
      throw new NullPointerException(
          "Requires resourceId to find resource data dir");
    }
    File dir = new File(dataDIR, resourceId.toString());
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  public static File getResourceDataFile(Long resourceId, String relPath) {
    if (relPath.startsWith("/")) {
      relPath = relPath.substring(1);
    }
    File f = new File(getResourceDataDir(resourceId), relPath);
    return f;
  }

  public static File getResourceLogoFile(Long resourceId) {
    File file = new File(getResourceDataDir(resourceId), "logo.jpg");
    return file;
  }

  public static File getResourceSourceFile(Long resourceId, String filename) {
    return getResourceDataFile(resourceId,
        String.format("sources/%s", filename));
  }

  public static String getThesaurusDefinitionsUrl() {
    return String.format("%s/ipt/thesauri.json", registryURL);
  }

  // WEBAPP BASICS
  public static File getWebappDir() {
    return webappDIR;
  }

  public static File getWebappFile(String relPath) {
    return new File(webappDIR, relPath);
  }

  // UTILITY METHODS
  private static String trimUrl(String url) {
    url = url.trim();
    while (url.endsWith("/")) {
      url = (String) url.subSequence(0, url.length() - 1);
    }
    return url;
  }

  protected final Log log = LogFactory.getLog(AppConfig.class);

  @Autowired
  private IptNamingStrategy namingStrategy;

  private final ProviderCfgManager providerCfgManager;
  private ProviderCfg cfg;
  private String version;
  private String copyrightYear;
  private int fullTextSearchHitCount = DEFAULT_FULL_TEXT_SEARCH_HIT_COUNT;

  private AppConfig(ProviderCfgManager providerCfgManager, String webappDir,
      String dataDir, String registryUrl, String gbifAnalyticsKey,
      String iptVersion, String copyrightYear, String fullTextSearchHitCount) {
    super();
    AppConfig.dataDIR = dataDir; // new File(dataDir).getAbsolutePath();
    AppConfig.webappDIR = new File(webappDir);
    AppConfig.registryURL = registryUrl;
    AppConfig.gbifAnalyticsKey = gbifAnalyticsKey;
    try {
      this.fullTextSearchHitCount = Integer.parseInt(fullTextSearchHitCount);
    } catch (NumberFormatException e) {
      log.error("Setting full text search hit count to default 10");
    }
    this.version = iptVersion;
    this.copyrightYear = copyrightYear;
    this.providerCfgManager = providerCfgManager;
    cfg = providerCfgManager.load();
    setBaseUrl(cfg.getBaseUrl());
    reloadLogger();
    log.info(String.format(
        "\n--------------------\nIPT_DATA_DIR: %s\nIPT_WEBAPP_DIR: %s\nIPT_BASE_URL: %s\nIPT_GEOSERVER_URL: %s\nIPT_GEOSERVER_DATA_DIR: %s\n--------------------\n",
        dataDIR, webappDIR.getAbsolutePath(), baseURL, cfg.getGeoserverUrl(),
        cfg.getGeoserverDataDir()));
  }

  public File getArchiveDescriptor(Long resourceId) {
    return new File(getResourceDataDir(resourceId), "archive/meta.xml");
  }

  public File getArchiveFile(Long resourceId) {
    return new File(getResourceDataDir(resourceId), "archive-dwc.zip");
  }

  // ARCHIVES
  public File getArchiveFile(Long resourceId, Extension extension)
      throws IOException {
    return new File(getResourceDataDir(resourceId), String.format(
        "archive/%s.txt", extension.getName()));
  }

  public File getArchiveTcsFile(Long resourceId) {
    return new File(getResourceDataDir(resourceId), "archive-tcs.zip");
  }

  public String getArchiveTcsUrl(String guid) {
    return String.format("%sarchive-tcs.zip", getResourceUrl(guid));
  }

  public String getArchiveUrl(String guid) {
    return String.format("%sarchive-dwc.zip", getResourceUrl(guid));
  }

  public String getAtomFeedURL() {
    return String.format("%s/atom.xml", baseURL);
  }

  public String getAtomFeedURL(Integer page) {
    return String.format("%s?page=%s", getAtomFeedURL(), page);
  }

  public String getBaseUrl() {
    return cfg.getBaseUrl();
  }

  public String getCopyrightYear() {
    return copyrightYear;
  }

  // ALL ESSENTIAL DATA DIR
  public String getDataDir() {
    return dataDIR;
  }

  public String getDescriptionImage() {
    return cfg.getDescriptionImage();
  }

  // CORE RECORDS
  public String getDetailUrl(CoreRecord core) {
    return getDetailUrl(core, null);
  }

  public String getDetailUrl(CoreRecord core, String format) {
    ExtensionType type = ExtensionType.byCoreClass(core.getClass());
    if (type == null) {
      throw new IllegalArgumentException("Core record class unknown");
    }
    format = format == null ? "" : "/" + format;
    return String.format("%s/%s/%s%s", getBaseUrl(), type.alias,
        core.getGuid(), format);
  }

  public String getEmlUrl(String guid) {
    return String.format("%seml", getResourceUrl(guid));
  }

  public String getEmlUrl(String guid, int version) {
    return String.format("%sv%s/eml", getResourceUrl(guid), version);
  }

  public int getFullTextSearchHitCount() {
    return fullTextSearchHitCount;
  }

  public String getGeoserverDataDir() {
    // default is geoserver/data in the same webapps folder
    if (StringUtils.trimToNull(cfg.getGeoserverDataDir()) == null) {
      File webappDir = new File(getDataDir()).getParentFile().getParentFile();
      return new File(webappDir, "geoserver/data").getAbsolutePath();
    }
    return cfg.getGeoserverDataDir();
  }

  public File getGeoserverDataDirFile() {
    if (StringUtils.trimToNull(cfg.getGeoserverDataDir()) != null) {
      return new File(cfg.getGeoserverDataDir());
    }
    return null;
  }

  public String getGeoserverPass() {
    return cfg.getGeoserverPass();
  }

  public String getGeoserverUrl() {
    return cfg.getGeoserverUrl();
  }

  public String getGeoserverUser() {
    return cfg.getGeoserverUser();
  }

  public String getGeoserverWebCacheUrl(Long resourceId) {
    return cfg.getGeoserverUrl() + "/gwc/service/gmaps?LAYERS=gbif%3Aresource"
        + resourceId;
  }

  public String getGoogleMapsApiKey() {
    return cfg.getGoogleMapsApiKey();
  }

  public String getHeaderHtml() {
    return cfg.getHeaderHtml();
  }

  // CFG DELEGATE METHODS
  public Long getId() {
    return cfg.getId();
  }

  public ResourceMetadata getIpt() {
    return cfg.getIptMeta();
  }

  public File getLog4jFile() {
    return new File(this.getDataDir(), "logging/" + cfg.getLog4jFilename());
  }

  public String getLog4jFilename() {
    return cfg.getLog4jFilename();
  }

  public ResourceMetadata getOrg() {
    return cfg.getOrgMeta();
  }

  public String getOrgNode() {
    return cfg.getOrgNode();
  }

  public String getOrgPassword() {
    return cfg.getOrgPassword();
  }

  public URL getResourceCacheUrl(Long resourceId, String relPath) {
    if (relPath.startsWith("/")) {
      relPath = relPath.substring(1);
    }
    return getWebappURL(String.format("cache/%s/%s", resourceId, relPath));
  }

  public String getResourceLogoUrl(Long resourceId) {
    return String.format("%slogo.jpg", getResourceUrl(resourceId));
  }

  public URL getResourceUrl(Long resourceId) {
    return getWebappURL(String.format("resource/%s/", resourceId));
  }

  // RESOURCE BASICS
  public URL getResourceUrl(String guid) {
    return getWebappURL(String.format("resource/%s/", guid));
  }

  // SOURCE/UPLOAD FILES
  public File getSourceFile(Long resourceId, String fileName) {
    File f = new File(getResourceDataDir(resourceId), String.format(
        "sources/%s", fileName));
    if (!f.getParentFile().exists()) {
      f.getParentFile().mkdirs();
    }
    return f;
  }

  // SERVICE ENDPOINTS
  public String getTapirEndpoint(Long resourceId) {
    String base = getBaseUrl();
    return String.format("%s/tapir/%s/", base, resourceId.toString());
  }

  public String getVersion() {
    return version;
  }

  public URL getWebappURL(String relPath) {
    if (relPath.startsWith("/")) {
      relPath = relPath.substring(1);
    }
    String url = String.format("%s/%s", baseURL, relPath);
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public String getWfsEndpoint(Long resourceId) {
    return String.format(
        "%s/wfs?request=DescribeFeatureType&typeName=gbif:resource%s",
        getGeoserverUrl(), resourceId);
  }

  public String getWmsEndpoint(Long resourceId) {
    return String.format(
        "%s/wms?request=GetMap&bbox=-180,-90,180,90&layers=gbif:resource%s&width=320&height=160&bgcolor=0x7391AD&Format=image/jpeg",
        getGeoserverUrl(), resourceId);
  }

  public boolean isGbifAnalytics() {
    return cfg.isGbifAnalytics();
  }

  public boolean isIptRegistered() {
    if (StringUtils.trimToNull(cfg.getIptMeta().getUddiID()) == null) {
      return false;
    }
    return true;
  }

  public boolean isOrgRegistered() {
    if (StringUtils.trimToNull(cfg.getOrgMeta().getUddiID()) == null) {
      return false;
    }
    return true;
  }

  // MANAGER "DELEGATE" METHODS
  public void load() {
    cfg = providerCfgManager.load();
  }

  public void reloadLogger() {
    if (getLog4jFile().exists()) {
      LogManager.resetConfiguration();
      IptFileAppender.LOGDIR = getLog4jFile().getParent();
      DOMConfigurator.configure(getLog4jFile().getAbsolutePath());
      log.info("Reloaded log4j settings from "
          + getLog4jFile().getAbsolutePath());
    }
  }

  public void resetIpt() {
    cfg.setIptMeta(new ResourceMetadata());
  }

  public void resetOrg() {
    cfg.setOrgMeta(new ResourceMetadata());
  }

  public void save() {
    providerCfgManager.save(cfg);
  }

  public void setBaseUrl(String baseUrl) {
    cfg.setBaseUrl(trimUrl(baseUrl));
    baseURL = getBaseUrl();
  }

  public void setDescriptionImage(String descriptionImage) {
    cfg.setDescriptionImage(descriptionImage);
  }

  public void setGbifAnalytics(boolean gbifAnalytics) {
    cfg.setGbifAnalytics(gbifAnalytics);
  }

  public void setGeoserverDataDir(String geoserverDataDir) {
    cfg.setGeoserverDataDir(geoserverDataDir);
  }

  public void setGeoserverPass(String geoserverPass) {
    cfg.setGeoserverPass(geoserverPass);
  }

  public void setGeoserverUrl(String geoserverUrl) {
    cfg.setGeoserverUrl(trimUrl(geoserverUrl));
  }

  public void setGeoserverUser(String geoserverUser) {
    cfg.setGeoserverUser(geoserverUser);
  }

  public void setGoogleMapsApiKey(String googleMapsApiKey) {
    cfg.setGoogleMapsApiKey(googleMapsApiKey);
  }

  public void setHeaderHtml(String headerHtml) {
    cfg.setHeaderHtml(headerHtml);
  }

  public void setLog4jFilename(String log4jFile) {
    cfg.setLog4jFilename(log4jFile);
  }

  public void setOrgNode(String orgNode) {
    cfg.setOrgNode(orgNode);
  }

  public void setOrgPassword(String orgPassword) {
    cfg.setOrgPassword(orgPassword);
  }

}
