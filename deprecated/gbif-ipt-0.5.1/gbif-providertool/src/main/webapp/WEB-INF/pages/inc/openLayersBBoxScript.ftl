
<script src="http://openlayers.org/dev/OpenLayers.js"></script>
<script type="text/javascript">
  
  var map, wmsLayer, satelliteLayer, polygonControl, polyOptions, boundingBox;

	map = new OpenLayers.Map('map');
          
    wmsLayer = new OpenLayers.Layer.WMS( "OpenLayers WMS",
            "http://labs.metacarta.com/wms/vmap0",
            {layers: 'basic'} );
            
	satelliteLayer = new OpenLayers.Layer.WMS( "Satellite",
              "http://labs.metacarta.com/wms-c/Basic.py?", {layers: 'satellite', format: 'image/png' } );            
    
    //add the polygon layer
    var polygonLayer = new OpenLayers.Layer.Vector("Polygon Layer");
    
    if ($('bbox_left').value!="" && $('bbox_bottom').value!="" && $('bbox_right').value !=null && $('bbox_top').value!=null){
	    var bounds = new OpenLayers.Bounds($('bbox_left').value, $('bbox_bottom').value, $('bbox_right').value, $('bbox_top').value);
	    boundingBox = new OpenLayers.Feature.Vector(bounds.toGeometry());
	    polygonLayer.addFeatures(boundingBox);        
    }
    
    map.addLayers([wmsLayer, satelliteLayer, polygonLayer]);
    map.addControl( new OpenLayers.Control.LayerSwitcher() );
    map.addControl( new OpenLayers.Control.MousePosition() );
    
    map.zoomToMaxExtent();
    //map.setCenter(new OpenLayers.LonLat(0, 0), 1);
    map.zoomToMaxExtent();
    
    polyOptions = {sides: 4};
    polygonControl = new OpenLayers.Control.DrawFeature(
                                    polygonLayer,
                                    OpenLayers.Handler.RegularPolygon,
                                    {handlerOptions: polyOptions});

	polygonControl.featureAdded = function mapEvent(geometry){
		if(boundingBox!=null){
			polygonLayer.removeFeatures([boundingBox]);
		}
		//removes features
		boundingBox = geometry;
		$('bbox_top').value = geometry.geometry.getBounds().top;
		$('bbox_bottom').value = geometry.geometry.getBounds().bottom;
		$('bbox_left').value = geometry.geometry.getBounds().left;
		$('bbox_right').value = geometry.geometry.getBounds().right;
	};

    polygonControl.handler.setOptions({snapAngle: parseFloat(0)});
    polygonControl.handler.setOptions({irregular: true});
    map.addControl(polygonControl);
    polygonControl.activate();

	function clearBounds(){
		$('bbox_top').value = "";
		$('bbox_bottom').value = "";
		$('bbox_left').value = "";
		$('bbox_right').value = "";
		polygonLayer.destroyFeatures();
	}

</script>
