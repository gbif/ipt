package org.gbif.provider.model;

import javax.persistence.Embeddable;

@Embeddable
public class GeoPoint extends Point{
	private Integer elevationInMeters;
	private Integer uncertaintyInMeters;
	
	public Integer getElevationInMeters() {
		return elevationInMeters;
	}
	public void setElevationInMeters(Integer elevationInMeters) {
		this.elevationInMeters = elevationInMeters;
	}
	public Integer getUncertaintyInMeters() {
		return uncertaintyInMeters;
	}
	public void setUncertaintyInMeters(Integer uncertaintyInMeters) {
		this.uncertaintyInMeters = uncertaintyInMeters;
	}
	
}
