/**
 * 
 */
package org.gbif.ipt.service;

/**
 * The base class used to indicate types of configuration errors. All configuration must provide a message and a cause
 * 
 * @author tim
 */
public class InvalidConfigException extends RuntimeException {
  /**
   * Generated
   */
  private static final long serialVersionUID = 8568781101282056985L;

  public enum TYPE {
    INVALID_BASE_URL, INACCESSIBLE_BASE_URL, INVALID_DATA_DIR, NON_WRITABLE_DATA_DIR, CONFIG_WRITE, USER_CONFIG, REGISTRY_CONFIG, INVALID_EXTENSION, DATADIR_ALREADY_REGISTERED, RESOURCE_ALREADY_REGISTERED, REGISTRATION_CONFIG, RESOURCE_CONFIG, EML, INVALID_PROXY, FORMAT_ERROR
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

  public InvalidConfigException(TYPE type, String message, Exception e) {
    super(message, e);
    this.type = type;
  }

  /**
   * @return the type of configuration exception. This allows for internationalized display
   */
  public TYPE getType() {
    return type;
  }
}
