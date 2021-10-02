/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.service.registry;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;

import java.util.List;
import java.util.UUID;

import com.google.inject.ImplementedBy;

@ImplementedBy(RegistryManagerImpl.class)
public interface RegistryManager {

  /**
   * Deregister a resource from the GBIF Registry. This effectively deletes the registration of the resource.
   *
   * @param resource Resource
   *
   * @throws RegistryException if the resource could not be deregistered for any reason
   */
  void deregister(Resource resource) throws RegistryException;

  /**
   * Gets list of all registered extensions from the Registry.
   *
   * @return list of extensions, or an empty list if none were retrieved from valid response
   *
   * @throws RegistryException if the list of extensions couldn't be populated
   */
  List<Extension> getExtensions() throws RegistryException;

  /**
   * Retrieves a list of Organisation from the Registry.
   *
   * @return list of Organisation, or an empty list if none were retrieved from valid response
   *
   * @throws RegistryException if the list of Organisation couldn't be populated
   */
  List<Organisation> getOrganisations();

  /**
   * Retrieves an Organisation from the Registry using its key.
   *
   * @param key Organisation key
   *
   * @return a single Organisation, or null if no organisation was retrieved
   *
   * @throws RegistryException if the Organisation couldn't be retrieved
   */
  Organisation getRegisteredOrganisation(String key);

  /**
   * Gets list of all registered vocabularies from the Registry.
   *
   * @return list of vocabularies, or an empty list if none were retrieved from valid response
   *
   * @throws RegistryException if the list of thesauri couldn't be populated
   */
  List<Vocabulary> getVocabularies() throws RegistryException;

  /**
   * Register a new resource with the GBIF Registry associated with the IPT and organisation specified. Parses
   * GBIF UUID key from response received.
   *
   * @param resource     Resource being registered
   * @param organisation Organisation owning resource
   * @param ipt          IPT hosting resource
   *
   * @return the GBIF UUID key of the newly created resource
   *
   * @throws RegistryException if registration failed for any reason, including a non valid UUID key was found
   */
  UUID register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException;

  /**
   * Register an IPT instance against the GBIF Registry. Parses GBIF UUID key from response received.
   *
   * @param ipt          IPT being registered
   * @param organisation the organisation to register the IPT with
   *
   * @return the GBIF UUID key of newly registered IPT
   *
   * @throws RegistryException if registration failed for any reason, including a non valid UUID key was found
   */
  String registerIPT(Ipt ipt, Organisation organisation) throws RegistryException;

  /**
   * Update an IPT instance against the GBIF Registry. Also updates all registered resources against the GBIF Registry.
   * This method ensures the GBIF Registry has the correct endpoint URLs for both the IPT and all its registered
   * resources.
   *
   * @param ipt IPT whose registration is being updated
   *
   * @throws RegistryException if updating IPT or any registered resource was unsuccessful for any reason
   */
  void updateIpt(Ipt ipt) throws RegistryException;

  /**
   * Updates a resource's registration in the GBIF Registry. This means that the resource's metadata is updated,
   * including its services. The resource's owning organization is used to authenticate the update with the Registry.
   * The resource's owning organization, and hosting organization are set only during initial registration, and will
   * never change during the update.
   *
   * @param resource Resource whose registration is being updated
   * @param iptKey key of IPT whose registration is being updated
   *
   * @throws RegistryException        if update was unsuccessful for any reason
   * @throws IllegalArgumentException is resource is not registered yet
   */
  void updateResource(Resource resource, String iptKey) throws RegistryException, IllegalArgumentException;

  /**
   * Validate whether an Organization identified by its key and password is registered.
   *
   * @param organisationKey Organisation key
   * @param password        Organisation password (in Registry)
   *
   * @return whether the Organisation is registered having this key and password
   */
  boolean validateOrganisation(String organisationKey, String password);

  /**
   * Retrieves a list of all registered Resources associated to an Organization. If the name and UUID of the resource
   * cannot be populated, it isn't returned with the list.
   *
   * @param key organization key (UUID in String format)
   *
   * @return list of all registered Resources associated to an Organization, or an empty list if none were retrieved
   *
   * @throws RegistryException if the list could not be retrieved for any reason
   */
  List<Resource> getOrganisationsResources(String key) throws RegistryException;
}
