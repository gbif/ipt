package org.gbif.provider.model.hibernate;

import java.util.Date;

/**
 * Persistent classes that want to track their datetime last modified should implement this interface 
 * so the AuditInterceptor can set these automatically. 
 * Needs to have a property called "modified" which has a Date type, not only the get method!
 * @author markus
 *
 */
public interface Timestampable {
	public void setModified(Date when);
}
