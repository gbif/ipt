/*
* Copyright 2008 Google Inc. 
* Licensed under the Apache License, Version 2.0:
*  http://www.apache.org/licenses/LICENSE-2.0
*/
package com.google.maps.extras.planetary {

import com.google.maps.CopyrightCollection;
import com.google.maps.TileLayerBase;

import flash.display.DisplayObject;
import flash.display.Loader;
import flash.events.*;
import flash.geom.Point;
import flash.net.URLRequest;

/**
 * @private
 */
public class MarsTileLayer extends TileLayerBase {

  private var _baseUrl:String;
  
  /**
   * Constructor for the class.
   */
  public function MarsTileLayer(baseUrl:String, copyrightCollection:CopyrightCollection, zoomLevels:Number) {
    super(copyrightCollection, 0, zoomLevels);
    this._baseUrl = baseUrl; 
  }

  /**
   * Creates and loads a tile (x, y) at the given zoom level.
   * @param tilePos  Tile coordinates.
   * @param zoom  Tile zoom.
   * @return  Display object representing the tile.
   */
  public override function loadTile(tilePos:Point, zoom:Number):DisplayObject {
    var bound:Number = Math.pow(2, zoom);
    var x:Number = tilePos.x;
	var y:Number = tilePos.y;
	var quads:Array = ['t'];

	for (var z:Number = 0; z < zoom; z++) {
	   bound = bound / 2;
	   if (y < bound) {
	       if (x < bound) {
	           quads.push('q');
      		} else {
        	   quads.push('r');
        	   x -= bound;
      	     }
    	} else {
    	   if (x < bound) {
        	   quads.push('t');
        	   y -= bound;
      	   } else {
        	   quads.push('s');
        	   x -= bound;
        	   y -= bound;
      	   }
        }
    }

    var testLoader:Loader = new Loader();
    var urlRequest:URLRequest = new URLRequest(
       this._baseUrl + quads.join("") + ".jpg"); 
    testLoader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
    testLoader.load(urlRequest);	
    return testLoader;
  }

  private function ioErrorHandler(event:IOErrorEvent):void {
    trace("ioErrorHandler: " + event);
  }
}
}
