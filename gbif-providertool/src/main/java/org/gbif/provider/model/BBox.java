package org.gbif.provider.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
	private Point max;
	@AttributeOverrides( {
        @AttributeOverride(name="latitude", column = @Column(name="min_lat") ),
        @AttributeOverride(name="longitude", column = @Column(name="min_long") )
	} )
	private Point min;


	public BBox() {
		super();
	}
	public BBox(Point max, Point min) {
		super();
		setMax(max);
		setMin(min);
	}
		
	public Point getMax() {
		if (max==null){
			max = new Point();
		}
		return new Point(max);
	}
	
	private void setMax(Point max) {
		this.max = new Point(max);
	}
	
	public Point getMin() {
		if (min==null){
			min = new Point();
		}
		return new Point(min);
	}
	
	private void setMin(Point min) {
		this.min = new Point(min);
	}
	
	/**
	 * Expands bounding box boundaries to fit this coordinate into the box
	 * @param latitude
	 * @param longitude
	 */
	public void expandBox(Point p){
		if (p!=null){
			if (!contains(p)){
				if (!isValid()){
					// this BBox doesnt yet contain any points. Use this point for min+max
					setMin(p);
					setMax(p);
				}else{
					if (p.getLatitude() > getMax().getLatitude()){
						max.setLatitude(p.getLatitude());
					}
					if (p.getLatitude() < getMin().getLatitude()){
						min.setLatitude(p.getLatitude());
					}
					if (p.getLongitude() > getMax().getLongitude()){
						max.setLongitude(p.getLongitude());
					}
					if (p.getLongitude() < getMin().getLongitude()){
						min.setLongitude(p.getLongitude());
					}
				}
			}
		}
	}
	
	@Transient
	public boolean isValid(){
		if (getMin().isValid() && getMax().isValid()){
			return true;
		}
		return false;
	}

	/** Increases BBox to make sure that bbox and map width/height ratios are the same
	 * @param ratio
	 */
	@Transient
	public void fitRatio(double ratio){
		if (isValid()){
			double lat = (double) max.getLatitude() - (double) min.getLatitude();
			double lon = (double) max.getLongitude() - (double) min.getLongitude();
			double existingRatio = lat/lon;
			if (ratio > existingRatio){
				// need to extend the latitude on both min+max
				Float increase = (float) ((lat * ratio / existingRatio) - lat)/2;
				min.setLatitude(min.getLatitude()-increase);
				max.setLatitude(max.getLatitude()+increase);
			}else if (ratio < existingRatio){
				// need to extend the longitude on both min+max
				Float increase = (float) ((lon * ratio / existingRatio) - lon)/2;
				min.setLongitude(min.getLongitude()-increase);
				max.setLongitude(max.getLongitude()+increase);
			}
		}
	}
	
	/**
	 * Check if point lies within this bbox
	 * @param p
	 * @return
	 */
	public boolean contains(Point p) {
		if (p != null && p.isValid() && isValid()){
			if (p.getLatitude() <= getMax().getLatitude() && p.getLatitude() >= getMin().getLatitude()  &&  p.getLongitude() <= getMax().getLongitude() && p.getLongitude() >= getMin().getLongitude()){
				return true;
			}
		}
		return false;
	}
	
	public String toWMSString(){
		return String.format("%s,%s,%s,%s", min.getLongitude(), min.getLatitude(), max.getLongitude(), max.getLatitude());
	}
	public String toString(){
		return String.format("%s %s", min, max);
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof BBox)) {
			return false;
		}
		BBox rhs = (BBox) object;
		return new EqualsBuilder().append(this.min, rhs.min).append(this.max,
				rhs.max).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1425547953, 342545271).append(this.min)
				.append(this.max).toHashCode();
	}
}
