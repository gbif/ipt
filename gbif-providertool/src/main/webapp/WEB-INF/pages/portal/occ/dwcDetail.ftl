<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${dwc.resource.title}"/>
    <meta name="submenu" content="resource"/>
</head>


<body onunload="GUnload()">

<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />

<h2>${dwc.collectionCode} - ${dwc.catalogNumber}</h2>	
<h3>${dwc.scientificName}</h3>

<@s.form>

<table>	
 <tr>
	<th>GUID</th>
	<td><a href="${cfg.getDetailUrl(dwc)}">${dwc.guid}</a></td>
 </tr>
 <tr>
	<th>SourceID</th>
	<td><a href="${dwc.link}">${dwc.localId}</a></td>
 </tr>
</table>


<#assign core=dwc.resource.coreMapping>
<fieldset>
	<h2>${core.extension.name}</h2>
<#if (dwc.location)?? && cfg.googleMapsApiKey??>
	<#-- STATIC IMAGE: <img class="right" src="http://maps.google.com/staticmap?center=${dwc.location.latitude!0},${dwc.location.longitude!0}&zoom=10&size=300x300&maptype=terrain&markers=${dwc.location.latitude!0},${dwc.location.longitude!0},reds&key=${cfg.googleMapsApiKey}" /-->	
    <div id="map" style="width: 250px; height: 250px" class="right"></div>
	
	<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=${cfg.googleMapsApiKey}" type="text/javascript"></script>
    <script type="text/javascript">
      if (GBrowserIsCompatible()) {
        var point = new GLatLng(${dwc.location.latitude!0},${dwc.location.longitude!0});
        var map = new GMap2(document.getElementById("map"));
        map.setMapType(G_PHYSICAL_MAP);
        map.addMapType(G_PHYSICAL_MAP);
		map.addControl(new GMapTypeControl(true));
        map.enableDoubleClickZoom();
        map.enableContinuousZoom();
        map.enableScrollWheelZoom();
        map.setCenter(point, 8);
        map.addOverlay(new GMarker(point));
        
      }
    </script>
    	
</#if>	

	<table>	
	<#list core.extension.properties as p>
	 <#if core.hasMappedProperty(p)>
	  <tr>
		<th>${p.name}</th>
		<td>${dwc.getPropertyValue(p)!"---"}</td>
	  </tr>
	 </#if>
	</#list>
	</table>
</fieldset>

<#list dwc.resource.extensionMappings as view>
<fieldset>
	<h2>${view.extension.name}</h2>	
	<table>	
	<#list view.propertyMappings.values() as pm>
	 <tr>
		<th>${pm.property.name}</th>
		<td>sorry, not implemented</td>
	 </tr>
	</#list>
	</table>
</fieldset>
</#list>
	
</@s.form>

</body>
