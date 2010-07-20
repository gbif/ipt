/**
 * 
 */
package org.gbif.ipt.action.admin;

import java.util.List;

import org.gbif.ipt.action.FormAction;
import org.gbif.ipt.model.registry.BriefOrganisation;
import org.gbif.ipt.service.admin.GBIFRegistryManager;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the GBIF registry options
 * @author tim
 */
public class GBIFRegistryAction extends FormAction {
	private static final long serialVersionUID = -6522969037528106704L;

	// the managers for the action
	// managers are injected by Google Guice
	@Inject private GBIFRegistryManager registryManager;
	
	// these are transient properties that are set on a per request basis
	// getters and setters are called by the Struts2 interceptors based on the 
	// http request submitted
	private List<BriefOrganisation> organisations;

	// Proper action methods
	public String initRegistration(){
		log.debug("getting list of organisations");
		organisations=registryManager.listAllOrganisations();
		log.debug("getting list of organisations" + organisations);
		return SUCCESS;
	}
	
	// Getters / Setters follow
	/**
	 * @return the organisations
	 */
	public List<BriefOrganisation> getOrganisations() {
		return organisations;
	}
	
}
