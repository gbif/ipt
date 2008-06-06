package org.gbif.provider.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;

/**
 * A specific resource representing the external datasource for uploading darwincore records
 * @author markus
 *
 */
@Entity
public class OccurrenceResource extends DatasourceBasedResource {
	private Set<ViewMapping> mappings = new HashSet<ViewMapping>();
	
	@Transient
	public ViewMapping getDwcMapping() {
		for (ViewMapping m : mappings){
			if (m.getExtension() == null){
				return m; 
			}
		}
		return null;
	}
	
	//fetch=FetchType.EAGER
	@OneToMany(mappedBy="resource", cascade=CascadeType.ALL)
	public Set<ViewMapping> getMappings() {
		return mappings;
	}
	public void setMappings(Set<ViewMapping> mappings) {
		this.mappings = mappings;
	}
	public void addMapping(ViewMapping mapping) {
		mapping.setResource(this);
		this.mappings.add(mapping);
	}
	
}
