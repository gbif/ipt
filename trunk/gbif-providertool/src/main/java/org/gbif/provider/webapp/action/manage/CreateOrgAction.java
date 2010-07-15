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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.Organisation;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This Action class is used to create a new GBIF Organisation in the GBRDS.
 * 
 */
public class CreateOrgAction extends BasePostAction {

  private static final long serialVersionUID = -9042048057208451118L;

  @Autowired
  private RegistryManager registryManager;

  private Organisation org = new Organisation();

  public AppConfig getConfig() {
    return this.cfg;
  }

  public Organisation getOrg() {
    return org;
  }

  public String getRegistryNodeUrl() {
    return AppConfig.getRegistryNodeUrl();
  }

  public String getRegistryOrgUrl() {
    return AppConfig.getRegistryOrgUrl();
  }

  /**
   * Registers an organisation with the GBIF Registry if it has not already been
   * registered.
   */
  public String register() {
    String orgKey = org.getOrganisationKey();
    if (organisationExists(orgKey)) {
      saveMessage("The organisation is already registered with GBIF");
    }
    GbrdsOrganisation go = getGbifOrganisation(org);
    CreateOrgResponse response = registryManager.createOrg(go);
    if (response.getStatus() == HttpStatus.SC_CREATED) {
      saveMessage(getText("register.org.success"));
    } else {
      saveMessage(getText("register.org.problem"));
    }
    return SUCCESS;
  }

  /**
   * Saves changes to an existing organisation by updating the GBIF Registry.
   */
  @Override
  public String save() {
    String orgKey = org.getOrganisationKey();
    if (StringUtils.trimToNull(orgKey) == null) {
      saveMessage("Unable to save organisation because key is invalid");
      return SUCCESS;
    }
    OrgCredentials creds;
    try {
      String key = org.getOrganisationKey();
      String password = org.getPassword();
      creds = OrgCredentials.with(key, password);
    } catch (Exception e) {
      saveMessage("Unable to save organisation because of bad credentials");
      return SUCCESS;
    }
    if (organisationExists(orgKey)) {
      GbrdsOrganisation go = getGbifOrganisation(org);
      try {
        if (registryManager.updateOrg(go, creds).getResult()) {
          saveMessage(getText("registry.updated"));
        } else {
          saveMessage(getText("registry.problem"));
        }
      } catch (BadCredentialsException e) {
        saveMessage(getText("registry.problem"));
      }
    }
    return SUCCESS;
  }

  public void setConfig(AppConfig cfg) {
    this.cfg = cfg;
  }

  public void setOrg(Organisation org) {
    this.org = org;
  }

  /**
   * @param org2
   * @return ResourceMetadata
   */
  private GbrdsOrganisation getGbifOrganisation(Organisation org) {
    GbrdsOrganisation go = GbrdsOrganisation.builder().description(
        org.getDescription()).descriptionLanguage(org.getDescriptionLanguage()).homepageURL(
        org.getHomepageUrl()).key(org.getOrganisationKey()).name(org.getName()).nameLanguage(
        org.getNameLanguage()).nodeContactEmail(org.getPrimaryContactEmail()).nodeName(
        org.getNodeName()).nodeKey(org.getNodeKey()).password(org.getPassword()).primaryContactAddress(
        org.getPrimaryContactAddress()).primaryContactEmail(
        org.getPrimaryContactEmail()).primaryContactType(
        org.getPrimaryContactType()).build();
    return go;
  }

  private boolean organisationExists(String orgKey) {
    return registryManager.readOrg(orgKey).getResult() != null;
  }
}