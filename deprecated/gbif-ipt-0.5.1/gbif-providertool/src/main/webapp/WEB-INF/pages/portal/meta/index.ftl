<head>
    <title>Metadata Repository</title>
    <meta name="submenu" content="meta"/>
<style>
h2        {clear: both;}
.col         {width: 100%;  margin: 1em 0; padding: 0; counter-reset: ol;}
.col      li {float: left;  margin: 0;     padding: 0; list-style-type: none; width: 18em;  margin-right: 2.5%;}
.col.two  li {width: 47.5%; margin-right: 2.5%;}
.col.four li {width: 22.5%; margin-right: 2.5%;}
</style>

<style>
#map {
    width: 512px;
    height: 256px;
    border: 1px solid #ccc;
}
</style>

<script src="http://openlayers.org/dev/OpenLayers.js"></script>

</head>


<h2>Latest EML documents</h2>	

<#if (resources?size==0)>
	<p>The repository is still empty.</p>
<#else>
	<table class="layout">	
	 <tr>
		<th>Title</th>
		<th>Last Modified</th>
		<th>Originator</th>
	 </tr>
	 <#list resources as m>
	 <tr>
		<td><a href="metaResource.html?resource_id=${m.id?c}">${m.title!""}</a></td>
		<td>${m.modified?date}</td>
		<td>${m.creator.lastName!(m.creator.firstName!(m.creator.username!))}</td>
	 </tr>
	 </#list>
	</table>
</#if>


<#assign alphabet = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z']>
<div id="titleindex">
  <h2>Title Index</h2>
  <ul class="indexmenu">
    <#list alphabet as c>
  	<li>${c}</li>
  	</#list>
  </ul>
  <div id="tags">
	<ol class="col two">
		<li>Cambodia</li>
		<li>Cameroon</li>
		<li>Canada</li>
		<li>Cape Verde</li>
		<li>Cayman Islands</li>
		<li>Central African Republic</li>
		<li>Chad</li>
		<li>Chile</li>
		<li>China</li>
		<li>Christmas Island</li>
		<li>Cocos (Keeling) Islands</li>
		<li>Colombia</li>
	</ol>
  </div> 
</div>

&nbsp;
<br/>

<div id="geosearch">
  <h2>Geospatial Search</h2>	
  <div id="map"></div>
  <@s.form action="search">
	<input type="hidden" id="bbox_top" name="bbox_top" value="" />
	<input type="hidden" id="bbox_bottom" name="bbox_bottom" value="" />
	<input type="hidden" id="bbox_left" name="bbox_left" value="" />
	<input type="hidden" id="bbox_right" name="bbox_right" value="" />
    <@s.submit cssClass="button" key="button.search" theme="simple"/>
  </@s.form>


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
</div>


<div id="tagindex">
  <h2>Keyword Index</h2>	
  <ul class="indexmenu">
    <#list alphabet as c>
  	<li>${c}</li>
  	</#list>
  </ul>
  <div id="tags">
	<ol class="col four">
		<li>Cambodia</li>
		<li>Cameroon</li>
		<li>Canada</li>
		<li>Cape Verde</li>
		<li>Cayman Islands</li>
		<li>Central African Republic</li>
		<li>Chad</li>
		<li>Chile</li>
		<li>China</li>
		<li>Christmas Island</li>
		<li>Cocos (Keeling) Islands</li>
		<li>Colombia</li>
		<li>Comoros</li>
		<li>Congo</li>
		<li>Congo</li>
		<li>Cook Islands</li>
		<li>Costa Rica</li>
		<li>Côte d'Ivoire</li>
		<li>Croatia</li>
		<li>Cuba</li>
		<li>Cyprus</li>
		<li>Czech Republic</li>
	</ol>
  </div> 
</div>

&nbsp;
<br/>
<br/>
