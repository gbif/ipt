package org.gbif.ipt.service;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base of all manager implementations.
 */
public abstract class BaseManager {

  protected final Logger LOG = LogManager.getLogger(this.getClass());
  protected AppConfig cfg;
  protected DataDir dataDir;

  private BaseManager() {
  }

  public BaseManager(AppConfig cfg, DataDir dataDir) {
    this.cfg = cfg;
    this.dataDir = dataDir;
  }

}
