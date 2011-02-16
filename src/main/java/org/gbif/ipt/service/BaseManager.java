/**
 * 
 */
package org.gbif.ipt.service;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;

import org.apache.log4j.Logger;

/**
 * Base of all manager implementations
 * 
 * @author tim
 */
public abstract class BaseManager {
  protected Logger log = Logger.getLogger(this.getClass());
  protected AppConfig cfg;
  protected DataDir dataDir;

  private BaseManager() {
    super();
  }

  public BaseManager(AppConfig cfg, DataDir dataDir) {
    super();
    this.cfg = cfg;
    this.dataDir = dataDir;
  }

}