package org.gbif.provider.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.vividsolutions.jts.geom.Coordinate;



/**
 * There are several formats for writing degrees, all of them appearing in the same Lat, Long order.
 * In DD Decimal Degrees (49.5000°,-123.5000°), generally with 4-6 decimal numbers.
 * Latitude (Lat. or φ) N positive, S negative. Is the angle from a point on the Earth's surface and the equatorial plane, measured from the centre of the sphere. Lines joining points of the same latitude are called parallels, which trace concentric circles on the surface of the Earth, parallel to the equator. The north pole is 90° N; the south pole is 90° S. The 0° parallel of latitude is designated the equator. The equator is the fundamental plane of all geographic coordinate systems. The equator divides the globe into Northern and Southern Hemispheres.
 * Longitude (Long. or λ) E positive, W negative. Is the angle east or west of a reference meridian between the two geographical poles to another meridian that passes through an arbitrary point. All meridians are halves of great circles, and are not parallel. They converge at the north and south poles.
 * @author markus
 *
 */
@Embeddable
public class Point implements Serializable{
	public static final Double MAX_LONGITUDE = new Double(180);
	public static final Double MIN_LONGITUDE = new Double(-180);
	public static final Double MAX_LATITUDE = new Double(90);
	public static final Double MIN_LATITUDE = new Double(-90);
	// x (east/west), -180/180
	private Double longitude;
	// y (north/south), -90/90
	private Double latitude;
	

	public Point() {
	}
	public Point(Point p){
		setLatitude(p.latitude);
		setLongitude(p.longitude);
	}
	public Point(Double latitude, Double longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	@Transient
	public Coordinate getCoordinate() {
		return new Coordinate(latitude, longitude);
	}
	public void setCoordinate(Coordinate coord) {
		this.latitude = coord.x;
		this.longitude= coord.y;
	}

	@Transient
	public Double getX() {
		return getLatitude();
	}
	public Double getLatitude() {
		return latitude==null ? null : new Double(latitude);
	}
	public void setX(Double x) {
		setLatitude(x);
	}
	public void setLatitude(Double latitude) {
		if (latitude != null && (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE)){
			throw new IllegalArgumentException();
		}
		if (latitude == null){
			this.latitude = null;
		}else{
			this.latitude = new Double(latitude);
		}
	}
	@Transient
	public Double getY() {
		return getLongitude();
	}
	public Double getLongitude() {
		return longitude==null ? null : new Double(longitude);
	}
	public void setY(Double y) {
		setLongitude(y);
	}
	public void setLongitude(Double longitude) {
		if (longitude != null && (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE)){
			throw new IllegalArgumentException();
		}
		if (longitude == null){
			this.longitude = null;
		}else{
			this.longitude = new Double(longitude);
		}
	}

	@Transient
	public double distanceX(Point p) {
		return Math.abs(getX()-p.getX());
	}
	@Transient
	public double distanceY(Point p) {
		return Math.abs(getY()-p.getY());
	}
	@Transient
	public double distance(Point p) {
		return Math.sqrt(getX()*getX()+getY()*getY());
	}

	@Transient
	public boolean isValid(){
		if (latitude != null && longitude!=null){
			return true;
		}
		return false;
	}
	public String toString(){
		return String.format("%s,%s", latitude, longitude);
	}
	public String toStringShort(int decimals){
		return String.format("%."+decimals+"f,%."+decimals+"f", latitude, longitude);
	}
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Point)) {
			return false;
		}
		Point rhs = (Point) object;
		return new EqualsBuilder().append(this.longitude, rhs.longitude)
				.append(this.latitude, rhs.latitude).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1842455565, -1433355773).append(
				this.longitude).append(this.latitude).toHashCode();
	}
}
