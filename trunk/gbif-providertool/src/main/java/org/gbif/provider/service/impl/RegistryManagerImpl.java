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
package org.gbif.provider.service.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;

import static org.gbif.provider.model.voc.ServiceType.DWC_ARCHIVE;
import static org.gbif.provider.model.voc.ServiceType.EML;
import static org.gbif.provider.model.voc.ServiceType.TAPIR;
import static org.gbif.provider.model.voc.ServiceType.TCS_RDF;
import static org.gbif.provider.model.voc.ServiceType.WFS;
import static org.gbif.provider.model.voc.ServiceType.WMS;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbrdsExtension;
import org.gbif.registry.api.client.GbrdsImpl;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.GbrdsThesaurus;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.CreateOrgResponse;
import org.gbif.registry.api.client.Gbrds.CreateResourceResponse;
import org.gbif.registry.api.client.Gbrds.CreateServiceResponse;
import org.gbif.registry.api.client.Gbrds.DeleteResourceResponse;
import org.gbif.registry.api.client.Gbrds.DeleteServiceResponse;
import org.gbif.registry.api.client.Gbrds.IptApi;
import org.gbif.registry.api.client.Gbrds.ListServiceResponse;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.OrganisationApi;
import org.gbif.registry.api.client.Gbrds.ReadOrgResponse;
import org.gbif.registry.api.client.Gbrds.ReadResourceResponse;
import org.gbif.registry.api.client.Gbrds.ResourceApi;
import org.gbif.registry.api.client.Gbrds.ServiceApi;
import org.gbif.registry.api.client.Gbrds.UpdateOrgResponse;
import org.gbif.registry.api.client.Gbrds.UpdateResourceResponse;
import org.gbif.registry.api.client.Gbrds.UpdateServiceResponse;
import org.gbif.registry.api.client.Gbrds.ValidateOrgCredentialsResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This class provides a default implementation of {@link RegistryManager}.
 * 
 */
public class RegistryManagerImpl implements RegistryManager {

  private static final ImmutableSet<ServiceType> SUPPORTED_SERVICE_TYPES = ImmutableSet.of(
      EML, DWC_ARCHIVE, TAPIR, WFS, WMS, TCS_RDF);

  private static boolean notNullOrEmpty(String val) {
    return val != null && val.trim().length() > 0;
  }

  @Autowired
  private AppConfig appConfig;

  private final OrganisationApi orgApi;
  private final ResourceApi resourceApi;
  private final ServiceApi serviceApi;
  private final IptApi iptApi;

  RegistryManagerImpl() {
    Gbrds gbrds = GbrdsImpl.init("http://gbrdsdev.gbif.org");
    orgApi = gbrds.getOrganisationApi();
    resourceApi = gbrds.getResourceApi();
    serviceApi = gbrds.getServiceApi();
    iptApi = gbrds.getIptApi();
  }

  /**
   * For testing.
   */
  RegistryManagerImpl(Gbrds gbrds, AppConfig appConfig) {
    this.appConfig = appConfig;
    orgApi = gbrds.getOrganisationApi();
    resourceApi = gbrds.getResourceApi();
    serviceApi = gbrds.getServiceApi();
    iptApi = gbrds.getIptApi();
  }

  /**
   * @see RegistryManager#createOrg(GbrdsOrganisation)
   */
  public CreateOrgResponse createOrg(GbrdsOrganisation org) {
    return orgApi.create(org).execute();
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#createResource(GbrdsResource, OrgCredentials)
   */
  public CreateResourceResponse createResource(GbrdsResource resource,
      OrgCredentials creds) throws BadCredentialsException {
    return resourceApi.create(resource).execute(creds);
  }

  /**
   * @see RegistryManager#createServices(GbrdsResource, Resource)
   */
  public ImmutableSet<String> createResourceServices(
      GbrdsResource gbrdsResource, Resource resource) {
    checkNotNull(gbrdsResource, "GBRDS resource is null");
    checkNotNull(resource, "IPT resource is null");

    ImmutableSet.Builder<String> errors = ImmutableSet.builder();

    // Checks that the GBRDS resource exists:
    String key = gbrdsResource.getKey();
    if (!resourceExists(key)) {
      errors.add("Warning: GBRDS resource doesn't exist");
      return errors.build();
    }

    // Checks GBRDS resource credentials:
    OrgCredentials creds = null;
    try {
      creds = OrgCredentials.with(resource.getOrgUuid(),
          resource.getOrgPassword());
    } catch (Exception e) {
      errors.add("Warning: Invalid resource credentials");
    }
    if (creds == null) {
      return errors.build();
    }

    // Creates GBRDS services:
    GbrdsService.Builder service;
    for (ServiceType type : SUPPORTED_SERVICE_TYPES) {
      service = GbrdsService.builder().resourceKey(key).accessPointURL(
          getServiceUrl(type, resource)).type(type.getCode());
      try {
        CreateServiceResponse csr = createService(service.build(), creds);
        int status = csr.getStatus();
        if (status != HttpStatus.SC_CREATED) {
          errors.add("Warning: Unable to create service - status " + status);
        }
      } catch (BadCredentialsException e) {
        errors.add("Warning: Unable to create service - bad credentials "
            + creds);
      }
    }

    return errors.build();
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#createService(GbrdsService, OrgCredentials)
   */
  public CreateServiceResponse createService(GbrdsService service,
      OrgCredentials creds) throws BadCredentialsException {
    checkNotNull(service);
    checkNotNull(creds, "Credentials are null");
    checkArgument(!isLocalhost(service.getAccessPointURL()),
        "Invalid service URL");
    return serviceApi.create(service).execute(creds);
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#deleteResource(String, OrgCredentials)
   */
  public DeleteResourceResponse deleteResource(String resourceKey,
      OrgCredentials creds) throws BadCredentialsException {
    checkArgument(notNullOrEmpty(resourceKey), "Invalid resource key");
    checkNotNull(creds, "Credentials are null");
    return resourceApi.delete(resourceKey).execute(creds);
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#deleteService(String, OrgCredentials)
   */
  public DeleteServiceResponse deleteService(String serviceKey,
      OrgCredentials creds) throws BadCredentialsException {
    checkArgument(notNullOrEmpty(serviceKey), "Invalid service key");
    checkNotNull(creds, "Credentials are null");
    return serviceApi.delete(serviceKey).execute(creds);
  }

  /**
   * @see RegistryManager#getCreds(String, String)
   */
  public OrgCredentials getCreds(String key, String pass) {
    OrgCredentials creds = null;
    try {
      creds = OrgCredentials.with(key, pass);
      if (!validateCreds(creds).getResult()) {
        creds = null;
      }
    } catch (Exception e) {
      return null;
    }
    return creds;
  }

  /**
   * @see RegistryManager#getMeta(GbrdsOrganisation)
   */
  public ResourceMetadata getMeta(GbrdsOrganisation org) {
    checkNotNull(org, "Organisation is null");
    ResourceMetadata meta = new ResourceMetadata();
    meta.setDescription(org.getDescription());
    meta.setContactEmail(org.getPrimaryContactEmail());
    meta.setContactName(org.getPrimaryContactName());
    meta.setLink(org.getHomepageURL());
    meta.setTitle(org.getName());
    meta.setUddiID(org.getKey());
    return meta;
  }

  /**
   * @see RegistryManager#getMeta(GbrdsResource)
   */
  public ResourceMetadata getMeta(GbrdsResource resource) {
    checkNotNull(resource, "Resource is null");
    ResourceMetadata meta = new ResourceMetadata();
    meta.setDescription(resource.getDescription());
    meta.setContactEmail(resource.getPrimaryContactEmail());
    meta.setContactName(resource.getPrimaryContactName());
    meta.setLink(resource.getHomepageURL());
    meta.setTitle(resource.getName());
    meta.setUddiID(resource.getKey());
    return meta;
  }

  /**
   * @see RegistryManager#getOrgBuilder(ResourceMetadata)
   */
  public GbrdsOrganisation.Builder getOrgBuilder(ResourceMetadata meta) {
    checkNotNull(meta, "Resource metadata is null");
    return GbrdsOrganisation.builder().description(meta.getDescription()).primaryContactEmail(
        meta.getContactEmail()).primaryContactName(meta.getContactName()).homepageURL(
        meta.getLink()).name(meta.getTitle()).key(meta.getUddiID());
  }

  /**
   * @see RegistryManager#getResourceBuilder(ResourceMetadata)
   */
  public GbrdsResource.Builder getResourceBuilder(ResourceMetadata meta) {
    checkNotNull(meta, "Resource metadata is null");
    return GbrdsResource.builder().description(meta.getDescription()).primaryContactEmail(
        meta.getContactEmail()).primaryContactName(meta.getContactName()).homepageURL(
        meta.getLink()).name(meta.getTitle()).key(meta.getUddiID());
  }

  /**
   * @see RegistryManager#getServiceUrl(ServiceType, Resource)
   */
  public String getServiceUrl(ServiceType type, Resource resource) {
    checkNotNull(type, "Service type is null");
    checkNotNull(resource, "Resource is null");
    checkNotNull(resource.getId(), "Resource id is null");
    checkArgument(notNullOrEmpty(resource.getGuid()), "Invalid resource GUID");
    switch (type) {
      case EML:
        return appConfig.getEmlUrl(resource.getGuid());
      case DWC_ARCHIVE:
        return appConfig.getArchiveUrl(resource.getGuid());
      case TAPIR:
        return appConfig.getTapirEndpoint(resource.getId());
      case WFS:
        return appConfig.getWfsEndpoint(resource.getId());
      case WMS:
        return appConfig.getWmsEndpoint(resource.getId());
      case TCS_RDF:
        return appConfig.getArchiveTcsUrl(resource.getGuid());
      default:
        return null;
    }
  }

  public boolean isLocalhost(String url) {
    return !notNullOrEmpty(url) || url.contains("localhost")
        || url.contains("127.0.0.1");
  }

  /**
   * @see RegistryManager#listAllExtensions()
   */
  public List<GbrdsExtension> listAllExtensions() {
    return iptApi.listExtensions().execute().getResult();
  }

  /**
   * @see RegistryManager#listAllThesauri()
   */
  public List<GbrdsThesaurus> listAllThesauri() {
    return iptApi.listThesauri().execute().getResult();
  }

  /**
   * @see RegistryManager#listGbifServices(String )
   */
  public ListServiceResponse listServices(String resourceKey) {
    checkArgument(notNullOrEmpty(resourceKey), "Invalid resource key");
    return serviceApi.list(resourceKey).execute();
  }

  /**
   * @see RegistryManager#orgExists(String)
   */
  public boolean orgExists(String key) {
    return notNullOrEmpty(key) && readOrg(key).getResult() != null;
  }

  /**
   * @throws RegistryException
   * @see RegistryManager#readGbrdsResource(String)
   */
  public ReadResourceResponse readGbrdsResource(String resourceKey) {
    checkArgument(notNullOrEmpty(resourceKey), "Invalid resource key");
    return resourceApi.read(resourceKey).execute();
  }

  /**
   * @throws RegistryException
   * @see RegistryManager#readOrg(String)
   */
  public ReadOrgResponse readOrg(String organisationKey) {
    checkArgument(notNullOrEmpty(organisationKey), "Invalid organisation key");
    return orgApi.read(organisationKey).execute();
  }

  /**
   * @see RegistryManager#resourceExists(String)
   */
  public boolean resourceExists(String key) {
    return notNullOrEmpty(key) && readGbrdsResource(key).getResult() != null;
  }

  /**
   * @see RegistryManager#updateIptRssService(String, String, String)
   */
  public String updateIptRssServiceUrl(String key, String password,
      String rssUrl) {
    checkArgument(notNullOrEmpty(key), "Key is null");
    checkArgument(notNullOrEmpty(password), "Password is null");
    checkArgument(notNullOrEmpty(rssUrl), "URL is null");

    // Checks if the resource exists:
    if (!resourceExists(key)) {
      return null;
    }

    // Checks for localhost in the RSS URL:
    if (isLocalhost(rssUrl)) {
      return "Warning: Unable to update RSS service because of invalid base URL";
    }

    // Looks for the RSS service in the GBRDS:
    ListServiceResponse lsr = listServices(key);
    if (lsr.getStatus() != HttpStatus.SC_OK) {
      return "Warning: Unable to get services";
    }
    List<GbrdsService> results = lsr.getResult();
    GbrdsService rss = null;
    for (GbrdsService s : results) {
      if (ServiceType.fromCode(s.getType()) == ServiceType.RSS) {
        rss = s;
      }
    }
    if (rss == null) {
      return "Warning: An RSS service does not exist for the IPT";
    }

    // Updates the service URL:
    OrgCredentials creds = OrgCredentials.with(key, password);
    rss = GbrdsService.builder(rss).accessPointURL(rssUrl).build();
    UpdateServiceResponse usr = null;
    try {
      usr = updateService(rss, creds);
    } catch (BadCredentialsException e) {
      return "Warning: Unable to update RSS service because of bad credentials: "
          + creds;
    }
    if (usr.getStatus() != HttpStatus.SC_OK) {
      return "Warning: Updating RSS service returned HTTP status "
          + usr.getStatus();
    }

    return null;
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#updateOrg(GbrdsOrganisation, OrgCredentials)
   */
  public UpdateOrgResponse updateOrg(GbrdsOrganisation org, OrgCredentials creds)
      throws BadCredentialsException {
    return orgApi.update(org).execute(creds);
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#updateResource(GbrdsResource, OrgCredentials)
   */
  public UpdateResourceResponse updateResource(GbrdsResource resource,
      OrgCredentials creds) throws BadCredentialsException {
    checkNotNull(resource, "Resource is null");
    checkNotNull(creds, "Credentials are null");
    return resourceApi.update(resource).execute(creds);
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#updateService(GbrdsService, OrgCredentials)
   */
  public UpdateServiceResponse updateService(GbrdsService service,
      OrgCredentials creds) throws BadCredentialsException {
    checkNotNull(service, "Service is null");
    checkArgument(notNullOrEmpty(service.getKey()), "Invalid service key");
    checkArgument(!isLocalhost(service.getAccessPointURL()),
        "Invalid service URL");
    checkNotNull(creds, "Credentials are null");
    return serviceApi.update(service).execute(creds);
  }

  /**
   * @see RegistryManager#updateServiceUrls(List)
   */
  public ImmutableSet<String> updateServiceUrls(List<Resource> resources) {
    checkNotNull(resources, "Resource list is null");

    ImmutableSet.Builder<String> errors = ImmutableSet.builder();

    // Updates all service URLs for all IPT resources:
    for (Resource r : resources) {
      // Checks if a GBRDS resource exists for current IPT resource:
      String resourceKey = r.getMeta().getUddiID();
      if (!resourceExists(resourceKey)) {
        errors.add("Warning: No GBRDS resource for IPT resource " + r.getId());
        continue;
      }

      // Checks credentials:
      String key = r.getOrgUuid();
      String password = r.getOrgPassword();
      OrgCredentials creds = OrgCredentials.with(key, password);
      if (creds == null) {
        errors.add("Warning: Invalid credentials: " + creds);
        continue;
      }

      // Gets services from GBRDS:
      ListServiceResponse lsr = listServices(resourceKey);
      if (lsr.getStatus() != HttpStatus.SC_OK) {
        errors.add("Warninig: Unable to list services for " + resourceKey);
        continue;
      }

      // Updates each service URL:
      List<GbrdsService> services = lsr.getResult();
      for (GbrdsService s : services) {
        ServiceType type = ServiceType.fromCode(s.getType());
        String url = getServiceUrl(type, r);
        GbrdsService gs = GbrdsService.builder(s).accessPointURL(url).build();
        try {
          UpdateServiceResponse usr = updateService(gs, creds);
          if (usr.getStatus() != HttpStatus.SC_OK) {
            errors.add("Warning: Unable to update service " + gs.getKey());
          }
        } catch (BadCredentialsException e) {
          continue;
        }
      }
    }

    return errors.build();
  }

  /**
   * @see RegistryManager#validateCreds(OrgCredentials)
   */
  public ValidateOrgCredentialsResponse validateCreds(OrgCredentials creds) {
    return orgApi.validateCredentials(creds).execute();
  }
}