/**
 * 
 */
package org.gbif.ipt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;

import com.google.inject.Inject;

/**
 * Base of all manager implementations
 * @author tim
 */
public abstract class BaseManager {
	protected Log log = LogFactory.getLog(this.getClass());
	@Inject protected AppConfig cfg;
	@Inject protected DataDir dataDir;
}