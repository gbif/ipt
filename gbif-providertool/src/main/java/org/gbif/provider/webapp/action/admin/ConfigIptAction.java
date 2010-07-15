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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.model.voc.ContactType;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides action support for creating and updating the GBRDS resource and the
 * GBRDS RSS service that is associated with an IPT instance.
 * 
 */
@SuppressWarnings("serial")
public class ConfigIptAction extends BasePostAction {
  static boolean nullOrEmpty(String val) {
    return val == null || val.trim().length() == 0;
  }

  static ImmutableSet<String> validateResource(GbrdsResource resource) {
    checkNotNull(resource, "Resource is null");
    ImmutableSet.Builder<String> errors = ImmutableSet.builder();
    if (nullOrEmpty(resource.getName())) {
      errors.add("Error: Resource name required");
    }
    if (nullOrEmpty(resource.getPrimaryContactType())) {
      errors.add("Error: Resource contact type required");
    }
    if (nullOrEmpty(resource.getPrimaryContactEmail())) {
      errors.add("Error: Resource contact email required");
    }
    if (nullOrEmpty(resource.getOrganisationKey())) {
      errors.add("Error: Resource key required");
    }
    return errors.build();
  }

  private GbrdsResource.Builder resourceBuilder = GbrdsResource.builder();

  @Autowired
  private RegistryManager registry;

  public AppConfig getConfig() {
    return this.cfg;
  }

  public String getResourceContactEmail() {
    return resourceBuilder.getPrimaryContactEmail();
  }

  public String getResourceContactName() {
    return resourceBuilder.getPrimaryContactName();
  }

  public String getResourceDescription() {
    return resourceBuilder.getDescription();
  }

  public String getResourceHomepageUrl() {
    return resourceBuilder.getHomepageURL();
  }

  public String getResourceKey() {
    return resourceBuilder.getKey();
  }

  public String getResourceTitle() {
    return resourceBuilder.getName();
  }

  @Override
  public String read() {
    ImmutableSet<String> errors = checkForErrors();
    if (!errors.isEmpty()) {
      for (String e : errors) {
        saveMessage(e);
      }
      return SUCCESS;
    }
    resourceBuilder = registry.getResourceBuilder(cfg.getIpt());
    resourceBuilder.organisationKey(cfg.getOrg().getUddiID());
    resourceBuilder.primaryContactType(ContactType.technical.name());
    return SUCCESS;
  }

  public String register() {
    // Checks for errors:
    ImmutableSet<String> errors = checkForErrors();
    if (!errors.isEmpty()) {
      for (String e : errors) {
        saveMessage(e);
      }
      return SUCCESS;
    }

    // Does nothing if the resource exists in the GBRDS:
    if (registry.resourceExists(resourceBuilder.getKey())) {
      return SUCCESS;
    }

    // Checks credentials:
    resourceBuilder.primaryContactType(ContactType.technical.name());
    resourceBuilder.organisationKey(cfg.getOrg().getUddiID());
    GbrdsResource gr = resourceBuilder.build();
    OrgCredentials creds = getCreds();
    if (creds == null) {
      saveMessage(getText("config.ipt.warning.badCredentials"));
      return SUCCESS;
    }

    // Creates the resource:
    CreateResourceResponse crr = null;
    try {
      crr = registry.createResource(gr, creds);
    } catch (BadCredentialsException e) {
      saveMessage(getText("config.ipt.warning.badCredentials"));
      return SUCCESS;
    }
    if (crr.getStatus() != HttpStatus.SC_CREATED) {
      saveMessage(getText("config.ipt.warning.resourceNotCreated") + " "
          + crr.getStatus());
      return SUCCESS;
    }
    String resourceKey = crr.getResult().getKey();
    saveMessage(getText("config.ipt.resourceCreated") + " " + resourceKey);

    // Saves resource properties to app config:
    gr = resourceBuilder.key(resourceKey).build();
    cfg.setIpt(registry.getMeta(gr));
    cfg.save();

    // Creates the RSS service:
    String rssUrl = cfg.getAtomFeedURL();
    String type = ServiceType.RSS.code;
    GbrdsService gs = GbrdsService.builder().accessPointURL(rssUrl).key(
        resourceKey).type(type).resourceKey(resourceKey).build();
    CreateServiceResponse csr = null;
    try {
      csr = registry.createService(gs, creds);
    } catch (BadCredentialsException e) {
      saveMessage(getText("config.ipt.warning.RSSBadCredentials"));
      return SUCCESS;
    }
    if (csr.getStatus() != HttpStatus.SC_CREATED) {
      saveMessage(getText("config.ipt.warning.RSSnotCreated") + " "
          + csr.getStatus());
      return SUCCESS;
    }
    String serviceKey = csr.getResult().getKey();
    saveMessage(getText("config.ipt.serviceCreated") + " " + serviceKey);
    return SUCCESS;
  }

  @Override
  public String save() {
    resourceBuilder.primaryContactType(ContactType.technical.name());
    resourceBuilder.organisationKey(cfg.getOrg().getUddiID());
    GbrdsResource gr = resourceBuilder.build();

    // Checks if resource is registered:
    if (!registry.resourceExists(gr.getKey())) {
      register();
      return SUCCESS;
    }

    // Checks for errors:
    ImmutableSet<String> errors = checkForErrors();
    if (!errors.isEmpty()) {
      for (String e : errors) {
        saveMessage(e);
      }
      return SUCCESS;
    }

    // Updates resource
    OrgCredentials creds = getCreds();
    if (creds == null) {
      saveMessage(getText("config.ipt.warning.resourceUpdateBadCredentials"));
      return SUCCESS;
    }

    // Updates registry:
    UpdateResourceResponse urr = null;
    try {
      urr = registry.updateResource(gr, creds);
    } catch (BadCredentialsException e) {
      saveMessage(getText("config.ipt.warning.resourceUpdateBadCredentials"));
      return SUCCESS;
    }
    if (urr.getStatus() != HttpStatus.SC_OK) {
      saveMessage(getText("config.ipt.warning.resourceNotUpdated") + " "
          + urr.getStatus());
      return SUCCESS;
    }

    // Updates app config:
    cfg.setIpt(registry.getMeta(gr));
    cfg.save();
    saveMessage(getText("config.ipt.resourceUpdated"));
    return SUCCESS;
  }

  public void setConfig(AppConfig cfg) {
    this.cfg = cfg;
  }

  public void setResourceContactEmail(String val) {
    resourceBuilder.primaryContactEmail(val);
  }

  public void setResourceContactName(String val) {
    resourceBuilder.primaryContactName(val);
  }

  public void setResourceDescription(String val) {
    resourceBuilder.description(val);
  }

  public void setResourceHomepageUrl(String val) {
    resourceBuilder.homepageURL(val);
  }

  public void setResourceKey(String val) {
    resourceBuilder.key(val);
  }

  public void setResourceTitle(String val) {
    resourceBuilder.name(val);
  }

  private ImmutableSet<String> checkForErrors() {
    ImmutableSet.Builder<String> b = ImmutableSet.builder();

    // Checks if the IPT base URL contains localhost:
    if (registry.isLocalhost(cfg.getBaseUrl())) {
      b.add(getText("config.ipt.warning.localhost"));
    }

    // Checks if the IPT organisation exists:
    String key = cfg.getOrg().getUddiID();
    if (registry.orgExists(key)) {
      if (getCreds() == null) {
        b.add(getText("config.ipt.warning.badCredentials"));
      }
    } else {
      b.add(getText("config.ipt.warning.noOrg"));
    }

    return b.build();
  }

  private OrgCredentials getCreds() {
    String key = cfg.getOrg().getUddiID();
    String pass = cfg.getOrgPassword();
    return registry.getCreds(key, pass);
  }

}