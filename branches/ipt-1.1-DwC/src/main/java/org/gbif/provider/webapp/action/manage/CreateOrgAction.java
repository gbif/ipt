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
package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.model.Organization;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class CreateOrgAction extends BasePostAction {

  private static final long serialVersionUID = -9042048057208451118L;

  @Autowired
  private RegistryManager registryManager;

  private Organization org = new Organization();

  public AppConfig getConfig() {
    return this.cfg;
  }

  public Organization getOrg() {
    return org;
  }

  public String getRegistryNodeUrl() {
    return AppConfig.getRegistryNodeUrl();
  }

  public String getRegistryOrgUrl() {
    return AppConfig.getRegistryOrgUrl();
  }

  /**
   * Registers an organization with the GBIF Registry if it has not already been
   * registered.
   */
  public String register() {
    if (registryManager.isOrganizationRegistered(org)) {
      saveMessage("The organisation is already registered with GBIF");
    } else {
      // Register a new organization:
      try {
        registryManager.registerOrganization(org);
        saveMessage(getText("register.org.success"));
      } catch (RegistryException e) {
        saveMessage(getText("register.org.problem"));
      }
    }
    return SUCCESS;
  }

  /**
   * Saves changes to an existing organization by updating the GBIF Registry.
   */
  @Override
  public String save() {
    if (registryManager.isOrganizationRegistered(org)) {
      try {
        registryManager.updateIptInstanceOrganization();
        saveMessage(getText("registry.updated"));
      } catch (RegistryException e) {
        saveMessage(getText("registry.problem"));
        log.warn(e);
      }
    } else {
      saveMessage(getText("config.check.orgLogin"));
    }
    return SUCCESS;
  }

  public void setConfig(AppConfig cfg) {
    this.cfg = cfg;
  }

  public void setOrg(Organization org) {
    this.org = org;
  }
}