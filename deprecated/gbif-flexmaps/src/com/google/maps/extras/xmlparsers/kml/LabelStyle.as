/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class LabelStyle extends ColorStyle
	{
		private var _scale:Number;
		
		public function LabelStyle(x:XMLList)
		{
			super(x);
			this._scale = ParsingTools.nanCheck(this.x.kml::scale);
		}
		
		
		public function get scale():Number{
			return this._scale;
		}
	}
}