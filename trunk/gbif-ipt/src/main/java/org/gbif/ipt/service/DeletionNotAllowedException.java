package org.gbif.ipt.service;


/** Exception thrown when removing an entity is not allowed for some reason
 * @author markus
 *
 */
public class DeletionNotAllowedException extends Exception {
	public enum Reason {LAST_ADMIN, LAST_RESOURCE_MANAGER};
	protected Reason reason;

	public DeletionNotAllowedException(Reason reason){
		this.reason=reason;
	}
	
	/**
	 * @return the reason why the deletion is not possible. This allows for internationalized display
	 */
	public Reason getReason() {
		return reason; 
	}
}
