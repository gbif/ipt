package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.RegistrationManagerImpl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the Organisations associated with the IPT.
 */
@ImplementedBy(RegistrationManagerImpl.class)
public interface RegistrationManager {

  /**
   * Associate a new organisation to this IPT installation, but doesnt persist the change.
   */
  Organisation addAssociatedOrganisation(Organisation organisation) throws AlreadyExistingException;

  /**
   * Register the IPT against an existing organisation.
   */
  Organisation addHostingOrganisation(Organisation organisation);

  /**
   * Add all the IPT specific data.
   */
  void addIptInstance(Ipt ipt);

  /**
   * Removes the specified organisation from the in memory list of organisations. See save() to persist this change to
   * files. An organisation can only be deleted if not linked with any resoruces
   *
   * @return organisation that has been removed or null if not existing
   *
   * @throws DeletionNotAllowedException if organisation is attached to any resource
   */
  Organisation delete(String key) throws DeletionNotAllowedException;

  /**
   * Returns a single organisation associated to the key.
   */
  Organisation get(String key);

  /**
   * Returns a single organisation associated to this UUID.
   */
  Organisation get(UUID key);

  /**
   * Returns the hosting organisation of this IPT.
   */
  Organisation getHostingOrganisation();

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
   * Loads all user associated organisations from file into the manager.
   */
  void load() throws InvalidConfigException;

  /**
   * Saves all organisations (associated to this IPT) from the manager to file. Needs to be manually called if
   * organisation properties have been modified or if organisations have been added or removed.
   */
  void save() throws IOException;

  /**
   * Sets the IPT password.
   */
  void setIptPassword(String password);
}
