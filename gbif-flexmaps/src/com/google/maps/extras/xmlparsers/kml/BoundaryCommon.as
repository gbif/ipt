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
	*	Base class for innerBoundaryIs and outerBoundaryIs. They're used to describe different
	*     boundaries, but both contain a single LinearRing element.
	*/
	public class BoundaryCommon extends KmlObject
	{
		
		private var _linearRing:LinearRing;
  
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function BoundaryCommon(x:XMLList)
		{
			super(x);
			
			this._linearRing = new LinearRing(this.x.kml::LinearRing);
		}

		/**
		*	Represents the &lt;LinearRing&gt; child element.
		*/	
		public function get linearRing():LinearRing
		{
			return this._linearRing;
	 	}
	 	
	 	public override function toString():String {
	 		return "Boundary Common: linearRing: " + this._linearRing;
	 	}
	}
}
