package org.gbif.ipt.service.registry;

import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.registry.impl.RegistryManagerImpl;

import com.google.inject.ImplementedBy;

@ImplementedBy(RegistryManagerImpl.class)
public interface RegistryManager {

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
