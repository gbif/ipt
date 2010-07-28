/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.OrganisationsManagerImpl;

import com.google.inject.ImplementedBy;

import java.io.IOException;
import java.util.List;

/**
 * This interface details ALL methods associated with the Organisations associated with the IPT.
 * 
 * @author tim
 * @author josecuadra
 */
@ImplementedBy(OrganisationsManagerImpl.class)
public interface OrganisationsManager {

  /**
   * Associate a new organisation to this IPT installation, but doesnt persist the change.
   * 
   * @throws AlreadyExistingException
   */
  public void add(Organisation organisation) throws AlreadyExistingException;

  /**
   * Returns a single organisation associated to the key
   * 
   * @param key
   * @return
   */
  public Organisation get(String key);

  /**
   * Returns list of all associated organisations
   * 
   * @return
   */
  public List<Organisation> list();

  /**
   * Loads all user associated organisations from file into the manager
   */
  public void load() throws InvalidConfigException;

  /**
   * Saves all organisations (associated to this IPT) from the manager to file. Needs to be manually called if
   * organisation properties have been modified or if organisations have been added or removed.
   * 
   * @throws IOException
   */
  public void save() throws IOException;
}
