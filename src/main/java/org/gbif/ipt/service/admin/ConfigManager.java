/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.config.ConfigManagerImpl;
import org.gbif.ipt.service.InvalidConfigException;

import com.google.inject.ImplementedBy;

import java.io.File;
import java.net.URL;

/**
 * This interface details ALL methods associated with an IPT configuration.
 * This includes configuration such as the deployment URL, the data directory
 * etc.
 * 
 * @author tim
 */
@ImplementedBy(ConfigManagerImpl.class)
public interface ConfigManager {

  /**
   * @return the local host name
   */
  public String getHostName();

  /**
   * Checks for an existing base URL from the AppConfig, and ensures that it is accessible over http.
   * 
   * @return false if there is no base URL set or if it is inaccessible
   */
  public boolean isBaseURLValid();

  /**
   * Loads all in memory configuration persisting in the datadir. This is:
   * - main IPT configuration, AppConfig
   * - user accounts
   * - list of configured resources
   * - reload lucene indices
   * 
   * @throws InvalidConfigException
   */
  public void loadDataDirConfig() throws InvalidConfigException;

  /**
   * Persists the main IPT AppConfig configuration which can be modified for simple properties independently of this
   * manager as its a singleton.
   * Highly recommended is to use the setConfigProperty method in this manager though to edit the configuration.
   * 
   * @throws InvalidConfigException
   */
  public void saveConfig() throws InvalidConfigException;

  /**
   * Simple wrapper around AppConfig to set the google analytics key for the IPT
   * The modified AppConfig is not immediately persisted - remember to call save() at some point!
   * 
   * @param key
   * @throws InvalidConfigException
   */
  public void setAnalyticsKey(String key) throws InvalidConfigException;

  /**
   * Sets the base URL for the IPT installation.
   * This affects all accessible resources
   * through the IPT. The baseURL cannot be determined programmatically as it is not possible
   * to know things such as virtual host definitions, URL rewriting or proxies that might come
   * into play in the deployment. If any services have been registered, then this will communicate through
   * the registryAPI to update those URLs that have changed.
   * The modified AppConfig is not immediately persisted - remember to call save() at some point!
   * 
   * @param baseURL The new baseURL for the IPT
   * @throws InvalidConfigException If the URL appears to be localhost, 127.0.0.1 or something that clearly
   *         will not be addressable from the internet.
   */
  public void setBaseURL(URL baseURL) throws InvalidConfigException;

  /**
   * Generic method to set an appconfig property in memory without persisting it
   * 
   * @param key
   * @param value
   */
  public void setConfigProperty(String key, String value);

  /**
   * Tries to assign a new data directory to the IPT.
   * This has huge a impact as all configuration apart the data dirs location itself is stored in the data directory.
   * If the directory provided is empty a new skeleton dir will be setup.
   * If the data dir is valid and writable the configuration is loaded via loadDataDirConfig().
   * 
   * @param dataDir a valid, writable directory. If empty a new skeleton will be used, if its an existing, valid IPT
   *        data dir it will be read.
   * @return true if a new data dir was created, false when an existing was read
   * @throws InvalidConfigException
   */
  public boolean setDataDir(File dataDir) throws InvalidConfigException;

  /**
   * Simple wrapper around AppConfig to set the IPT debug mode.
   * The modified AppConfig is not immediately persisted - remember to call save() at some point!
   * 
   * @param key
   * @throws InvalidConfigException
   */
  public void setDebugMode(boolean debug) throws InvalidConfigException;

  /**
   * Simple wrapper around AppConfig to en/disable google analytics for all IPTs monitored by gbif
   * The modified AppConfig is not immediately persisted - remember to call save() at some point!
   * 
   * @param key
   * @throws InvalidConfigException
   */
  public void setGbifAnalytics(boolean useGbifAnalytics) throws InvalidConfigException;

  public void setIptLocation(Double lat, Double lon) throws InvalidConfigException;

  /**
   * @param proxy
   */
  public void setProxy(String proxy) throws InvalidConfigException;

  /**
   * @return true if the basic setup routine is completed, false otherwise
   */
  public boolean setupComplete();

}
