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
	import com.google.maps.extras.xmlparsers.XmlParser;
	
	/**
	*	Class that represents a KML2.2 feed.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html
	*/	
	public class Kml22 extends XmlParser
	{
		private var kml:Namespace = Namespaces.KML_NS;
		private var _feature:Feature;

		/**
		*	Constructor for class.
		* 
		* 	@param xmlStr An Xml string containing valid KML.
		*/	
		public function Kml22(xmlStr:String)
		{
			super();
			parse(xmlStr);
			// todo support other features
			if (ParsingTools.nullCheck(this.x.kml::Placemark)) {
				this._feature = new Placemark(this.x.kml::Placemark);
			}
			if (ParsingTools.nullCheck(this.x.kml::GroundOverlay)) {
				this._feature = new GroundOverlay(this.x.kml::GroundOverlay);
			}
			if (ParsingTools.nullCheck(this.x.kml::Folder)) {
				this._feature = new Folder(this.x.kml::Folder);
			}
			if (ParsingTools.nullCheck(this.x.kml::Document)) {
				this._feature = new Document(this.x.kml::Document);
			}
		}

		/**
        * Represents the child feature of the KML root (there can only be one).
		*/	
		public function get feature():Feature
		{
			return this._feature;
		}
		
		public function toString():String {
			return "Kml22: " + this._feature.toString();
		}
	}
}