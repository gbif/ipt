/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class Pair extends KmlObject
	{
		private var _key:String;
		private var _styleUrl:String;
		private var _id:String;
		
		public function Pair(x:XMLList)
		{
			super(x);
			this._key = ParsingTools.nullCheck(this.x.kml::key);
			this._styleUrl = ParsingTools.nullCheck(this.x.kml::styleUrl);
		}
		
		public function get key():String
		{
			return this._key;
		}
		
		public function get styleUrl():String
		{
			return this._styleUrl;
		}
	}
}