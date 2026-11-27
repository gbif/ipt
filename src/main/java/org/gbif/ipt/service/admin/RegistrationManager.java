/*
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
package org.gbif.ipt.service.admin;

import org.gbif.doi.service.DoiService;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Network;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * This interface details ALL methods associated with the Organisations associated with the IPT.
 */
public interface RegistrationManager {

  /**
   * Associate a new organisation to this IPT installation, but doesnt persist the change.
   */
  Organisation addAssociatedOrganisation(Organisation organisation) throws AlreadyExistingException,
    InvalidConfigException;

  /**
   * Register the IPT against an existing organisation.
   */
  Organisation addHostingOrganisation(Organisation organisation);

  /**
   * Add all the IPT specific data.
   */
  void addIptInstance(Ipt ipt);

  /**
   * Delete an organisation. Will remove the specified organisation from the in memory list of organisations.
   * See save() to persist this change to files. In order to delete the organisation, the IPT cannot be registered
   * against it (it cannot be the hosting organisation), nor can there be any resource registered against it.
   *
   * @param key key of organisation to be deleted
   * @param resources all resources
   *
   * @return Organisation if it was not null and was deleted successfully, or null if it couldn't be found
   *
   * @throws DeletionNotAllowedException if the deletion was not allowed for some reason
   */
  Organisation delete(String key, List<Resource> resources) throws DeletionNotAllowedException;

  /**
   * Returns a single organisation associated to the key, from list of organisations loaded into memory.
   *
   * @param key of organisation to be returned
   */
  Organisation get(String key);

  /**
   * Returns a single organisation associated to this UUID, from list of organisations loaded into memory.
   *
   * @param key of organisation to be returned
   */
  Organisation get(UUID key);

  /**
   * Returns a single organisation associated to the key, from disk.
   *
   * @param key of organisation to be returned
   */
  Organisation getFromDisk(String key);

  /**
   * Returns the hosting organisation of this IPT.
   */
  Organisation getHostingOrganisation();

  /**
   * Returns the default GBIF network of this IPT.
   */
  Network getNetwork();

  /**
   * Returns the IPT instance.
   *
   * @return ipt instance
   */
  Ipt getIpt();

  /**
   * Returns list of all organisations able to host resources.
   */
  List<Organisation> list();

  /**
   * Returns list of all associated organisations.
   */
  List<Organisation> listAll();

  /**
   * @return organisation associated to the IPT that has a DOI agency account that has been activated, null if none
   * found
   */
  Organisation findPrimaryDoiAgencyAccount();

  /**
   * Construct and return the appropriate DOI service depending on the type of DOI agency account that has been
   * activated in the IPT (since version 2.4.0, DataCite only).
   *
   * @return the DataCite service capable of reserving, minting, deleting DOIs
   *
   * @throws InvalidConfigException if the DOI agency account has been badly configured
   */
  DoiService getDoiService() throws InvalidConfigException;

  /**
   * Loads all user associated organisations from file into the manager.
   */
  void load() throws InvalidConfigException;

  /**
   * Saves all organisations (associated to this IPT) from the manager to file. Needs to be manually called if
   * organisation properties have been modified or if organisations have been added or removed.
   */
  void save() throws IOException;

  /**
   * Migrate former registration (registration.xml) into new registration (registration2.xml) with passwords encrypted.
   */
  void encryptRegistration();

  /**
   * Updates one associated organization's details (name, description, homepage, node, primary contact) from the
   * Registry and saves them. Settings made in the IPT, like alias, token and DOI account, stay as they are.
   *
   * @param key the organization's key
   *
   * @return false if the Registry doesn't know the organization: its details stay as they were
   *
   * @throws RegistryException if the Registry couldn't be reached or its response couldn't be read
   * @throws IOException if the updated details couldn't be saved
   */
  boolean updateOrganisationFromRegistry(String key) throws RegistryException, IOException;

  /**
   * Update (from the Registry) metadata for all associated organizations in the IPT.
   *
   * @return the organizations that couldn't be updated (e.g. the Registry couldn't be reached), empty if all were
   */
  List<Organisation> updateAssociatedOrganisationsMetadata() throws IOException;

  /**
   * Associate IPT with the network and save the registration.
   *
   * @throws IOException if the registration could not be saved
   */
  void associateWithNetwork(String networkKey, String networkName) throws IOException;

  /**
   * Remove association with the network and save the registration.
   *
   * @throws IOException if the registration could not be saved
   */
  void removeAssociationWithNetwork() throws IOException;
}
