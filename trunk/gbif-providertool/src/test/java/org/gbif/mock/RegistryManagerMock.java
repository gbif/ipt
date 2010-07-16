/*
 * Copyright 2010 GBIF.
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
package org.gbif.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.service.RegistryManager;
import org.gbif.registry.api.client.GbrdsExtension;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.GbrdsThesaurus;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsOrganisation.Builder;
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

import java.util.List;

/**
 *
 */
public class RegistryManagerMock implements RegistryManager {

  public GbrdsService service;
  public List<GbrdsService> serviceList = Lists.newArrayList();

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#createOrg(org.gbif.registry.api
   * .client.GbrdsOrganisation)
   */
  public CreateOrgResponse createOrg(GbrdsOrganisation org) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#createResource(org.gbif.registry
   * .api.client.GbrdsResource,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public CreateResourceResponse createResource(GbrdsResource resource,
      OrgCredentials creds) throws BadCredentialsException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#createResourceServices(org.gbif
   * .registry.api.client.GbrdsResource, org.gbif.provider.model.Resource)
   */
  public ImmutableSet<String> createResourceServices(
      GbrdsResource gbrdsResource, Resource resource) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#createService(org.gbif.registry
   * .api.client.GbrdsService,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public CreateServiceResponse createService(GbrdsService service,
      OrgCredentials creds) throws BadCredentialsException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#deleteResource(java.lang.String,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public DeleteResourceResponse deleteResource(String resourceKey,
      OrgCredentials creds) throws BadCredentialsException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#deleteService(java.lang.String,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public DeleteServiceResponse deleteService(String serviceKey,
      OrgCredentials creds) throws BadCredentialsException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.RegistryManager#getCreds(java.lang.String,
   * java.lang.String)
   */
  public OrgCredentials getCreds(String key, String pass) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#getMeta(org.gbif.registry.api
   * .client.GbrdsOrganisation)
   */
  public ResourceMetadata getMeta(GbrdsOrganisation org) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#getMeta(org.gbif.registry.api
   * .client.GbrdsResource)
   */
  public ResourceMetadata getMeta(GbrdsResource resource) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#asOrg(org.gbif.provider.model
   * .ResourceMetadata)
   */
  public Builder getOrgBuilder(ResourceMetadata meta) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#asResource(org.gbif.provider.
   * model.ResourceMetadata)
   */
  public org.gbif.registry.api.client.GbrdsResource.Builder getResourceBuilder(
      ResourceMetadata meta) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#getServiceUrl(org.gbif.provider
   * .model.voc.ServiceType, org.gbif.provider.model.Resource)
   */
  public String getServiceUrl(ServiceType type, Resource resource) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#isLocalhost(java.lang.String)
   */
  public boolean isLocalhost(String url) {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.RegistryManager#listAllExtensions()
   */
  public List<GbrdsExtension> listAllExtensions() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.RegistryManager#listAllThesauri()
   */
  public List<GbrdsThesaurus> listAllThesauri() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#listServices(java.lang.String)
   */
  public ListServicesResponse listServices(String resourceKey) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.RegistryManager#orgExists(java.lang.String)
   */
  public boolean orgExists(String key) {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#readGbrdsResource(java.lang.String
   * )
   */
  public ReadResourceResponse readGbrdsResource(String resourceKey) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.RegistryManager#readOrg(java.lang.String)
   */
  public ReadOrgResponse readOrg(String organisationKey) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#resourceExists(java.lang.String)
   */
  public boolean resourceExists(String key) {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#updateIptRssServiceUrl(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  public String updateIptRssServiceUrl(String key, String password,
      String rssUrl) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#updateOrg(org.gbif.registry.api
   * .client.GbrdsOrganisation,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public UpdateOrgResponse updateOrg(GbrdsOrganisation organisation,
      OrgCredentials creds) throws BadCredentialsException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#updateResource(org.gbif.registry
   * .api.client.GbrdsResource,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public UpdateResourceResponse updateResource(GbrdsResource resource,
      OrgCredentials creds) throws BadCredentialsException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#updateService(org.gbif.registry
   * .api.client.GbrdsService,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public UpdateServiceResponse updateService(GbrdsService service,
      OrgCredentials creds) throws BadCredentialsException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#updateServiceUrls(java.util.List)
   */
  public ImmutableSet<String> updateServiceUrls(List<Resource> resources) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#validateCreds(org.gbif.registry
   * .api.client.Gbrds.OrgCredentials)
   */
  public ValidateOrgCredentialsResponse validateCreds(OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

}
