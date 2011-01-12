/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class BalloonStyle extends KmlObject
	{
		private var _id:String;
		private var _bgColor:int;
		private var _textColor:int;
		private var _text:String;
		private var _displayMode:String;
		 
		public function BalloonStyle(x:XMLList)
		{
			super(x);
			
			var bgColor:String = ParsingTools.nullCheck(this.x.kml::bgColor);
			this._bgColor = parseInt("0x" + bgColor);
			var textColor:String = ParsingTools.nullCheck(this.x.kml::textColor);
			this._textColor = parseInt("0x" + textColor);
			this._text = ParsingTools.nullCheck(this.x.kml::text);
			this._displayMode = ParsingTools.nullCheck(this.x.kml::displayMode);
		}
		
		public function get bgColor():int
		{
			return this._bgColor;
		}
		
		public function get textColor():int{
			return this._textColor;
		}
		
		public function get text():String {
			return this._text;
		}
		
		public function get displayMode():String {
			return this._displayMode;
		}
	}
}