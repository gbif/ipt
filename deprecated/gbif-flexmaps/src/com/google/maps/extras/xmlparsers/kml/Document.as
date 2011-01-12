/*
* Copyright 2008 Google Inc. 
* Licensed under the Apache License, Version 2.0:
*  http://www.apache.org/licenses/LICENSE-2.0
*/
// todo: Support Schema, StyleSelector elements
package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	/**
	*	Class that represents a &lt;Document&gt; element within a KML file.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#document
	*/
	public class Document extends Container
	{
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/
		
		public var _styleMap:StyleMap;
		public var _styles:Array;
			
		public function Document(x:XMLList)
		{
			super(x);
			
			this._styles = new Array();
		 	var i:XML;
			for each (i in this.x.kml::Style) {
					this._styles.push(new com.google.maps.extras.xmlparsers.kml.Style(XMLList(i)));
			}
			if (ParsingTools.nullCheck(this.x.kml::StyleMap) != null) {
				this._styleMap = new com.google.maps.extras.xmlparsers.kml.StyleMap(this.x.kml::StyleMap);
			}
		}
		
		///(cmR)
		public function get styles():Array{
			return this._styles;
		}
		
		public function get styleMap():StyleMap{
			return this._styleMap;	
		}
		///////////
		public override function toString():String {
			return "Document: " + super.toString();
		}
	}
}
