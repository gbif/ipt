/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the vocabularies in use within the IPT
 * @author tim
 */
public class VocabulariesAction extends BaseAction {
	private static final long serialVersionUID = 7277675384287096912L;

	// the managers for the action
	// managers are injected by Google Guice
	
	// these are transient properties that are set on a per request basis
	// getters and setters are called by the Struts2 interceptors based on the 
	// http request submitted
	
	// Getters / Setters follow
	// Managers are marked for injection by Google Guice
}
