package org.gbif.ipt.service;

/**
 * Exception thrown when undeleting a resource fails for some reason.
 */
public class UndeletNotAllowedException extends Exception {

  public enum Reason {
    /**
     * Because the prefix of the DOI does not match the prefix of the DOI account activated in the IPT.
     */
    DOI_PREFIX_NOT_MATCHING,
    /**
     * Because the resource organisation is no longer associated to the IPT.
     */
    ORGANISATION_NOT_ASSOCIATED_TO_IPT,
    /**
     * Because the DOI status is not deleted (after resolving it with DOI registration agency).
     */
    DOI_NOT_DELETED,
    /**
     * Because the DOI is no longer registered (no longer resolves).
     */
    DOI_DOES_NOT_EXIST,
    /**
     * Because some DOI Registration Agency error occurred.
     */
    DOI_REGISTRATION_AGENCY_ERROR,
    /**
     * Because the resource is registered with an organization that no longer exists.
     */
    RESOURCE_DOI_REGISTERED_WITH_ORGANISATION,
  }

  protected Reason reason;

  public UndeletNotAllowedException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  /**
   * @return the reason why the undelete operation is not possible. This allows for internationalized display
   */
  public Reason getReason() {
    return reason;
  }
}
