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
	*	Class that represents a &lt;Polygon&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#polygon
	*/
	public class Polygon extends Geometry
	{
		// Can contain: <extrude>, <tessellate>, <altitudeMode>, <coordinates>, <outerBoundaryIs>, <innerBoundaryIs>*
		// We support coordinates only
		// @todo add innerBoundaryIs support
		
		private var _outerBoundaryIs:OuterBoundaryIs;		
  		
  		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/
		public function Polygon(x:XMLList)
		{
			super(x);
			
			this._outerBoundaryIs = new OuterBoundaryIs(this.x.kml::outerBoundaryIs);
		}
		
		/**
		*	Represents the &lt;outerBoundaryIs&gt; child element.
		*/
		public function get outerBoundaryIs():OuterBoundaryIs {
			return this._outerBoundaryIs;
		}
		
		public override function toString():String {
			return "Polygon: " + "outerBoundaryIs: " + this._outerBoundaryIs;
		}
	}
}
