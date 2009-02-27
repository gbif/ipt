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
	*	Class that represents a &lt;coordinates&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#coordinates
	*/
	public class Coordinates 
	{
		private var _coordsList:Array;		
  
		/**
		*	Constructor for class.
		* 
		*	@param x
		*
		*/	
		public function Coordinates(string:String)
		{
			var stringSplit:Array = string.split(" ");
			
			_coordsList = new Array();
			for (var i:Number = 0; i < stringSplit.length; i++) {
				var coordinate:Object = new Object();
				var coordString:Array = stringSplit[i].split(",");
				coordinate.lon = coordString[0];
				coordinate.lat = coordString[1];
				coordinate.alt = coordString[2];
				_coordsList.push(coordinate);
			}
		}
	 	
	 	
	 	/**
		*	Represents a list of coordinate objects. A coordinate object has lat, lon, and alt properties.
		*/	
		public function get coordsList():Array
		{
			return this._coordsList;
	 	}
	 	
	 	public function toString():String {
	 		return "Coords";
	 		//return "Coordinates: " + " lat: " + this._lat + " lon: " + this._lon + " alt: " + this._alt;
	 	}
	}
}
