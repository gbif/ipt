<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="taxon.title"/></title>
    <meta name="resource" content="<s:property value="taxon.resource.title"/>"/>
    <meta name="submenu" content="occ"/>
</head>
	

<h2><s:property value="taxon.scientificName"/></h2>  

<s:form>

<table>	
 <tr>
	<th>GUID</th>	
	<td><a href="<s:property value="%{cfg.getDetailUrl(taxon)}"/>"><s:property value="%{taxon.guid}"/></a></td>
 </tr>
 <tr>
	<th>Data</th>
	<td><a href="<s:property value="%{cfg.getDetailUrl(taxon,'xml')}"/>">XML</a></td>
 </tr>
 <tr>
	<th><s:text name="region.occTotal"/></th>
	<td><s:property value="%{taxon.occTotal}"/></td>
 </tr>
 <!-- 
 <tr>
	<th>Related</th>
	<s:url id="taxDetailUrl" action="taxDetail" namespace="/" includeParams="none">
		<s:param name="resource_id" value="%{resource_id}"/>
		<s:param name="id" value="%{taxon.id}"/>
	</s:url>
	<td><a href="<s:property value="taxDetailUrl" escape="false"/>">Taxon Details</a></td>
 </tr>
  -->
</table>

</s:form>

<div id="loc-countries" class="stats map">
	<label><s:text name="stats.occByCountry"/></label>	
	<div id="imgByCountry">
		<s:action name="occResourceStatsByCountry" namespace="/ajax" executeResult="true">
			<s:param name="filter" value="%{id}"/>
		</s:action>
	</div>
</div>
<div id="loc-geoserver" class="stats map">
	<label><s:text name="stats.occPointMap"/></label>
	<img src="<s:property value="geoserverMapUrl"/>" width="<s:property value="width"/>" height="<s:property value="height"/>" />
</div>

			
<br class="clearfix" />

<%@ include file="/WEB-INF/pages/inc/occurrenceList.jsp"%>


<br />
