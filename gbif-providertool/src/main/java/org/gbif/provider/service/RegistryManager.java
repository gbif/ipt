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

import org.gbif.provider.model.Organisation;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;

import java.util.Collection;

/**
 * TODO: Documentation.
 * 
 */
public interface RegistryManager {
  void deleteResource(Resource resource) throws RegistryException;

  /**
   * Verifies {@link Organisation} credentials against the GBIF Registry.
   * 
   * @param org the organisation
   * @return true if the organisation credentials are valid, false otherwise
   */
  boolean isOrganisationRegistered(Organisation org);

  /**
   * Verifies {@link Organisation} credentials against the GBIF Registry.
   * 
   * @param org the organisation
   * @return true if the organisation credentials are valid, false otherwise
   */
  boolean isResourceRegistered(String resourceUuid);

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

  String registerIPT() throws RegistryException;

  /**
   * Registers the {@link Organisation} associated with the IPT instance with
   * the GBIF Registry.
   * 
   * @see http://goo.gl/H17q
   * 
   * @return the registered organisation
   * @throws RegistryException
   */
  Organisation registerIptInstanceOrganisation() throws RegistryException;

  /**
   * Registers an {@link Organisation} with the GBIF Registry.
   * 
   * @see http://goo.gl/H17q
   * 
   * @param organisation the organisation to register
   * @return the registered organisation
   * @throws RegistryException
   */
  Organisation registerOrganisation(Organisation organisation)
      throws RegistryException;

  String registerResource(Resource resource) throws RegistryException;

  String registerResource(ResourceMetadata resourceMetadata)
      throws RegistryException;

  boolean testLogin();

  void updateIPT() throws RegistryException;

  /**
   * Updates the organisation associated with the IPT instance with the GBIF
   * Registry.
   * 
   * @return the updated IPT organisation
   * @throws RegistryException
   */
  Organisation updateIptInstanceOrganisation() throws RegistryException;

  /**
   * Updates an organisation with the GBIF Registry.
   * 
   * @param organisation the organisation to update.
   * @return the updated organisation
   * @throws RegistryException
   */
  Organisation updateOrganisation(Organisation organisation)
      throws RegistryException;

  void updateResource(Resource resource) throws RegistryException;
}
