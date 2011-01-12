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
	*	Base class for Icon and Link. Both contain exactly the same child elements.
	*     and are different only in where they're used.
	*/
	public class AbstractLink extends KmlObject
	{
		//todo: add constants for the enum values?
		private var _href:String;
		private var _refreshMode:String;
		private var _refreshInterval:Number;
		private var _viewRefreshMode:String;
		private var _viewRefreshTime:Number;
		private var _viewBoundScale:Number;
		private var _viewFormat:String;
		private var _httpQuery:String;
	
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function AbstractLink(x:XMLList)
		{
			super(x);
			
			this._href = ParsingTools.nullCheck(this.x.kml::href);
			this._refreshMode = ParsingTools.nullCheck(this.x.kml::refreshMode);
			this._refreshInterval = ParsingTools.nanCheck(this.x.kml::refreshInterval);
			this._viewRefreshMode = ParsingTools.nullCheck(this.x.kml::viewRefreshMode);
			this._viewRefreshTime = ParsingTools.nanCheck(this.x.kml::viewRefreshTime);
			this._viewBoundScale = ParsingTools.nanCheck(this.x.kml::viewBoundScale);
			this._viewFormat = ParsingTools.nullCheck(this.x.kml::viewFormat);
			this._httpQuery = ParsingTools.nullCheck(this.x.kml::httpQuery);
		}

		/**
		*	Represents &lt;href&gt; child element.
		*/	
		public function get href():String
		{
			return this._href;
	 	}
	 	
		/**
		*	Represents &lt;refreshMode&gt; child element.
		*/	
		public function get refreshMode():String
		{
			return this._refreshMode;
	 	} 	
	 	
	 		 	
		/**
		*	Represents &lt;refreshInterval&gt; child element.
		*/	
		public function get refreshInterval():Number
		{
			return this._refreshInterval;
	 	} 	
	 	
	 		 	
		/**
		*	Represents &lt;viewRefreshMode&gt; child element.
		*/	
		public function get viewRefreshMode():String
		{
			return this._viewRefreshMode;
	 	} 	
	 	
	 		 	
		/**
		*	Represents &lt;viewRefreshTime&gt; child element.
		*/	
		public function get viewRefreshTime():Number
		{
			return this._viewRefreshTime;
	 	} 	
	 	
	 		 	
		/**
		*	Represents &lt;viewBoundScale&gt; child element.
		*/	
		public function get viewBoundScale():Number
		{
			return this._viewBoundScale;
	 	} 	
	 	
		/**
		*	Represents &lt;viewFormat&gt; child element.
		*/	
		public function get viewFormat():String
		{
			return this._viewFormat;
	 	} 		 	
	 	
	 	/**
		*	Represents &lt;httpQuery&gt; child element.
		*/	
		public function get httpQuery():String
		{
			return this._httpQuery;
	 	} 	
	 	
	 	public override function toString():String {
	 		return "AbstractLink: " + "href: " + this._href;
	 	}
	}
}
