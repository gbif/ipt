package org.gbif.ipt.service.registry;

import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;

import com.google.inject.ImplementedBy;

import java.util.UUID;

@ImplementedBy(RegistryManagerImpl.class)
public interface RegistryManager {

  /**
   * Register a new resource with the GBIF registry and associate with to the given organisation.
   * 
   * @param resource
   * @param organisation
   * @return the newly created registry key for the resource
   * @throws RegistryException
   */
  public UUID register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException;

  /**
   * Register an IPT instance against the GBIF Registry
   * 
   * @param ipt
   * @return
   * @throws RegistryException
   */
  public String registerIPT(Ipt ipt) throws RegistryException;

  /**
   * Sets the Registry credentials
   * 
   * @param username
   * @param password
   */
  public void setRegistryCredentials(String username, String password);

}
