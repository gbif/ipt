<%@ include file="/common/taglibs.jsp"%>

<s:push value="region">
<head>
    <title><s:text name="region.title"/></title>
    <meta name="resource" content="<s:property value="resource.title"/>"/>
    <meta name="submenu" content="occ"/>
</head>
	

<h2><s:property value="label"/></h2>  

<s:form>
	<fieldset>
	<legend><a onclick="Effect.toggle('details', 'blind', { duration: 0.3 }); return false;">(<s:text name="region.details"/>)</a></legend>
	<table id="details" style="display:none">
		<tr>
		  <th><s:text name="region.label"/></th>
		  <td><s:property value="%{label}"/></td>
		</tr>
		<tr>
		  <th><s:text name="region.type"/></th>
		  <td><s:property value="%{type}"/></td>
		</tr>
		<tr>
		  <th><s:text name="region.parent"/></th>
		  <td><s:property value="%{parent}"/></td>
		</tr>
		<tr>
		  <th><s:text name="region.occTotal"/></th>
		  <td><s:property value="%{occTotal}"/></td>
		</tr>
		<tr>
		  <th><s:text name="taxon.bbox"/></th>
		  <td><s:property value="%{region.bbox}"/></td>
		</tr>
	</table>
	</fieldset>
</s:form>

<br/>

<div id="loc-geoserver" class="stats map">
	<label><s:text name="stats.occPointMap"/></label>
	<img src="<s:property value="geoserverMapUrl"/>" width="<s:property value="width"/>" height="<s:property value="height"/>" />
</div>

			
</s:push>
<br class="clearfix" />

<%@ include file="/WEB-INF/pages/inc/occurrenceList.jsp"%>


<br />
