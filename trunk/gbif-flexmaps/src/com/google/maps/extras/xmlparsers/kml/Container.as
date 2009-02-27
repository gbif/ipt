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
	*	Abstract element extended by Document and Folder.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#container
	*/
	public class Container extends Feature
	{
		private var _features:Array;
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Container(x:XMLList)
		{
			super(x);

			// Features are: Placemark, GroundOverlay, ScreenOverlay, PhotoOverlay, NetworkLink, Folder, Document
			// We'll only support Placemark, GroundOverlay, Folder, and Document
			
			this._features = new Array();
		 	var i:XML;
			for each (i in this.x.kml::Placemark) {
				this._features.push(new Placemark(XMLList(i)));
			}
			for each (i in this.x.kml::GroundOverlay) {
				this._features.push(new GroundOverlay(XMLList(i)));
			}
			for each (i in this.x.kml::Folder) {
				this._features.push(new Folder(XMLList(i)));
			}
			for each (i in this.x.kml::Document) {
				this._features.push(new Folder(XMLList(i)));
			}
		}

		/**
		*	An array of child features of this container.
		*/	
		public function get features():Array
		{
			return this._features;
		}
		
		public override function toString():String {
			var str:String = "";
			var i:Feature;
			for each (i in this._features) {
				str += this._features + "\n";
			}
			return "Container: " + str;
		}
	}
}
