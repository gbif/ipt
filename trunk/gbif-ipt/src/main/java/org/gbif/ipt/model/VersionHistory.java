package org.gbif.ipt.model;

import org.gbif.ipt.model.voc.IdentifierStatus;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Class representing all the information about a version of a resource.
 */
public class VersionHistory {

  private String doi;
  private BigDecimal version;
  private Date released;
  private IdentifierStatus status;
  private User modifiedBy;
  private String changeSummary;
  private int recordsPublished;

  public VersionHistory(BigDecimal version, Date released, User modifiedBy) {
    this.version = version;
    this.released = released;
    this.modifiedBy = modifiedBy;
  }

  /**
   * TODO
   * @return
   */
  @Nullable
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
  @NotNull
  public BigDecimal getVersion() {
    return version;
  }

  public void setVersion(BigDecimal version) {
    this.version = version;
  }

  /**
   * TODO
   * @return
   */
  @NotNull
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
  @Nullable
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
  @NotNull
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
  @Nullable
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
  @Nullable
  public int getRecordsPublished() {
    return recordsPublished;
  }

  public void setRecordsPublished(int recordsPublished) {
    this.recordsPublished = recordsPublished;
  }
}
