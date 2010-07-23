/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.registration.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.impl.OrganisationManagerImpl;

import com.google.inject.ImplementedBy;

import java.io.IOException;

/**
 * This interface details ALL methods associated with the Organisations associated with the IPT.
 * 
 * @author tim
 * @author josecuadra
 */
@ImplementedBy(OrganisationManagerImpl.class)
public interface OrganisationManager {

  /**
   * Associate a new organisation to this IPT installation, but doesnt persist the change.
   * 
   * @throws AlreadyExistingException
   */
  public void add(Organisation organisation) throws AlreadyExistingException;

  /**
   * Saves all organisations (associated to this IPT) from the manager to file. Needs to be manually called if
   * organisation properties have been modified or if organisations have been added or removed.
   * 
   * @throws IOException
   */
  public void save() throws IOException;
}
