/*
        Copyright (c) 2008, Adobe Systems Incorporated
        All rights reserved.

        Redistribution and use in source and binary forms, with or without 
        modification, are permitted provided that the following conditions are
        met:

    * Redistributions of source code must retain the above copyright notice, 
        this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
                notice, this list of conditions and the following disclaimer in the 
        documentation and/or other materials provided with the distribution.
    * Neither the name of Adobe Systems Incorporated nor the names of its 
        contributors may be used to endorse or promote products derived from 
        this software without specific prior written permission.

        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
        IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
        THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
        PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
        CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
        EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
        PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
        PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
        LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
        NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
        SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.google.maps.extras.xmlparsers.atom
{
	import com.google.maps.extras.xmlparsers.XmlElement;
	import com.google.maps.extras.xmlparsers.XmlParser;
	import com.google.maps.extras.xmlparsers.ParsingTools;
	import com.google.maps.extras.xmlparsers.Namespaces;
	
	/**
	*	Class that represents a Link element within an Atom feed.
	* 
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	* 
	* 	@see http://www.atomenabled.org/developers/syndication/atom-format-spec.php#rfc.section.4.2.7
	*/
	public class Link extends XmlElement
	{
		private var atom:Namespace = Namespaces.ATOM_NS;
		
		/**
		*	Constant for the "alternate" value of the rel property.
		* 
		* 	@langversion ActionScript 3.0
		*	@playerversion Flash 8.5
		*	@tiptext
		*/			
		public static const REL_ALTERNATE:String = "alternate";
		
		/**
		*	Constant for the "related" value of the rel property.
		* 
		* 	@langversion ActionScript 3.0
		*	@playerversion Flash 8.5
		*	@tiptext
		*/			
		public static const REL_RELATED:String = "related";
		
		/**
		*	Constant for the "self" value of the rel property.
		* 
		* 	@langversion ActionScript 3.0
		*	@playerversion Flash 8.5
		*	@tiptext
		*/			
		public static const REL_SELF:String = "self";
		
		/**
		*	Constant for the "enclosure" value of the rel property.
		* 
		* 	@langversion ActionScript 3.0
		*	@playerversion Flash 8.5
		*	@tiptext
		*/			
		public static const REL_ENCLOSURE:String = "enclosure";
		
		/**
		*	Constant for the "via" value of the rel property.
		* 
		* 	@langversion ActionScript 3.0
		*	@playerversion Flash 8.5
		*	@tiptext
		*/			
		public static const REL_VIA:String = "via";
		
		private var _rel:String;
		private var _type:String;
		private var _hreflang:String;
		private var _href:String;
		private var _title:String;
		private var _length:Number;
		
		public function Link(x:XMLList)
		{
			super(x);
			
			this._rel = ParsingTools.nullCheck(this.x.atom::link.@rel);
			this._type = ParsingTools.nullCheck(this.x.atom::link.@type);
			this._hreflang = ParsingTools.nullCheck(this.x.atom::link.@hreflang);
			this._href = ParsingTools.nullCheck(this.x.atom::link.@href);
			this._title = ParsingTools.nullCheck(this.x.atom::link.@title);
			this._length = ParsingTools.nanCheck(this.x.atom::link.@["length"]);
		}
		
	
		public function get rel():String
		{
			return this._rel;
		}
		
		public function get type():String
		{
			return this._type;
		}
		
		public function get hreflang():String
		{
			return this._hreflang;
		}
		
		public function get title():String
		{
			return this._title;
		}
		
		public function get length():Number
		{
			return this._length;
		}
	}
}