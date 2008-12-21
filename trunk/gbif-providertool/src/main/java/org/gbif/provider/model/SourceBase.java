package org.gbif.provider.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;

@Entity
public abstract class SourceBase implements BaseObject, ResourceRelatedObject{
	private Long id;	
	@NotNull
	protected DataResource resource;
	@NotNull
	protected String name;

	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
			
	@ManyToOne(optional=false)
	@JoinColumn(name = "resource_fk")
	public DataResource getResource() {
		return resource;
	}
	public void setResource(DataResource resource) {
		this.resource = resource;
	}
	@Transient
	public Long getResourceId() {
		return resource.getId();
	}
	
	@Column(length=128)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Transient
	public abstract boolean isValid();

}
