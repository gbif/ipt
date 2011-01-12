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
	*	Class that represents the &lt;Icon&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#icon
	*/
	public class Icon extends AbstractLink
	{
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function Icon(x:XMLList)
		{
			super(x);
		}
		
		public override function toString():String {
			return "Icon: " + super.toString();
		}
	}
}
