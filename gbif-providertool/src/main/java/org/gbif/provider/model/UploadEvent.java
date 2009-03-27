package org.gbif.provider.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Event keeping track of upload statistics to the cache for a certain resource.
 * uploaded records is the total of all uploaded records and thus the new record count for the resource.
 * The following should be true: 
 * recordsUploaded = previousRecordCount + recordsAdded - recordsDeleted
 * @author markus
 *
 */
@Entity
public class UploadEvent implements ResourceRelatedObject{
	private Long id;
	private Resource resource;
	private Date executionDate;
	private int emlVersion;
	private int recordsUploaded;
	private int recordsDeleted;
	private int recordsChanged;
	private int recordsAdded;
	private int recordsErroneous;
	// job metadata. needed for linking to eventLogs
	private int jobSourceId;
	private int jobSourceType;
	
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name="resource_fk", nullable=false) 
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	@Transient
	public Long getResourceId() {
		return resource.getId();
	}
	
	public Date getExecutionDate() {
		return executionDate;
	}
	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}
	
	public int getEmlVersion() {
		return emlVersion;
	}
	public void setEmlVersion(int emlVersion) {
		this.emlVersion = emlVersion;
	}
	
	public int getRecordsUploaded() {
		return recordsUploaded;
	}
	public void setRecordsUploaded(int recordsUploaded) {
		this.recordsUploaded = recordsUploaded;
	}
	
	public int getRecordsDeleted() {
		return recordsDeleted;
	}
	public void setRecordsDeleted(int recordsDeleted) {
		this.recordsDeleted = recordsDeleted;
	}
	
	public int getRecordsChanged() {
		return recordsChanged;
	}
	public void setRecordsChanged(int recordsChanged) {
		this.recordsChanged = recordsChanged;
	}
	
	public int getRecordsAdded() {
		return recordsAdded;
	}
	public void setRecordsAdded(int recordsAdded) {
		this.recordsAdded = recordsAdded;
	}
	
	public int getRecordsErroneous() {
		return recordsErroneous;
	}
	public void setRecordsErroneous(int recordsErroneous) {
		this.recordsErroneous = recordsErroneous;
	}
	public int getJobSourceId() {
		return jobSourceId;
	}
	public void setJobSourceId(int jobSourceId) {
		this.jobSourceId = jobSourceId;
	}
	
	public int getJobSourceType() {
		return jobSourceType;
	}
	public void setJobSourceType(int jobSourceType) {
		this.jobSourceType = jobSourceType;
	}
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof UploadEvent)) {
			return false;
		}
		UploadEvent rhs = (UploadEvent) object;
		return new EqualsBuilder().append(this.recordsDeleted,
				rhs.recordsDeleted).append(this.recordsChanged,
				rhs.recordsChanged).append(this.recordsUploaded,
				rhs.recordsUploaded)
				.append(this.recordsAdded, rhs.recordsAdded).append(
						this.executionDate, rhs.executionDate).append(
						this.resource, rhs.resource).append(this.id, rhs.id)
				.isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-1475384193, 1013469649).append(
				this.recordsDeleted).append(this.recordsChanged).append(
				this.recordsUploaded).append(this.recordsAdded).append(
				this.executionDate).append(this.resource)
				.toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("recordsUploaded",
				this.recordsUploaded)
				.append("resource", this.resource)
				.append(
				"id", this.id).append("recordsChanged", this.recordsChanged)
				.append("executionDate", this.executionDate).append(
						"recordsAdded", this.recordsAdded).append(
						"recordsDeleted", this.recordsDeleted).toString();
	}
	
}
