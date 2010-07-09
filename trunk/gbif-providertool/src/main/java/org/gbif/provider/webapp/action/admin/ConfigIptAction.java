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

import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides action support for creating and updating the GBRDS resource and
 * GBRDS RSS service that is associated with an IPT instance.
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
   * Attempts to create a GBRDS resource that represents this IPT instance. If
   * successful, a new GBRDS RSS service representing this IPT instance is also
   * created.
   */
  public String register() {
    if (!checkOrg()) {
      return SUCCESS;
    }
    if (cfg.isIptRegistered()) {
      saveMessage(getText("register.ipt.already"));
      return SUCCESS;
    }
    String resourceKey = createIptGbifResource();
    if (resourceKey != null) {
      createRssGbifService(resourceKey);
    } else {
      saveMessage("Unable to create GBRDS RSS service");
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
    OrgCredentials creds = getCreds();
    if (creds == null) {
      saveMessage(getText("config.check.orgLogin"));
      result = false;
    } else if (!registryManager.validateCredentials(creds).getResult()) {
      saveMessage(getText("config.check.orgLogin"));
      result = false;
    }
    return result;
  }

  private String createIptGbifResource() {
    OrgCredentials creds = getCreds();
    if (creds == null) {
      saveMessage("Unable to create resource in GBRDS: bad credentials");
      return null;
    }
    ResourceMetadata rm = cfg.getIptResourceMetadata();
    GbrdsResource gr = registryManager.buildGbrdsResource(rm).organisationKey(
        creds.getKey()).build();
    gr = registryManager.createGbrdsResource(gr, creds).getResult();
    if (gr == null) {
      saveMessage(getText("register.ipt.problem"));
      return null;
    }
    saveMessage(getText("register.ipt.success"));
    rm.setUddiID(gr.getKey());
    cfg.save();
    return gr.getKey();
  }

  private void createRssGbifService(String resourceKey) {
    String rssUri = cfg.getAtomFeedURL();
    if (rssUri.contains("localhost")) {
      saveMessage("Unable to register RSS service because the IPT base URL is localhost");
      return;
    }
    OrgCredentials creds = getCreds();
    if (creds == null) {
      saveMessage("Unable to create resource in GBRDS: bad credentials");
      return;
    }
    String type = ServiceType.RSS.name();
    GbrdsService gs = GbrdsService.builder().accessPointURL(rssUri).resourceKey(
        resourceKey).type(type).build();
    if (registryManager.createGbrdsService(gs, creds).getResult() == null) {
      saveMessage("Unable to register RSS service");
    }
  }

  private OrgCredentials getCreds() {
    OrgCredentials creds = null;
    String orgKey = cfg.getOrg().getUddiID();
    String orgPassword = cfg.getOrgPassword();
    if (trimToNull(orgKey) != null && trimToNull(orgPassword) != null) {
      creds = OrgCredentials.with(orgKey, orgPassword);
    }
    return creds;
  }

  private void updateIptGbifResource() {
    OrgCredentials creds = getCreds();
    if (creds == null) {
      saveMessage("Unable to create resource in GBRDS: bad credentials");
      return;
    }
    ResourceMetadata rm = cfg.getIptResourceMetadata();
    GbrdsResource gr = registryManager.buildGbrdsResource(rm).organisationKey(
        creds.getKey()).build();
    if (registryManager.updateGbrdsResource(gr, creds).getResult()) {
      saveMessage(getText("The GBIF Resource associated with this IPT instance has been updated in the GBRDS."));
      cfg.save();
    } else {
      saveMessage(getText("The GBIF Resource associated with this IPT instance could not be updated in the GBRDS."));
    }
  }
}