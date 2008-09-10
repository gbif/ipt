package org.gbif.provider.model;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

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
	public Point(Float latitude, Float longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
	}
	public Float getLatitude() {
		return latitude;
	}
	public void setLatitude(Float latitude) {
		if (latitude < -90 || latitude > 90){
			throw new IllegalArgumentException();
		}
		this.latitude = latitude;
	}
	public Float getLongitude() {
		return longitude;
	}
	public void setLongitude(Float longitude) {
		if (longitude < -180 || longitude > 180){
			throw new IllegalArgumentException();
		}
		this.longitude = longitude;
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
}
