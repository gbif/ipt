/**
 * 
 */
package org.gbif.ipt.service;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;

import com.google.inject.Inject;

import org.apache.log4j.Logger;

/**
 * Base of all manager implementations
 * 
 * @author tim
 */
public abstract class BaseManager {
  protected Logger log = Logger.getLogger(this.getClass());
  @Inject
  protected AppConfig cfg;
  @Inject
  protected DataDir dataDir;
}