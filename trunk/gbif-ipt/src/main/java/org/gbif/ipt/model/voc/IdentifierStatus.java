package org.gbif.ipt.model.voc;

/**
 * Enumeration that describes the status of an identifier.
 */
public enum IdentifierStatus {
  /**
   * An identifier that is not public, meaning it is not known to resolvers.
   */
  RESERVED,
  /**
   * An identifier that is public, meaning that it is known to resolvers.
   */
  PUBLIC,
  /**
   * An identifier that is public, but the object it references is no longer available.
   */
  UNAVAILABLE;
}
