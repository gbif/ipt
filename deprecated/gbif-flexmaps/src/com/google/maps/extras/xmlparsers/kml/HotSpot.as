/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/


package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class HotSpot extends KmlObject
	{
		private var _x:Number;
		private var _y:Number;
		private var _xunits:String;
		private var _yunits:String;
		
		public function HotSpot(x:XMLList)
		{
			super(x);
			this._x = ParsingTools.nanCheck(this.x.@x);
			this._y = ParsingTools.nanCheck(this.x.@y);
			this._xunits = ParsingTools.nullCheck(this.x.@xunits);
			this._yunits = ParsingTools.nullCheck(this.x.@yunits);
		}
		
		public function get xx():Number{
			return this._x;
		}
		
		public function get yy():Number{
			return this._y;
		}
		
		public function get xunits():String{
			return this._xunits;
		}
		
		public function get yunits():String{
			return this._yunits;
		}
	}
}