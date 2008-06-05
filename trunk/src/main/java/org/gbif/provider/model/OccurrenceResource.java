package org.gbif.provider.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

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
			if (m.getView() == null){
				return m; 
			}
		}
		return null;
	}
	
	@OneToMany(mappedBy="resource", fetch=FetchType.EAGER)
	public Set<ViewMapping> getMappings() {
		return mappings;
	}
	public void setMappings(Set<ViewMapping> mappings) {
		this.mappings = mappings;
	}

	
}
