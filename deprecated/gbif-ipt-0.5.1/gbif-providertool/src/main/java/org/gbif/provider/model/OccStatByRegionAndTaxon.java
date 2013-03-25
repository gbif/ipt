package org.gbif.provider.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class OccStatByRegionAndTaxon implements ResourceRelatedObject{
	private Long id;
	private Resource resource;
	private Taxon taxon;
	private Region region;
	private int numOcc;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@ManyToOne(optional = false)
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	@ManyToOne(optional = true)
	public Taxon getTaxon() {
		return taxon;
	}
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}
	@ManyToOne(optional = true)
	public Region getRegion() {
		return region;
	}
	public void setRegion(Region region) {
		this.region = region;
	}
	public int getNumOcc() {
		return numOcc;
	}
	public void setNumOcc(int numOcc) {
		this.numOcc = numOcc;
	}
	public void incrementNumOcc() {
		this.numOcc++;
	}
	
}
