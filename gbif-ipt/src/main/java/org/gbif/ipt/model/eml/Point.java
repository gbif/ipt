/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.ipt.model.eml;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;


/**
 * There are several formats for writing degrees, all of them appearing in the
 * same Lat, Long order. In DD Decimal Degrees (49.5000°,-123.5000°), generally
 * with 4-6 decimal numbers. Latitude (Lat. or φ) N positive, S negative. Is the
 * angle from a point on the Earth's surface and the equatorial plane, measured
 * from the centre of the sphere. Lines joining points of the same latitude are
 * called parallels, which trace concentric circles on the surface of the Earth,
 * parallel to the equator. The north pole is 90° N; the south pole is 90° S.
 * The 0° parallel of latitude is designated the equator. The equator is the
 * fundamental plane of all geographic coordinate systems. The equator divides
 * the globe into Northern and Southern Hemispheres. Longitude (Long. or λ) E
 * positive, W negative. Is the angle east or west of a reference meridian
 * between the two geographical poles to another meridian that passes through an
 * arbitrary point. All meridians are halves of great circles, and are not
 * parallel. They converge at the north and south poles.
 * 
 */
public class Point implements Serializable {
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

  public Point(Double latitude, Double longitude) {
    setLatitude(latitude);
    setLongitude(longitude);
  }

  public Point(Point p) {
    setLatitude(p.latitude);
    setLongitude(p.longitude);
  }

  public double distance(Point p) {
    return Math.sqrt(getX() * getX() + getY() * getY());
  }

  public double distanceX(Point p) {
    return Math.abs(getX() - p.getX());
  }

  public double distanceY(Point p) {
    return Math.abs(getY() - p.getY());
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Point)) {
      return false;
    }
    Point rhs = (Point) object;
    return new EqualsBuilder().append(this.longitude, rhs.longitude).append(
        this.latitude, rhs.latitude).isEquals();
  }

  public Double getLatitude() {
    return latitude == null ? null : new Double(latitude);
  }

  public Double getLongitude() {
    return longitude == null ? null : new Double(longitude);
  }

  public Double getX() {
    return getLongitude();
  }

  public Double getY() {
    return getLatitude();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1842455565, -1433355773).append(this.longitude).append(
        this.latitude).toHashCode();
  }

  public boolean isValid() {
    if (latitude != null && longitude != null) {
      return true;
    }
    return false;
  }

  public void setLatitude(Double latitude) {
    if (latitude != null
        && (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE)) {
      throw new IllegalArgumentException();
    }
    if (latitude == null) {
      this.latitude = null;
    } else {
      this.latitude = new Double(latitude);
    }
  }

  public void setLongitude(Double longitude) {
    if (longitude != null
        && (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE)) {
      throw new IllegalArgumentException();
    }
    if (longitude == null) {
      this.longitude = null;
    } else {
      this.longitude = new Double(longitude);
    }
  }

  public void setX(Double x) {
    setLongitude(x);
  }

  public void setY(Double y) {
    setLatitude(y);
  }

  @Override
  public String toString() {
    return String.format("%s,%s", latitude, longitude);
  }

  public String toStringShort(int decimals) {
    return String.format("%." + decimals + "f,%." + decimals + "f", latitude,
        longitude);
  }

  public String toStringSpace() {
    return String.format("%s %s", latitude, longitude);
  }
  
  
  /**
   * Copied from com.vividsolutions.jts.geom.Coordinate:
   * 
   * 
   *  Compares this {@link Point} with the specified {@link Point} for order.
   *  This method ignores the z value when making the comparison.
   *  Returns:
   *  <UL>
   *    <LI> -1 : this.x < other.x || ((this.x == other.x) && (this.y <
   *    other.y))
   *    <LI> 0 : this.x == other.x && this.y = other.y
   *    <LI> 1 : this.x > other.x || ((this.x == other.x) && (this.y > other.y))
   *
   *  </UL>
   *  Note: This method assumes that ordinate values
   * are valid numbers.  NaN values are not handled correctly.
   *
   *@param  o  the <code>Point</code> with which this <code>Point</code> is being compared
   *@return    -1, zero, or 1 as this <code>Point</code>
   *      is less than, equal to, or greater than the specified <code>Point</code>
   */
  public int compareTo(Object o) {
    Point other = (Point) o;

    if (longitude < other.longitude) return -1;
    if (longitude > other.longitude) return 1;
    if (latitude < other.latitude) return -1;
    if (latitude > other.latitude) return 1;
    return 0;
  }
}
