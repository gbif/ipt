package org.gbif.provider.model.eml;

import java.io.Serializable;

import org.gbif.provider.model.BBox;

public class GeoKeyword implements Serializable{
	private String description;
	private BBox boundingCoordinates = BBox.NewWorldInstance();
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BBox getBoundingCoordinates() {
		return boundingCoordinates;
	}
	public void setBoundingCoordinates(BBox boundingCoordinates) {
		this.boundingCoordinates = boundingCoordinates;
	}
	
	
}
