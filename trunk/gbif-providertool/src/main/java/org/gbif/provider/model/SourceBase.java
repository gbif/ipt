package org.gbif.provider.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public abstract class SourceBase implements BaseObject, ResourceRelatedObject{
	private Long id;	
	protected DataResource resource;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
			
	@ManyToOne(optional=false)
	@JoinColumn(name = "resource_fk", insertable = false, updatable = false, nullable = false)
	public DataResource getResource() {
		return resource;
	}
	public void setResource(DataResource resource) {
		this.resource = resource;
	}
	
	@Transient
	public abstract boolean isValid();

}
