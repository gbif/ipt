/*
* Copyright 2008 Google Inc. 
* Licensed under the Apache License, Version 2.0:
*  http://www.apache.org/licenses/LICENSE-2.0
*/
package com.google.maps.extras.markermanager {
import flash.geom.Point;

public class GridBounds {
  public var z:Number;
  public var minX:Number;
  public var minY:Number;
  public var maxX:Number;
  public var maxY:Number;

  
/**
 * A Bounds is defined by minimum and maximum X and Y coordinates on a plane.
 * @param {Array.<Point>} opt_points  Points which this Bound must contain.
 * @constructor
 */
public function GridBounds(opt_points:Array):void {
  this.minX = Infinity;
  this.minY = Infinity;
  this.maxX = -Infinity;
  this.maxY = -Infinity;
  if (opt_points && opt_points.length) {
    for (var i:Number = 0; i < opt_points.length; i++) {
      this.extend(opt_points[i]);
    }
  } 
}

/**
 * Gets the minimum x and y in this bound.
 *
 * @return {Point}
 */
public function min():Point {
  return new Point(this.minX, this.minY);
}


/**
 * Gets the maximum x and y in this bound.
 *
 * @return {Point}
 */
public function max():Point {
  return new Point(this.maxX, this.maxY);
}


/**
 * @return {Size} The size of this bounds.
 */
public function getSize():Point {
  return new Point(this.maxX - this.minX, this.maxY - this.minY);
}


/**
 * Gets the midpoint x and y in this bound.
 *
 * @return {Point} The midpoint.
 */
public function mid():Point {
  return new Point((this.minX + this.maxX) / 2, (this.minY + this.maxY) / 2);
}


/**
 * Returns a string representation of this bound.
 *
 * @returns {string}
 */
public function toString():String {
  return "(" + this.min() + ", " + this.max() + ")";
}

/**
 * Test for empty bounds.
 * @return {boolean}  This Bounds is empty
 */
public function isEmpty():Boolean {
  return (this.minX > this.maxX || this.minY > this.maxY);
}


/**
 * Returns true if this bounds (inclusively) contains the given bounds.
 * @param {Bounds} inner  Inner Bounds.
 * @return {boolean} This Bounds contains the given Bounds.
 */
public function containsBounds(inner:GridBounds): Boolean {
  var outer:GridBounds = this;
  return (outer.minX <= inner.minX &&
          outer.maxX >= inner.maxX &&
          outer.minY <= inner.minY &&
          outer.maxY >= inner.maxY);
}


/**
 * Returns true if this bounds (inclusively) contains the given point.
 * @param {Point} point  The point to test.
 * @return {boolean} This Bounds contains the given Point.
 */
public function containsPoint(point:Point):Boolean {
  var outer:GridBounds = this;
  return (outer.minX <= point.x &&
          outer.maxX >= point.x &&
          outer.minY <= point.y &&
          outer.maxY >= point.y);
}



/**
 * Extends this bounds to contain the given point.
 *
 * @param {Point} point  Additional point.
 */
public function extend(point:Point):void {
  if (this.isEmpty()) {
    this.minX = this.maxX = point.x;
    this.minY = this.maxY = point.y;
  } else {
    this.minX = Math.min(this.minX, point.x);
    this.maxX = Math.max(this.maxX, point.x);
    this.minY = Math.min(this.minY, point.y);
    this.maxY = Math.max(this.maxY, point.y);
  }
}


/**
 * Compare this bounds to another.
 * @param {Bounds} bounds The bounds to test against.
 * @return {boolean} True when the bounds are equal.
 */
public function equals(bounds:GridBounds):Boolean {
  return this.minX == bounds.minX &&
         this.minY == bounds.minY &&
         this.maxX == bounds.maxX &&
         this.maxY == bounds.maxY;
}

}

}
