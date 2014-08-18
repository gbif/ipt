package org.gbif.ipt.model;

import org.gbif.ipt.model.voc.IdentifierStatus;

import java.util.Date;

/**
 * Class representing all the information about a version of a resource.
 */
public class VersionHistory {

  private String doi;
  private String version;
  private Date released;
  private IdentifierStatus status;
  private User modifiedBy;
  private String changeSummary;
  private boolean isLatest;
  //private UpdateFrequency updateFrequency;

  public VersionHistory() {
  }

  /**
   * TODO
   * @return
   */
  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  /**
   * TODO
   * @return
   */
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * TODO
   * @return
   */
  public Date getReleased() {
    return released;
  }

  public void setReleased(Date released) {
    this.released = released;
  }

  /**
   * TODO
   * @return
   */
  public IdentifierStatus getStatus() {
    return status;
  }

  public void setStatus(IdentifierStatus status) {
    this.status = status;
  }

  /**
   * TODO
   * @return
   */
  public User getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(User modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  /**
   * TODO
   * @return
   */
  public String getChangeSummary() {
    return changeSummary;
  }

  public void setChangeSummary(String changeSummary) {
    this.changeSummary = changeSummary;
  }

  /**
   * TODO
   * @return
   */
  public boolean isLatest() {
    return isLatest;
  }

  public void setLatest(boolean isLatest) {
    this.isLatest = isLatest;
  }
}
