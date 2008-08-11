<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="occResourceOverview.title"/> Statistics</title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="search"/>
</head>


<div id="stats-chart">
	<img src="<s:property value="chartUrl"/>" />
</div>

<div id="stats-menu">
	<ul class="actionmenu">
	<s:iterator value="types" status="tStat">
		<li>
			<s:url id="linkID" includeParams="none">
				<s:param name="resource_id" value="%{resource_id}" />
				<s:param name="zoom" value="true" />
				<s:param name="type" value="%{ordinal()}" />
			</s:url>
			<s:a href="%{linkID}"><s:property value="name()"/></s:a>
		</li>
	</s:iterator>		
	</ul>
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
