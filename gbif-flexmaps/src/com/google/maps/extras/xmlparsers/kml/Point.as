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
	*	Class that represents a &lt;Point&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#point
	*/
	public class Point extends Geometry
	{
		private var _coordinates:Coordinates;		
  
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Point(x:XMLList)
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
	 		return "Point: " + this._coordinates;	
	 	}
	}
}
