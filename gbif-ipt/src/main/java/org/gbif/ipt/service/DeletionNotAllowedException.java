package org.gbif.ipt.service;

/**
 * Exception thrown when removing an entity is not allowed for some reason.
 */
public class DeletionNotAllowedException extends Exception {

  public enum Reason {
    /**
     * Because this is the last administrator. Used while deleting a user.
     */
    LAST_ADMIN,
    /**
     * Because this is the last resource manager. Used while deleting a user.
     */
    LAST_RESOURCE_MANAGER,
    /**
     * Because the extension has been mapped in at least one resource. Used while deleting an extension.
     */
    EXTENSION_MAPPED,
    /**
     * Because the vocabulary is a default vocabulary. Used while deleting a vocabulary.
     */
    BASE_VOCABULARY,
    /**
     * Because the vocabulary is used in a registered extension. Used while deleting a vocabulary.
     */
    VOCABULARY_USED_IN_EXTENSION,
    /**
     * Because there is at least one resource registered to this organization. Used while deleting an organization.
     */
    RESOURCE_REGISTERED_WITH_ORGANISATION,
    /**
     * Because the IPT is registered against this organization. Used while deleting an organization.
     */
    IPT_REGISTERED_WITH_ORGANISATION,
    /**
     * Because some registry error occurred.
     */
    REGISTRY_ERROR
  }

  protected Reason reason;

  public DeletionNotAllowedException(Reason reason) {
    this.reason = reason;
  }

  public DeletionNotAllowedException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  /**
   * @return the reason why the deletion is not possible. This allows for internationalized display
   */
  public Reason getReason() {
    return reason;
  }
}
