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
	<table id="details">
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
		  <s:url id="occRegionUrl" action="occRegion" namespace="/" includeParams="none">
			<s:param name="resource_id" value="%{resource_id}"/>
			<s:param name="id" value="%{region.parent.id}"/>
		  </s:url>
		  <td><a href="<s:property value="occRegionUrl" escape="false"/>"><s:property value="%{parent}"/></a></td>
		</tr>
		<tr>
		  <th><s:text name="region.occTotal"/></th>
		  <td><s:property value="%{occTotal}"/></td>
		</tr>
		<!-- 
		<tr>
		  <th>Number of Taxa</th>
		  <td><s:property value="%{occTotal}"/></td>
		</tr>
		 -->
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
