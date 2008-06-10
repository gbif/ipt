package org.gbif.provider.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * A mapping between a resource and an extension (incl darwincore itself).
 * The ViewMapping defines the sql statement used to upload data for a certain extension,
 * therefore for every extension there exists a separate sql statement which should be uploaded one after the other.
 * @author markus
 *
 */
@Entity
public class ViewMapping {
	private Long id;	
	private OccurrenceResource resource;
	private DwcExtension extension;
	private String viewSql;
	private Set<PropertyMapping> propertyMappings = new HashSet<PropertyMapping>();
	
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
	
	@OneToMany(mappedBy="viewMapping", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	public Set<PropertyMapping> getPropertyMappings() {
		return propertyMappings;
	}
	public void setPropertyMappings(Set<PropertyMapping> propertyMappings) {
		this.propertyMappings = propertyMappings;
	}
	
	
}
