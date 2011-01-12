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
	*	Class that represents a &lt;Placemark&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#placemark
	*/
	public class Placemark extends Feature
	{
		private var _geometry:Geometry;
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Placemark(x:XMLList)
		{
			super(x);
			
			// Features are: <Point>, <LineString>, <LinearRing>, <Polygon>, <MultiGeometry>, <Model>
			// We'll only support <Point>, <LineString>, <LinearRing>, <Polygon>
			if (ParsingTools.nullCheck(this.x.kml::Point)) {
				this._geometry = new Point(this.x.kml::Point);
			}
			if (ParsingTools.nullCheck(this.x.kml::LineString)) {
				this._geometry = new LineString(this.x.kml::LineString);
			}
			if (ParsingTools.nullCheck(this.x.kml::LinearRing)) {
				this._geometry = new LinearRing(this.x.kml::LinearRing);
			}
			if (ParsingTools.nullCheck(this.x.kml::Polygon)) {
				this._geometry = new Polygon(this.x.kml::Polygon);
			}
		}
		
		/**
		*	Represents the child geometry element (there can be only one).
		*/
		public function get geometry():Geometry {
			return this._geometry;
		}
		
		public override function toString():String {
			return "Placemark: " + " geometry: " + this._geometry;
		}
	}
}
