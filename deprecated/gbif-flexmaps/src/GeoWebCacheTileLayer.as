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
	
	public class GeoWebCacheTileLayer extends TileLayerBase
	{
		
		private var geowebcacheUrl:String;
	    		
		public function GeoWebCacheTileLayer(_geowebcacheUrl:String)
		{
			geowebcacheUrl=_geowebcacheUrl;
			var copyrightCollection:CopyrightCollection = new CopyrightCollection();
			copyrightCollection.addCopyright(new Copyright("gbif", new LatLngBounds(new LatLng(-180, 90), new LatLng(180, -90)), 21,"ennefox"));
			
			super(copyrightCollection, 0, 23,0.7);
			
		}
		
		public override function loadTile(tilePos:Point,zoom:Number):DisplayObject {			
			var loader:Loader = new Loader();
           	
           	var tileUrl:String = geowebcacheUrl +"&zoom="+zoom+"&x="+tilePos.x+"&y="+tilePos.y;
            loader.load(new URLRequest(tileUrl));
            return loader;
			
			
		}				
		
		
	}
}