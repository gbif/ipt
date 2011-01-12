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
	*	Base class for LatLonBox and LatLonAltBox (not implemented).
	*/
	public class AbstractLatLonBox extends KmlObject
	{

		private var _north:Number;	
		private var _south:Number;	
		private var _east:Number;	
		private var _west:Number;		
  
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function AbstractLatLonBox(x:XMLList)
		{
			super(x);
			this._north  = ParsingTools.nanCheck(this.x.kml::north);
			this._south = ParsingTools.nanCheck(this.x.kml::south);
			this._east = ParsingTools.nanCheck(this.x.kml::east);
			this._west = ParsingTools.nanCheck(this.x.kml::west);
		}
	 	
		/**
		*	Represents the &lt;north&gt; child element.
		*/	
		public function get north():Number
		{
			return this._north;
	 	}
	 	
	 	/**
		*	Represents the &lt;south&gt; child element.
		*/	
		public function get south():Number
		{
			return this._south;
	 	}
	 	
	 	/**
		*	Represents the &lt;east&gt; child element.
		*/	
		public function get east():Number
		{
			return this._east;
	 	}
	 	
	 	/**
		*	Represents the &lt;west&gt; child element.
		*/	
		public function get west():Number
		{
			return this._west;
	 	}

		public override function toString():String {
			return "AbstractLatLonBox: " + " north: " + this._north + "south: " + this._south + "east: " + this._east + " west: " + this._west;
		}
	}
}
