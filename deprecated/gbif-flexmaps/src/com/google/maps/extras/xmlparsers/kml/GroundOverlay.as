/*
* Copyright 2008 Google Inc. 
* Licensed under the Apache License, Version 2.0:
*  http://www.apache.org/licenses/LICENSE-2.0
*/
package com.google.maps.extras.xmlparsers.kml
{
    import com.google.maps.extras.xmlparsers.Namespaces;
	import com.google.maps.extras.xmlparsers.ParsingTools;
	import com.google.maps.extras.xmlparsers.XmlElement;

	/**
	*	Class that represents a &lt;GroundOverlay&gt;
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#groundoverlay
	*/
	public class GroundOverlay extends Overlay
	{
		private var _latLonBox:LatLonBox;
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function GroundOverlay(x:XMLList)
		{
			super(x);
			
			this._latLonBox = new LatLonBox(this.x.kml::LatLonBox);
		}	
		
		/**
		* Represents the &lt;LatLonBox&gt; child element.
		*/
		public function get latLonBox():LatLonBox {
			return this._latLonBox;
		}
		
		public override function toString():String {
			return "GroundOverlay: " + super.toString() + this._latLonBox;
		}
	}
}
