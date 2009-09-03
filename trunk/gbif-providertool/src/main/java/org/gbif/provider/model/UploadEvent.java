/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Event keeping track of upload statistics to the cache for a certain resource.
 * uploaded records is the total of all uploaded records and thus the new record
 * count for the resource. The following should be true: recordsUploaded =
 * previousRecordCount + recordsAdded - recordsDeleted
 * 
 */
@Entity
public class UploadEvent implements ResourceRelatedObject {
  private Long id;
  private Resource resource;
  private Date executionDate;
  private long duration;
  private int emlVersion;
  private int recordsUploaded;
  private int recordsDeleted;
  private int recordsChanged;
  private int recordsAdded;
  private int recordsErroneous;
  // job metadata. needed for linking to eventLogs
  private int jobSourceId;
  private int jobSourceType;

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof UploadEvent)) {
      return false;
    }
    UploadEvent rhs = (UploadEvent) object;
    return new EqualsBuilder().append(this.recordsDeleted, rhs.recordsDeleted).append(
        this.recordsChanged, rhs.recordsChanged).append(this.recordsUploaded,
        rhs.recordsUploaded).append(this.recordsAdded, rhs.recordsAdded).append(
        this.executionDate, rhs.executionDate).append(this.resource,
        rhs.resource).append(this.id, rhs.id).isEquals();
  }

  public long getDuration() {
    return duration;
  }

  public int getEmlVersion() {
    return emlVersion;
  }

  public Date getExecutionDate() {
    return executionDate;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  public int getJobSourceId() {
    return jobSourceId;
  }

  public int getJobSourceType() {
    return jobSourceType;
  }

  public int getRecordsAdded() {
    return recordsAdded;
  }

  public int getRecordsChanged() {
    return recordsChanged;
  }

  public int getRecordsDeleted() {
    return recordsDeleted;
  }

  public int getRecordsErroneous() {
    return recordsErroneous;
  }

  public int getRecordsUploaded() {
    return recordsUploaded;
  }

  @ManyToOne
  @JoinColumn(name = "resource_fk", nullable = false)
  public Resource getResource() {
    return resource;
  }

  @Transient
  public Long getResourceId() {
    return resource.getId();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(-1475384193, 1013469649).append(
        this.recordsDeleted).append(this.recordsChanged).append(
        this.recordsUploaded).append(this.recordsAdded).append(
        this.executionDate).append(this.resource).toHashCode();
  }

  /**
   * set duration to ms since exec date and now
   */
  public void setDuration() {
    this.duration = new Date().getTime() - this.executionDate.getTime();
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public void setEmlVersion(int emlVersion) {
    this.emlVersion = emlVersion;
  }

  public void setExecutionDate(Date executionDate) {
    this.executionDate = executionDate;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setJobSourceId(int jobSourceId) {
    this.jobSourceId = jobSourceId;
  }

  public void setJobSourceType(int jobSourceType) {
    this.jobSourceType = jobSourceType;
  }

  public void setRecordsAdded(int recordsAdded) {
    this.recordsAdded = recordsAdded;
  }

  public void setRecordsChanged(int recordsChanged) {
    this.recordsChanged = recordsChanged;
  }

  public void setRecordsDeleted(int recordsDeleted) {
    this.recordsDeleted = recordsDeleted;
  }

  public void setRecordsErroneous(int recordsErroneous) {
    this.recordsErroneous = recordsErroneous;
  }

  public void setRecordsUploaded(int recordsUploaded) {
    this.recordsUploaded = recordsUploaded;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("recordsUploaded",
        this.recordsUploaded).append("resource", this.resource).append("id",
        this.id).append("recordsChanged", this.recordsChanged).append(
        "executionDate", this.executionDate).append("recordsAdded",
        this.recordsAdded).append("recordsDeleted", this.recordsDeleted).toString();
  }

}
