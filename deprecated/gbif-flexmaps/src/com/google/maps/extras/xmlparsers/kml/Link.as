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
	*	Class that represents a &lt;Link&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#link
	*/
	public class Link extends AbstractLink
	{
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Link(x:XMLList)
		{
			super(x);
		}
		
		public override function toString():String {
	 		return "Link: " + super.toString();
	 	}
	}
}
