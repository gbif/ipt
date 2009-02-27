/*
* Copyright 2008 Google Inc. 
* Licensed under the Apache License, Version 2.0:
*  http://www.apache.org/licenses/LICENSE-2.0
*/
package com.google.maps.extras.planetary {
	import com.google.maps.Copyright;
	import com.google.maps.CopyrightCollection;
	import com.google.maps.LatLng;
	import com.google.maps.LatLngBounds;
	import com.google.maps.MapType;
	import com.google.maps.MapTypeOptions;
	import com.google.maps.TileLayerBase;
	
    /**
     * Contains all the static Mars map types. To use, import this class and then call on your Map object:
     * addMapType(Sky.VISIBLE_MAP_TYPE);
     */
	public class Sky  {
		
        private static var PLANETARY_MAPS_SERVER:String =  "http://mw1.google.com/mw-planetary/";
        
        private static var SKY_PARAMETERS:Object = {
        "visible" : {
            url: PLANETARY_MAPS_SERVER + "sky/skytiles_v1/",
            zoom_levels: 19
        }
        };
        
        private static var SKY_MAP_TYPES:Object = {};
        
        /**
        * This map type shows a mosaic of the sky, covering the full celestial sphere.
        */
        public static function get VISIBLE_MAP_TYPE():MapType {
            return getMapType("visible");
        }  
        
        private static function getMapType(name:String):MapType {
        	if (!SKY_MAP_TYPES[name]) {
	            var params:Object = SKY_PARAMETERS[name];
	            var copyright:CopyrightCollection = new CopyrightCollection();
	            copyright.addCopyright(
	                new Copyright(
	                  "sky_" + name,
	                   new LatLngBounds(new LatLng(-180, -90), new LatLng(180,90)), 
	                   0, 
	                   "SDSS, DSS Consortium, NASA/ESA/STScI"));
	            var layer:SkyTileLayer = new SkyTileLayer(params.url, copyright, params.zoom_levels);
	            var maptype:MapType = new MapType(
	                [layer],  
	                MapType.NORMAL_MAP_TYPE.getProjection(), 
	                name, 
	                new MapTypeOptions({
	                                  radius: 57.2957763671875,
	                                  shortName: name,
	                                  alt: "Show " + name + " map"
	                }));
	            SKY_MAP_TYPES[name] = maptype;
            }
            return SKY_MAP_TYPES[name];
        }
    } 
}