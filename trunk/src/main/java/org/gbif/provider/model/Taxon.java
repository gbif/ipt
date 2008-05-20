package org.gbif.provider.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Taxon extends ResolvableBase {
	private String fullScientificName;
	private String scientificName;
	private Taxon parent;
	
	
	public String getFullScientificName() {
		return fullScientificName;
	}
	public void setFullScientificName(String fullScientificName) {
		this.fullScientificName = fullScientificName;
	}
	
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	
	@ManyToOne
	public Taxon getParent() {
		return parent;
	}
	public void setParent(Taxon parent) {
		this.parent = parent;
	}
	
}
