package org.gbif.provider.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Event keeping track of upload statistics to the cache for a certain resource
 * @author markus
 *
 */
@Entity
public class UploadEvent {
	private Long id;
	private DatasourceBasedResource resource;
	private Date executionDate;
	private Long recordsUploaded;
	private Long recordsDeleted;
	private Long recordsChanged;
	private Long recordsAdded;
	
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name="resource_id", nullable=false) 
	public DatasourceBasedResource getResource() {
		return resource;
	}
	public void setResource(DatasourceBasedResource resource) {
		this.resource = resource;
	}
	
	public Date getExecutionDate() {
		return executionDate;
	}
	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}
	
	public Long getRecordsUploaded() {
		return recordsUploaded;
	}
	public void setRecordsUploaded(Long recordsUploaded) {
		this.recordsUploaded = recordsUploaded;
	}
	
	public Long getRecordsDeleted() {
		return recordsDeleted;
	}
	public void setRecordsDeleted(Long recordsDeleted) {
		this.recordsDeleted = recordsDeleted;
	}
	
	public Long getRecordsChanged() {
		return recordsChanged;
	}
	public void setRecordsChanged(Long recordsChanged) {
		this.recordsChanged = recordsChanged;
	}
	
	public Long getRecordsAdded() {
		return recordsAdded;
	}
	public void setRecordsAdded(Long recordsAdded) {
		this.recordsAdded = recordsAdded;
	}
	
	
}
