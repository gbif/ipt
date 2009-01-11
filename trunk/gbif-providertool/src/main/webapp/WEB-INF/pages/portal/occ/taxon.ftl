<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${taxon.resource.title}"/>
    <meta name="submenu" content="occ"/>
</head>
	

<h2>${taxon.scientificName}</h2>  

<@s.form>

<table>	
 <tr>
	<th>GUID</th>	
	<td><a href="${cfg.getDetailUrl(taxon)}">${taxon.guid}</a></td>
 </tr>
 <tr>
	<th>Data</th>
	<td><a href="${cfg.getDetailUrl(taxon,'xml')}">XML</a></td>
 </tr>
 <tr>
	<th><@s.text name="region.occTotal"/></th>
	<td>${taxon.occTotal}</td>
 </tr>
 <!-- 
 <tr>
	<th>Related</th>
	<@s.url id="taxDetailUrl" action="taxDetail" namespace="/" includeParams="none">
		<@s.param name="resource_id" value="%{resource_id}"/>
		<@s.param name="id" value="%{taxon.id}"/>
	</@s.url>
	<td><a href="<@s.property value="taxDetailUrl" escape="false"/>">Taxon Details</a></td>
 </tr>
  -->
</table>

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
