/**
 * 
 */
package org.gbif.ipt.action.admin;

import java.util.List;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.registry.Organisation;
import org.gbif.ipt.service.admin.GBIFRegistryManager;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the GBIF registry options
 * @author tim
 */
public class GBIFRegistryAction extends BaseAction {
	private static final long serialVersionUID = -6522969037528106704L;

	// the managers for the action
	// managers are injected by Google Guice
	@Inject private GBIFRegistryManager registryManager;
	
	// these are transient properties that are set on a per request basis
	// getters and setters are called by the Struts2 interceptors based on the 
	// http request submitted
	private List<Organisation> organisations;

	// Proper action methods
	public String initRegistration(){
		organisations=registryManager.listAllOrganisations();
		return SUCCESS;
	}
	
	// Getters / Setters follow
	/**
	 * @return the organisations
	 */
	public List<Organisation> getOrganisations() {
		return organisations;
	}
	
}
