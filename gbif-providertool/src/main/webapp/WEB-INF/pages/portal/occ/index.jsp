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
	<div id="imgByCountry">
		<s:action name="occResourceStatsByCountry" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<div id="loc-tax" class="stats map">
	<label><s:text name="stats.speciesPerCountry"/></label>
	<div id="imgBySpeciesPerCountry">
		<s:action name="occResourceStatsBySpeciesPerCountry" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<div id="loc-pie" class="stats chart">
	<label><s:text name="stats.occByRegion"/></label>
	<s:form id="regionClassForm" theme="ajax" action="occResourceStatsByRegion" namespace="/ajax">
		<s:hidden name="resource_id" value="%{resource_id}" />
		<s:select name="region" list="regionClasses" value="region" onchange="dojo.event.topic.publish('imgByRegion_topic');return false;" theme="ajax"/>
	</s:form>
	<s:url id="imgByRegionUrl" action="occResourceStatsByRegion" namespace="/ajax" includeParams="none"/>
	<s:div id="imgByRegion" href="%{imgByRegionUrl}" formId="regionClassForm" listenTopics="imgByRegion_topic" theme="ajax"></s:div>
</div>
<div id="loc-geoserver" class="stats map">
	<label><s:text name="stats.occPointMap"/></label>
	<img src="<s:property value="geoserverMapUrl"/>" />
</div>
			

<br class="clearfix" />


<div id="tax-pie" class="stats chart">
	<label><s:text name="stats.occByTaxon"/></label>
	<s:form id="rankForm" theme="ajax" >
		<s:hidden name="resource_id" value="%{resource_id}" />
		<s:select name="rank" list="ranks" value="rank" onchange="dojo.event.topic.publish('imgByTaxon_topic');return false;"/>
	</s:form>
	<s:url id="imgByTaxonUrl" action="occResourceStatsByTaxon" namespace="/ajax" includeParams="none"/>
	<s:div id="imgByTaxon" listenTopics="imgByTaxon_topic" theme="ajax" formId="rankForm" href="%{imgByTaxonUrl}">
		<s:action name="occResourceStatsByTaxon" namespace="/ajax" executeResult="true"/>
	</s:div>
</div>
<div id="tax2-pie" class="stats chart">
	<label><s:text name="stats.occByTop10Taxa"/></label>
	<div id="imgByTop10Taxa">
		<s:action name="occResourceStatsByTop10Taxa" namespace="/ajax" executeResult="true"/>
	</div>
</div>


<br class="clearfix" />



<div id="inst-pie" class="stats chart">
	<label><s:text name="stats.occByInstitution"/></label>
	<div id="imgByInstitution">
		<s:action name="occResourceStatsByInstitution" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<div id="col-pie" class="stats chart">
	<label><s:text name="stats.occByCollection"/></label>
	<div id="imgByCollection">
		<s:action name="occResourceStatsByCollection" namespace="/ajax" executeResult="true"/>
	</div>
</div>


<br class="clearfix" />


<div id="recordbasis-pie" class="stats chart">
	<label><s:text name="stats.occByBasisOfRecord"/></label>
	<div id="imgByBasisOfRecord">
		<s:action name="occResourceStatsByBasisOfRecord" namespace="/ajax" executeResult="true"/>
	</div>
</div>
<div id="time-pie" class="stats chart">
	<label><s:text name="stats.occByDateColected"/></label>
	<div id="imgByDateColected">
		<s:action name="occResourceStatsByDateColected" namespace="/ajax" executeResult="true"/>
	</div>
</div>

<br class="clearfix" />
