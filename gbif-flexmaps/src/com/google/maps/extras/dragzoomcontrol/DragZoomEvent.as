/* 
 * DragZoomControl v1.0
 * Author: Brian Richardson
 * Email: irieb@mac.com
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 * 
 */
package com.google.maps.extras.dragzoomcontrol
{
	import com.google.maps.LatLngBounds;
	
	import flash.events.Event;

	public class DragZoomEvent extends Event {
		
		public static const ZOOM_COMMIT:String = "zoomCommit";
		
		private var _bounds:LatLngBounds;
	
		/**
		 * Creates the DragZoomEvent
		 *
		 * @constructor
		 */		
		public function DragZoomEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}
		
		/**
		 * Set LatLngBounds
		 *
		 * @param {LatLngBounds} pBounds The LatLngBounds
		 */			
		public function set bounds(pBounds:LatLngBounds):void {
			_bounds = pBounds;		
		}
		
		/**
		 * Return the set LatLngBounds
		 *
		 */			
		public function get bounds():LatLngBounds {
			return _bounds;
		}	
		
	}
}