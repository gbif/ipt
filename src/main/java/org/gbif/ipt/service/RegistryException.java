/**
 * 
 */
package org.gbif.ipt.service;

/**
 * The base class used to indicate types of errors occurring during interaction with the GBIF registry.
 * All exceptions must provide a message and a cause
 * 
 * @author markus
 */
public class RegistryException extends RuntimeException {
  public enum TYPE {
    NOT_AUTHORISED, RESOURCE_EXISTS_ALREADY, MISSING_METADATA, BAD_RESPONSE, IO_ERROR, FAILED, UNKNOWN, PROXY, SITE_DOWN, NO_INTERNET
  };

  protected TYPE type;

  public RegistryException(TYPE type, Exception e) {
    super(e.getMessage(), e);
    this.type = type;
  }

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   * 
   * @param type Is stored in the exception
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
}
