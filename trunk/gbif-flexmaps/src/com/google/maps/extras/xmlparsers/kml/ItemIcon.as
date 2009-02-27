/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class ItemIcon extends Icon
	{
		private var _state:String;
		
		public function ItemIcon(x:XMLList)
		{
			super(x);
			this._state = ParsingTools.nullCheck(this.x.kml::state);
		}
		
		public function get state():String{
			return this._state;
		}
	}
}