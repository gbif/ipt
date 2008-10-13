<%@ include file="/common/taglibs.jsp"%>
<style>

<c:choose>
<c:when test="${largeMap}">
.smallmap {
    width: 900px;
   	height: 450px;
    border: 1px solid #ccc;
}
<c:set var="zoomLevel" value="2"/>
</c:when>
<c:otherwise>
.smallmap {
    width: 512px;
   	height: 256px;
    border: 1px solid #ccc;
}
<c:set var="zoomLevel" value="1"/>
</c:otherwise>
</c:choose>
</style>

<div id="map" class="smallmap"></div>
<c:set var="northCoordinateInput" value="${not empty northCoordinateInput ? northCoordinateInput : 'northCoordinateInput'}" scope="request"/>
<c:set var="southCoordinateInput" value="${not empty southCoordinateInput ? southCoordinateInput : 'southCoordinateInput'}" scope="request"/>
<c:set var="eastCoordinateInput" value="${not empty eastCoordinateInput ? eastCoordinateInput : 'eastCoordinateInput'}" scope="request"/>
<c:set var="westCoordinateInput" value="${not empty westCoordinateInput ? westCoordinateInput : 'westCoordinateInput'}" scope="request"/>

<c:set var="polygon" value="${not empty polygon ? polygon : ''}" scope="request"/>

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
    

	<c:if test="${not empty northValue && not empty southValue && not empty eastValue && not empty westValue}">
    var bounds = new OpenLayers.Bounds(${westValue}, ${southValue}, ${eastValue}, ${northValue});
    boundingBox = new OpenLayers.Feature.Vector(bounds.toGeometry());
    polygonLayer.addFeatures(boundingBox);        
    </c:if>
    
    
    map.addLayers([wmsLayer, satelliteLayer, polygonLayer]);
    map.addControl( new OpenLayers.Control.LayerSwitcher() );
    map.addControl( new OpenLayers.Control.MousePosition() );
    
    map.zoomToMaxExtent();
    map.setCenter(new OpenLayers.LonLat(0, 0), ${zoomLevel});
    
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
		document.getElementById('${northCoordinateInput}').value = geometry.geometry.getBounds().top;
		document.getElementById('${southCoordinateInput}').value = geometry.geometry.getBounds().bottom;
		document.getElementById('${eastCoordinateInput}').value = geometry.geometry.getBounds().left;
		document.getElementById('${westCoordinateInput}').value = geometry.geometry.getBounds().right;
		<c:if test="${not empty polygon}">
		//document.getElementById('${polygonInput}').value = geometry.geometry.toString();
		</c:if>		
	};

    polygonControl.handler.setOptions({snapAngle: parseFloat(0)});
    polygonControl.handler.setOptions({irregular: true});
    map.addControl(polygonControl);
    polygonControl.activate();

	function clearBounds(){
		document.getElementById('${northCoordinateInput}').value = "";
		document.getElementById('${southCoordinateInput}').value = "";
		document.getElementById('${eastCoordinateInput}').value = "";
		document.getElementById('${westCoordinateInput}').value = "";
		//document.getElementById('${polygonInput}').value = "";
		polygonLayer.destroyFeatures();
	}

</script>
