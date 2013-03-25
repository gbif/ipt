<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/> Statistics</title>
    <meta name="resource" content="<s:property value="resource.title"/>"/>
    <meta name="submenu" content="occ"/>
	<s:head theme="ajax" debug="false"/>
</head>


<div id="stats-map">
	<img src="<s:property value="chartUrl"/>" />
</div>

<div id="stats-menu">
	<ul class="actionmenu">		
		<li>
			<s:form id="form1" theme="ajax" action="occResourceStatsBy%{action}" namespace="/ajax">
				<s:hidden name="zoom" value="true" />
				<s:hidden name="area" value="world" />
				<s:hidden name="resource_id" value="%{resource_id}" />
				<s:a targets="stats-map" theme="ajax">World</s:a>
			</s:form>
		</li>
		<li>
			<s:form id="form2" theme="ajax" action="occResourceStatsBy%{action}" namespace="/ajax">
				<s:hidden name="zoom" value="true" />
				<s:hidden name="area" value="africa" />
				<s:hidden name="resource_id" value="%{resource_id}" />
				<s:a targets="stats-map" theme="ajax">Africa</s:a>
			</s:form>
		</li>
		<li>
			<s:form id="form3" theme="ajax" action="occResourceStatsBy%{action}" namespace="/ajax">
				<s:hidden name="zoom" value="true" />
				<s:hidden name="area" value="asia" />
				<s:hidden name="resource_id" value="%{resource_id}" />
				<s:a targets="stats-map" theme="ajax">Asia</s:a>
			</s:form>
		</li>
		<li>
			<s:form id="form4" theme="ajax" action="occResourceStatsBy%{action}" namespace="/ajax">
				<s:hidden name="zoom" value="true" />
				<s:hidden name="area" value="europe" />
				<s:hidden name="resource_id" value="%{resource_id}" />
				<s:a targets="stats-map" theme="ajax">Europe</s:a>
			</s:form>
		</li>
		<li>
			<s:form id="form5" theme="ajax" action="occResourceStatsBy%{action}" namespace="/ajax">
				<s:hidden name="zoom" value="true" />
				<s:hidden name="area" value="middle_east" />
				<s:hidden name="resource_id" value="%{resource_id}" />
				<s:a targets="stats-map" theme="ajax">Middle East</s:a>
			</s:form>
		</li>
		<li>
			<s:form id="form6" theme="ajax" action="occResourceStatsBy%{action}" namespace="/ajax">
				<s:hidden name="zoom" value="true" />
				<s:hidden name="area" value="south_america" />
				<s:hidden name="resource_id" value="%{resource_id}" />
				<s:a targets="stats-map" theme="ajax">South America</s:a>
			</s:form>
		</li>
		<li>
			<s:form id="form7" theme="ajax" action="occResourceStatsBy%{action}" namespace="/ajax">
				<s:hidden name="zoom" value="true" />
				<s:hidden name="area" value="usa" />
				<s:hidden name="resource_id" value="%{resource_id}" />
				<s:a targets="stats-map" theme="ajax">USA</s:a>
			</s:form>
		</li>
	</ul>
			
</div>

<s:url id="recordUrl" action="%{recordAction}" includeParams="none">
	<s:param name="resource_id" value="resource_id" />
</s:url>

<div id="stats-table">
	<display:table name="data" class="table" requestURI="" id="dataList" export="true" pagesize="50">
	    <display:column property="label" sortable="true" titleKey="stats.country"/>
	    <s:if test="%{recordAction.length() > 0}">	    
	    	<display:column property="count" sortable="true" titleKey="stats.count" href="${recordUrl}" media="html" paramId="id" paramProperty="id"/>
		</s:if>
		<s:else>
	    	<display:column property="count" sortable="true" titleKey="stats.count"/>
		</s:else>
	
	    <display:setProperty name="paging.banner.item_name"><s:text name="stats.country"/></display:setProperty>
	    <display:setProperty name="paging.banner.items_name"><s:text name="stats.countries"/></display:setProperty>
	</display:table>
</div>
<br/>
