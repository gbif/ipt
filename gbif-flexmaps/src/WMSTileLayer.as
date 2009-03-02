package
{
	import com.google.maps.Copyright;
	import com.google.maps.CopyrightCollection;
	import com.google.maps.LatLng;
	import com.google.maps.LatLngBounds;
	import com.google.maps.TileLayerBase;	
	import flash.display.DisplayObject;
	import flash.display.Loader;
	import flash.geom.Point;
	import flash.net.URLRequest;
	
	public class WMSTileLayer extends TileLayerBase
	{
		
		private var wmsUrl:String;
		private var MAGIC_NUMBER:Number=6378137.0;
	    private var offset:Number=16777216;
	    private var radius:Number=offset / Math.PI; 
	    		
		public function WMSTileLayer(_wmsUrl:String)
		{
			wmsUrl=_wmsUrl;
			var copyrightCollection:CopyrightCollection = new CopyrightCollection();
			copyrightCollection.addCopyright(new Copyright("gbif", new LatLngBounds(new LatLng(-180, 90), new LatLng(180, -90)), 21,"ennefox"));
			
			super(copyrightCollection, 0, 23,0.7);
			
		}
		
		public override function loadTile(tilePos:Point,zoom:Number):DisplayObject {

 			zoom = 17 - zoom;
			
			//LowerLeft Corner
			var tileIndexLL:Point = new Point(256*tilePos.x, 256*(tilePos.y+1));
			//UpperRight Corner
			var tileIndexUR:Point = new Point(256*(tilePos.x+1), 256*(tilePos.y));
			
			var bbox:String;
			var LL:LatLng = new LatLng(YToL(zoom,tileIndexLL.y),XToL(zoom,tileIndexLL.x));
			var UR:LatLng = new LatLng(YToL(zoom,tileIndexUR.y),XToL(zoom,tileIndexUR.x));
			
			bbox=LL.lng()+","+LL.lat()+","+UR.lng()+","+UR.lat();
			
			var loader:Loader = new Loader();
           	
           	var tileUrl:String = wmsUrl +"&BBOX="+bbox;
           	trace(tileUrl);
            loader.load(new URLRequest(tileUrl));
            return loader;
			
			
		}
		
		private function dd2MercMetersLng(p_lng:Number):Number { 
			return Number((MAGIC_NUMBER * p_lng).toFixed(4)); 
		}
		
		private function dd2MercMetersLat(p_lat:Number):Number {
			if (p_lat >= 85) p_lat=85;
			if (p_lat <= -85) p_lat=-85;
			return Number((MAGIC_NUMBER * Math.log(Math.tan(p_lat / 2 + Math.PI / 4))).toFixed(4));
		}   
		
		private function LToX(z:Number,x:Number):Number {
			return (offset+radius*x*Math.PI/180)>>z;
		}

		private function LToY(z:Number,y:Number):Number {
			return (offset-radius*Math.log((1+Math.sin(y*Math.PI/180))/(1-Math.sin(y*Math.PI/180)))/2)>>z;
		}

		private function XToL(z:Number,x:Number):Number {
        	return (((x<<z)-offset)/radius)*180/Math.PI;
		}

		private function YToL(z:Number,y:Number):Number {
		    return (Math.PI/2-2*Math.atan(Math.exp(((y<<z)-offset)/radius)))*180/Math.PI;
		} 		
				
		
		
	}
}