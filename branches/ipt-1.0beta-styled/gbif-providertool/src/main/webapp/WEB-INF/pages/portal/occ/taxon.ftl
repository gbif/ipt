<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${taxon.resource.title}"/>
    <meta name="submenu" content="occ"/>
</head>
	

<h2>${taxon.scientificName}</h2>  

<@s.form>

<#assign rec=taxon>
<#include "/WEB-INF/pages/inc/coreDetails.ftl">  

</@s.form>

<div id="loc-countries" class="stats map">
	<label><@s.text name="stats.occByCountry"/></label>	
	<div id="imgByCountry">
		<@s.action name="occResourceStatsByCountry" namespace="/ajax" executeResult="true">
			<@s.param name="filter" value="%{id}"/>
		</@s.action>
	</div>
</div>
<div id="loc-geoserver" class="stats map">
	<label><@s.text name="stats.occPointMap"/></label>
	<img src="${geoserverMapUrl}" width="${width}" height="${height}" />
</div>

			
<br class="clearfix" />

<#include "/WEB-INF/pages/inc/occurrenceList.ftl">  
