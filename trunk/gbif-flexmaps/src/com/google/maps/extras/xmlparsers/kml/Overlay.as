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
	*	Base class extended by GroundOverlay, ScreenOverlay (not implemented), and PhotoOverlay (not implemented).
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#overlay
	*/
	public class Overlay extends Feature
	{
		private var _color:String;
		private var _drawOrder:Number;
		private var _icon:Icon;
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Overlay(x:XMLList)
		{
			super(x);
			this._color = ParsingTools.nullCheck(this.x.kml::color);
			this._drawOrder = ParsingTools.nanCheck(this.x.kml::drawOrder);
			if (ParsingTools.nullCheck(this.x.kml::Icon)) {
				this._icon = new Icon(this.x.kml::Icon);
			}
		}

		/**
		*	Represents the &lt;color&gt; child element.
		*/	
		public function get color():String
		{
			return this._color;
	 	}
	 	
		/**
		*	Represents the &lt;drawOrder&gt; child element.
		*/	
		public function get drawOrder():Number
		{
			return this._drawOrder;
	 	} 	
	 	
	 	/**
		*	Represents the &lt;href&gt; child element.
		*/	
		public function get icon():Icon
		{
			return this._icon;
	 	} 	
	 	
	 	public override function toString():String {
	 		return "Overlay: " + "color: " + this._color + "drawOrder: " + this._drawOrder	+ "icon: " + this._icon;
	 	}
	}
}
