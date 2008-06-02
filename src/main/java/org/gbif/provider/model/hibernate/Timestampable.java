package org.gbif.provider.model.hibernate;

import java.util.Date;

import org.appfuse.model.User;

/**
 * Persistent classes that want to track their datetime last modified should implement this interface 
 * so the AuditInterceptor can set these automatically. 
 * Needs to have a property called "modified" which has a Date type, not only the get method!
 * @author markus
 *
 */
public interface Timestampable {
	public void setCreated(Date when);
	public void setCreator(User modifier);	
	public void setModified(Date when);
	public void setModifier(User modifier);	
}
