package org.gbif.provider.model;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class CoreRecord extends ResolvableBase  {
	private DatasourceBasedResource occurrenceResource;
	private String coreID;
	private boolean isDeleted;


	@ManyToOne
	public DatasourceBasedResource getOccurrenceResource() {
		return occurrenceResource;
	}
	public void setOccurrenceResource(DatasourceBasedResource occurrenceResource) {
		this.occurrenceResource = occurrenceResource;
	}
	
	public String getCoreID() {
		return coreID;
	}
	public void setCoreID(String coreID) {
		this.coreID = coreID;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
}
