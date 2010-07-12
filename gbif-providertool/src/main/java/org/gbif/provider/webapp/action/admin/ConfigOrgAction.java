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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;

import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ContactType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action support for editing the GBRDS organisation associated with this IPT
 * instance.
 * 
 */
@SuppressWarnings("serial")
public class ConfigOrgAction extends BasePostAction {

  static class Helper {

    static OrgCredentials createOrg(GbrdsOrganisation org, RegistryManager rm) {
      checkNotNull(org, "Organisation is null");
      checkArgument(validateOrg(org).isEmpty(), "Invalid organisation");
      return rm.createGbrdsOrganisation(org).getResult();
    }

    static OrgCredentials getCreds(String key, String pass) {
      try {
        return OrgCredentials.with(key, pass);
      } catch (Exception e) {
        return null;
      }
    }

    static GbrdsOrganisation getOrg(ResourceMetadata meta) {
      checkNotNull(meta, "Resource metadata is null");
      return GbrdsOrganisation.builder().description(meta.getDescription()).primaryContactEmail(
          meta.getContactEmail()).primaryContactName(meta.getContactName()).homepageURL(
          meta.getLink()).name(meta.getTitle()).key(meta.getUddiID()).build();
    }

    static ResourceMetadata getResourceMetadata(GbrdsOrganisation org) {
      checkNotNull(org, "Organisation is nul");
      ResourceMetadata meta = new ResourceMetadata();
      meta.setDescription(org.getDescription());
      meta.setContactEmail(org.getPrimaryContactEmail());
      meta.setContactName(org.getPrimaryContactName());
      meta.setLink(org.getHomepageURL());
      meta.setTitle(org.getName());
      meta.setUddiID(org.getKey());
      return meta;
    }

    static boolean nullOrEmpty(String val) {
      return val == null || val.trim().length() == 0;
    }

    static boolean orgExists(String orgKey, RegistryManager rm) {
      checkNotNull(rm, "Registry manager is null");
      return !nullOrEmpty(orgKey)
          && rm.readGbrdsOrganisation(orgKey).getResult() != null;
    }

    static boolean resourceExists(String resourceKey, RegistryManager rm) {
      return !nullOrEmpty(resourceKey)
          && rm.readGbrdsResource(resourceKey).getResult() != null;
    }

    static boolean updateOrg(GbrdsOrganisation org, RegistryManager rm) {
      checkNotNull(org, "Organisation is null");
      checkNotNull(rm, "Resource manager is null");
      checkArgument(validateOrg(org).isEmpty(), "Invalid organisation");
      OrgCredentials creds = getCreds(org.getKey(), org.getPassword());
      checkArgument(validateCreds(creds, rm), "Invalid credentials");
      return rm.updateGbrdsOrganisation(org, creds).getResult();
    }

    static boolean validateCreds(OrgCredentials creds, RegistryManager rm) {
      checkNotNull(rm, "Registry manager is null");
      if (creds == null) {
        return false;
      }
      return rm.validateCredentials(creds).getResult();
    }

    static ImmutableSet<String> validateOrg(GbrdsOrganisation org) {
      checkNotNull(org, "Organisation is null");
      ImmutableSet.Builder<String> errors = ImmutableSet.builder();
      if (nullOrEmpty(org.getName())) {
        errors.add("Error: Organisation name required");
      }
      if (nullOrEmpty(org.getPrimaryContactType())) {
        errors.add("Error: Primary contact type required");
      }
      if (nullOrEmpty(org.getPrimaryContactEmail())) {
        errors.add("Error: Primary contact email required");
      }
      if (nullOrEmpty(org.getNodeKey())) {
        errors.add("Error: Node key required");
      }
      return errors.build();
    }
  }

  @Autowired
  private RegistryManager registryManager;

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

  @Override
  public String read() {
    // Notifies the user if the organisation can't be changed:
    String resourceKey = cfg.getIpt().getUddiID();
    if (Helper.resourceExists(resourceKey, registryManager)) {
      saveMessage("Warning: You can edit this organisation but not change it to a different one");
    }
    // Notifies the user if the organisation doesn't exist in GBRDS:
    String orgKey = cfg.getOrg().getUddiID();
    if (!Helper.orgExists(orgKey, registryManager)) {
      // TODO: Should a UI message be surfaced here?
    }
    // Initializes builder with org values stored by AppConfig:
    orgBuilder = GbrdsOrganisation.builder(Helper.getOrg(cfg.getOrg()));
    orgBuilder.password(cfg.getOrgPassword());
    return SUCCESS;
  }

  public String register() {
    orgBuilder.primaryContactType(ContactType.technical.name());
    GbrdsOrganisation go = orgBuilder.build();

    boolean isValidated = true;
    // Checks if the organisation exists in GBRDS:
    String key = go.getKey();
    if (Helper.orgExists(key, registryManager)) {
      saveMessage("Warning: Organisation is already registered in the GBRDS");
      isValidated = false;
    }
    // Checks organisation values:
    ImmutableSet<String> errors = Helper.validateOrg(go);
    if (!errors.isEmpty()) {
      isValidated = false;
      for (String error : errors) {
        saveMessage(error);
      }
    }
    if (!isValidated) {
      return SUCCESS;
    }

    // Creates a new organisation in GBRDS:
    OrgCredentials creds = Helper.createOrg(go, registryManager);
    if (creds != null) {
      orgBuilder.key(creds.getKey());
      orgBuilder.password(creds.getPassword());
      go = orgBuilder.build();
      cfg.setOrg(Helper.getResourceMetadata(go));
      cfg.setOrgPassword(creds.getPassword());
      cfg.save();
      saveMessage("Organisation successfully registered in the GBRDS: "
          + creds.getKey());
    } else {
      saveMessage("Warning: Unable to register organisation in the GBRDS");
    }

    return SUCCESS;
  }

  @Override
  public String save() {
    orgBuilder.primaryContactType(ContactType.technical.name());
    GbrdsOrganisation go = orgBuilder.build();

    // Checks organisation values:
    boolean isValidated = true;
    ImmutableSet<String> errors = Helper.validateOrg(go);
    if (!errors.isEmpty()) {
      isValidated = false;
      for (String error : errors) {
        saveMessage(error);
      }
    }
    // Checks organisation credentials:
    String key = go.getKey();
    String pass = go.getPassword();
    if (!Helper.validateCreds(Helper.getCreds(key, pass), registryManager)) {
      saveMessage(getText("config.check.orgLogin"));
      isValidated = false;
    }
    // Checks if the organisation exists in GBRDS:
    if (!Helper.orgExists(key, registryManager)) {
      saveMessage("Warning: Organisation must be registered before saved");
      isValidated = false;
    }
    if (!isValidated) {
      return SUCCESS;
    }

    // Updates the organisation in GBRDS:
    if (Helper.updateOrg(go, registryManager)) {
      cfg.setOrg(Helper.getResourceMetadata(go));
      cfg.save();
      saveMessage("Organisation saved and the GBRDS updated successfully");
    } else {
      saveMessage("Warning: Organisation not saved and the GBRDS not updated");
    }
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