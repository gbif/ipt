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

	import com.google.maps.extras.xmlparsers.atom.Link;
	import com.google.maps.extras.xmlparsers.atom.Author;
	
	/**
	*	Abstract element extended by Container, Overlay, and Placemark.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#feature
	*/
	public class Feature extends KmlObject
	{
		private var atom:Namespace = Namespaces.ATOM_NS;
		
		public var _name:String;
		public var _id:String;
		public var _link: com.google.maps.extras.xmlparsers.atom.Link::Link;
		public var _visibility:Boolean = true;
		public var _open:Boolean = false;
		public var _author:Author;
		public var _snippet:String;
		public var _description:String;
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Feature(x:XMLList)
		{
			super(x);

			this._id = ParsingTools.nullCheck(this.x.@id);
			this._name = ParsingTools.nullCheck(this.x.kml::name);
			
			this._description = ParsingTools.nullCheck(this.x.kml::description);
			this._snippet = ParsingTools.nullCheck(this.x.kml::Snippet);
			
			if (ParsingTools.nullCheck(this.x.atom::link) != null) {
				this._link = new com.google.maps.extras.xmlparsers.atom.Link::Link(this.x.atom::link);
			}

			if (ParsingTools.nullCheck(this.x.atom::author) != null) {
				this._author = new Author(this.x.atom::author);
			}
			
			var visibility:Number = ParsingTools.nanCheck(this.x.kml::visibility);
			if (visibility == 1) {
				this._visibility = true;
			} else if (visibility == 0) {
				this._visibility = false;
			}
			
			var open:Number = ParsingTools.nanCheck(this.x.kml::open);
			if (open == 1) {
				this._open = true;
			} else if (open == 0) {
				this._open = false;
			}
		}

		/**
		*	Represents the &lt;name&gt; child element.
		*/	
		public function get name():String
		{
			return this._name;
		}

		/**
		*	Represents the &lt;atom:link&gt; child element.
		*/	
		public function get link(): com.google.maps.extras.xmlparsers.atom.Link
		{	
			return this._link;
		}

		/**
		*	Represents the &lt;visibility&gt; child element.
		*/	
		public function get visibility():Boolean
		{
			return this._visibility;
		}

		/**
		*	Represents the &lt;open&gt; child element.
		*/	
		public function get open():Boolean
		{
			return this._open;
		}

		
		/**
		*	Represents the &lt;atom:author&gt; child element.
		*/	
		public function get author():Author
		{
			return this._author;
		}


		/**
		*	Represents the &lt;description&gt; child element.
		*/	
		public function get description():String
		{
			return this._description;
		}

		/**
		*	Represents the &lt;snippet&gt; child element.
		*/	
		public function get snippet():String
		{
			return this._snippet;
		}
		
		public override function toString():String {
			return super.toString() + "name: " + _name +
				   "id: " + _id +
				   "link: " + _link +
				   "visibility: " + _visibility +
				   "open: " + _open +
				   "author: " + _author +
				   "snippet: " + snippet +
				   "description: " + description +
				   "\n";
		}
	}
}
