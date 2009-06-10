/**
 * 
 */
package org.gbif.scheduler.scheduler;

import java.util.Map;

/**
 * Anything that is scheduled, must be launchable
 * @author timrobertson
 */
public interface Launchable {
	public static final String WEBAPP_DIR = "webappDir";
	public static final String JOB_ID = "jobId";
	
	public void launch(Map<String, Object> seed) throws Exception;
}
