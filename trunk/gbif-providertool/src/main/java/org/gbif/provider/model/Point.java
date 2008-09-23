package org.gbif.provider.model;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * There are several formats for writing degrees, all of them appearing in the same Lat, Long order.
 * In DD Decimal Degrees (49.5000°,-123.5000°), generally with 4-6 decimal numbers.
 * Latitude (Lat. or φ) N positive, S negative. Is the angle from a point on the Earth's surface and the equatorial plane, measured from the centre of the sphere. Lines joining points of the same latitude are called parallels, which trace concentric circles on the surface of the Earth, parallel to the equator. The north pole is 90° N; the south pole is 90° S. The 0° parallel of latitude is designated the equator. The equator is the fundamental plane of all geographic coordinate systems. The equator divides the globe into Northern and Southern Hemispheres.
 * Longitude (Long. or λ) E positive, W negative. Is the angle east or west of a reference meridian between the two geographical poles to another meridian that passes through an arbitrary point. All meridians are halves of great circles, and are not parallel. They converge at the north and south poles.
 * @author markus
 *
 */
@Embeddable
public class Point {
	private Float latitude;
	private Float longitude;
	
	public Point() {
	}
	public Point(Point p){
		setLatitude(p.latitude);
		setLongitude(p.longitude);
	}
	public Point(Float latitude, Float longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	public Float getLatitude() {
		return latitude==null ? null : new Float(latitude);
	}
	public void setLatitude(Float latitude) {
		if (latitude != null && (latitude < -90 || latitude > 90)){
			throw new IllegalArgumentException();
		}
		if (latitude == null){
			this.latitude = null;
		}else{
			this.latitude = new Float(latitude);
		}
	}
	public Float getLongitude() {
		return longitude==null ? null : new Float(longitude);
	}
	public void setLongitude(Float longitude) {
		if (longitude != null && (longitude < -180 || longitude > 180)){
			throw new IllegalArgumentException();
		}
		if (longitude == null){
			this.longitude = null;
		}else{
			this.longitude = new Float(longitude);
		}
	}

	/**
	 * Transforms coordinates into WGS84 coordinates
	 * @param latitude
	 * @param longitude
	 * @param geodatum
	 */
	public void transformIntoWGS84(String geodatum) {
		//TODO: transform point into standard WGS84 datum. Might beed external library for this...
		if (geodatum==null){
			return;
		}
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
