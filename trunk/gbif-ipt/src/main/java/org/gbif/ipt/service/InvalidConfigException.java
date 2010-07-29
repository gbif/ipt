/**
 * 
 */
package org.gbif.ipt.service;

/**
 * The base class used to indicate types of configuration errors. All configuration must provide a message and a cause
 * 
 * @author tim
 */
public class InvalidConfigException extends Exception {
  public enum TYPE {
    INVALID_BASE_URL, INVALID_DATA_DIR, NON_WRITABLE_DATA_DIR, IPT_CONFIG_WRITE, USER_CONFIG, REGISTRY_CONFIG, INVALID_EXTENSION, DATADIR_ALREADY_REGISTERED, ORGANISATION_CONFIG, RESOURCE_CONFIG
  };

  protected TYPE type;

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   * 
   * @param type Is stored in the exception
   * @param message The message to use for logging (not display through the web application)
   */
  public InvalidConfigException(TYPE type, String message) {
    super(message);
    this.type = type;
  }

  /**
   * @return the type of configuration exception. This allows for internationalized display
   */
  public TYPE getType() {
    return type;
  }
}
