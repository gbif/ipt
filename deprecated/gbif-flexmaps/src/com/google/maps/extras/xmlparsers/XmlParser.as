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

package com.google.maps.extras.xmlparsers
{
	/**
	 * The parent class of all the XML parsers. Provides functions for
	 * setting and parsing XML.
	 */	
	public class XmlParser
	{
		protected var x:XML;

		/**
		 * Parses the specified feed into an XML object, and populates the
		 * subclassing parser.
		 * 
		 * @param xmlStr A string of XML that is an RSS or an Atom feed.
		 */
		public function parse(xmlStr:String):void
		{
			this.populate(new XML(xmlStr));
		}

		/**
		 * Populates the subclassing parser.
		 * 
		 * @param newXml An XML object that represents an RSS or an Atom feed.
		 */
		public function populate(newXml:XML):void
		{
			this.x = newXml;
		}
	}
}