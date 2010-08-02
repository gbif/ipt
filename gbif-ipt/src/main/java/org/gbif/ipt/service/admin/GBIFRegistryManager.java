/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.impl.GBIFRegistryManagerImpl;

import com.google.inject.ImplementedBy;

import java.util.List;
import java.util.UUID;

/**
 * This interface details ALL methods associated with the GBIF Registry.
 * 
 * @author tim
 * @author josecuadra
 */
@ImplementedBy(GBIFRegistryManagerImpl.class)
public interface GBIFRegistryManager {

  /**
   * Remove a registered resource from the registry.
   * Registered resources cant made private again, so this method should only be called when deleting a resource.
   * 
   * @param resource
   * @throws RegistryException
   */
  public void deregister(Resource resource) throws RegistryException;

  /**
   * Calls the central registry to receive a list of the Organisations that are available
   * 
   * @return The list of all available organisations
   */
  public List<Organisation> listAllOrganisations();

  /**
   * Register a new resource with the GBIF registry and associate with to the given organisation.
   * 
   * @param resource
   * @param organisation
   * @return the newly created registry key for the resource
   * @throws RegistryException
   */
  public UUID register(Resource resource, Organisation organisation) throws RegistryException;

  /**
   * Validate the credentials provided for an Organisation
   * 
   * @param key
   * @param password
   * @return true in case credentials are valid, false if not
   */
  public boolean validateOrganisation(String key, String password);
}
