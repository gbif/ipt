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
   * Gets list of extensions.
   */
  List<Extension> getExtensions() throws RegistryException;

  /**
   * List of organisations.
   */
  List<Organisation> getOrganisations() throws RegistryException;

  /**
   * Gets list of vocabularies, but only the basic metadata, i.e. each without the list concepts.
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
   * Updates a resource's metadata with the GBIF registry and associate with to the given organisation.
   *
   * @throws IllegalArgumentException is resource is not registered yet
   */
  void updateResource(Resource resource, Ipt ipt) throws RegistryException, IllegalArgumentException;

  boolean validateOrganisation(String organisationKey, String password);

}
