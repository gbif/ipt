/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.admin.impl.GBIFRegistryManagerImpl;

import com.google.inject.ImplementedBy;

import java.net.URL;
import java.util.List;

/**
 * This interface details ALL methods associated with the GBIF Registry.
 * 
 * @author tim
 * @author josecuadra
 */
@ImplementedBy(GBIFRegistryManagerImpl.class)
public interface GBIFRegistryManager {

  /**
   * @return URL to the json list of registered dwc extensions in GBIF
   */
  public URL getExtensionListUrl();

  /**
   * Calls the central registry to receive a list of the Organisations that are available
   * 
   * @return The list of all available organisations
   */
  public List<Organisation> listAllOrganisations();

  /**
   * Validate the credentials provided for an Organisation
   * 
   * @param key
   * @param password
   * @return true in case credentials are valid, false if not
   */
  public boolean validateOrganisation(String key, String password);
}
