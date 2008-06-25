package org.gbif.provider.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Event keeping track of upload statistics to the cache for a certain resource.
 * uploaded records is the total of all uploaded records and thus the new record count for the resource.
 * The following should be true: 
 * recordsUploaded = previousRecordCount + recordsAdded - recordsDeleted
 * @author markus
 *
 */
@Entity
public class UploadEvent {
	private Long id;
	private DatasourceBasedResource resource;
	private Date executionDate;
	private int recordsUploaded;
	private int recordsDeleted;
	private int recordsChanged;
	private int recordsAdded;
	
	
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
	
	
}
