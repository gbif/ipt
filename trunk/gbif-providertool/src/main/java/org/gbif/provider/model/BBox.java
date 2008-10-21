package org.gbif.provider.model;

import java.io.Serializable;
import java.util.Arrays;

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
public class BBox implements Serializable{
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


	public static BBox NewWorldInstance() {
		BBox world = new BBox(new Point(90f,180f), new Point(-90f,-180f));
		return world;
	}

	
	public BBox() {
		super();
	}
	public BBox(Point max, Point min) {
		super();
		setMax(max);
		setMin(min);
	}
	public BBox(Float maxX, Float maxY, Float minX, Float minY) {
		super();
		setMax(new Point(maxX, maxY));
		setMin(new Point(minX, minY));
	}
	
	public Point getMax() {
		return max;
	}
	
	private void setMax(Point max) {
		if (max==null){
			this.max=null;
		}else{
			this.max = new Point(max);
		}
	}
	
	public Point getMin() {
		return min;
	}
	
	private void setMin(Point min) {
		if (min==null){
			this.min=null;
		}else{
			this.min = new Point(min);
		}
	}
	
	/**
	 * Expands bounding box boundaries to fit this coordinate into the box
	 * @param latitude
	 * @param longitude
	 */
	public void expandBox(Point p){
		if (p!=null && p.isValid()){
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
		if (min!=null && max!=null && min.isValid() && max.isValid()){
			return true;
		}
		return false;
	}

	/** Try to expand BBox by factor given but keep box centered and expand to maximum possible in case we reach world limits.
	 * @param factor 0-1 for shrinking, >1 for expanding boxes
	 */
	@Transient
	public void resize(float factor){
		if (factor < 0f){
			throw new IllegalArgumentException("Factor must be larger than 0");
		}
		if (!isValid()){
			throw new IllegalStateException("BBox is not valid");
		}
		float minX = min.getLongitude();
		float minY = min.getLatitude();
		float maxX = max.getLongitude();
		float maxY = max.getLatitude();
		float width = maxX-minX;
		float height = maxY-minY;
		// detect maximum possible expand factor
		float[] maxFactors = {(factor-1)/2f, (Point.MAX_LATITUDE-maxY)/height, (Point.MAX_LATITUDE+minY)/height,  (Point.MAX_LONGITUDE-maxX)/width, (Point.MAX_LONGITUDE+minX)/width};
		Arrays.sort(maxFactors);
		float expandFactor = maxFactors[0];
		// change bbox		
		minX=minX-(expandFactor*width);
		maxX=maxX+(expandFactor*width);
		minY=minY-(expandFactor*height);
		maxY=maxY+(expandFactor*height);
		min.setLongitude(minX);
		min.setLatitude(minY);
		max.setLongitude(maxX);
		max.setLatitude(maxY);
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
				if (minX-equalWidthIncrease < Point.MIN_LONGITUDE){
					minX=Point.MIN_LONGITUDE;
					maxX = width*mapRatio;
				} else if (maxX+equalWidthIncrease > Point.MAX_LONGITUDE){
					minX= Point.MAX_LONGITUDE - width*mapRatio;
					maxX = Point.MAX_LONGITUDE;
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
				if (minY-equalHeightIncrease < Point.MIN_LATITUDE){
					minY=Point.MIN_LATITUDE;
					maxY = width*mapRatio;
				} else if (maxY+equalHeightIncrease > Point.MAX_LATITUDE){
					minY= Point.MAX_LATITUDE - width*mapRatio;
					maxY = Point.MAX_LATITUDE;
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


	/** returns the size of the surface defined by this bbox
	 * @return
	 */
	@Transient
	public float surface() {
		float width=max.getLongitude()-min.getLongitude();
		float height=max.getLatitude()-min.getLatitude();
		return width*height;
	}
}
