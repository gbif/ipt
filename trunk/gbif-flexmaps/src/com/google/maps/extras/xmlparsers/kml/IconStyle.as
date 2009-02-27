/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;

	public class IconStyle extends ColorStyle
	{
		private var _scale:Number;
		private var _heading:Number;
		private var _icon:com.google.maps.extras.xmlparsers.kml.Icon;
		private var _hotSpot:com.google.maps.extras.xmlparsers.kml.HotSpot;
		
		public function IconStyle(x:XMLList)
		{
			super(x);
			this._scale = ParsingTools.nanCheck(this.x.kml::scale);
			this._heading = ParsingTools.nanCheck(this.x.kml::heading);
			
			if (ParsingTools.nullCheck(this.x.kml::Icon) != null) {
				this._icon = new com.google.maps.extras.xmlparsers.kml.Icon(this.x.kml::Icon);
			}
			if (ParsingTools.nullCheck(this.x.kml::hotSpot) != null) {
				this._hotSpot = new com.google.maps.extras.xmlparsers.kml.HotSpot(this.x.kml::hotSpot);
			}
		}
		
		public function get scale():Number
		{
			return this._scale;
		}
		
		public function get heading():Number
		{
			return this._heading;
		}
		
		public function get icon():com.google.maps.extras.xmlparsers.kml.Icon{
			return this._icon;
		}
		
		public function get hotSpot():com.google.maps.extras.xmlparsers.kml.HotSpot{
			return this._hotSpot;
		}
	}
}