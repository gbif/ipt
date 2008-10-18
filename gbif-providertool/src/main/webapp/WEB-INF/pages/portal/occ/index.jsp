<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="occ"/>
	<s:head theme="ajax" debug="false"/>
</head>
	
  
<s:form>
<fieldset>
	<legend><s:text name="resource.description"/></legend>
	<div id="metadata">
		<img class="right" src="<s:property value="%{cfg.getResourceLogoUrl(resource_id)}"/>" />
		<span><s:property value="occResource.description"/></span>
		
		<s:label key="occResourceOverview.cache" value="%{occResource.lastUpload.recordsUploaded} total records uploaded %{occResource.lastUpload.executionDate}"/>
		<ul class="minimenu">
			<li>
				<a onclick="Effect.toggle('services', 'blind', { duration: 0.3 }); return false;">(<s:text name="occResourceOverview.services"/>)</a>
			</li>
		</ul>
		<div id="services" style="display:none">
			<table class="lefthead">
				<tr>
					<th>Contact</th>
					<td><s:property value="%{occResource.contactName}"/> <s:if test="%{occResource.contactEmail}">&lt;<s:property value="%{occResource.contactEmail}"/>&gt;</s:if></td>
				</tr>
				<tr>
					<th>Homepage</th>
					<td><a href="<s:property value="%{occResource.link}"/>"><s:property value="%{occResource.link}"/></a></td>
				</tr>
				<tr>
					<th>EML</th>
					<td><a href="<s:property value="%{cfg.getEmlUrl(occResource.guid)}"/>"><s:property value="%{cfg.getEmlUrl(occResource.guid)}"/></a></td>
				</tr>
				<tr>
					<th><s:text name="occResourceOverview.tabfile"/></th>
					<td><a href="<s:property value="%{cfg.getDumpArchiveUrl(resource_id)}"/>"><s:property value="%{cfg.getDumpArchiveUrl(resource_id)}"/></a></td>
				</tr>
				<tr>
					<th><s:text name="occResourceOverview.tapir"/></th>
					<td><a href="<s:property value="%{cfg.getTapirEndpoint(resource_id)}"/>"><s:property value="%{cfg.getTapirEndpoint(resource_id)}"/></a></td>
				</tr>
				<tr>
					<th><s:text name="occResourceOverview.wfs"/></th>
					<td><a href="<s:property value="%{cfg.getWfsEndpoint(resource_id)}"/>request=GetCapabilities"><s:property value="%{cfg.getWfsEndpoint(resource_id)}"/></a></td>
				</tr>
				<tr>
					<th><s:text name="occResourceOverview.wms"/></th>
					<td><a href="<s:property value="%{cfg.getWmsEndpoint(resource_id)}"/>request=GetCapabilities"><s:property value="%{cfg.getWmsEndpoint(resource_id)}"/></a></td>
				</tr>
			</table>
		</div>
	</div>
</s:form>

<s:push value="occResource">

<div id="loc-stats" class="stats stat-table">
	<label><s:text name="stats.geoStats"/></label>
	<table class="lefthead">
		<tr>
			<td><s:text name="occResource.recWithCoordinates"/></td>
			<td><s:property value="recWithCoordinates"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.recWithCountry"/></td>
			<td><s:property value="recWithCountry"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.recWithAltitude"/></td>
			<td><s:property value="recWithAltitude"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.numCountries"/></td>
			<td><s:property value="numCountries"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.numRegions"/></td>
			<td><s:property value="numRegions"/></td>
		</tr>
	</table>
</div>

<div id="tax-stats" class="stats stat-table">
	<label><s:text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td><s:text name="occResource.numTaxa"/></td>
			<td><s:property value="numTaxa"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.numTerminalTaxa"/></td>
			<td><s:property value="numTerminalTaxa"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.numGenera"/></td>
			<td><s:property value="numGenera"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.numFamilies"/></td>
			<td><s:property value="numFamilies"/></td>
		</tr>
		<tr>
			<td><s:text name="occResource.numOrders"/></td>
			<td><s:property value="numOrders"/></td>
		</tr>
	</table>
</div>

<div id="temp-stats" class="stats stat-table">
	<label><s:text name="stats.tempStats"/></label>
	<table class="lefthead">
		<tr>
			<td><s:text name="occResource.recWithDate"/></td>
			<td><s:property value="recWithDate"/></td>
		</tr>
	</table>
</div>

</s:push>



<br class="clearfix" />


<div id="loc-countries" class="stats map">
	<label><s:text name="stats.occByCountry"/></label>	
	<div id="imgByCountry">
		<s:action name="occResourceStatsByCountry" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<div id="loc-tax" class="stats map stat-right">
	<label><s:text name="stats.taxaPerCountry"/></label>
	<div id="imgByTaxaPerCountry">
		<s:action name="occResourceStatsByTaxaPerCountry" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<div id="loc-pie" class="stats chart">
	<label><s:text name="stats.occByRegion"/></label>
	<s:form id="regionClassForm">
		<s:select id="regionClass" name="region" list="regionClasses" value="4" theme="simple"/>
	</s:form>
	<s:url id="imgByRegionUrl" action="occResourceStatsByRegion" namespace="/ajax" includeParams="get"/>
	<div id="imgByRegion">
		<s:action name="occResourceStatsByRegion" namespace="/ajax" executeResult="true">
			<s:param name="type" value="4"/>
		</s:action>
	</div>
</div>
<script>
function updateByRegion(){
	var url = '<s:property value="imgByRegionUrl"/>';
	var params = { type: $F("regionClass") }; 
	var target = 'imgByRegion';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('regionClass').observe('change', updateByRegion);
</script>	
<div id="loc-geoserver" class="stats map stat-right">
	<label><s:text name="stats.occPointMap"/></label>
	<img width="<s:property value="%{width}"/>" height="<s:property value="%{height}"/>" src="<s:property value="geoserverMapUrl"/>" />
</div>

			

<br class="clearfix" />


<div id="tax-pie" class="stats chart">
	<label><s:text name="stats.occByTaxon"/></label>
	<s:form id="rankForm">
		<s:select id="rank" list="ranks" value="rank" theme="simple"/>
	</s:form>
	<s:url id="imgByTaxonUrl" action="occResourceStatsByTaxon" namespace="/ajax" includeParams="get"/>
	<div id="imgByTaxon">
		<s:action name="occResourceStatsByTaxon" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<script>
function updateByTaxon(){
	var url = '<s:property value="imgByTaxonUrl"/>';
	var params = { type: $F("rank") }; 
	var target = 'imgByTaxon';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('rank').observe('change', updateByTaxon);
</script>

<div id="recordbasis-pie" class="stats chart stat-right">
	<label><s:text name="stats.occByBasisOfRecord"/></label>
	<div id="imgByBasisOfRecord">
		<s:action name="occResourceStatsByBasisOfRecord" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<br class="clearfix" />


<div id="host-pie" class="stats chart">
	<label><s:text name="stats.occByHost"/></label>
	<s:form id="hostForm">
		<s:select id="hostType" list="hostTypes" theme="simple"/>
	</s:form>
	<s:url id="imgByHostUrl" action="occResourceStatsByHost" namespace="/ajax" includeParams="get"/>
	<div id="imgByHost">
		<s:action name="occResourceStatsByHost" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<script>
function updateByHost(){
	var url = '<s:property value="imgByHostUrl"/>';
	var params = { type: $F("hostType") }; 
	var target = 'imgByHost';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('hostType').observe('change', updateByHost);
</script>	

<div id="time-pie" class="stats chart stat-right">
	<label><s:text name="stats.occByDateColected"/></label>
	<div id="imgByDateColected">
		<s:action name="occResourceStatsByDateColected" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<br class="clearfix" />
