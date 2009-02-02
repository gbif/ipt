package org.gbif.provider.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class OccStatByRegionAndTaxon implements ResourceRelatedObject{
	private Long id;
	private Resource resource;
	private Taxon taxon;
	private Region region;
	private int numOcc;
	private BBox bbox = new BBox();
	
	public OccStatByRegionAndTaxon(){
		super();
	}
	public OccStatByRegionAndTaxon(Resource resource, Taxon taxon, Region region, Long numOcc) {
		super();
		this.resource = resource;
		this.taxon = taxon;
		this.region = region;
		this.numOcc = numOcc.intValue();
	}
	/**
	 * Used by OccStatManager in HQL...
	 * @param resource
	 * @param taxon
	 * @param region
	 * @param numOcc
	 * @param minY
	 * @param minX
	 * @param maxY
	 * @param maxX
	 */
	public OccStatByRegionAndTaxon(Resource resource, Taxon taxon, Region region, Long numOcc, Double minY,Double minX, Double maxY,Double maxX) {
		// new OccStatByRegionAndTaxon(res, t, r, count(d), min(d.location.latitude), min(d.location.longitude), max(d.location.latitude), max(d.location.longitude))
		// latitude=y, longitude=x
		super();
		this.resource = resource;
		this.taxon = taxon;
		this.region = region;
		this.numOcc = numOcc.intValue();
		this.bbox = new BBox(minY,minX, maxY,maxX);
	}
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
	@Transient
	public Long getResourceId() {
		return resource.getId();
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
	public BBox getBbox() {
		return bbox;
	}
	public void setBbox(BBox bbox) {
		this.bbox = bbox;
	}	
}
