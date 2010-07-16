/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT
 * @author tim
 */
public class OrganisationsAction extends BaseAction {
	private static final long serialVersionUID = 7297470324204084809L;

	// the managers for the action
	// managers are injected by Google Guice
	
	// these are transient properties that are set on a per request basis
	// getters and setters are called by the Struts2 interceptors based on the 
	// http request submitted
	
	// Getters / Setters follow
	// Managers are marked for injection by Google Guice
}
