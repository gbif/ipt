package org.gbif.ipt.service.registry;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;

import com.google.inject.ImplementedBy;

import java.util.List;
import java.util.UUID;

@ImplementedBy(RegistryManagerImpl.class)
public interface RegistryManager {

  public void deregister(Resource resource) throws RegistryException;

  /**
   * Gets list of extensions
   * 
   * @return
   */
  public List<Extension> getExtensions() throws RegistryException;

  /**
   * List of organisations
   * 
   * @return
   */
  public List<Organisation> getOrganisations() throws RegistryException;

  /**
   * Gets list of vocabularies, but only the basic metadata, i.e. each without the list concepts
   * 
   * @return
   */
  public List<Vocabulary> getVocabularies() throws RegistryException;

  /**
   * Register a new resource with the GBIF registry and associate with to the given organisation.
   * 
   * @param resource
   * @param organisation
   * @param ipt
   * @param eml
   * @return the newly created registry key for the resource
   * @throws RegistryException
   */
  public UUID register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException;

  /**
   * Register an IPT instance against the GBIF Registry
   * 
   * @param ipt
   * @param organisation the organisation to register the IPT with
   * @return
   * @throws RegistryException
   */
  public String registerIPT(Ipt ipt, Organisation organisation) throws RegistryException;

  /**
   * Update an IPT instance agains the GBIF Registry
   * 
   * @param ipt
   * @throws RegistryException
   */
  public void updateIpt(Ipt ipt) throws RegistryException;
  
  /**
   * Updates a resource's metadata with the GBIF registry and associate with to the given organisation.
   * 
   * @param resource
   * @param organisation
   * @param ipt
   * @param eml
   * @throws RegistryException
   * @throws IllegalArgumentException is resource is not registered yet
   */
  public void updateResource(Resource resource, Ipt ipt) throws RegistryException, IllegalArgumentException;

  public boolean validateOrganisation(String organisationKey, String password);

}
