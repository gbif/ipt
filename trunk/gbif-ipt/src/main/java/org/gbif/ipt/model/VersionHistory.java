package org.gbif.ipt.model;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Class representing all the essential information about a historical version of a resource.
 */
public class VersionHistory {

  private DOI doi;
  private String version;
  private Date released;
  private IdentifierStatus status;
  private PublicationStatus publicationStatus;
  private User modifiedBy;
  private String changeSummary;
  private int recordsPublished;

  public VersionHistory(BigDecimal version, Date released, User modifiedBy, PublicationStatus publicationStatus) {
    this.version = version.toPlainString();
    this.released = released;
    this.modifiedBy = modifiedBy;
    this.publicationStatus = publicationStatus;
  }

  /**
   * @return the doi of this version, always in prefix/suffix format excluding "doi:", e.g. 10.1234/qu83ng
   */
  @Nullable
  public DOI getDoi() {
    return doi;
  }

  public void setDoi(DOI doi) {
    this.doi = doi;
  }

  /**
   * @return the version number
   */
  @NotNull
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the date this version was released
   */
  @NotNull
  public Date getReleased() {
    return released;
  }

  public void setReleased(Date released) {
    this.released = released;
  }

  /**
   * @return the doi status
   */
  @Nullable
  public IdentifierStatus getStatus() {
    return status;
  }

  public void setStatus(IdentifierStatus status) {
    this.status = status;
  }

  /**
   * @return the date this version history was last modified (the change summary is editable after publication)
   */
  @NotNull
  public User getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(User modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  /**
   * @return the change summary for this version
   */
  @Nullable
  public String getChangeSummary() {
    return changeSummary;
  }

  public void setChangeSummary(String changeSummary) {
    this.changeSummary = changeSummary;
  }

  /**
   * @return the number of records published in this version
   */
  public int getRecordsPublished() {
    return recordsPublished;
  }

  public void setRecordsPublished(int recordsPublished) {
    this.recordsPublished = recordsPublished;
  }

  /**
   * @return the visibility of the resource, e.g. was it private, public, registered, deleted?
   */
  @NotNull
  public PublicationStatus getPublicationStatus() {
    return publicationStatus;
  }

  public void setPublicationStatus(PublicationStatus publicationStatus) {
    this.publicationStatus = publicationStatus;
  }
}
