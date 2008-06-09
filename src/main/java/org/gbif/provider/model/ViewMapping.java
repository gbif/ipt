package org.gbif.provider.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
public class ViewMapping {
	private Long id;	
	private OccurrenceResource resource;
	private DwcExtension extension;
	private String viewSql;
	private Set<ExtensionProperty> properties = new HashSet<ExtensionProperty>();
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(optional=false)
	public OccurrenceResource getResource() {
		return resource;
	}
	public void setResource(OccurrenceResource resource) {
		this.resource = resource;
	}
	
	/**
	 * view is null for the darwin core mapping
	 * @return
	 */
	@ManyToOne
	public DwcExtension getExtension() {
		return extension;
	}
	public void setExtension(DwcExtension extension) {
		this.extension = extension;
	}
	
	public String getViewSql() {
		return viewSql;
	}
	public void setViewSql(String sql) {
		this.viewSql = sql;
	}
	
	@OneToMany
	public Set<ExtensionProperty> getProperties() {
		return properties;
	}
	public void setProperties(Set<ExtensionProperty> properties) {
		this.properties = properties;
	}
	
	
}
