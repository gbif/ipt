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

import static org.apache.commons.lang.StringUtils.trimToNull;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.RegistryManager.RegistryException;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.Credentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides action support for creating and updating the GBIF Resource and GBIF
 * Service that is associated with an IPT instance in the GBDRS.
 * 
 */
@SuppressWarnings("serial")
public class ConfigIptAction extends BasePostAction {

  @Autowired
  private RegistryManager registryManager;

  public AppConfig getConfig() {
    return this.cfg;
  }

  @Override
  public String read() {
    checkOrg();
    return SUCCESS;
  }

  /**
   * Attempts to create a GBIF Resource in the GBRDS that represents this IPT
   * instance. If successful, a new GBIF Service representing this IPT instance
   * RSS feed is also created.
   */
  public String register() {
    if (!checkOrg()) {
      return SUCCESS;
    }
    if (cfg.isIptRegistered()) {
      saveMessage(getText("register.ipt.already"));
      return SUCCESS;
    }
    String gbifResourceKey = createIptGbifResource();
    if (gbifResourceKey != null) {
      createRssGbifService(gbifResourceKey);
    }
    return SUCCESS;
  }

  @Override
  public String save() {
    if (checkOrg()) {
      updateIptGbifResource();
    }
    return SUCCESS;
  }

  public void setConfig(AppConfig cfg) {
    this.cfg = cfg;
  }

  private boolean checkOrg() {
    boolean result = true;

    // Checks if the IPT base URL includes localhost:
    if (cfg.getBaseUrl().contains("localhost")) {
      result = false;
      saveMessage("Before creating a new GBIF Resource for this IPT instance, please change the base URL from localhost to your host");
    }

    // Checks if a GBIF Organisation is registered:
    if (!cfg.isOrgRegistered()) {
      result = false;
      saveMessage(getText("register.org.missing"));
    }

    // Checks the GBIF Organisation credentials:
    Credentials creds = getOrgCreds();
    if (creds == null) {
      saveMessage(getText("config.check.orgLogin"));
      result = false;
    } else {
      ValidateOrgCredentialsResponse response;
      response = registryManager.validateGbifOrganisationCredentials(
          creds.getId(), creds);
      if (!response.getResult()) {
        saveMessage(getText("config.check.orgLogin"));
        result = false;
      }
    }
    return result;
  }

  /**
   * Creates a new GBIF Resource in the GBRDS that represents this IPT instance.
   * 
   * If this IPT instance was already created as a GBIF Resource in the GBRDS,
   * the user is notified and SUCCESS is returned.
   * 
   * Otherwise an attempt is made to create a new GBIF Resource in the GBRDS
   * that represents this IPT instance. If the GBIF Organisation associated with
   * this IPT instance was not already created as a GBIF Organisation with the
   * GBRDS, registration fails and the user is notified. Else a new GBIF
   * Resource that represents this IPT instance is created in the GBRDS and the
   * user is notified.
   */
  private String createIptGbifResource() {
    String gbifResourceKey = null;
    try {
      ResourceMetadata rm = cfg.getIptResourceMetadata();
      Credentials creds = getOrgCreds();
      GbrdsResource gm = registryManager.buildGbrdsResource(rm).organisationKey(
          creds.getId()).organisationPassword(creds.getPasswd()).build();
      CreateResourceResponse response = registryManager.createGbrdsResource(gm);
      if (response.getStatus() == HttpStatus.SC_CREATED) {
        saveMessage(getText("register.ipt.success"));
        GbrdsResource r = response.getResult();
        gbifResourceKey = r.getKey();
        rm.setUddiID(gbifResourceKey);
        cfg.save();
      } else {
        saveMessage(getText("register.ipt.problem"));
      }
    } catch (Exception e) {
      e.printStackTrace();
      String msg = String.format("%s: %s", getText("register.ipt.problem"), e);
      saveMessage(msg);
    }
    return gbifResourceKey;
  }

  private void createRssGbifService(String gbifResourceKey) {
    String rssUri = cfg.getAtomFeedURL();
    if (rssUri.contains("localhost")) {
      saveMessage("Unable to register RSS service because the IPT base URL is localhost");
      return;
    }
    String type = ServiceType.RSS.name();
    Credentials creds = getOrgCreds();
    GbrdsService gbifService = GbrdsService.builder().accessPointURL(rssUri).resourceKey(
        gbifResourceKey).type(type).organisationKey(creds.getId()).resourcePassword(
        creds.getPasswd()).build();
    try {
      registryManager.createGbrdsService(gbifService);
    } catch (RegistryException e) {
      e.printStackTrace();
      String msg = String.format("Unable to register RSS service: %s", e);
      saveMessage(msg);
    }
  }

  private Credentials getOrgCreds() {
    Credentials creds = null;
    String orgKey = cfg.getOrg().getUddiID();
    String orgPassword = cfg.getOrgPassword();
    if (trimToNull(orgKey) != null && trimToNull(orgPassword) != null) {
      creds = Credentials.with(orgKey, orgPassword);
    }
    return creds;
  }

  private void updateIptGbifResource() {
    try {
      ResourceMetadata rm = cfg.getIptResourceMetadata();
      Credentials creds = getOrgCreds();
      GbrdsResource gr = registryManager.buildGbrdsResource(rm).organisationKey(
          creds.getId()).organisationPassword(creds.getPasswd()).build();
      UpdateResourceResponse response = registryManager.updateGbrdsResource(gr);
      if (response.getStatus() == HttpStatus.SC_OK) {
        saveMessage(getText("The GBIF Resource associated with this IPT instance has been updated in the GBRDS."));
        cfg.save();
      } else {
        saveMessage(getText("The GBIF Resource associated with this IPT instance could not be updated in the GBRDS."));
      }
    } catch (Exception e) {
      e.printStackTrace();
      String msg = String.format("%s: %s", getText("register.ipt.problem"), e);
      saveMessage(msg);
    }
  }
}