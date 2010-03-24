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
package org.gbif.provider.model;

import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class ProviderCfg {
  private Long id;
  private ResourceMetadata org = new ResourceMetadata();
  private String orgPassword;
  private String orgNode;
  private ResourceMetadata ipt = new ResourceMetadata();
  private String descriptionImage;
  private String baseUrl;
  private String googleMapsApiKey;
  private String geoserverUrl;
  private String geoserverDataDir;
  private String geoserverUser;
  private String geoserverPass;
  private String log4jFile;
  private String headerHtml;
  private boolean gbifAnalytics = true;

  @Column(length = 128)
  public String getBaseUrl() {
    return baseUrl;
  }

  public String getDescriptionImage() {
    return descriptionImage;
  }

  @Column(length = 128)
  public String getGeoserverDataDir() {
    return geoserverDataDir;
  }

  @Column(length = 64)
  public String getGeoserverPass() {
    return geoserverPass;
  }

  @Column(length = 128)
  public String getGeoserverUrl() {
    return geoserverUrl;
  }

  @Column(length = 64)
  public String getGeoserverUser() {
    return geoserverUser;
  }

  @Column(length = 128)
  public String getGoogleMapsApiKey() {
    return googleMapsApiKey;
  }

  @Lob
  public String getHeaderHtml() {
    return headerHtml;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  public ResourceMetadata getIptMeta() {
    if (ipt == null) {
      ipt = new ResourceMetadata();
    }
    return ipt;
  }

  @Column(length = 64)
  public String getLog4jFilename() {
    return log4jFile;
  }

  public ResourceMetadata getOrgMeta() {
    if (org == null) {
      org = new ResourceMetadata();
    }
    return org;
  }

  @Column(length = 128)
  public String getOrgNode() {
    return orgNode;
  }

  @Column(length = 128)
  public String getOrgPassword() {
    return orgPassword;
  }

  public boolean isGbifAnalytics() {
    return gbifAnalytics;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setDescriptionImage(String descriptionImage) {
    this.descriptionImage = descriptionImage;
  }

  public void setGbifAnalytics(boolean gbifAnalytics) {
    this.gbifAnalytics = gbifAnalytics;
  }

  public void setGeoserverDataDir(String geoserverDataDir) {
    this.geoserverDataDir = geoserverDataDir;
  }

  public void setGeoserverPass(String geoserverPass) {
    this.geoserverPass = geoserverPass;
  }

  public void setGeoserverUrl(String geoserverUrl) {
    this.geoserverUrl = geoserverUrl;
  }

  public void setGeoserverUser(String geoserverUser) {
    this.geoserverUser = geoserverUser;
  }

  public void setGoogleMapsApiKey(String googleMapsApiKey) {
    googleMapsApiKey = StringUtils.trimToNull(googleMapsApiKey);
    this.googleMapsApiKey = googleMapsApiKey;
  }

  public void setHeaderHtml(String headerHtml) {
    this.headerHtml = StringUtils.trimToEmpty(headerHtml);
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setIptMeta(ResourceMetadata meta) {
    this.ipt = meta;
  }

  public void setLog4jFilename(String log4jFile) {
    this.log4jFile = log4jFile;
  }

  public void setOrgMeta(ResourceMetadata meta) {
    this.org = meta;
  }

  public void setOrgNode(String orgNode) {
    this.orgNode = orgNode;
  }

  public void setOrgPassword(String orgPassword) {
    this.orgPassword = orgPassword;
  }

}
