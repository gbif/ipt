<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${dwc.resource.title}"/>
    <meta name="submenu" content="occ"/>
</head>


<body onunload="GUnload()">

<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />

<h2>${dwc.collectionCode} - ${dwc.catalogNumber}</h2>	
<h3>${dwc.scientificName}</h3>

<@s.form>

<#assign recExtended=rec>
<#assign rec=dwc>
<#include "/WEB-INF/pages/inc/coreDetails.ftl">  
<#assign rec=recExtended>


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

<#list rec.getExtensions() as ext>
<fieldset>
	<h2>${ext.name}</h2>	
	<table>	
	<#list rec.getExtensionRecords(ext) as eRec>
		<#list eRec.properties as p>
		 <tr>
			<th>${p.name}</th>
			<td>${eRec.getPropertyValue(p)}</td>
		 </tr>
		</#list>
		 <tr>
			<th>&nbsp;</th>
			<td>&nbsp;</td>
		 </tr>
	</#list>
	</table>
</fieldset>
</#list>
	
</@s.form>

</body>
