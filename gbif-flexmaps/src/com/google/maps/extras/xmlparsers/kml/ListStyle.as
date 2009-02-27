/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/


package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class ListStyle extends KmlObject
	{
		private var _bgColor:int;
		private var _listItemType:String;
		private var _itemIcon:ItemIcon;
		
		public function ListStyle(x:XMLList)
		{
			super(x);
			
			var bgColor:String =  ParsingTools.nullCheck(this.x.kml::bgColor);
			this._bgColor = parseInt("0x" + bgColor);
			this._listItemType = ParsingTools.nullCheck(this.x.kml::listItemType);
			if (ParsingTools.nullCheck(this.x.kml::ItemIcon) != null) {
				this._itemIcon = new ItemIcon(this.x.kml::ItemIcon);
			}
		}
		
		public function get bgColor():int
		{
			return this._bgColor;
		}
		
		public function get listItemType():String {
			return this._listItemType;
		}
		
		public function get itemIcon():ItemIcon {
			return this._itemIcon;
		}
		
	}
}