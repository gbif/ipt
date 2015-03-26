package org.gbif.ipt.model.voc;

/**
 * Enumeration that describes the status of an identifier.
 */
public enum IdentifierStatus {
  /**
   * An identifier that is not reserved, meaning it is not known to the DOI registration agency at all yet.
   */
  UNRESERVED,
  /**
   * An identifier that is not public, but will go public the next time the resource is published so long as
   * the resource is public.
   */
  PUBLIC_PENDING_PUBLICATION,
  /**
   * An identifier that is public, meaning that it is known to resolvers.
   */
  PUBLIC,
  /**
   * An identifier that is public, but the object it references is no longer available.
   */
  UNAVAILABLE
}
