<head>
    <title><@s.text name='occResourceOverview.title'/></title>
    <meta name="resource" content="${occResource.title}"/>
    <meta name="submenu" content="resource"/>
	<@s.head theme="ajax" debug="false"/>
</head>
	
  
<@s.form>
<fieldset>
	<legend><@s.text name="occResource.description"/></legend>
	<div id="metadata">
		<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
		<span>${occResource.description}</span>
		<@s.label key="occResourceOverview.cache" value="${occResource.lastUpload.recordsUploaded} total records uploaded ${occResource.lastUpload.executionDate}"/>
		<ul class="minimenu">
			<li>
				<a onclick="Effect.toggle('services', 'blind', { duration: 0.3 }); return false;">(<@s.text name="occResourceOverview.services"/>)</a>
			</li>
		</ul>
		<div id="services" style="display:none">
			<table class="lefthead">
				<tr>
					<th><@s.text name="occResourceOverview.tabfile"/></th>
					<td><a href="${cfg.getDumpArchiveUrl(resource_id)}">${cfg.getDumpArchiveUrl(resource_id)}</a></td>
				</tr>
				<tr>
					<th><@s.text name="occResourceOverview.tapir"/></th>
					<td><a href="${cfg.getTapirEndpoint(resource_id)}">${cfg.getTapirEndpoint(resource_id)}</a></td>
				</tr>
				<tr>
					<th><@s.text name="occResourceOverview.wfs"/></th>
					<td>${cfg.getWfsEndpoint(resource_id)}</td>
				</tr>
			</table>
		</div>
	</div>
</@s.form>


<br class="clearfix" />

<@s.push value="occResource">

<div id="loc-stats" class="stats stat-table">
	<label><@s.text name="stats.geoStats"/></label>
	<table class="lefthead">
		<tr>
			<td><@s.text name="occResource.recWithCoordinates"/></td>
			<td>${recWithCoordinates}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.recWithCountry"/></td>
			<td>${recWithCountry}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.recWithAltitude"/></td>
			<td>${recWithAltitude}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numCountries"/></td>
			<td>${numCountries}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numRegions"/></td>
			<td>${numRegions}</td>
		</tr>
	</table>
</div>

<div id="tax-stats" class="stats stat-table">
	<label><@s.text name="stats.taxStats"/></label>
	<table class="lefthead">
		<tr>
			<td><@s.text name="occResource.numTaxa"/></td>
			<td>${numTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numTerminalTaxa"/></td>
			<td>${numTerminalTaxa}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numGenera"/></td>
			<td>${numGenera}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numFamilies"/></td>
			<td>${numFamilies}</td>
		</tr>
		<tr>
			<td><@s.text name="occResource.numOrders"/></td>
			<td>${numOrders}</td>
		</tr>
	</table>
</div>

<div id="temp-stats" class="stats stat-table">
	<label><@s.text name="stats.tempStats"/></label>
	<table class="lefthead">
		<tr>
			<td><@s.text name="occResource.recWithDate"/></td>
			<td>${recWithDate}</td>
		</tr>
	</table>
</div>

</@s.push>



<br class="clearfix" />


<div id="loc-countries" class="stats map">
	<label><@s.text name="stats.occByCountry"/></label>	
	<div id="imgByCountry">
		<@s.action name="occResourceStatsByCountry" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<div id="loc-tax" class="stats map stat-right">
	<label><@s.text name="stats.speciesPerCountry"/></label>
	<div id="imgBySpeciesPerCountry">
		<@s.action name="occResourceStatsBySpeciesPerCountry" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<div id="loc-pie" class="stats chart">
	<label><@s.text name="stats.occByRegion"/></label>
	<@s.form id="regionClassForm">
		<@s.select id="regionClass" name="region" list="regionClasses" value="3" theme="simple"/>
	</@s.form>
	<@s.url id="imgByRegionUrl" action="occResourceStatsByRegion" namespace="/ajax" includeParams="get"/>
	<div id="imgByRegion">
		<@s.action name="occResourceStatsByRegion" namespace="/ajax" executeResult="true">
			<@s.param name="type" value="3"/>
		</@s.action>
	</div>
</div>
<script>
function updateByRegion(){
	var url = '${imgByRegionUrl}';
	var params = { type: $F("regionClass") }; 
	var target = 'imgByRegion';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('regionClass').observe('change', updateByRegion);
</script>	
<div id="loc-geoserver" class="stats map stat-right">
	<label><@s.text name="stats.occPointMap"/></label>
	<img src="${geoserverMapUrl}" />
</div>

			

<br class="clearfix" />


<div id="tax-pie" class="stats chart">
	<label><@s.text name="stats.occByTaxon"/></label>
	<@s.form id="rankForm">
		<@s.select id="rank" list="ranks" value="rank" theme="simple"/>
	</@s.form>
	<@s.url id="imgByTaxonUrl" action="occResourceStatsByTaxon" namespace="/ajax" includeParams="get"/>
	<div id="imgByTaxon">
		<@s.action name="occResourceStatsByTaxon" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<script>
function updateByTaxon(){
	var url = '${imgByTaxonUrl}';
	var params = { type: $F("rank") }; 
	var target = 'imgByTaxon';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('rank').observe('change', updateByTaxon);
</script>

<div id="recordbasis-pie" class="stats chart stat-right">
	<label><@s.text name="stats.occByBasisOfRecord"/></label>
	<div id="imgByBasisOfRecord">
		<@s.action name="occResourceStatsByBasisOfRecord" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<br class="clearfix" />


<div id="host-pie" class="stats chart">
	<label><@s.text name="stats.occByHost"/></label>
	<@s.form id="hostForm">
		<@s.select id="hostType" list="ranks" value="rank" theme="simple"/>
	</@s.form>
	<@s.url id="imgByHostUrl" action="occResourceStatsByHost" namespace="/ajax" includeParams="get"/>
	<div id="imgByHost">
		<@s.action name="occResourceStatsByCollection" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<script>
function updateByHost(){
	var url = '${imgByHostUrl}';
	var params = { type: $F("hostType") }; 
	var target = 'imgByHost';	
	var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
};
$('hostType').observe('change', updateByHost);
</script>	

<div id="time-pie" class="stats chart stat-right">
	<label><@s.text name="stats.occByDateColected"/></label>
	<div id="imgByDateColected">
		<@s.action name="occResourceStatsByDateColected" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<br/>
