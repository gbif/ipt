/*
 * Copyright 2010 Regents of the University of California, University of Kansas.
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

import com.google.common.collect.Lists;

import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.service.RegistryManager;
import org.gbif.registry.api.client.GbrdsExtension;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.GbrdsThesaurus;
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
public abstract class RegistryManagerMock implements RegistryManager {

  public GbrdsService service;
  public List<GbrdsService> serviceList = Lists.newArrayList();

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#buildGbrdsOrganisation(org.gbif
   * .provider.model.ResourceMetadata)
   */
  public Builder buildGbrdsOrganisation(ResourceMetadata resourceMetadata) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#buildGbrdsResource(org.gbif.provider
   * .model.ResourceMetadata)
   */
  public org.gbif.registry.api.client.GbrdsResource.Builder buildGbrdsResource(
      ResourceMetadata resourceMetadata) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#createGbrdsOrganisation(org.gbif
   * .registry.api.client.GbrdsOrganisation)
   */
  public CreateOrgResponse createGbrdsOrganisation(
      GbrdsOrganisation organisation) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#createGbrdsResource(org.gbif.
   * registry.api.client.GbrdsResource,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public CreateResourceResponse createGbrdsResource(GbrdsResource resource,
      OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#createGbrdsService(org.gbif.registry
   * .api.client.GbrdsService,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public CreateServiceResponse createGbrdsService(GbrdsService service,
      OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#deleteGbrdsResource(java.lang
   * .String, org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public DeleteResourceResponse deleteGbrdsResource(String resourceKey,
      OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#deleteGbrdsService(java.lang.
   * String, org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public DeleteServiceResponse deleteGbrdsService(String serviceKey,
      OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
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
   * org.gbif.provider.service.RegistryManager#listGbrdsServices(java.lang.String
   * )
   */
  public ListServicesResponse listGbrdsServices(String resourceKey) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#readGbrdsOrganisation(java.lang
   * .String)
   */
  public ReadOrgResponse readGbrdsOrganisation(String organisationKey) {
    // TODO Auto-generated method stub
    return null;
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
   * @see
   * org.gbif.provider.service.RegistryManager#updateGbrdsOrganisation(org.gbif
   * .registry.api.client.GbrdsOrganisation,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public UpdateOrgResponse updateGbrdsOrganisation(
      GbrdsOrganisation organisation, OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#updateGbrdsResource(org.gbif.
   * registry.api.client.GbrdsResource,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public UpdateResourceResponse updateGbrdsResource(GbrdsResource resource,
      OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#updateGbrdsService(org.gbif.registry
   * .api.client.GbrdsService,
   * org.gbif.registry.api.client.Gbrds.OrgCredentials)
   */
  public UpdateServiceResponse updateGbrdsService(GbrdsService service,
      OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#validateCredentials(org.gbif.
   * registry.api.client.Gbrds.OrgCredentials)
   */
  public ValidateOrgCredentialsResponse validateCredentials(OrgCredentials creds) {
    // TODO Auto-generated method stub
    return null;
  }

}
