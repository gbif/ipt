/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class PolyStyle extends ColorStyle
	{
		private var _fill:Boolean;
		private var _outline:Boolean;
		
		public function PolyStyle(x:XMLList)
		{
			super(x);
			
			var fill:Number = ParsingTools.nanCheck(this.x.kml::fill);
			if (fill == 1) {
				this._fill = true;
			} else if (fill == 0) {
				this._fill = false;
			}
			
			var outline:Number = ParsingTools.nanCheck(this.x.kml::outline);
			if (outline == 1) {
				this._outline = true;
			} else if (outline == 0) {
				this._outline = false;
			}
			
		}
		
		public function get fill():Boolean{
			return this._fill;
		}
		
		public function get outline():Boolean{
			return this._outline;
		}
	}
}