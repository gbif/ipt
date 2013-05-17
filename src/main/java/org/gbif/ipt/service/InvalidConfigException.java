package org.gbif.ipt.service;

/**
 * The base class used to indicate types of configuration errors. All configuration must provide a message and a cause.
 */
public class InvalidConfigException extends RuntimeException {

  /**
   * Generated.
   */
  private static final long serialVersionUID = 8568781101282056985L;

  public enum TYPE {
    /**
     * Invalid Base URL specified.
     */
    INVALID_BASE_URL,
    /**
     * Base URL specified is inaccessible.
     */
    INACCESSIBLE_BASE_URL,
    /**
     * Data directory is invalid.
     */
    INVALID_DATA_DIR,
    /**
     * Data directory is not writable.
     */
    NON_WRITABLE_DATA_DIR,
    /**
     * Configuration changes failed to be written.
     */
    CONFIG_WRITE,
    /**
     * User account configuration could not be read.
     */
    USER_CONFIG,
    /**
     * Extension is invalid for some reason. For example it has XML breaking characters and can't be parsed.
     */
    INVALID_EXTENSION,
    /**
     * The IPT mode (test or production) has been set, and this cannot change.
     */
    DATADIR_ALREADY_REGISTERED,
    /**
     * Visibility change not permitted: registered status is final.
     */
    RESOURCE_ALREADY_REGISTERED,
    /**
     * Registration configuration cannot be read.
     */
    REGISTRATION_CONFIG,
    /**
     * Resource configuration cannot be read.
     */
    RESOURCE_CONFIG,
    /**
     * An EML template exception has been encountered.
     */
    EML,
    /**
     * Proxy specified is invalid.
     */
    INVALID_PROXY,
    /**
     * Latitude/longitude are in invalid format.
     */
    FORMAT_ERROR,
    /**
     * An extension with given rowType has already been installed.
     */
    ROWTYPE_ALREADY_INSTALLED,
    /**
     * The resource cannot be migrated as configured.
     */
    INVALID_RESOURCE_MIGRATION,
    /**
     * The resource cannot be migrated as configured.
     */
    AUTO_PUBLISHING_ALREADY_OFF
  }

  protected TYPE type;

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   *
   * @param type    Is stored in the exception
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
