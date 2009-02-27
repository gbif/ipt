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
	*	Class that represents the &lt;Folder&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#folder
	*/
	public class Folder extends Container
	{
		
		/**
		* Constructor for the class.
		*  
		* @param x
		*/	
		public function Folder(x:XMLList)
		{
			super(x);
		}
		
		public override function toString():String {
			return "Folder: " + super.toString();
		}
	}
}
