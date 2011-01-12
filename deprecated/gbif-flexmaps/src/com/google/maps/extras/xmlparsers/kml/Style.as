/* Added by Cecil M. Reid (cmR)
 * cecilmreid@gmail.com	
*/

package com.google.maps.extras.xmlparsers.kml
{
	import com.google.maps.extras.xmlparsers.ParsingTools;
	
	public class Style extends StyleSelector
	{
		private var _iconStyle:IconStyle;
		private var _labelStyle:LabelStyle;
		private var _lineStyle:LineStyle;
		private var _polyStyle:PolyStyle;
		
		public function Style(x:XMLList)
		{
			super(x);
			if (ParsingTools.nullCheck(this.x.kml::IconStyle) != null) {
				this._iconStyle = new com.google.maps.extras.xmlparsers.kml.IconStyle(this.x.kml::IconStyle);
			}
			if (ParsingTools.nullCheck(this.x.kml::LabelStyle) != null) {
				this._labelStyle = new com.google.maps.extras.xmlparsers.kml.LabelStyle(this.x.kml::LabelStyle);
			}
			if (ParsingTools.nullCheck(this.x.kml::LineStyle) != null) {
				this._lineStyle = new com.google.maps.extras.xmlparsers.kml.LineStyle(this.x.kml::LineStyle);
			}
			if (ParsingTools.nullCheck(this.x.kml::PolyStyle) != null) {
				this._polyStyle = new com.google.maps.extras.xmlparsers.kml.PolyStyle(this.x.kml::PolyStyle);
			}
		}
		
		public function get iconStyle():IconStyle{
			return this._iconStyle;
		}
		
		public function get labelStyle():LabelStyle{
			return this._labelStyle;
		}
		
		public function get lineStyle():LineStyle{
			return this._lineStyle;
		}
		
		public function get polyStyle():PolyStyle{
			return this._polyStyle;
		}
	}
}