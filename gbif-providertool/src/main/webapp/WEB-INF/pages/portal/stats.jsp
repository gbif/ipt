<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/> Statistics</title>
    <meta name="heading" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="search"/>
	<s:head theme="ajax" debug="true"/>
</head>


<div id="stats-chart">
	<img src="<s:property value="chartUrl"/>" />
</div>

<div id="stats-table">
	<display:table name="data" class="table" requestURI="" id="dataList" export="true" pagesize="50">
	    <display:column property="label" sortable="true" titleKey="stats.category"/>
	    <display:column property="count" sortable="true" titleKey="stats.count"/>
	
	    <display:setProperty name="paging.banner.item_name"><s:text name="stats.category"/></display:setProperty>
	    <display:setProperty name="paging.banner.items_name"><s:text name="stats.categories"/></display:setProperty>
	</display:table>
</div>
<br/>
