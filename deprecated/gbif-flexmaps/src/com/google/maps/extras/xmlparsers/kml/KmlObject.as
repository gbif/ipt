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
	*	Class that represents any generic KML object. Extended by all KML elements.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#object
	*/
	public class KmlObject extends XmlElement
	{
		public var kml:Namespace = Namespaces.KML_NS;
		
		private var _id:String;
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function KmlObject(x:XMLList)
		{
			super(x);
			this._id = ParsingTools.nullCheck(this.x.kml::id);
		}

		/**
		*	Represents the id attribute of the element.
		*/	
		public function get id():String
		{
			return this._id;
	 	}
	 	
	 	public function toString():String {
	 		return "KmlObject with id: " + this._id;
	 	}
	}
}
