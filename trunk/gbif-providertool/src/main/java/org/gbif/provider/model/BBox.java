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
 * Bounding box representation with 2 points. P1 has maximum latitude and
 * longitude, while p2 represents the minimum ones, i.e. p1 is NorthEast, p2 is
 * SouthWest
 * 
 */
@Embeddable
public class BBox implements Serializable {

  public static BBox newWorldInstance() {
    BBox world = new BBox(new Point(-90.0, -180.0), new Point(90.0, 180.0));
    return world;
  }

  // x = east/west=longitude, -180/180
  // y = north/south = latitude, -90/90
  @AttributeOverrides( {
      @AttributeOverride(name = "latitude", column = @Column(name = "max_lat")),
      @AttributeOverride(name = "longitude", column = @Column(name = "max_long"))})
  private Point max;

  @AttributeOverrides( {
      @AttributeOverride(name = "latitude", column = @Column(name = "min_lat")),
      @AttributeOverride(name = "longitude", column = @Column(name = "min_long"))})
  private Point min;

  public BBox() {
    this(-90d, -180d, 90d, 180d);
  }

  public BBox(Double minY, Double minX, Double maxY, Double maxX) {
    super();
    this.min = new Point(minY, minX);
    this.max = new Point(maxY, maxX);
    setOrderedBounds(minY, minX, maxY, maxX);
  }

  public BBox(Point min, Point max) {
    super();
    this.min = new Point(min);
    this.max = new Point(max);
    setOrderedBounds(min.getLatitude(), min.getLongitude(), max.getLatitude(),
        max.getLongitude());
  }

  /**
   * Returns the Point of the bbox centre
   * 
   * @return
   */
  @Transient
  public Point centre() {
    if (max == null || min == null) {
      return null;
    }
    return new Point(min.getLatitude() + height() / 2.0, min.getLongitude()
        + width() / 2.0);
  }

  /**
   * Check if point lies within this bbox
   * 
   * @param p
   * @return
   */
  public boolean contains(Point p) {
    if (p != null && p.isValid() && isValid()) {
      if (p.getLatitude() <= getMax().getLatitude()
          && p.getLatitude() >= getMin().getLatitude()
          && p.getLongitude() <= getMax().getLongitude()
          && p.getLongitude() >= getMin().getLongitude()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof BBox)) {
      return false;
    }
    BBox rhs = (BBox) object;
    return new EqualsBuilder().append(this.min, rhs.min).append(this.max,
        rhs.max).isEquals();
  }

  /**
   * Expands bounding box boundaries to fit this coordinate into the box
   * 
   * @param latitude
   * @param longitude
   */
  public void expandBox(Point p) {
    if (p != null && p.isValid()) {
      if (!contains(p)) {
        if (!isValid()) {
          // this BBox doesnt yet contain any points. Use this point for min+max
          setMin(p);
          setMax(p);
        } else {
          if (p.getLatitude() > getMax().getLatitude()) {
            max.setLatitude(p.getLatitude());
          }
          if (p.getLatitude() < getMin().getLatitude()) {
            min.setLatitude(p.getLatitude());
          }
          if (p.getLongitude() > getMax().getLongitude()) {
            max.setLongitude(p.getLongitude());
          }
          if (p.getLongitude() < getMin().getLongitude()) {
            min.setLongitude(p.getLongitude());
          }
        }
      }
    }
  }

  /**
   * Expands BBox so that its longitude/latitude ratio becomes 2:1 which is
   * often used for maps (360° : 180°).
   */
  @Transient
  public void expandToMapRatio() {
    expandToMapRatio(2f);
  }

  /**
   * Expands BBox so that its longitude/latitude ratio becomes the specified
   * width/height ratio. Takes care to not go beyond the -180/180 + -90/90 bboc
   * limits and might shift the center of the bbox if this is not otherwise
   * possible.
   */
  @Transient
  public void expandToMapRatio(double mapRatio) {
    // longitude=x, latitude=y
    if (isValid()) {
      double width = max.getLongitude() - min.getLongitude();
      double height = max.getLatitude() - min.getLatitude();
      double ratio = width / height;

      if (mapRatio > ratio) {
        // was rather a square before. need to extend the latitude on both
        // min+max
        double equalWidthIncrease = ((height * mapRatio) - width) / 2;
        double minX = min.getLongitude();
        double maxX = max.getLongitude();
        if (minX - equalWidthIncrease < Point.MIN_LONGITUDE) {
          minX = Point.MIN_LONGITUDE;
          maxX = width * mapRatio;
        } else if (maxX + equalWidthIncrease > Point.MAX_LONGITUDE) {
          minX = Point.MAX_LONGITUDE - width * mapRatio;
          maxX = Point.MAX_LONGITUDE;
        } else {
          minX = minX - equalWidthIncrease;
          maxX = maxX + equalWidthIncrease;
        }
        min.setLongitude(minX);
        max.setLongitude(maxX);
      } else if (mapRatio < ratio) {
        // was more of a flat rectangle before. need to extend the longitude on
        // both min+max
        double equalHeightIncrease = ((width / mapRatio) - height) / 2;
        double minY = min.getLatitude();
        double maxY = max.getLatitude();
        if (minY - equalHeightIncrease < Point.MIN_LATITUDE) {
          minY = Point.MIN_LATITUDE;
          maxY = width * mapRatio;
        } else if (maxY + equalHeightIncrease > Point.MAX_LATITUDE) {
          minY = Point.MAX_LATITUDE - width * mapRatio;
          maxY = Point.MAX_LATITUDE;
        } else {
          minY = minY - equalHeightIncrease;
          maxY = maxY + equalHeightIncrease;
        }
        min.setLatitude(minY);
        max.setLatitude(maxY);
      }
    }
  }

  public Point getMax() {
    return max;
  }

  public Point getMin() {
    return min;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1425547953, 342545271).append(this.min).append(
        this.max).toHashCode();
  }

  @Transient
  public double height() {
    if (max == null || min == null) {
      return 0.0;
    }
    return max.getY() - min.getY();
  }

  @Transient
  public boolean isValid() {
    if (min != null && max != null && min.isValid() && max.isValid()) {
      return true;
    }
    return false;
  }

  public boolean overlaps(BBox bbox) {
    if (bbox == null || !bbox.isValid()) {
      throw new IllegalArgumentException();
    }
    Point c1 = this.centre();
    Point c2 = bbox.centre();
    // If distance between centre X's is smaller than the sum of half the widths
    // of the two boxes and
    // the distance between centre Y's is smaller than the sum of half the
    // heights of the two boxes,
    // then the two boxes overlap! A shared boundary is not considered an
    // overlap here.
    if (c1.distanceX(c2) < (this.width() / 2.0 + bbox.width() / 2.0)
        && (c1.distanceY(c2) < (this.height() / 2.0 + bbox.height() / 2.0))) {
      return true;
    }
    return false;
  }

  /**
   * Try to expand BBox by factor given but keep box centered and expand to
   * maximum possible in case we reach world limits.
   * 
   * @param factor 0-1 for shrinking, >1 for expanding boxes
   */
  @Transient
  public void resize(double factor) {
    if (factor < 0f) {
      throw new IllegalArgumentException("Factor must be larger than 0");
    }
    if (!isValid()) {
      throw new IllegalStateException("BBox is not valid");
    }
    double minX = min.getLongitude();
    double minY = min.getLatitude();
    double maxX = max.getLongitude();
    double maxY = max.getLatitude();
    double width = maxX - minX;
    double height = maxY - minY;
    // detect maximum possible expand factor
    double[] maxFactors = {
        (factor - 1) / 2f, (Point.MAX_LATITUDE - maxY) / height,
        (Point.MAX_LATITUDE + minY) / height,
        (Point.MAX_LONGITUDE - maxX) / width,
        (Point.MAX_LONGITUDE + minX) / width};
    Arrays.sort(maxFactors);
    double expandFactor = maxFactors[0];
    // change bbox
    minX = minX - (expandFactor * width);
    maxX = maxX + (expandFactor * width);
    minY = minY - (expandFactor * height);
    maxY = maxY + (expandFactor * height);
    min.setLongitude(minX);
    min.setLatitude(minY);
    max.setLongitude(maxX);
    max.setLatitude(maxY);
  }

  public void setMax(Point max) {
    if (max == null) {
      this.max = null;
      return;
    } else {
      if (this.min == null) {
        this.min = new Point(max.getX(), max.getY());
      }
      setOrderedX(this.min.getX(), max.getX());
      setOrderedY(this.min.getY(), max.getY());
    }
  }

  /**
   * Sets the westing coordinate
   */
  public void setMaxX(String s) {
    try {
      setOrderedX(max.getLongitude(), Double.parseDouble(s));
    } catch (NumberFormatException e) {
      // not a Double, so not much can be done really
      e.printStackTrace();
    }
  }

  /**
   * Sets the northing coordinate
   */
  public void setMaxY(String s) {
    try {
      setOrderedY(max.getLatitude(), Double.parseDouble(s));
    } catch (NumberFormatException e) {
      // not a Double, so not much can be done really
    }
  }

  public void setMin(Point min) {
    if (min == null) {
      this.min = null;
      return;
    } else {
      if (this.max == null) {
        this.max = new Point(min.getX(), min.getY());
      }
      setOrderedX(this.min.getX(), max.getX());
      setOrderedY(this.min.getY(), max.getY());
    }
  }

  /**
   * Sets the easting coordinate
   */
  public void setMinX(String s) {
    try {
      setOrderedX(min.getLongitude(), Double.parseDouble(s));
    } catch (NumberFormatException e) {
      // not a Double, so not much can be done really
    }
  }

  /**
   * Sets the southing coordinate
   */
  public void setMinY(String s) {
    try {
      setOrderedY(min.getLatitude(), Double.parseDouble(s));
    } catch (NumberFormatException e) {
      // not a Double, so not much can be done really
    }
  }

  // Insure p1 is NE (max lat and long) and p2 is SW (min lat and long)
  public void setOrderedBounds(Double minY, Double minX, Double maxY,
      Double maxX) {
    setOrderedX(minX, maxX);
    setOrderedY(minY, maxY);
  }

  // Insure that the greater of the x values goes in max and the lesser in min
  public void setOrderedX(Double x1, Double x2) {
    if (max == null) {
      max = new Point(x2, 0d);
    }
    if (min == null) {
      min = new Point(x1, 0d);
    }
    if (x1 > x2) {
      min.setLongitude(x2);
      max.setLongitude(x1);
    } else {
      min.setLongitude(x1);
      max.setLongitude(x2);
    }
  }

  // Insure that the greater of the y values goes in max and the lesser in min
  public void setOrderedY(Double y1, Double y2) {
    if (max == null) {
      max = new Point(0d, y2);
    }
    if (min == null) {
      min = new Point(0d, y1);
    }
    if (y1 > y2) {
      min.setLatitude(y2);
      max.setLatitude(y1);
    } else {
      min.setLatitude(y1);
      max.setLatitude(y2);
    }
  }

  /**
   * returns the size of the surface defined by this bbox
   * 
   * @return
   */
  @Transient
  public double surface() {
    return width() * height();
  }

  @Override
  public String toString() {
    // minY,minX maxY,maxX
    return String.format("%s %s", min, max);
  }

  /*
   * @See http://georss.org/simple
   * 
   * @return polygon string which is a space separated list of
   * latitude-longitude pairs
   */
  public String toStringGeoRSS() {
    return String.format("%s   %s %s   %s   %s %s", min.toStringSpace(),
        max.getLongitude(), min.getLatitude(), max.toStringSpace(),
        min.getLongitude(), max.getLatitude());
  }

  public String toStringShort(int decimals) {
    // minY,minX maxY,maxX
    return String.format("%s %s", min.toStringShort(decimals),
        max.toStringShort(decimals));
  }

  /**
   * format used in WMS for bboxes: minX(longitude), minY(latitude), maxX, maxY
   * 
   * @return
   */
  public String toStringWMS() {
    // minX,minY,maxX,maxY
    return String.format("%s,%s,%s,%s", min.getLongitude(), min.getLatitude(),
        max.getLongitude(), max.getLatitude());
  }

  @Transient
  public double width() {
    if (max == null || min == null) {
      return 0.0;
    }
    return max.getX() - min.getX();
  }
}