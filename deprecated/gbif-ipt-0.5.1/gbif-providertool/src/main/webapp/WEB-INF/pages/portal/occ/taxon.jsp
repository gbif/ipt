<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="taxon.title"/></title>
    <meta name="resource" content="<s:property value="taxon.resource.title"/>"/>
    <meta name="submenu" content="occ"/>
</head>
	

<h2><s:property value="taxon.fullname"/></h2>  

<s:form>
	<fieldset>
	<legend><a onclick="Effect.toggle('details', 'blind', { duration: 0.3 }); return false;">(<s:text name="taxon.details"/>)</a></legend>
	<table id="details" style="display:none">
		<tr>
		  <th><s:text name="taxon.fullname"/></th>
		  <td><s:property value="%{taxon.fullname}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.name"/></th>
		  <td><s:property value="%{taxon.name}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.authorship"/></th>
		  <td><s:property value="%{taxon.authorship}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.rank"/></th>
		  <td><s:property value="%{taxon.rank}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.code"/></th>
		  <td><s:property value="%{taxon.code}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.parent"/></th>
		  <td><s:property value="%{taxon.parent}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.occTotal"/></th>
		  <td><s:property value="%{taxon.occTotal}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.bbox"/></th>
		  <td><s:property value="%{taxon.bbox}"/></td>
		</tr>
	</table>
	</fieldset>
</s:form>

<br/>

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
