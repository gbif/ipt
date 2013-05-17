package org.gbif.ipt.service;

/**
 * The base class used to indicate types of resource publication errors. All configuration must provide a message and a
 * cause.
 */
public class PublicationException extends RuntimeException {

  public enum TYPE {
    /**
     * Exception occurred while publishing DwC-A.
     */
    DWCA,
    /**
     * Exception occurred while publishing either the EML file.
     */
    EML,
    /**
     * Exception occurred while publishing either the RTF file.
     */
    RTF,
    /**
     * Exception occurred while communicating with the GBIF Registry.
     */
    REGISTRY,
    /**
     * Exception occurred while trying to schedule the resource for its next publication.
     */
    SCHEDULING,
    /**
     * Exception occurred while trying to publish the resource even though it is already being published.
     */
    LOCKED
  }

  protected TYPE type;

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   *
   * @param type    Is stored in the exception
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
