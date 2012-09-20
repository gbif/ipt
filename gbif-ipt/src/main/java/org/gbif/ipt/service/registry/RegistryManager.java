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

  void deregister(Resource resource) throws RegistryException;

  /**
   * Gets list of extensions from the Registry.
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
   * Retrieves a list of Vocabulary from the Registry, but only the basic metadata, i.e. each without the list concepts.
   *
   * @return list of thesauri, or an empty list if none were retrieved from valid response
   *
   * @throws RegistryException if the list of thesauri couldn't be populated
   */
  List<Vocabulary> getVocabularies() throws RegistryException;

  /**
   * Register a new resource with the GBIF registry and associate with to the given organisation.
   *
   * @return the newly created registry key for the resource
   */
  UUID register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException;

  /**
   * Register an IPT instance against the GBIF Registry.
   *
   * @param organisation the organisation to register the IPT with
   */
  String registerIPT(Ipt ipt, Organisation organisation) throws RegistryException;

  /**
   * Update an IPT instance agains the GBIF Registry.
   */
  void updateIpt(Ipt ipt) throws RegistryException;

  /**
   * Updates a resource's registration in the GBIF Registry. This means that the resource's metadata is updated,
   * including its services. The resource's owning organization is used to authenticate the update with the Registry.
   * The resource's owning organization, and hosting organization are set only during initial registration, and will
   * never change during the update.
   *
   * @throws IllegalArgumentException is resource is not registered yet
   */
  void updateResource(Resource resource) throws RegistryException, IllegalArgumentException;

  /**
   * Validate whether an Organization identified by its key and password is registered.
   *
   * @param organisationKey Organisation key
   * @param password Organisation password (in Registry)
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
   * @return list of all registered Resources associated to an Organization
   */
  List<Resource> getOrganisationsResources(String key);

}
