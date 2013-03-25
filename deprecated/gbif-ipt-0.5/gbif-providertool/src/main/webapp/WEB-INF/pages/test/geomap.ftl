<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
        <head>
        <title>Geoserver test</title>
        <style type="text/css">
			body {
				color: #999999;
				background-color: #ffffff;
			}
            #map {
                width: 800px;
                height: 375px;
                border: 1px solid #999999;
            }
            #wrapper {
                width: 800px;
            }
            #location {
                float: right;
            }
        </style>
        <script src="${cfg.geoserverUrl}/openlayers/OpenLayers.js" type="text/javascript">
        </script>
        <script defer="defer" type="text/javascript">
        var map;
        var untiled;
        var tiled;
		var countries;
		var taxon;
        function setHTML(response) { 
            document.getElementById('nodelist').innerHTML = response.responseText;
        };
        
        OpenLayers.IMAGE_RELOAD_ATTEMPTS = 5;
        OpenLayers.DOTS_PER_INCH = 25.4 / 0.28;
        
        function init(){
            var bounds = new OpenLayers.Bounds(
                -198.0, -99.0,
                198.0, 99.0
            );
            var options = {
                controls: [],
                maxExtent: bounds,
                maxResolution: 1.546875,
                projection: "EPSG:4326",
                units: 'degrees'
            };
            map = new OpenLayers.Map('map', options);
            
            // setup tiled layer for background countries
            countries = new OpenLayers.Layer.WMS(
                "Countries", "${cfg.geoserverUrl}/wms",
                {
                    layers: 'gbif:countries',
                    styles: '',
                    height: '375',
                    width: '800',
                    srs: 'EPSG:4326',
                    format: 'image/png',
                    tiled: 'true',
                    tilesOrigin : "-198.0,-99.0",
					bgcolor: '0x7391AD'
                },
                {buffer: 10} 
            );
            
            // setup tiled layer
            tiled = new OpenLayers.Layer.WMS(
                "All occurrences (Tiled)", "${cfg.geoserverUrl}/wms",
                {
                    layers: 'gbif:occurrence',
                    styles: '',
                    height: '375',
                    width: '800',
                    srs: 'EPSG:4326',
                    format: 'image/png',
                    tiled: 'true',
                    tilesOrigin : "-198.0,-99.0"
                },
                {buffer: 10} 
            );
            
            // setup single untiled layer
            untiled = new OpenLayers.Layer.WMS(
                "All occurrences (Untiled)", "${cfg.geoserverUrl}/wms",
                {
                    layers: 'gbif:occurrence',
                    styles: '',
                    height: '375',
                    width: '800',
                    srs: 'EPSG:4326',
                    format: 'image/png',
					transparent: true
                },
                {singleTile: true, ratio: 1} 
            );
        
            map.addLayers([countries, untiled, tiled]);
            
            // setup controls and initial zooms
            map.addControl(new OpenLayers.Control.PanZoomBar());
            map.addControl(new OpenLayers.Control.Navigation());
            map.addControl(new OpenLayers.Control.MousePosition({element: $('location')}));
            map.addControl(new OpenLayers.Control.LayerSwitcher());
            map.zoomToExtent(bounds);
        }
        </script>

    </head>
    <body onload="init()">
		<div id="map"></div>
		<div id="wrapper">
			<div id="location"></div>
		</div>
    </body>
</html>
