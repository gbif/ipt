<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${taxon.resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="occ"/>
</head>
	
<h1>${taxon.scientificName}</h1>  
<div class="horizontal_dotted_line_large_foo"></div>
<div class="break20"></div>

<@s.form>

<#assign rec=taxon>
<#assign resource_id=taxon.resource.id>
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

<div class="break20"></div>			
<div class="break79"></div>


<#include "/WEB-INF/pages/inc/occurrenceList.ftl">  
