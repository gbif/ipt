package org.gbif.ipt.service;

import org.gbif.ipt.action.BaseAction;

/**
 * The base class used to indicate types of errors occurring during interaction with the GBIF registry.
 * All exceptions must provide a message and a cause.
 */
public class RegistryException extends RuntimeException {

  public enum TYPE {
    /**
     * Proper credentials weren't specified.
     */
    NOT_AUTHORISED,
    /**
     * No key was returned for registered resource.
     */
    MISSING_METADATA,
    /**
     * The response from the Registry was empty or invalid.
     */
    BAD_RESPONSE,
    /**
     * Some kind of IO error occurred.
     */
    IO_ERROR,
    /**
     * Generic exception: Registration/update with Registry failed.
     */
    FAILED,
    /**
     * Unknown failure occurred while communicating with Registry.
     */
    UNKNOWN,
    /**
     * A connection exception occurred. Likely Proxy or Firewall related.
     */
    PROXY,
    /**
     * If server could connect to Google, but not to GBIF Registry.
     */
    SITE_DOWN,
    /**
     * There is no connection to the Internet. Indicates that the IP address of a host could not be determined.
     */
    NO_INTERNET
  }

  protected TYPE type;

  public RegistryException(TYPE type, Exception e) {
    super(e.getMessage(), e);
    this.type = type;
  }

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   *
   * @param type    Is stored in the exception
   * @param message The message to use for logging (not display through the web application)
   */
  public RegistryException(TYPE type, String message) {
    super(message);
    this.type = type;
  }

  public RegistryException(TYPE type, String message, Exception e) {
    super(message, e);
    this.type = type;
  }

  /**
   * @return the type of configuration exception. This allows for internationalized display
   */
  public TYPE getType() {
    return type;
  }

  /**
   * Depending on the RegistryException TYPE, retrieves an i18n message, and returns it. Ideally, the message returned
   * will be the most specific error possible as to why the exception was thrown.
   *
   * @param type   RegistryException.TYPE
   * @param action BaseAtion
   *
   * @return log message
   */
  public static String logRegistryException(RegistryException.TYPE type, BaseAction action) {
    // retrieve specific log message, depending on TYPE
    String msg = action.getText("admin.registration.error.registry");
    if (type != null) {
      if (type == RegistryException.TYPE.PROXY) {
        msg = action.getText("admin.registration.error.proxy");
      } else if (type == RegistryException.TYPE.SITE_DOWN) {
        msg = action.getText("admin.registration.error.siteDown");
      } else if (type == RegistryException.TYPE.NO_INTERNET) {
        msg = action.getText("admin.registration.error.internetConnection");
      } else if (type == RegistryException.TYPE.BAD_RESPONSE) {
        msg = action.getText("admin.registration.error.badResponse");
      } else if (type == RegistryException.TYPE.IO_ERROR) {
        msg = action.getText("admin.registration.error.io");
      } else if (type == RegistryException.TYPE.UNKNOWN) {
        msg = action.getText("admin.registration.error.unknown");
      }
    }
    return msg;
  }
}
