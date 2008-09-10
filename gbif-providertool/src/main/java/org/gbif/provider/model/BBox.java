package org.gbif.provider.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Bounding box representation with 2 points.
 * P1 has maximum latitude and longitude, while p2 represents the minimum ones, i.e. p1 is NorthEast, p2 is SoutWest
 * @author markus
 *
 */
@Embeddable
public class BBox {
	@AttributeOverrides( {
        @AttributeOverride(name="latitude", column = @Column(name="max_lat") ),
        @AttributeOverride(name="longitude", column = @Column(name="max_long") )
	} )
	private Point max = new Point();
	@AttributeOverrides( {
        @AttributeOverride(name="latitude", column = @Column(name="min_lat") ),
        @AttributeOverride(name="longitude", column = @Column(name="min_long") )
	} )
	private Point min = new Point();


	public Point getMax() {
		return max;
	}
	public void setMax(Point max) {
		this.max = max;
	}
	public Point getMin() {
		return min;
	}
	public void setMin(Point min) {
		this.min = min;
	}
	
	@Transient
	public Float getMaxLatitude(){
		return max.getLatitude();
	}
	@Transient
	public Float getMaxLongitude(){
		return max.getLongitude();
	}
	@Transient
	public Float getMinLatitude(){
		return min.getLatitude();
	}
	@Transient
	public Float getMinLongitude(){
		return min.getLongitude();
	}
	/**
	 * Expands bounding box boundaries to fit this coordinate into the box
	 * @param latitude
	 * @param longitude
	 */
	public void expandBox(Point p){
		if (p != null && p.isValid() && !contains(p)){
			if (p.getLatitude() > max.getLatitude()){
				max.setLatitude(p.getLatitude());
			}
			if (p.getLatitude() < min.getLatitude()){
				min.setLatitude(p.getLatitude());
			}
			if (p.getLongitude() > max.getLongitude()){
				max.setLongitude(p.getLongitude());
			}
			if (p.getLongitude() < min.getLongitude()){
				min.setLongitude(p.getLongitude());
			}
		}
	}
	
	/**
	 * Check if point lies within this bbox
	 * @param p
	 * @return
	 */
	private boolean contains(Point p) {
		if (p != null && p.isValid() && p.getLatitude() <= max.getLatitude() && p.getLatitude() >= min.getLatitude()  &&  p.getLongitude() <= max.getLongitude() && p.getLongitude() >= min.getLongitude()){
			return true;
		}
		return false;
	}
	
	public String toString(){
		return String.format("%s %s", min, max);
	}
}
