/* 
 * MarkerManager, v1.0
 * Copyright (c) 2007 Google Inc.
 * Author: Doug Ricket, others
 *  Ported to AS3 by Pamela Fox 
 *
 * Licensed under the Apache License, Version 2.0:
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Marker manager is an interface between the map and the user, designed
 * to manage adding and removing many points when the viewport changes.
 *
 * Algorithm: The MM places its markers onto a grid, similar to the map tiles.
 * When the user moves the viewport, the MM computes which grid cells have
 * entered or left the viewport, and shows or hides all the markers in those
 * cells.
 * (If the users scrolls the viewport beyond the markers that are loaded,
 * no markers will be visible until the EVENT_moveend triggers an update.)
 *
 * In practical consequences, this allows 10,000 markers to be distributed over
 * a large area, and as long as only 100-200 are visible in any given viewport,
 * the user will see good performance corresponding to the 100 visible markers,
 * rather than poor performance corresponding to the total 10,000 markers.
 *
 * Note that some code is optimized for speed over space,
 * with the goal of accommodating thousands of markers.
 *
 */
package com.google.maps.extras.markermanager {
import flash.events.Event;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.display.Shape;
import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;
import com.google.maps.MapEvent;
import com.google.maps.MapMoveEvent;
import com.google.maps.Map;
import com.google.maps.LatLng;
import com.google.maps.LatLngBounds;
import com.google.maps.overlays.Marker;
import com.google.maps.interfaces.IMap;
import com.google.maps.interfaces.IProjection;
import com.google.maps.extras.markermanager.GridBounds;

public class MarkerManager {
	 
// Static constants:
public static const DEFAULT_TILE_SIZE:Number = 1024;
public static const DEFAULT_MAX_ZOOM:Number = 17;
public static const DEFAULT_BORDER_PADDING:Number = 100;
public static const MERCATOR_ZOOM_LEVEL_ZERO_RANGE:Number = 256;

private var map_:IMap;
private var mapZoom_:Number;
private var maxZoom_:Number;
private var projection_:IProjection;
private var trackMarkers_:Boolean;
private var swPadding_:Point;
private var nePadding_:Point;
private var borderPadding_:Number;
private var gridWidth_:Array;
private var grid_:Array;
private var numMarkers_:Array;
private var shownBounds_:GridBounds;
private var shownMarkers_:Number;
private var tileSize_:Number;

/**
 * Creates a new MarkerManager that will show/hide markers on a map.
 *
 * @constructor
 * @param {Map} The map to manage.
 * @param {Object} A container for optional arguments:
 *   {Number} The maximum zoom level for which to create tiles.
 *   {Number} The width in pixels beyond the map border,
 *                   where markers should be display.
 *   {Boolean} Whether or not this manager should track marker
 *                   movements.
 */
public function MarkerManager(map:IMap, opt_opts:Object) {
  this.map_ = map;
  this.mapZoom_ = map.getZoom();
  this.projection_ = map.getCurrentMapType().getProjection();

  opt_opts = opt_opts || {};
  this.tileSize_ = DEFAULT_TILE_SIZE;
  
  var maxZoom:Number = DEFAULT_MAX_ZOOM;
  if(opt_opts.maxZoom != undefined) {
    maxZoom = opt_opts.maxZoom;
  }
  this.maxZoom_ = maxZoom;

  this.trackMarkers_ = opt_opts.trackMarkers;

  var padding:Number;
  if (opt_opts.borderPadding) {
    padding = opt_opts.borderPadding;
  } else {
    padding = DEFAULT_BORDER_PADDING;
  }
  // The padding in pixels beyond the viewport, where we will pre-load markers.
  this.swPadding_ = new Point(-padding, padding);
  this.nePadding_ = new Point(padding, -padding);
  this.borderPadding_ = padding;

  this.gridWidth_ = new Array();

  this.grid_ = new Array();
  this.grid_[maxZoom] = new Array();
  this.numMarkers_ = new Array();
  this.numMarkers_[maxZoom] = 0;

  this.map_.addEventListener(MapMoveEvent.MOVE_END, this.onMapMoveEnd_);

  this.resetManager_();
  this.shownMarkers_ = 0;

  this.shownBounds_ = this.getMapGridBounds_();
};


// NOTE: These two closures provide easy access to the map.
// They are used as callbacks, not as methods.
private function removeOverlay_(marker:Marker):void {
  this.map_.removeOverlay(marker);
  this.shownMarkers_--;
}

private function addOverlay_(marker:Marker):void {
  this.map_.addOverlay(marker);
  this.shownMarkers_++;
}

/**
 * Initializes MarkerManager arrays for all zoom levels
 * Called by constructor and by clearAllMarkers
 */ 
private function resetManager_():void {
  var mapWidth:Number = MERCATOR_ZOOM_LEVEL_ZERO_RANGE;
  for (var zoom:Number = 0; zoom <= this.maxZoom_; ++zoom) {
    this.grid_[zoom] = new Array();
    this.numMarkers_[zoom] = 0;
    this.gridWidth_[zoom] = Math.ceil(mapWidth/this.tileSize_);
    mapWidth <<= 1;
  }
}

/**
 * Removes all currently displayed markers
 * and calls resetManager to clear arrays
 */
public function clearMarkers():void {
  this.processAll_(this.shownBounds_, this.removeOverlay_);
  this.resetManager_();
}


/**
 * Gets the tile coordinate for a given latlng point.
 *
 * @param {LatLng} The geographical point.
 * @param {Number} The zoom level.
 * @param {GSize} The padding used to shift the pixel coordinate.
 *               Used for expanding a bounds to include an extra padding
 *               of pixels surrounding the bounds.
 * @return {GPoint} The point in tile coordinates.
 *
 */
private function getTilePoint_(latlng:LatLng, zoom:Number, padding:Point):Point {
  var pixelPoint:Point = this.projection_.fromLatLngToPixel(latlng, zoom);
  return new Point(
      Math.floor((pixelPoint.x + padding.x) / this.tileSize_),
      Math.floor((pixelPoint.y + padding.y) / this.tileSize_));
};


/**
 * Finds the appropriate place to add the marker to the grid.
 * Optimized for speed; does not actually add the marker to the map.
 * Designed for batch-processing thousands of markers.
 *
 * @param {Marker} The marker to add.
 * @param {Number} The minimum zoom for displaying the marker.
 * @param {Number} The maximum zoom for displaying the marker.
 */
private function addMarkerBatch_(marker:Marker, minZoom:Number, maxZoom:Number):void {
  var mPoint:LatLng = marker.getLatLng();
  // Tracking markers is expensive, so we do this only if the
  // user explicitly requested it when creating marker manager.
  if (this.trackMarkers_) {
    marker.addEventListener("changed", this.onMarkerMoved_);
  }

  var gridPoint:Point = this.getTilePoint_(mPoint, maxZoom, new Point(0, 0));

  for (var zoom:Number = maxZoom; zoom >= minZoom; zoom--) {
    var cell:Array = this.getGridCellCreate_(gridPoint.x, gridPoint.y, zoom);
    cell.push(marker);

    gridPoint.x = gridPoint.x >> 1;
    gridPoint.y = gridPoint.y >> 1;
  }
};


/**
 * Returns whether or not the given point is visible in the shown bounds. This
 * is a helper method that takes care of the corner case, when shownBounds have
 * negative minX value.
 *
 * @param {Point} A point on a grid.
 * @return {Boolean} Whether or not the given point is visible in the currently
 * shown bounds.
 */
private function isGridPointVisible_(point:Point):Boolean {
  var vertical:Boolean = this.shownBounds_.minY <= point.y &&
      point.y <= this.shownBounds_.maxY;
  var minX:Number = this.shownBounds_.minX;
  var horizontal:Boolean = minX <= point.x && point.x <= this.shownBounds_.maxX;
  if (!horizontal && minX < 0) {
    // Shifts the negative part of the rectangle. As point.x is always less
    // than grid width, only test shifted minX .. 0 part of the shown bounds.
    var width:Number = this.gridWidth_[this.shownBounds_.z];
    horizontal = minX + width <= point.x && point.x <= width - 1;
  }
  return vertical && horizontal;
}


/**
 * Reacts to a notification from a marker that it has moved to a new location.
 * It scans the grid all all zoom levels and moves the marker from the old grid
 * location to a new grid location.
 *
 * @param {Marker} marker The marker that moved.
 * @param {LatLng} oldLatLng The old position of the marker.
 * @param {LatLng} newLatLng The new position of the marker.
 */
private function onMarkerMoved_(marker:Marker, oldLatLng:LatLng, newLatLng:LatLng):void {
  // NOTE: We do not know the minimum or maximum zoom the marker was
  // added at, so we start at the absolute maximum. Whenever we successfully
  // remove a marker at a given zoom, we add it at the new grid coordinates.
  var zoom:Number = this.maxZoom_;
  var changed:Boolean = false;
  var oldGrid:Point = this.getTilePoint_(oldLatLng, zoom, new Point(0, 0));
  var newGrid:Point = this.getTilePoint_(newLatLng, zoom, new Point(0, 0));
  while (zoom >= 0 && (oldGrid.x != newGrid.x || oldGrid.y != newGrid.y)) {
    var cell:Array = this.getGridCellNoCreate_(oldGrid.x, oldGrid.y, zoom);
    if (cell) {
      if (this.removeFromArray(cell, marker)) {
        this.getGridCellCreate_(newGrid.x, newGrid.y, zoom).push(marker);
      }
    }
    // For the current zoom we also need to update the map. Markers that no
    // longer are visible are removed from the map. Markers that moved into
    // the shown bounds are added to the map. This also lets us keep the count
    // of visible markers up to date.
    if (zoom == this.mapZoom_) {
      if (this.isGridPointVisible_(oldGrid)) {
        if (!this.isGridPointVisible_(newGrid)) {
          this.removeOverlay_(marker);
          changed = true;
        }
      } else {
        if (this.isGridPointVisible_(newGrid)) {
          this.addOverlay_(marker);
          changed = true;
        }
      }
    }
    oldGrid.x = oldGrid.x >> 1;
    oldGrid.y = oldGrid.y >> 1;
    newGrid.x = newGrid.x >> 1;
    newGrid.y = newGrid.y >> 1;
    --zoom;
  }
  if (changed) {
    this.notifyListeners_();
  }
};


/**
 * Searches at every zoom level to find grid cell
 * that marker would be in, removes from that array if found.
 * Also removes marker with removeOverlay if visible.
 * @param {GMarker} The marker to delete.
 */
public function removeMarker(marker:Marker):void {
  var zoom:Number = this.maxZoom_;
  var changed:Boolean = false;
  var point:LatLng = marker.getLatLng();
  var grid:Point = this.getTilePoint_(point, zoom, new Point(0, 0));
  while (zoom >= 0) {
    var cell:Array = this.getGridCellNoCreate_(grid.x, grid.y, zoom);

    if (cell) {
      this.removeFromArray(cell, marker);
    }
    // For the current zoom we also need to update the map. Markers that no
    // longer are visible are removed from the map. This also lets us keep the count
    // of visible markers up to date.
    if (zoom == this.mapZoom_) {
      if (this.isGridPointVisible_(grid)) {
          this.removeOverlay_(marker);
          changed = true;
      } 
    }
    grid.x = grid.x >> 1;
    grid.y = grid.y >> 1;
    --zoom;
  }
  if (changed) {
    this.notifyListeners_();
  }
}


/**
 * Add many markers at once.
 * Does not actually update the map, just the internal grid.
 *
 * @param {Array} The marker objects to add.
 * @param {Number} The minimum zoom level to display the markers.
 * @param {Number} The  maximum zoom level to display the markers.
 */
public function addMarkers(markers:Array, minZoom:Number, opt_maxZoom:Number = Infinity):void {
  var maxZoom:Number = this.getOptMaxZoom_(opt_maxZoom);
  for (var i:Number = markers.length - 1; i >= 0; i--) {
    this.addMarkerBatch_(markers[i], minZoom, maxZoom);
  }

  this.numMarkers_[minZoom] += markers.length;
}


/**
 * Returns the value of the optional maximum zoom. This method is defined so
 * that we have just one place where optional maximum zoom is calculated.
 *
 * @param {Number} The optinal maximum zoom.
 * @return The maximum zoom.
 */
private function getOptMaxZoom_(opt_maxZoom:Number):Number {
  return opt_maxZoom != Infinity ? opt_maxZoom : this.maxZoom_;
}


/**
 * Calculates the total number of markers potentially visible at a given
 * zoom level.
 *
 * @param {Number} The zoom level to check.
 * @return {Number}
 */
public function getMarkerCount(zoom:Number):Number {
  var total:Number = 0;
  for (var z:Number = 0; z <= zoom; z++) {
    total += this.numMarkers_[z];
  }
  return total;
};


/**
 * Add a single marker to the map.
 *
 * @param {Marker} The marker to add.
 * @param {Number} The minimum zoom level to display the marker.
 * @param {Number} The maximum zoom level to display the marker.
 */
public function addMarker(marker:Marker, minZoom:Number, opt_maxZoom:Number):void {
  var maxZoom:Number = this.getOptMaxZoom_(opt_maxZoom);
  this.addMarkerBatch_(marker, minZoom, maxZoom);
  var gridPoint:Point = this.getTilePoint_(marker.getLatLng(), this.mapZoom_, new Point(0, 0));
  if(this.isGridPointVisible_(gridPoint) && 
    minZoom <= this.shownBounds_.z &&
    this.shownBounds_.z <= maxZoom ) {
    this.addOverlay_(marker);
    this.notifyListeners_();
  }
  this.numMarkers_[minZoom]++;
};


/**
 * Get a cell in the grid, creating it first if necessary.
 *
 * Optimization candidate
 *
 * @param {Number} x The x coordinate of the cell.
 * @param {Number} y The y coordinate of the cell.
 * @param {Number} z The z coordinate of the cell.
 * @return {Array} The cell in the array.
 */
private function getGridCellCreate_(x:Number, y:Number, z:Number):Array {
  var grid:Array = this.grid_[z];
  if (x < 0) {
    x += this.gridWidth_[z];
  }
  var gridCol:Array = grid[x];
  if (!gridCol) {
    gridCol = grid[x] = [];
    return gridCol[y] = [];
  }
  var gridCell:Array = gridCol[y];
  if (!gridCell) {
    return gridCol[y] = [];
  }
  return gridCell;
}


/**
 * Get a cell in the grid, returning undefined if it does not exist.
 *
 * NOTE: Optimized for speed -- otherwise could combine with getGridCellCreate_.
 *
 * @param {Number} x The x coordinate of the cell.
 * @param {Number} y The y coordinate of the cell.
 * @param {Number} z The z coordinate of the cell.
 * @return {Array} The cell in the array.
 */
private function getGridCellNoCreate_(x:Number, y:Number, z:Number):Array {
  var grid:Array = this.grid_[z];
  if (x < 0) {
    x += this.gridWidth_[z];
  }
  var gridCol:Array = grid[x];
  return gridCol ? gridCol[y] : undefined;
};


/**
 * Turns at geographical bounds into a grid-space bounds.
 *
 * @param {LatLngBounds} bounds The geographical bounds.
 * @param {Number} zoom The zoom level of the bounds.
 * @param {GSize} swPadding The padding in pixels to extend beyond the
 * given bounds.
 * @param {GSize} nePadding The padding in pixels to extend beyond the
 * given bounds.
 * @return {GBounds} The bounds in grid space.
 */
private function getGridBounds_(bounds:LatLngBounds, zoom:Number, swPadding:Point, nePadding:Point):GridBounds {
  zoom = Math.min(zoom, this.maxZoom_);
  
  var bl:LatLng = bounds.getSouthWest();
  var tr:LatLng = bounds.getNorthEast();
  var sw:Point = this.getTilePoint_(bl, zoom, swPadding);
  var ne:Point = this.getTilePoint_(tr, zoom, nePadding);
  var gw:Number = this.gridWidth_[zoom];
  
  // Crossing the prime meridian requires correction of bounds.
  if (tr.lng() < bl.lng() || ne.x < sw.x) {
    sw.x -= gw;
  }
  if (ne.x - sw.x  + 1 >= gw) {
    // Computed grid bounds are larger than the world; truncate.
    sw.x = 0;
    ne.x = gw - 1;
  }
  var gridBounds:GridBounds = new GridBounds([sw, ne]);
  gridBounds.z = zoom;
  return gridBounds;
}


/**
 * Gets the grid-space bounds for the current map viewport.
 *
 * @return {Bounds} The bounds in grid space.
 */
private function getMapGridBounds_():GridBounds {
  return this.getGridBounds_(this.map_.getLatLngBounds(), this.mapZoom_,
                           this.swPadding_, this.nePadding_);
}


/**
 * Event listener for map:movend.
 * NOTE: Use a timeout so that the user is not blocked
 * from moving the map.
 *
 */
private function onMapMoveEnd_(event:MapMoveEvent):void {
  this.updateMarkers_();
  //this.objectSetTimeout_(this, this.updateMarkers_, 0);
}


/**
 * Call a function or evaluate an expression after a specified number of
 * milliseconds.
 *
 * Equivalent to the standard window.setTimeout function, but the given
 * function executes as a method of this instance. So the function passed to
 * objectSetTimeout can contain references to this.
 *    objectSetTimeout(this, function() { alert(this.x) }, 1000);
 *
 * @param {Object} object  The target object.
 * @param {Function} command  The command to run.
 * @param {Number} milliseconds  The delay.
 * @return {Boolean}  Success.
 */
/*
MarkerManager.prototype.objectSetTimeout_ = function(object, command, milliseconds) {
  return window.setTimeout(function() {
    command.call(object);
  }, milliseconds);
};
*/

/**
 * Refresh forces the marker-manager into a good state.
 * If never before initialized, shows all the markers.
 * If previously initialized, removes and re-adds all markers.
 */
public function refresh():void {
  if (this.shownMarkers_ > 0) {
    this.processAll_(this.shownBounds_, this.removeOverlay_);
  }
  this.processAll_(this.shownBounds_, this.addOverlay_);
  this.notifyListeners_();
};


/**
 * After the viewport may have changed, add or remove markers as needed.
 */
private function updateMarkers_():void {
  this.mapZoom_ = this.map_.getZoom();
  var newBounds:GridBounds = this.getMapGridBounds_();
  
  // If the move does not include new grid sections,
  // we have no work to do:
  if (newBounds.equals(this.shownBounds_) && newBounds.z == this.shownBounds_.z) {
    return;
  }

  if (newBounds.z != this.shownBounds_.z) {
    this.processAll_(this.shownBounds_, this.removeOverlay_);
    this.processAll_(newBounds, this.addOverlay_);
  } else {
    // Remove markers:
    this.rectangleDiff_(this.shownBounds_, newBounds, this.removeCellMarkers_);

    // Add markers:
    this.rectangleDiff_(newBounds, this.shownBounds_, this.addCellMarkers_);
  }
  this.shownBounds_ = newBounds;

  this.notifyListeners_();
};


/**
 * Notify listeners when the state of what is displayed changes.
 */
private function notifyListeners_():void {
  //this.dispatchEvent(new Event("changed"), this.shownBounds_, this.shownMarkers_);
}


/**
 * Process all markers in the bounds provided, using a callback.
 *
 * @param {Bounds} bounds The bounds in grid space.
 * @param {Function} callback The function to call for each marker.
 */
private function processAll_(bounds:GridBounds, callback:Function):void {
  for (var x:int = bounds.minX; x <= bounds.maxX; x++) {
    for (var y:int = bounds.minY; y <= bounds.maxY; y++) {
      this.processCellMarkers_(x, y,  bounds.z, callback);
    }
  }
}


/**
 * Process all markers in the grid cell, using a callback.
 *
 * @param {Number} x The x coordinate of the cell.
 * @param {Number} y The y coordinate of the cell.
 * @param {Number} z The z coordinate of the cell.
 * @param {Function} callback The function to call for each marker.
 */
private function processCellMarkers_(x:Number, y:Number, z:Number, callback:Function):void {
  var cell:Array = this.getGridCellNoCreate_(x, y, z);
  if (cell) {
    for (var i:int = cell.length - 1; i >= 0; i--) {
      callback(cell[i]);
    }
  }
};


/**
 * Remove all markers in a grid cell.
 *
 * @param {Number} x The x coordinate of the cell.
 * @param {Number} y The y coordinate of the cell.
 * @param {Number} z The z coordinate of the cell.
 */
private function removeCellMarkers_(x:Number, y:Number, z:Number):void {
  this.processCellMarkers_(x, y, z, this.removeOverlay_);
};


/**
 * Add all markers in a grid cell.
 *
 * @param {Number} x The x coordinate of the cell.
 * @param {Number} y The y coordinate of the cell.
 * @param {Number} z The z coordinate of the cell.
 */
private function addCellMarkers_(x:Number, y:Number, z:Number):void {
  this.processCellMarkers_(x, y, z, this.addOverlay_);
};


/**
 * Use the rectangleDiffCoords function to process all grid cells
 * that are in bounds1 but not bounds2, using a callback, and using
 * the current MarkerManager object as the instance.
 *
 * Pass the z parameter to the callback in addition to x and y.
 *
 * @param {Bounds} bounds1 The bounds of all points we may process.
 * @param {Bounds} bounds2 The bounds of points to exclude.
 * @param {Function} callback The callback function to call
 *                   for each grid coordinate (x, y, z).
 */
private function rectangleDiff_(bounds1:GridBounds, bounds2:GridBounds, callback:Function):void {
  var me:MarkerManager = this;
  this.rectangleDiffCoords(bounds1, bounds2, function(x:Number, y:Number):void {
    callback.apply(me, [x, y, bounds1.z]);
  });
};


/**
 * Calls the function for all points in bounds1, not in bounds2
 *
 * @param {Bounds} bounds1 The bounds of all points we may process.
 * @param {Bounds} bounds2 The bounds of points to exclude.
 * @param {Function} callback The callback function to call
 *                   for each grid coordinate.
 */
private function rectangleDiffCoords(bounds1:GridBounds, bounds2:GridBounds, callback:Function):void {
  var minX1:Number = bounds1.minX;
  var minY1:Number = bounds1.minY;
  var maxX1:Number = bounds1.maxX;
  var maxY1:Number = bounds1.maxY;
  var minX2:Number = bounds2.minX;
  var minY2:Number = bounds2.minY;
  var maxX2:Number = bounds2.maxX;
  var maxY2:Number = bounds2.maxY;

  var x:int;
  var y:int;

  for (x = minX1; x <= maxX1; x++) {  // All x in R1
    // All above:
    for (y = minY1; y <= maxY1 && y < minY2; y++) {  // y in R1 above R2
      callback(x, y);
    }
    // All below:
    for (y = Math.max(maxY2 + 1, minY1);  // y in R1 below R2
         y <= maxY1; y++) {
      callback(x, y);
    }
  }

  for (y = Math.max(minY1, minY2);
       y <= Math.min(maxY1, maxY2); y++) {  // All y in R2 and in R1
    // Strictly left:
    for (x = Math.min(maxX1 + 1, minX2) - 1;
         x >= minX1; x--) {  // x in R1 left of R2
      callback(x, y);
    }
    // Strictly right:
    for (x = Math.max(minX1, maxX2 + 1);  // x in R1 right of R2
         x <= maxX1; x++) {
      callback(x, y);
    }
  }
}


/**
 * Removes value from array. O(N).
 *
 * @param {Array} array  The array to modify.
 * @param {any} value  The value to remove.
 * @param {Boolean} opt_notype  Flag to disable type checking in equality.
 * @return {Number}  The number of instances of value that were removed.
 */
private function removeFromArray(array:Array, value:Object, opt_notype:Boolean = false):Number {
  var shift:int = 0;
  for (var i:int = 0; i < array.length; ++i) {
    if (array[i] === value || (opt_notype && array[i] == value)) {
      array.splice(i--, 1);
      shift++;
    }
  }
  return shift;
}

}
}
