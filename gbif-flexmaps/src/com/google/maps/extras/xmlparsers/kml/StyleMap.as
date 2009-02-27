/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/


package com.google.maps.extras.xmlparsers.kml
{
	public class StyleMap extends StyleSelector
	{
		private var _pairs:Array;
		
		public function StyleMap(x:XMLList)
		{
			super(x);
			
			this._pairs = new Array();
		 	var i:XML;
			for each (i in this.x.kml::Pair) {
				this._pairs.push(new com.google.maps.extras.xmlparsers.kml.Pair(XMLList(i)));
			}
		}
		
		public function get pairs():Array{
			return this._pairs;
		}
		
	}
}