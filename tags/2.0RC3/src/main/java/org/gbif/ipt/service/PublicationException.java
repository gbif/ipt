/**
 * 
 */
package org.gbif.ipt.service;

/**
 * The base class used to indicate types of resource publication errors. All configuration must provide a message and a
 * cause
 * 
 * @author markus
 */
public class PublicationException extends RuntimeException {
  public enum TYPE {
    DWCA, EML, REGISTRY, LOCKED
  };

  protected TYPE type;

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   * 
   * @param type Is stored in the exception
   * @param message The message to use for logging (not display through the web application)
   */
  public PublicationException(TYPE type, String message) {
    super(message);
    this.type = type;
  }

  public PublicationException(TYPE type, String message, Exception e) {
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
