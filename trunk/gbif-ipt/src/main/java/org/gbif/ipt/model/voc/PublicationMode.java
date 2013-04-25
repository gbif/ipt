package org.gbif.ipt.model.voc;

/**
 * Enumeration of possible publication modes for the dataset. The mode determines whether or not the dataset must be
 * republished manually every time, or whether it can be automatically published by the IPT.
 */
public enum PublicationMode {
  /**
   * The dataset will be auto-published. The resource manager has made a decision to auto-publish the dataset0.
   */
  AUTO_PUBLISH_ON,
  /**
   * The dataset will not be auto-published. The resource manager has not made a decision to auto-publish or not yet.
   */
  AUTO_PUBLISH_OFF,
  /**
   * The dataset should never be auto-published. The resource manager has made this decision.
   */
  AUTO_PUBLISH_NEVER
}
