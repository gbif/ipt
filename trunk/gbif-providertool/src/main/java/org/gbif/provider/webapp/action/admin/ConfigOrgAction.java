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

import com.google.common.collect.ImmutableSet;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.model.voc.ContactType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Action support for editing the GBRDS organisation associated with this IPT
 * instance.
 * 
 */
@SuppressWarnings("serial")
public class ConfigOrgAction extends BasePostAction {

  static ImmutableSet<String> checkOrg(GbrdsOrganisation go) {
    ImmutableSet.Builder<String> b = ImmutableSet.builder();

    // Checks required org properties:
    if (nullOrEmpty(go.getName())) {
      // b.add(getText("config.org.warning.orgName"));
      b.add("Warning: Organisation name required");
    }
    if (nullOrEmpty(go.getPrimaryContactType())) {
      // b.add(getText("config.org.warning.contactType"));
      b.add("Warning: Primary contact type required");
    }
    if (nullOrEmpty(go.getPrimaryContactEmail())) {
      // b.add(getText("config.org.warning.contactEmail"));
      b.add("Warning: Primary contact email required");
    }
    if (nullOrEmpty(go.getNodeKey())) {
      // b.add(getText("config.org.warning.nodeKey"));
      b.add("Warning: Node key required");
    }
    return b.build();
  }

  static boolean nullOrEmpty(String val) {
    return val == null || val.trim().length() == 0;
  }

  @Autowired
  private RegistryManager registry;

  private GbrdsOrganisation.Builder orgBuilder = GbrdsOrganisation.builder();

  public AppConfig getConfig() {
    return this.cfg;
  }

  public String getOrgContactEmail() {
    return orgBuilder.getPrimaryContactEmail();
  }

  public String getOrgContactName() {
    return orgBuilder.getPrimaryContactName();
  }

  public String getOrgDescription() {
    return orgBuilder.getDescription();
  }

  public String getOrgHomepageUrl() {
    return orgBuilder.getHomepageURL();
  }

  public String getOrgKey() {
    return orgBuilder.getKey();
  }

  public String getOrgNode() {
    return orgBuilder.getNodeKey();
  }

  public String getOrgNodeName() {
    return orgBuilder.getNodeName();
  }

  public String getOrgPassword() {
    return orgBuilder.getPassword();
  }

  public String getOrgTitle() {
    return orgBuilder.getName();
  }

  public String getRegistryNodeUrl() {
    return AppConfig.getRegistryNodeUrl();
  }

  public String getRegistryOrgUrl() {
    return AppConfig.getRegistryOrgUrl();
  }

  public boolean isOrgRegistered() {
    return registry.orgExists(orgBuilder.getKey());
  }

  @Override
  public String read() {
    // Notifies the user if the organisation can't be changed:
    String resourceKey = cfg.getIpt().getUddiID();
    if (registry.resourceExists(resourceKey)) {
      saveMessage(getText("config.org.warning.noNewOrg"));
    }
    // Notifies the user if the organisation doesn't exist in GBRDS:
    String orgKey = cfg.getOrg().getUddiID();
    if (!registry.orgExists(orgKey)) {
      saveMessage(getText("config.org.warning.noOrg"));
    }
    // Initializes builder with org values stored by AppConfig:
    orgBuilder = registry.getOrgBuilder(cfg.getOrg());
    orgBuilder.password(cfg.getOrgPassword());
    orgBuilder.primaryContactType(ContactType.technical.name());
    return SUCCESS;
  }

  public String register() {
    orgBuilder.primaryContactType(ContactType.technical.name());
    GbrdsOrganisation go = orgBuilder.build();

    // Checks org before registering:
    Set<String> errors = checkOrg(go);
    if (!errors.isEmpty()) {
      for (String e : errors) {
        saveMessage(e);
      }
      return SUCCESS;
    }

    // Checks if organisation already exists:
    String key = go.getKey();
    if (registry.orgExists(key)) {
      saveMessage(getText("config.org.warning.orgRegistered"));
      return SUCCESS;
    }

    // Creates new org:
    CreateOrgResponse cor = registry.createOrg(go);
    if (cor.getStatus() != HttpStatus.SC_CREATED) {
      saveMessage(getText("config.org.warning.orgNotCreated") + " "
          + cor.getStatus());
      return SUCCESS;
    }

    // Verifies new org credentials:
    OrgCredentials creds = cor.getResult();
    if (creds == null) {
      saveMessage(getText("config.org.warning.badOrgCredentials"));
      return SUCCESS;
    }

    // Updates application state:
    orgBuilder.key(creds.getKey());
    orgBuilder.password(creds.getPassword());
    go = orgBuilder.build();
    cfg.setOrg(registry.getMeta(go));
    cfg.setOrgPassword(creds.getPassword());
    cfg.save();
    saveMessage(getText("config.org.registered") + " " + creds.getKey());
    return SUCCESS;
  }

  @Override
  public String save() {
    orgBuilder.primaryContactType(ContactType.technical.name());
    GbrdsOrganisation go = orgBuilder.build();

    // Checks org properties:
    Set<String> errors = checkOrg(go);
    if (!errors.isEmpty()) {
      for (String e : errors) {
        saveMessage(e);
      }
      return SUCCESS;
    }

    // Checks organisation credentials:
    String key = go.getKey();
    String pass = go.getPassword();
    OrgCredentials creds = registry.getCreds(key, pass);
    if (creds == null) {
      saveMessage(getText("config.org.warning.badOrgCredentials"));
      return SUCCESS;
    }

    // Checks if the organisation exists in GBRDS and registers it if not:
    if (!registry.orgExists(key)) {
      register();
      return SUCCESS;
    }

    // Updates the organisation:
    UpdateOrgResponse uor = null;
    try {
      uor = registry.updateOrg(go, creds);
    } catch (BadCredentialsException e) {
      saveMessage(getText("config.org.warning.badOrgCredentials"));
      return SUCCESS;
    }
    if (uor.getStatus() != HttpStatus.SC_OK) {
      saveMessage(getText("config.org.warning.orgNotUpdated") + " "
          + uor.getStatus());
      // TODO: Return here?
    }
    cfg.setOrg(registry.getMeta(go));
    cfg.setOrgPassword(creds.getPassword());
    cfg.save();
    saveMessage("Organisation saved and the GBRDS updated successfully");
    return SUCCESS;
  }

  public void setConfig(AppConfig cfg) {
    this.cfg = cfg;
  }

  public void setOrgContactEmail(String val) {
    orgBuilder.primaryContactEmail(val);
  }

  public void setOrgContactName(String val) {
    orgBuilder.primaryContactName(val);
  }

  public void setOrgDescription(String val) {
    orgBuilder.description(val);
  }

  public void setOrgHomepageUrl(String val) {
    orgBuilder.homepageURL(val);
  }

  public void setOrgKey(String val) {
    orgBuilder.key(val);
  }

  public void setOrgNode(String val) {
    orgBuilder.nodeKey(val);
  }

  public void setOrgNodeName(String val) {
    orgBuilder.nodeName(val);
  }

  public void setOrgPassword(String val) {
    orgBuilder.password(val);
  }

  public void setOrgTitle(String val) {
    orgBuilder.name(val);
  }
}