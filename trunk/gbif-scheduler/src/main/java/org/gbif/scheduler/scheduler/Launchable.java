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
	public void launch(Map<String, Object> seed, String baseDir) throws Exception;
}
