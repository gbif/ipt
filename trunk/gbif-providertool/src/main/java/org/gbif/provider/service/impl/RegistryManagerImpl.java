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

import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ContactType;
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
  protected AppConfig cfg;
  private final OrganisationApi organsiationApi;
  private final ResourceApi resourceApi;
  private final ServiceApi serviceApi;
  private final IptApi iptApi;

  private RegistryManagerImpl() {
    Gbrds gbif = GbrdsRegistry.init("http://gbrdsdev.gbif.org");
    organsiationApi = gbif.getOrganisationApi();
    resourceApi = gbif.getResourceApi();
    serviceApi = gbif.getServiceApi();
    iptApi = gbif.getIptApi();
  }

  /**
   * @see RegistryManager#buildGbrdsOrganisation(ResourceMetadata)
   */
  public GbrdsOrganisation.Builder buildGbrdsOrganisation(
      ResourceMetadata resourceMetadata) {
    String description = resourceMetadata.getDescription();
    String homepageURL = resourceMetadata.getLink();
    String key = resourceMetadata.getUddiID();
    String name = resourceMetadata.getTitle();
    String nodeKey = cfg.getOrgNode();
    String nodeName = cfg.getOrgNodeName();
    String password = cfg.getOrgPassword();
    String primaryContactEmail = resourceMetadata.getContactEmail();
    String primaryContactName = resourceMetadata.getContactName();
    String primaryContactType = ContactType.administrative.name();
    return GbrdsOrganisation.builder().description(description).homepageURL(
        homepageURL).key(key).name(name).nodeKey(nodeKey).nodeName(nodeName).password(
        password).primaryContactEmail(primaryContactEmail).primaryContactName(
        primaryContactName).primaryContactType(primaryContactType);
  }

  /**
   * @see RegistryManager#buildGbrdsResource(ResourceMetadata)
   */
  public GbrdsResource.Builder buildGbrdsResource(
      ResourceMetadata resourceMetadata) {
    checkNotNull(resourceMetadata);
    String key = resourceMetadata.getUddiID();
    String name = resourceMetadata.getTitle();
    String description = resourceMetadata.getDescription();
    String homepageUrl = resourceMetadata.getLink();
    String primaryContactType = ContactType.administrative.name();
    String primaryContactName = resourceMetadata.getContactName();
    String primaryContactEmail = resourceMetadata.getContactEmail();
    return GbrdsResource.builder().key(key).name(name).description(description).homepageURL(
        homepageUrl).primaryContactType(primaryContactType).primaryContactName(
        primaryContactName).primaryContactEmail(primaryContactEmail);
  }

  /**
   * @see RegistryManager#createGbrdsOrganisation(GbrdsOrganisation)
   */
  public CreateOrgResponse createGbrdsOrganisation(
      GbrdsOrganisation organisation) {
    checkNotNull(organisation);
    return organsiationApi.create(organisation).execute();
  }

  /**
   * @see RegistryManager#createGbrdsResource(GbrdsResource, OrgCredentials)
   */
  public CreateResourceResponse createGbrdsResource(GbrdsResource resource,
      OrgCredentials creds) {
    checkNotNull(resource);
    checkNotNull(creds, "Credentials are null");
    return resourceApi.create(resource).execute(creds);
  }

  /**
   * @throws BadCredentialsException
   * @see RegistryManager#createGbrdsService(GbrdsService, OrgCredentials)
   */
  public CreateServiceResponse createGbrdsService(GbrdsService service,
      OrgCredentials creds) {
    checkNotNull(service);
    checkNotNull(creds, "Credentials are null");
    checkArgument(!service.getAccessPointURL().contains("localhost"),
        "Service URL contains localhost");
    return serviceApi.create(service).execute(creds);
  }

  /**
   * @see RegistryManager#deleteGbrdsResource(String, OrgCredentials)
   */
  public DeleteResourceResponse deleteGbrdsResource(String resourceKey,
      OrgCredentials creds) {
    checkArgument(notNullOrEmpty(resourceKey), "Invalid resource key");
    checkNotNull(creds, "Credentials are null");
    return resourceApi.delete(resourceKey).execute(creds);
  }

  /**
   * @see RegistryManager#deleteGbrdsService(String, OrgCredentials)
   */
  public DeleteServiceResponse deleteGbrdsService(String serviceKey,
      OrgCredentials creds) {
    checkArgument(notNullOrEmpty(serviceKey), "Invalid service key");
    checkNotNull(creds, "Credentials are null");
    return serviceApi.delete(serviceKey).execute(creds);
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
  public ListServicesResponse listGbrdsServices(String resourceKey) {
    checkArgument(notNullOrEmpty(resourceKey), "Invalid resource key");
    return serviceApi.list(resourceKey).execute();
  }

  /**
   * @throws RegistryException
   * @see RegistryManager#readGbrdsOrganisation(String)
   */
  public ReadOrgResponse readGbrdsOrganisation(String organisationKey) {
    checkArgument(notNullOrEmpty(organisationKey), "Invalid organisation key");
    return organsiationApi.read(organisationKey).execute();
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
   * @see RegistryManager#updateGbrdsOrganisation(GbrdsOrganisation,
   *      OrgCredentials)
   */
  public UpdateOrgResponse updateGbrdsOrganisation(
      GbrdsOrganisation organisation, OrgCredentials creds) {
    checkNotNull(organisation, "Organisation is null");
    checkNotNull(creds, "Credentials are null");
    return organsiationApi.update(organisation).execute(creds);
  }

  /**
   * @see RegistryManager#updateGbrdsResource(GbrdsResource, OrgCredentials)
   */
  public UpdateResourceResponse updateGbrdsResource(GbrdsResource resource,
      OrgCredentials creds) {
    checkNotNull(resource, "Resource is null");
    checkNotNull(creds, "Credentials are null");
    return resourceApi.update(resource).execute(creds);
  }

  /**
   * @throws RegistryException
   * @see RegistryManager#updateGbrdsService(GbrdsService, OrgCredentials)
   */
  public UpdateServiceResponse updateGbrdsService(GbrdsService service,
      OrgCredentials creds) {
    checkNotNull(service, "Service is null");
    checkNotNull(creds, "Credentials are null");
    return serviceApi.update(service).execute(creds);
  }

  /**
   * @see RegistryManager#validateCredentials(OrgCredentials)
   */
  public ValidateOrgCredentialsResponse validateCredentials(OrgCredentials creds) {
    return organsiationApi.validateCredentials(creds).execute();
  }
}