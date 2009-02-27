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
	 * addMapType(Mars.ELEVATION_MAP_TYPE);
	 * addMapType(Mars.VISIBLE_MAP_TYPE);
	 * addMapType(Mars.INFRARED_MAP_TYPE);
	 */
	public class Mars {
		
        private static var PLANETARY_MAPS_SERVER:String =  "http://mw1.google.com/mw-planetary/";
        
        private static var MARS_PARAMETERS:Object = {
        "elevation": {
            name: "elevation",
            url: PLANETARY_MAPS_SERVER + "mars/elevation/",
            zoom_levels: 8,
            credits: "NASA/JPL/GSFC"
            },
        "visible": {
            url: PLANETARY_MAPS_SERVER + "mars/visible/",
            zoom_levels: 9,
            credits: "NASA/JPL/ASU/MSSS"
            },
        "infrared": {
            url: PLANETARY_MAPS_SERVER + "mars/infrared/",
            zoom_levels: 12,
            credits: "NASA/JPL/ASU"
            }
        };  
        
        private static var MARS_MAP_TYPES:Object = {};
        
        /**
        * This map type displays a shaded relief map of the surface of Mars, color-coded by altitude. 
        */
        public static function get ELEVATION_MAP_TYPE():MapType {
        	return getMapType("elevation");
        }
        
        /**
        * This map type displays photographs taken from orbit around Mars. 
        */
        public static function get VISIBLE_MAP_TYPE():MapType {
            return getMapType("visible");
        }
        
        /**
        * This map type displays a shaded infrared map of the surface of Mars, where warmer areas appear brighter and colder areas appear darker.
        */
        public static function get INFRARED_MAP_TYPE():MapType {
            return getMapType("infrared");
        }      
        
        private static function getMapType(name:String):MapType {
        	if (!MARS_MAP_TYPES[name]) {
	            var params:Object = MARS_PARAMETERS[name];
	            var copyright:CopyrightCollection = new CopyrightCollection();
	            copyright.addCopyright(
	                new Copyright(
	                  "mars_" + name,
	                   new LatLngBounds(new LatLng(-180, -90), new LatLng(180,90)), 
	                   0, 
	                   params.credits));
	            var layer:MarsTileLayer = new MarsTileLayer(params.url, copyright, params.zoom_levels);
	            var maptype:MapType = new MapType(
	                [layer],  
	                MapType.NORMAL_MAP_TYPE.getProjection(), 
	                name, 
	                new MapTypeOptions({
	                                  radius: 3396200,
	                                  shortName: name,
	                                  alt: "Show " + name + " map"
	                }));
	            MARS_MAP_TYPES[name] = maptype;
	        }
            return MARS_MAP_TYPES[name];
        }
    } 
}