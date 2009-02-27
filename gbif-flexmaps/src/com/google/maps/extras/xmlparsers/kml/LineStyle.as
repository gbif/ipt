/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class LineStyle extends ColorStyle
	{
		private var _width:Number;
		
		public function LineStyle(x:XMLList)
		{
			super(x);
			this._width = ParsingTools.nanCheck(this.x.kml::width);
		}
		
		public function get width():Number{
			return this._width;
		}
		
	}
}