<head>
    <title><@s.text name="occResource.overview"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="occ"/>
    
	<script type="text/javascript" src="<@s.url value="/scripts/swfobject.js"/>" ></script>
	<script>
	function updateByRegion(){
		var url = '<@s.url value="/ajax/occResourceStatsByRegion.html"/>';
		var params = { resourceId: ${resourceId}, type: $("#regionClass").val() }; 
		var target = '#imgByRegion';	
		ajaxHtmlUpdate(url, target, params);
	};
	function updateByCountry(){
		var url = '<@s.url value="/ajax/occResourceStatsByCountry.html"/>';
		var params = { resourceId: ${resourceId}, type: $("#countryClass").val() }; 
		var target = '#imgByCountry';	
		ajaxHtmlUpdate(url, target, params);
	};
	function updateByTaxon(){
		var url = '<@s.url value="/ajax/occResourceStatsByTaxon.html"/>';
		var params = { resourceId: ${resourceId}, type: $("#rank").val() }; 
		var target = '#imgByTaxon';	
		ajaxHtmlUpdate(url, target, params);
	};
	function updateByHost(){
		var url = '<@s.url value="/ajax/occResourceStatsByHost.html"/>';
		var params = { resourceId: ${resourceId}, type: $("#hostType").val() }; 
		var target = '#imgByHost';	
		ajaxHtmlUpdate(url, target, params);
	};
	
	$(document).ready(function(){
		updateByRegion();
		updateByCountry();
		updateByTaxon();
		updateByHost();
		listenToChange("#regionClass", updateByRegion);
		listenToChange("#countryClass", updateByCountry);
		listenToChange("#rank", updateByTaxon);
		listenToChange("#hostType", updateByHost);
		
	    $("#showWebservices").click(function () {
	      $("#services").slideToggle("normal");
	    });
	    
		var so = new SWFObject("<@s.url value="/scripts/IptOccurrenceMap.swf"/>", "swf", "690", "${height}", "9"); 
		so.addParam("allowFullScreen", "true");
		so.addVariable("swf", "");
		so.addVariable("bbox", "${geoserverMapBBox}");
		so.addVariable("api_key", "${cfg.getGoogleMapsApiKey()}");
		so.addVariable("wms_url", escape("${geoserverMapUrl}"));
		so.addVariable("type", "wms");
		<#-- geowebcache doesnt work reliably so far
		so.addVariable("geowebcache_url", "${cfg.getGeoserverWebCacheUrl(resourceId)}");
		so.addVariable("type", "gwc");
		-->
		so.write("occmap");	    
	});
	</script>
</head>
	
<@s.form>
<h1 style="margin-bottom: 13px;"><@s.text name="resource.description"/></h1>
<div class="horizontal_dotted_line_large"></div>
<fieldset>
	<div id="metadata">
		<img class="right" src="${cfg.getResourceLogoUrl(resourceId)}" />
		<span>${resource.description}</span>
		<p style="margin-left: 0px;">
		<label><@s.text name="dataResource.cache"/></label>
		<label>${(resource.lastUpload.recordsUploaded)!0} <@s.text name='occ.index.recordsloaded'/> ${(resource.lastUpload.executionDate)!}</label>
		</p>
		<ul class="minimenu">
			<li class="last">
				<a id="showWebservices"><@s.text name="dataResource.services"/></a>
			</li>
			<li>
				<a href="metaResource.html?resourceId=${resourceId}"><@s.text name='occ.index.fullmetadata'/></a>
			</li>
			<li>
				<a href="annotations.html?resourceId=${resourceId}"><@s.text name='occ.index.annotations'/></a>
			</li>
		</ul>
		<div class="break35"></div>
		<div id="services" style="display:none">
			<table class="lefthead">
				<tr>
					<th><@s.text name='occ.index.contact'/></th>
					<td>${resource.contactName}"/> <#if resource.contactEmail??>&lt;${resource.contactEmail}&gt;</#if></td>
				</tr>
				<tr>
					<th><@s.text name='occ.index.homepage'/></th>
					<td><a href="${resource.link!}">${resource.link!}</a></td>
				</tr>
				<tr>
					<th>EML</th>
					<td><a href="${cfg.getEmlUrl(resource.guid)}">${cfg.getEmlUrl(resource.guid)}</a></td>
				</tr>
				<tr>
					<th><@s.text name="dataResource.archive"/></th>
					<td><a href="${cfg.getArchiveUrl(resource.guid)}">${cfg.getArchiveUrl(resource.guid)}</a></td>
				</tr>
				<tr>
					<th><@s.text name="dataResource.tapir"/></th>
					<td><a href="${cfg.getTapirEndpoint(resourceId)}">${cfg.getTapirEndpoint(resourceId)}</a></td>
				</tr>
				<tr>
					<th><@s.text name="dataResource.wfs"/></th>
					<td><a href="${cfg.getWfsEndpoint(resourceId)}">${cfg.getWfsEndpoint(resourceId)}</a></td>
				</tr>
				<tr>
					<th><@s.text name="dataResource.wms"/></th>
					<td><a href="${cfg.getWmsEndpoint(resourceId)}">${cfg.getWmsEndpoint(resourceId)}</a></td>
				</tr>
			</table>
		</div>
	</div>
</@s.form>


<div id="loc-stats1" class="stats stat-table">
	<label><@s.text name="stats.geoStats"/></label>
	<table class="lefthead">
		<tr>
			<td><@s.text name="occResource.recWithCoordinates"/></td>
			<td>${resource.recWithCoordinates}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.recWithCountry"/></td>
			<td>${resource.recWithCountry}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.recWithAltitude"/></td>
			<td>${resource.recWithAltitude}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numCountries"/></td>
			<td>${resource.numCountries}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numRegions"/></td>
			<td>${resource.numRegions}</td>
		</tr>
	</table>
</div>

<div id="tax-stats2" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td><@s.text name="occResource.numTaxa"/></td>
			<td>${resource.numTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numTerminalTaxa"/></td>
			<td>${resource.numTerminalTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numGenera"/></td>
			<td>${resource.numGenera}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numFamilies"/></td>
			<td>${resource.numFamilies}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numOrders"/></td>
			<td>${resource.numOrders}</td>
		</tr>
	</table>
</div>

<div id="tax-stats3" class="stats stat-table">
	<label><@s.text name="stats.extensions"/></label>
	<table class="lefthead">
 	  	<#list resource.getExtensionMappings() as e>
		<tr>
			<td>${e.extension.name}</td>
			<td>${e.recTotal}</td>
		</tr>
		</#list>
	</table>
</div>

<div id="loc-geoserver-big">
	<h2><@s.text name="stats.occPointMap"/></h2>	
	<div id="occmap"></div>	
</div>

<div id="loc-pie" class="stats chart">
	<label><@s.text name="stats.occByRegion"/></label>
	<@s.form id="regionClassForm">
		<@s.select id="regionClass" name="region" list="regionClasses" value="4" theme="simple"/>
	</@s.form>
	<div id="imgByRegion"></div>
</div>

<div id="tax-pie" class="stats chart stat-right">
	<label><@s.text name="stats.byTaxon"/></label>
	<@s.form id="rankForm">
		<@s.select id="rank" list="ranks" value="rank" theme="simple"/>
	</@s.form>
	<div id="imgByTaxon"></div>
</div>

<br class="clearfix" />


<div id="recordbasis-pie" class="stats chart">
	<label><@s.text name="stats.occByBasisOfRecord"/></label>
	<div id="imgByBasisOfRecord">
		<@s.action name="occResourceStatsByBasisOfRecord" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<div id="host-pie" class="stats chart stat-right">
	<label><@s.text name="stats.occByHost"/></label>
	<@s.form id="hostForm">
		<@s.select id="hostType" list="hostTypes" theme="simple"/>
	</@s.form>
	<div id="imgByHost"></div>
</div>

<br class="clearfix" />

<div id="time-pie" class="stats chart ">
	<label><@s.text name="stats.occByDateColected"/></label>
	<div id="imgByDateColected">
		<@s.action name="occResourceStatsByDateColected" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<div id="loc-countries" class="stats map stat-right">
	<label><@s.text name="stats.occByCountry"/></label>	
	<@s.form id="countryClassForm">
		<@s.select id="countryClass" name="country" list="countryClasses" value="1" theme="simple"/>
	</@s.form>
	<div id="imgByCountry"></div>
</div>

<br class="clearfix" />