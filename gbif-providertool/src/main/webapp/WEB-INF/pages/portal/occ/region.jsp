<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="region.title"/></title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="search"/>
</head>
	

<h2><s:property value="region.label"/></h2>  

<s:form>
	<fieldset>
	<legend><a onclick="Effect.toggle('details', 'blind', { duration: 0.3 }); return false;">(<s:text name="taxon.details"/>)</a></legend>
	<div id="details" style="display:none">
		<s:label key="region.label"/>
		<s:label key="region.type"/>
	</div>
	</fieldset>
</s:form>

<br/>

<div id="loc-geoserver" class="stats map">
	<label><s:text name="stats.occPointMap"/></label>
	<img src="<s:property value="geoserverMapUrl"/>" />
</div>

			
<br class="clearfix" />

<%@ include file="/pages/portal/occurrences.jsp" %>


<br />
