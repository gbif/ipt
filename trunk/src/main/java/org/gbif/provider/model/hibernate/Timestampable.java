package org.gbif.provider.model.hibernate;

import java.util.Date;

/**
 * Persistent classes that want to track their datetime last modified should implement this interface 
 * so the AuditInterceptor can set these automatically
 * @author markus
 *
 */
public interface Timestampable {
	public void setModified(Date when);
}
