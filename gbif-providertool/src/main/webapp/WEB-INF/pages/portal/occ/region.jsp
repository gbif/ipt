<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="region.title"/></title>
    <meta name="resource" content="<s:property value="region.resource.title"/>"/>
    <meta name="submenu" content="resource"/>
</head>
	

<h2><s:property value="region.label"/></h2>  

<s:form>
	<fieldset>
	<legend><a onclick="Effect.toggle('details', 'blind', { duration: 0.3 }); return false;">(<s:text name="region.details"/>)</a></legend>
	<div id="details" style="display:none">
		<s:label key="region.label"/>
		<s:label key="region.type"/>
		<s:label key="region.occTotal"/>
		<s:label key="region.parent"/>
	</div>
	</fieldset>
</s:form>

<br/>

<div id="loc-geoserver" class="stats map">
	<label><s:text name="stats.occPointMap"/></label>
	<img src="<s:property value="geoserverMapUrl"/>" />
</div>

			
<br class="clearfix" />

<%@ include file="/WEB-INF/pages/inc/occurrenceList.jsp"%>


<br />
