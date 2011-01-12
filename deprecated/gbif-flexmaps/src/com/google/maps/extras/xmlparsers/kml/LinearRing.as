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
	*	Class that represents a &lt;LinearRing&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#linearring
	*/
	public class LinearRing extends Geometry
	{
		//todo: add constants for the enum values?
		
		// Can contain: <extrude>, <tessellate>, <altitudeMode>, <coordinates>
		// We support coordinates only
		private var _coordinates:Coordinates;		
  
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function LinearRing(x:XMLList)
		{
			super(x);
			if (ParsingTools.nullCheck(this.x.kml::coordinates) != null) {
				this._coordinates = new Coordinates(ParsingTools.nullCheck(this.x.kml::coordinates));
			}
		}
	 	
		/**
		*	Represents the &lt;coordinates&gt; child element.
		*/	
		public function get coordinates():Coordinates
		{
			return this._coordinates;
	 	}
	 	
	 	public override function toString():String {
	 		return "LinearRing: " + super.toString() + " coordinates" + this._coordinates;
	 	}
	}
}
