/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.service.admin.impl.OrganisationManagerImpl;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the Organisations associated with the IPT.
 *  
 * @author tim
 */
@ImplementedBy(OrganisationManagerImpl.class)
public interface OrganisationManager {
}
