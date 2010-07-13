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

  static class Helper {

    static boolean checkLocalhostUrl(String url) {
      return !nullOrEmpty(url) && !url.contains("localhost")
          && !url.contains("127.0.0.1");
    }

    static GbrdsResource createResource(GbrdsResource resource,
        OrgCredentials creds, RegistryManager rm) {
      checkNotNull(resource, "Resource is null");
      checkNotNull(creds, "Organisation credentials are null");
      checkNotNull(rm, "Resource manager is null");
      checkArgument(validateResource(resource).isEmpty(), "Invalid resource");
      checkArgument(validateCreds(creds, rm), "Bad credentials");
      return rm.createGbrdsResource(resource, creds).getResult();
    }

    static GbrdsService createRssService(String resourceKey,
        OrgCredentials creds, String rssUri, RegistryManager rm) {
      checkArgument(!nullOrEmpty(resourceKey), "Invalid resource key");
      checkArgument(resourceExists(resourceKey, rm),
          "GBRDS resource does not exist");
      checkNotNull(creds, "Credentials are null");
      checkArgument(!nullOrEmpty(rssUri), "Invalid RSS URL");
      checkNotNull(rm, "Registry manager is null");
      checkArgument(validateCreds(creds, rm), "Invalid credentials");
      String type = ServiceType.RSS.name();
      GbrdsService gs = GbrdsService.builder().accessPointURL(rssUri).resourceKey(
          resourceKey).type(type).build();
      return rm.createGbrdsService(gs, creds).getResult();
    }

    static OrgCredentials getCreds(String key, String pass) {
      try {
        return OrgCredentials.with(key, pass);
      } catch (Exception e) {
        return null;
      }
    }

    static GbrdsResource.Builder getResourceBuilder(ResourceMetadata meta) {
      checkNotNull(meta, "Resource metadata is null");
      return GbrdsResource.builder().description(meta.getDescription()).primaryContactEmail(
          meta.getContactEmail()).primaryContactName(meta.getContactName()).homepageURL(
          meta.getLink()).name(meta.getTitle()).key(meta.getUddiID());
    }

    static ResourceMetadata getResourceMetadata(GbrdsResource resource) {
      checkNotNull(resource, "Organisation is nul");
      ResourceMetadata meta = new ResourceMetadata();
      meta.setDescription(resource.getDescription());
      meta.setContactEmail(resource.getPrimaryContactEmail());
      meta.setContactName(resource.getPrimaryContactName());
      meta.setLink(resource.getHomepageURL());
      meta.setTitle(resource.getName());
      meta.setUddiID(resource.getKey());
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

    static boolean updateResource(GbrdsResource resource, OrgCredentials creds,
        RegistryManager rm) {
      checkNotNull(resource, "Resource is null");
      checkNotNull(rm, "Resource manager is null");
      checkArgument(validateResource(resource).isEmpty(), "Invalid resource");
      checkArgument(validateCreds(creds, rm), "Invalid credentials");
      return rm.updateGbrdsResource(resource, creds).getResult();
    }

    static boolean validateCreds(OrgCredentials creds, RegistryManager rm) {
      checkNotNull(rm, "Registry manager is null");
      if (creds == null) {
        return false;
      }
      return rm.validateCredentials(creds).getResult();
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
  }

  private GbrdsResource.Builder resourceBuilder = GbrdsResource.builder();

  @Autowired
  private RegistryManager registryManager;

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
    if (problemsFound()) {
      // TODO: Surface additional UI messages here?
    }
    resourceBuilder = Helper.getResourceBuilder(cfg.getIpt()).organisationKey(
        cfg.getOrg().getUddiID());
    return SUCCESS;
  }

  public String register() {
    if (problemsFound()) {
      return SUCCESS;
    }

    if (Helper.resourceExists(resourceBuilder.getKey(), registryManager)) {
      return SUCCESS;
    }

    resourceBuilder.primaryContactType(ContactType.technical.name()).organisationKey(
        cfg.getOrg().getUddiID());
    GbrdsResource gr = resourceBuilder.build();
    OrgCredentials creds = getCreds();
    gr = Helper.createResource(gr, creds, registryManager);
    if (gr == null) {
      saveMessage("Warning: Unable to create GBRDS resource");
    } else {
      saveMessage("GBRDS resource created successfully: " + gr.getKey());
      cfg.setIpt(Helper.getResourceMetadata(gr));
      cfg.save();
      String rssUri = cfg.getAtomFeedURL();
      GbrdsService gs = Helper.createRssService(gr.getKey(), creds, rssUri,
          registryManager);
      if (gs == null) {
        saveMessage("Warning: Unable to create GBRDS service for resource");
      } else {
        saveMessage("GBRDS RSS service created successfully: " + gs.getKey());
      }
    }
    return SUCCESS;
  }

  @Override
  public String save() {
    if (problemsFound()) {
      return SUCCESS;
    }
    resourceBuilder.primaryContactType(ContactType.technical.name());
    GbrdsResource gr = resourceBuilder.build();
    OrgCredentials creds = getCreds();
    if (Helper.updateResource(gr, creds, registryManager)) {
      cfg.setIpt(Helper.getResourceMetadata(gr));
      cfg.save();
      saveMessage("GBRDS resource updated successfully");
    } else {
      saveMessage("Warning: Resource not updated in GBRDS");
    }
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

  private OrgCredentials getCreds() {
    String key = cfg.getOrg().getUddiID();
    String pass = cfg.getOrgPassword();
    return Helper.getCreds(key, pass);
  }

  private boolean problemsFound() {
    boolean problems = false;
    // Notifies the user if the IPT base URL contains localhost:
    if (!Helper.checkLocalhostUrl(cfg.getBaseUrl())) {
      saveMessage("Warning: Cannot create GBRDS resource because IPT base URL contains localhost");
      problems = true;
    }
    // Notifies the user if the IPT organisation doesn't yet exist in GBRDS:
    String orgKey = cfg.getOrg().getUddiID();
    if (!Helper.orgExists(orgKey, registryManager)) {
      saveMessage("Warning: Cannot create GBRDS resource because a GBRDS organisation is not associated with this IPT instance");
      problems = true;
    } else {
      // Notifies the user if organisation credentials are invalid:
      String key = cfg.getOrg().getUddiID();
      String pass = cfg.getOrgPassword();
      if (!Helper.validateCreds(Helper.getCreds(key, pass), registryManager)) {
        saveMessage("Warning: Cannot create GBRDS resource because the GBRDS organisation credentials are invalid");
        problems = true;
      }
    }
    // Notifies user if the resource already exists:
    String resourceKey = cfg.getIpt().getUddiID();
    if (Helper.resourceExists(resourceKey, registryManager)) {
      saveMessage("GBRDS resource registered for IPT: " + resourceKey);
      // problems = true;
    }

    return problems;
  }
}