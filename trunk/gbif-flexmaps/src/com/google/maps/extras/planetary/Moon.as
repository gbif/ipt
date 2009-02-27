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
     * addMapType(Moon.ELEVATION_MAP_TYPE);
     * addMapType(Moon.VISIBLE_MAP_TYPE);
     */
	public class Moon {
		
        private static var PLANETARY_MAPS_SERVER:String =  "http://mw1.google.com/mw-planetary/";
        
        private static var MOON_PARAMETERS:Object = {
        "visible" : {
            name: "visible",
            url: PLANETARY_MAPS_SERVER + "lunar/lunarmaps_v1/clem_bw/",
            zoom_levels: 9
        },
        "elevation": {
            name: "elevation",
            url: PLANETARY_MAPS_SERVER + "lunar/lunarmaps_v1/terrain/",
            zoom_levels: 7
        }
        };
        
        private static var MOON_MAP_TYPES:Object = {};
        
        /**
        * This map type displays a shaded terrain map of the surface of the Moon, color-coded by altitude. 
        */
        public static function get ELEVATION_MAP_TYPE():MapType {
        	return getMapType("elevation");
        }
        
        /**
        * This map type displays photographs taken from orbit around the moon.
        */
        public static function get VISIBLE_MAP_TYPE():MapType {
            return getMapType("visible");
        }
        
        private static function getMapType(name:String):MapType {
        	if (!MOON_MAP_TYPES[name]) {
	        	var params:Object = MOON_PARAMETERS[name];
	            var copyright:CopyrightCollection = new CopyrightCollection();
	            copyright.addCopyright(
	                new Copyright(
	                  "moon_" + name,
	                   new LatLngBounds(new LatLng(-180, -90), new LatLng(180,90)), 
	                   0, 
	                   "NASA/USGS"));
	            var layer:MoonTileLayer = new MoonTileLayer(params.url, copyright, params.zoom_levels);
	            var maptype:MapType = new MapType(
	                [layer],  
	                MapType.NORMAL_MAP_TYPE.getProjection(), 
	                name, 
	                new MapTypeOptions({
	                                  radius: 1738000,
	                                  shortName: name,
	                                  alt: "Show " + name + " map"
	                }));
	           MOON_MAP_TYPES[name] = maptype;
            }
            return MOON_MAP_TYPES[name];
        }
    } 
}