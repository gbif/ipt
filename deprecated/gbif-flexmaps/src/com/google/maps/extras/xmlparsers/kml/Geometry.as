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
	*	Abstract element extended by Point, LineString, LinearRing, Polygon, 
	*     MultiGeometry (not implemented), and Model (not implemented).
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#geometry
	*/
	public class Geometry extends KmlObject
	{
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Geometry(x:XMLList)
		{
			super(x);
		}
		
		public override function toString():String {
			return "Geometry: " + super.toString();
		}
	}
}
