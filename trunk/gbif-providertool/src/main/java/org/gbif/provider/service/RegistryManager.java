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

import org.gbif.provider.model.ResourceMetadata;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.Credentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ListServicesForResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;

import java.util.Collection;

/**
 * TODO: Documentation.
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

  GbrdsOrganisation.Builder buildGbrdsOrganisation(
      ResourceMetadata resourceMetadata);

  GbrdsResource.Builder buildGbrdsResource(ResourceMetadata resourceMetadata);

  CreateOrgResponse createGbrdsOrganisation(GbrdsOrganisation gbifOrganisation)
      throws RegistryException;

  /**
   * Creates and returns a new GBIF Resource in the GBRDS. Throws a
   * {@link NullPointerException} if <code>gbifResource</code> is null. If there
   * is an error executing the request, if the HTTP status code is not equal to
   * 201 (Created), or if the response results are null, a
   * {@link RegistryException} is thrown.
   * 
   * @param gbifResource the GBIF Resource to create in the GBRDS
   * @return the created GBIF Resource
   * @throws RegistryException
   */
  CreateResourceResponse createGbrdsResource(GbrdsResource gbifResource)
      throws RegistryException;

  CreateServiceResponse createGbrdsService(GbrdsService service)
      throws RegistryException;

  DeleteResourceResponse deleteGbrdsResource(GbrdsResource resource)
      throws RegistryException;

  DeleteServiceResponse deleteGbrdsService(GbrdsService service)
      throws RegistryException;

  /**
   * Calls the central registry to receive a list of the Extensions that are
   * available
   * 
   * @return The (supposedly) publicly accessible URLs of the extensions
   */
  Collection<String> listAllExtensions();

  /**
   * Calls the central registry to receive a list of the ThesaurusVocabularies
   * that are available
   * 
   * @return The (supposedly) publicly accessible URLs of the thesauri
   */
  Collection<String> listAllThesauri();

  ListServicesForResourceResponse listGbrdsServicesForGbrdsResource(
      String gbifResourceKey) throws RegistryException;

  ReadOrgResponse readGbrdsOrganisation(String organisationKey)
      throws RegistryException;

  ReadResourceResponse readGbrdsResource(String resourceKey)
      throws RegistryException;

  UpdateOrgResponse updateGbrdsOrganisation(GbrdsOrganisation gbifOrganisation)
      throws RegistryException;

  UpdateResourceResponse updateGbrdsResource(GbrdsResource gbifResource)
      throws RegistryException;

  ValidateOrgCredentialsResponse validateGbifOrganisationCredentials(
      String gbigOrganisationKey, Credentials credentials);

}
