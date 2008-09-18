<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="taxon.title"/></title>
    <meta name="resource" content="<s:property value="taxon.resource.title"/>"/>
    <meta name="submenu" content="resource"/>
</head>
	

<h2><s:property value="taxon.fullname"/></h2>  

<s:form>
	<fieldset>
	<legend><a onclick="Effect.toggle('details', 'blind', { duration: 0.3 }); return false;">(<s:text name="taxon.details"/>)</a></legend>
	<div id="details" style="display:none">
		<s:label key="taxon.fullname"/>
		<s:label key="taxon.name"/>
		<s:label key="taxon.authorship"/>
		<s:label key="taxon.rank"/>
		<s:label key="taxon.code"/>
		<s:label key="taxon.occTotal"/>
		<s:label key="taxon.parent"/>
	</div>
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
	<img src="<s:property value="geoserverMapUrl"/>" />
</div>

			
<br class="clearfix" />

<%@ include file="/WEB-INF/pages/inc/occurrenceList.jsp"%>


<br />
