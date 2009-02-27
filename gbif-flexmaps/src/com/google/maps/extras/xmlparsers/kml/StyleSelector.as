/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.Namespaces;
	import com.google.maps.extras.xmlparsers.ParsingTools;
	import com.google.maps.extras.xmlparsers.XmlElement;
	
	public class StyleSelector extends KmlObject
	{
		public function StyleSelector(x:XMLList)
		{
			super(x);
	    }
		
		public override function toString():String{
			return "StyleSelector: " + super.toString();
		}
	}
}