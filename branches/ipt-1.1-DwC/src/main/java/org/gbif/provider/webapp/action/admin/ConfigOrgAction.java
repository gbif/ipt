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
public class ConfigOrgAction extends BasePostAction {
  @Autowired
  private RegistryManager registryManager;
  private String organisationKey;

  public AppConfig getConfig() {
    return this.cfg;
  }

  public String getRegistryNodeUrl() {
    return AppConfig.getRegistryNodeUrl();
  }

  public String getRegistryOrgUrl() {
    return AppConfig.getRegistryOrgUrl();
  }

  @Override
  public String read() {
    check();
    if (cfg.isIptRegistered()) {
      saveMessage("The IPT is already registered with this organisation. You can only update its metadata, not switch to another organisation");
    }
    return SUCCESS;
  }

  public String register() {
    if (cfg.isOrgRegistered()) {
      saveMessage("The organisation is already registered with GBIF");
    } else {
      // register new organisation
      try {
        registryManager.registerIptInstanceOrganization();
        saveMessage(getText("register.org.success"));
        this.cfg.save();
      } catch (RegistryException e) {
        // cfg.resetOrg();
        cfg.setOrgNode(null);
        saveMessage(getText("register.org.problem"));
      }
    }
    return SUCCESS;
  }

  @Override
  public String save() {
    // cannot change the organisation once an IPT has been registered. So test!
    if (!cfg.isIptRegistered()) {
      cfg.getOrg().setUddiID(organisationKey);
    }
    // check if already registered. If yes, also update GBIF registry
    if (cfg.isOrgRegistered()) {
      try {
        registryManager.updateIptInstanceOrganization();
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

  public void setOrganisationKey(String organisationKey) {
    this.organisationKey = !check() ? null
        : StringUtils.trimToNull(organisationKey);
  }

  private boolean check() {
    boolean isValid = true;
    if (!cfg.isOrgRegistered()) {
      saveMessage(getText("config.check.orgRegistered"));
      isValid = false;
    } else if (StringUtils.trimToNull(cfg.getOrgPassword()) == null) {
      saveMessage(getText("config.check.orgPassword"));
      isValid = false;
    } else {
      if (!registryManager.testLogin()) {
        // authorization error
        saveMessage(getText("config.check.orgLogin"));
        isValid = false;
      }
    }
    return isValid;
  }
}