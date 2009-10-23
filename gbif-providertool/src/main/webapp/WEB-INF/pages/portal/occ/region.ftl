<head>
    <title><@s.text name="region.title"/></title>
    <meta name="resource" content="${region.resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="occ"/>
    
	<script type="text/javascript" src="<@s.url value="/scripts/swfobject.js"/>" ></script>
	<script>
	$(document).ready(function(){
		var so = new SWFObject("<@s.url value="/scripts/IptOccurrenceMap.swf"/>", "swf", "690", "${height}", "9"); 
		so.addParam("allowFullScreen", "true");
		so.addVariable("swf", "");
		so.addVariable("wms_url", escape("${geoserverMapUrl}"));
		so.addVariable("bbox", "${geoserverMapBBox}");
		so.addVariable("type", "wms");
		so.addVariable("api_key", "${cfg.getGoogleMapsApiKey()}");
		so.write("occmap");	    
	});
	</script>
    <style>
	    th{padding-right:15px;}
    </style>
</head>
	

<h1>${region.label}</h1>  
<div class="horizontal_dotted_line_large_foo"></div>

<@s.form>
	<fieldset>
	<table id="details">
		<tr>
		  <th><@s.text name="region.label"/></th>
		  <td>${region.label}</td>
		</tr>
		<tr>
		  <th><@s.text name="region.type"/></th>
		  <td>${region.type}</td>
		</tr>
		<#if region.parent??>
		<tr>
		  <th><@s.text name="region.parent"/></th>
		  <@s.url id="occRegionUrl" action="occRegion" namespace="/" includeParams="none">
			<@s.param name="resourceId" value="${resourceId}"/>
			<@s.param name="id" value="${region.parent.id}"/>
		  </@s.url>
		  <td><a href="${occRegionUrl}">${region.parent}</a></td>
		</tr>
		</#if>
		<tr>
		  <th><@s.text name="region.occTotal"/></th>
		  <td>${region.occTotal!0}</td>
		</tr>
	</table>
	</fieldset>
</@s.form>

<div id="loc-geoserver-big">
	<#-- <label><@s.text name="stats.occPointMap"/></label> -->
	<div id="occmap"></div>	
</div>


<#include "/WEB-INF/pages/inc/occurrenceList.ftl">  
