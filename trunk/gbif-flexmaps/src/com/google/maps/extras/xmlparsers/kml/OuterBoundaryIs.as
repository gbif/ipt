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
	*	Class that represents a &lt;outerBoundaryIs&gt; element.
	* 
	* 	@see http://code.google.com/apis/kml/documentation/kmlreference.html#outerboundaryis
	*/
	public class OuterBoundaryIs extends BoundaryCommon
	{
		
		/**
		*	Constructor for class.
		* 
		*	@param x
		*/	
		public function OuterBoundaryIs(x:XMLList)
		{
			super(x);
		}
		
		public override function toString():String {
			return "OuterBoundaryIs: " + super.toString();
		}
	}
}
