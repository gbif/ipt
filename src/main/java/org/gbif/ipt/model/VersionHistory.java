/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.model;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.ipt.model.voc.PublicationStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
  // record counts by extension: Map<rowType, count>
  private Map<String, Integer> recordsByExtension = new HashMap<>();

  public VersionHistory(BigDecimal version, Date released, PublicationStatus publicationStatus) {
    this.version = version.toPlainString();
    this.released = released;
    this.publicationStatus = publicationStatus;
  }

  public VersionHistory(BigDecimal version, PublicationStatus publicationStatus) {
    this.version = version.toPlainString();
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
   * @return the user that last modified the history (the change summary is editable after publication)
   */
  @Nullable
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

  /**
   * @return map containing record counts (map value) by extension (map key)
   */
  public Map<String, Integer> getRecordsByExtension() {
    return recordsByExtension;
  }

  /**
   * @param recordsByExtension map of record counts (map value) by extension (map key)
   */
  public void setRecordsByExtension(Map<String, Integer> recordsByExtension) {
    this.recordsByExtension = recordsByExtension;
  }
}
