/* Added by Cecil M. Reid
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class ColorStyle extends KmlObject
	{
		private var _color:int;
		private var _colorMode:String;
		private var _id:String;
		
		public function ColorStyle(x:XMLList)
		{
			super(x);
			
			var color:String = ParsingTools.nullCheck(this.x.kml::color);
			this._color = parseInt("0x" + color);
			this._colorMode = ParsingTools.nullCheck(this.x.kml::colorMode);
		}
		
		public function get color():int{
			return this._color;
		}	
		
		public function get colorMode():String{
			return this._colorMode;
		}
	}
}