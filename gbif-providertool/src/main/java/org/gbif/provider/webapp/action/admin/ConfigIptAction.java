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

import org.gbif.provider.model.Organisation;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class ConfigIptAction extends BasePostAction {
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

  /**
   * Registers IPT as a resource with the GBIF registry API.
   * 
   * @return String
   */
  public String register() {
    Organisation iptOrg = Organisation.builder().organisationKey(
        cfg.getIptOrgMetadata().getUddiID()).password(cfg.getIptOrgPassword()).build();
    String iptResourceKey = cfg.getIptResourceMetadata().getUddiID();

    if (registryManager.isResourceRegistered(iptResourceKey)) {
      saveMessage(getText("register.ipt.already"));
    } else if (!registryManager.isOrganisationRegistered(iptOrg)) {
      saveMessage(getText("register.org.missing"));
    } else if (StringUtils.trimToNull(iptOrg.getPassword()) == null) {
      saveMessage(getText("register.org.password.missing"));
    } else {
      // Register IPT as a resource
      try {
        registryManager.registerResource(cfg.getIptResourceMetadata());
        saveMessage(getText("register.ipt.success"));
        this.cfg.save();
      } catch (RegistryException e) {
        saveMessage(getText("register.ipt.problem"));
      }
    }
    return SUCCESS;
  }

  @Override
  public String save() {
    // check if already registered. If yes, also update GBIF registry
    if (cfg.isIptRegistered()) {
      try {
        registryManager.updateIPT();
        saveMessage(getText("registry.updated"));
      } catch (RegistryException e) {
        saveMessage(getText("registry.problem"));
        log.warn(e);
      }
    } else {
      saveMessage(getText("config.updated"));
    }
    this.cfg.save();
    check();
    return SUCCESS;
  }

  public void setConfig(AppConfig cfg) {
    this.cfg = cfg;
  }

  private void check() {
    // tests
    if (StringUtils.trimToNull(cfg.getIptResourceMetadata().getContactEmail()) == null
        || StringUtils.trimToNull(cfg.getIptResourceMetadata().getContactName()) == null) {
      saveMessage(getText("config.check.contact"));
    }
    if (StringUtils.trimToNull(cfg.getIptOrgMetadata().getUddiID()) == null) {
      saveMessage(getText("config.check.orgRegistered"));
    } else if (StringUtils.trimToNull(cfg.getIptOrgPassword()) == null) {
      saveMessage(getText("config.check.orgPassword"));
    } else {
      if (!registryManager.testLogin()) {
        // authorization error
        saveMessage(getText("config.check.orgLogin"));
      }
    }
  }
}