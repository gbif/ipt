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

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbrdsExtension;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsRegistry;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.GbrdsThesaurus;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.IptApi;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.OrganisationApi;
import org.gbif.registry.api.client.Gbrds.ResourceApi;
import org.gbif.registry.api.client.Gbrds.ServiceApi;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ListServicesResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This class provides a default implementation of {@link RegistryManager}.
 * 
 */
public class RegistryManagerImpl implements RegistryManager {

  private static boolean notNullOrEmpty(String val) {
    return val != null && val.trim().length() > 0;
  }

  @Autowired
  private AppConfig appConfig;

  private final OrganisationApi organsiationApi;
  private final ResourceApi resourceApi;
  private final ServiceApi serviceApi;
  private final IptApi iptApi;

  RegistryManagerImpl() {
    Gbrds gbif = GbrdsRegistry.init("http://gbrdsdev.gbif.org");
    organsiationApi = gbif.getOrganisationApi();
    resourceApi = gbif.getResourceApi();
    serviceApi = gbif.getServiceApi();
    iptApi = gbif.getIptApi();
  }

  /**
   * @see RegistryManager#createOrg(GbrdsOrganisation)
   */
  public CreateOrgResponse createOrg(GbrdsOrganisation organisation) {
    checkNotNull(organisation);
    return organsiationApi.create(organisation).execute();
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#createResource(GbrdsResource, OrgCredentials)
   */
  public CreateResourceResponse createResource(GbrdsResource resource,
      OrgCredentials creds) throws BadCredentialsException {
    checkNotNull(resource);
    checkNotNull(creds, "Credentials are null");
    return resourceApi.create(resource).execute(creds);
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
  public ListServicesResponse listServices(String resourceKey) {
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
    return organsiationApi.read(organisationKey).execute();
  }

  /**
   * @see RegistryManager#resourceExists(String)
   */
  public boolean resourceExists(String key) {
    return notNullOrEmpty(key) && readGbrdsResource(key).getResult() != null;
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#updateOrg(GbrdsOrganisation, OrgCredentials)
   */
  public UpdateOrgResponse updateOrg(GbrdsOrganisation organisation,
      OrgCredentials creds) throws BadCredentialsException {
    checkNotNull(organisation, "Organisation is null");
    checkNotNull(creds, "Credentials are null");
    return organsiationApi.update(organisation).execute(creds);
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
   * @see RegistryManager#validateCreds(OrgCredentials)
   */
  public ValidateOrgCredentialsResponse validateCreds(OrgCredentials creds) {
    return organsiationApi.validateCredentials(creds).execute();
  }
}