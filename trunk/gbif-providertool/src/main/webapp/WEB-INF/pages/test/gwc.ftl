<!DOCTYPE html "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
        <title>Google Maps with GeoWebCache</title>
        <script src='http://maps.google.com/maps?file=api&amp;v=2&amp;key=${cfg.getGoogleMapsApiKey()}'></script>
        <script type="text/javascript">

            function initialize() {  
                if (GBrowserIsCompatible()) {
                    var map = new GMap2(document.getElementById("map_canvas"));
                    map.setCenter(new GLatLng(39, -96), 4);
		
		    var tilelayer =  new GTileLayer(null, null, null, {
		        tileUrlTemplate: '${cfg.getGeoserverWebCacheUrl(resource_id)}&zoom={Z}&x={X}&y={Y}', 
                        isPng:true,
                        opacity:0.5 }
			);
                   
                    var myTileLayer = new GTileLayerOverlay(tilelayer);
		    map.addOverlay(myTileLayer);
      }
    }

        </script>
    </head>
    <body onload="initialize()" onunload="GUnload()">
        <div id="map_canvas" style="width: 500px; height: 300px"></div>
    </body>
</html>