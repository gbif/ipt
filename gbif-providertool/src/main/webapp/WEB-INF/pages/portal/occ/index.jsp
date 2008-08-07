<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="search"/>
	<s:head theme="ajax" debug="true"/>
</head>



<s:form>
<fieldset>
	<legend><s:text name="occResource.description"/></legend>
	<div id="metadata">
		<s:property value="occResource.description"/>
		<s:label key="occResourceOverview.cache" value="%{occResource.lastUpload.recordsUploaded} total records uploaded %{occResource.lastUpload.executionDate}"/>
		<ul class="minimenu">
			<li>
				<a onclick="Effect.toggle('services', 'blind', { duration: 0.3 }); return false;">(<s:text name="occResourceOverview.services"/>)</a>
			</li>
		</ul>
		<div id="services" style="display:none">
			<s:label key="occResourceOverview.tabfile" value="%{occResource.getDumpArchiveUrl()}"/>
			<s:label key="occResourceOverview.tapir" value="%{occResource.getTapirEndpoint()}"/>
			<s:label key="occResourceOverview.wfs" value="%{occResource.getWfsEndpoint()}"/>
		</div>
	</div>
</s:form>


<br class="clearfix" />


<div id="loc-stats" class="stats">
	<label><s:text name="stats.geoStats"/></label>
	<ul class="plain">
		<li>3421 with coordinates, precision from 0 to 10000m</li> 
		<li>1673 with altitude, precision from 0 to 10000m</li> 
		<li>23 countries</li> 
	</ul>
</div>
<div id="tax-stats" class="stats">
	<label><s:text name="stats.taxStats"/></label>
	<ul class="plain">
		<li>673 Terminal taxa</li> 
		<li>673 Genera</li> 
	</ul>
</div>


<br class="clearfix" />


<div id="loc-countries" class="stats map">
	<label><s:text name="stats.occByCountry"/></label>
	<s:url id="occResourceChartByCountry" action="occResourceStatsByCountry">
		<s:param name="resource_id" value="resource_id" />
		<s:param name="region" value="4" />
	</s:url>
	<s:a href="%{occResourceChartByCountryUrl}" >
		<s:action name="occResourceChartByCountry" namespace="/ajax" executeResult="true"/>
	</s:a>
</div>
<div id="loc-tax" class="stats map">
	<label><s:text name="stats.speciesPerCountry"/></label>
	<s:url id="occResourceChartBySpeciesPerCountryUrl" action="occResourceStatsByCountrySpecies">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceChartBySpeciesPerCountryUrl}" >
		<s:action name="occResourceChartBySpeciesPerCountry" namespace="/ajax" executeResult="true"/>
	</s:a>
</div>

<div id="loc-pie" class="stats chart">
	<label><s:text name="stats.occByRegion"/></label>
	<s:select name="locType" list="locTypes" value="locDefault.columnName" theme="simple"/>

	<s:url id="occResourceStatsByRegionUrl" action="occResourceStatsByRegion">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByRegionUrl}" >
		<s:action name="occResourceChartByRegion" namespace="/ajax" executeResult="true"/>
	</s:a>

	<!--
	<s:div id="recordCount" theme="ajax" href="%{recordCountUrl}">
		<s:property value="occResource.getRecordCount()"/>
	</s:div>
	<img src="<s:property value="occByRegionUrl"/>" />
	-->
</div>
<div id="loc-geoserver" class="stats map">
	<label><s:text name="stats.occPointMap"/></label>
	<img src="<s:property value="geoserverMapUrl"/>" />
</div>
			

<br class="clearfix" />


<div id="tax-pie" class="stats chart">
	<label><s:text name="stats.occByTaxon"/></label>
	<s:select name="taxType" title="rank" list="taxTypes" value="taxDefault.columnName" theme="simple"/>
	<s:url id="occResourceStatsByTaxonUrl" action="occResourceStatsByTaxon">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByTaxonUrl}" >
		<s:action name="occResourceChartByTaxon" namespace="/ajax" executeResult="true"/>
	</s:a>

</div>
<div id="tax2-pie" class="stats chart">
	<label><s:text name="stats.occByTop10Taxa"/></label>
	<s:url id="occResourceStatsByTop10TaxaUrl" action="occResourceStatsByTop10Taxa">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByTop10TaxaUrl}" >
		<s:action name="occResourceChartByTop10Taxa" namespace="/ajax" executeResult="true"/>
	</s:a>
</div>


<br class="clearfix" />



<div id="inst-pie" class="stats chart">
	<label><s:text name="stats.occByInstitution"/></label>
	<s:url id="occResourceStatsByInstitutionUrl" action="occResourceStatsByInstitution">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByInstitutionUrl}" >
		<s:action name="occResourceChartByInstitution" namespace="/ajax" executeResult="true"/>
	</s:a>
</div>
<div id="col-pie" class="stats chart">
	<label><s:text name="stats.occByCollection"/></label>
	<s:url id="occResourceStatsByCollectionUrl" action="occResourceStatsByCollection">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByCollectionUrl}" >
		<s:action name="occResourceChartByCollection" namespace="/ajax" executeResult="true"/>
	</s:a>
</div>


<br class="clearfix" />


<div id="recordbasis-pie" class="stats chart">
	<label><s:text name="stats.occByBasisOfRecord"/></label>
	<s:url id="occResourceStatsByBasisOfRecordUrl" action="occResourceStatsByBasisOfRecord">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByBasisOfRecordUrl}" >
		<s:action name="occResourceChartByBasisOfRecord" namespace="/ajax" executeResult="true"/>
	</s:a>
</div>
<div id="time-pie" class="stats chart">
	<label><s:text name="stats.occByDateColected"/></label>
	<s:url id="occResourceStatsByDateColectedUrl" action="occResourceStatsByDateColected">
		<s:param name="resource_id" value="resource_id" />
	</s:url>
	<s:a href="%{occResourceStatsByDateColectedUrl}" >
		<s:action name="occResourceChartByDateColected" namespace="/ajax" executeResult="true"/>
	</s:a>
</div>

<br class="clearfix" />
