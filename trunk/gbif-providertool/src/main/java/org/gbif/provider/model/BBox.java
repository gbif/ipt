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

	/** Expands BBox so that its longitude/latitude ratio becomes 2:1 which is often used for maps (360° : 180°).
	 */
	@Transient
	public void expandToMapRatio(){
		expandToMapRatio(2f);
	}
	
	/** Expands BBox so that its longitude/latitude ratio becomes the specified width/height ratio. Takes care to not go beyond the -180/180 + -90/90 bboc limits and might shift the center of the bbox if this is not otherwise possible.
	 */
	@Transient
	public void expandToMapRatio(float mapRatio){
		// longitude=x, latitude=y
		if (isValid()){
			float width = max.getLongitude() - min.getLongitude();
			float height = max.getLatitude() - min.getLatitude();
			float ratio = width/height;
			
			if (mapRatio > ratio){
				// was rather a square before. need to extend the latitude on both min+max
				float equalWidthIncrease = ((height * mapRatio) - width)/2;
				float minX =  min.getLongitude();
				float maxX =  max.getLongitude();
				if (minX-equalWidthIncrease < -90f){
					minX=-90f;
					maxX = width*mapRatio;
				} else if (maxX+equalWidthIncrease > 90f){
					minX= 90 - width*mapRatio;
					maxX = 90f;
				} else{
					minX= minX-equalWidthIncrease;
					maxX = maxX+equalWidthIncrease;
				}
				min.setLongitude(minX);
				max.setLongitude(maxX);
			}else if (mapRatio < ratio){
				// was more of a flat rectangle before. need to extend the longitude on both min+max
				float equalHeightIncrease = ((width / mapRatio) - height)/2;
				float minY =  min.getLatitude();
				float maxY =  max.getLatitude();
				if (minY-equalHeightIncrease < -90f){
					minY=-90f;
					maxY = width*mapRatio;
				} else if (maxY+equalHeightIncrease > 90f){
					minY= 90 - width*mapRatio;
					maxY = 90f;
				} else{
					minY= minY-equalHeightIncrease;
					maxY = maxY+equalHeightIncrease;
				}
				min.setLatitude(minY);
				max.setLatitude(maxY);
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
	
	/**format used in WMS for bboxes: minX(longitude), minY(latitude), maxX, maxY
	 * @return
	 */
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
