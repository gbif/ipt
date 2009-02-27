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
public class MoonTileLayer extends TileLayerBase {

  private var _baseUrl:String;
  
  /**
   * Constructor for the class.
   */
  public function MoonTileLayer(baseUrl:String, copyrightCollection:CopyrightCollection, zoomLevels:Number) {
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
    var url:String = this._baseUrl + zoom + "/" + tilePos.x + "/" +
      (bound - tilePos.y - 1) + ".jpg";
      
    var testLoader:Loader = new Loader();
    var urlRequest:URLRequest = new URLRequest(url); 
    testLoader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
    testLoader.load(urlRequest);	
    return testLoader;
  }

  private function ioErrorHandler(event:IOErrorEvent):void {
    trace("ioErrorHandler: " + event);
  }
}
}
