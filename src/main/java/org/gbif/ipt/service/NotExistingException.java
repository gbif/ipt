package org.gbif.ipt.service;

/** Exception thrown when the entity requested for modification/deletion doesnt exist
 * @author markus
 *
 */
public class NotExistingException extends Exception {

	public NotExistingException(Class entityClass){
		
	}
}
