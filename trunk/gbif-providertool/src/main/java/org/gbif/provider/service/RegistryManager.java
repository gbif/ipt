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
package org.gbif.provider.service;

import com.google.common.collect.ImmutableSet;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.registry.api.client.GbrdsExtension;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.GbrdsThesaurus;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
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
 * This class provides a service interface for working with the GBRDS.
 * 
 */
public interface RegistryManager {

  @SuppressWarnings("serial")
  public class RegistryException extends Exception {
    public RegistryException() {
      super();
    }

    public RegistryException(String string) {
      super(string);
    }

    public RegistryException(String string, Throwable e) {
      super(string, e);
    }
  }

  /**
   * Creates a new GBRDS organisation and returns the resulting
   * {@link CreateOrgResponse}.
   * 
   * Throws {@link NullPointerException} if {@code org} is null. Throws
   * {@link IllegalArgumentException} if any of the following {@code org}
   * properties are null or the empty string:
   * 
   * <pre>
   * {@code name}
   * {@code primaryContactType}
   * {@code primaryContactEmail}
   * {@code nodeKey}
   * </pre>
   * 
   * Additionally throws {@link IllegalArgumentException} if {@code
   * primaryContactType} is not 'administrative' or 'technical'. Note that the
   * GBRDS expects the {@code nodeKey} to match a 'key' value returned by:
   * http://gbrdsdev.gbif.org/registry/node.json
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/OrganisationAPI#
   *      CREATE_ORGANISATION
   * @param org the GBRDS organisation to create
   * @return CreateOrgResponse
   */
  CreateOrgResponse createOrg(GbrdsOrganisation org);

  CreateResourceResponse createResource(GbrdsResource resource,
      OrgCredentials creds) throws BadCredentialsException;

  ImmutableSet<String> createResourceServices(GbrdsResource gbrdsResource,
      Resource resource);

  CreateServiceResponse createService(GbrdsService service, OrgCredentials creds)
      throws BadCredentialsException;

  DeleteResourceResponse deleteResource(String resourceKey, OrgCredentials creds)
      throws BadCredentialsException;

  DeleteServiceResponse deleteService(String serviceKey, OrgCredentials creds)
      throws BadCredentialsException;

  OrgCredentials getCreds(String key, String pass);

  ResourceMetadata getMeta(GbrdsOrganisation org);

  ResourceMetadata getMeta(GbrdsResource resource);

  GbrdsOrganisation.Builder getOrgBuilder(ResourceMetadata meta);

  GbrdsResource.Builder getResourceBuilder(ResourceMetadata meta);

  String getServiceUrl(ServiceType type, Resource resource);

  boolean isLocalhost(String url);

  List<GbrdsExtension> listAllExtensions();

  List<GbrdsThesaurus> listAllThesauri();

  ListServicesResponse listServices(String resourceKey);

  boolean orgExists(String key);

  ReadResourceResponse readGbrdsResource(String resourceKey);

  ReadOrgResponse readOrg(String organisationKey);

  boolean resourceExists(String key);

  String updateIptRssServiceUrl(String key, String password, String rssUrl);

  UpdateOrgResponse updateOrg(GbrdsOrganisation organisation,
      OrgCredentials creds) throws BadCredentialsException;

  UpdateResourceResponse updateResource(GbrdsResource resource,
      OrgCredentials creds) throws BadCredentialsException;

  UpdateServiceResponse updateService(GbrdsService service, OrgCredentials creds)
      throws BadCredentialsException;

  ImmutableSet<String> updateServiceUrls(List<Resource> resources);

  ValidateOrgCredentialsResponse validateCreds(OrgCredentials creds);
}