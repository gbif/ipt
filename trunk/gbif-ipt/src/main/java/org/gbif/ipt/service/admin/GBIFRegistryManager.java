/**
 * 
 */
package org.gbif.ipt.service.admin;

import java.util.List;

import org.gbif.ipt.model.registry.Organisation;
import org.gbif.ipt.service.admin.impl.GBIFRegistryManagerImpl;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the GBIF Registry.
 *  
 * @author tim
 * @author josecuadra
 */
@ImplementedBy(GBIFRegistryManagerImpl.class)
public interface GBIFRegistryManager {
	
	  /**
	   * Calls the central registry to receive a list of the Organisations that are
	   * available
	   * 
	   * @return The list of all available organisations
	   */
	  List<Organisation> listAllOrganisations();
}
