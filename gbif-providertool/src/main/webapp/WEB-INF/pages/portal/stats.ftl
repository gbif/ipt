<head>
    <title><@s.text name="occResourceOverview.title"/> Statistics</title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="${resourceType}"/>
</head>


<div id="stats-chart">
	<img src="${chartUrl}"/>" width="${width}" height="${height}" />
</div>

<div id="stats-menu">
	<ul class="actionmenu">
	<@s.iterator value="types" status="tStat">
		<li>
			<@s.url id="linkID" includeParams="none">
				<@s.param name="resource_id" value="%{resource_id}" />
				<@s.param name="zoom" value="true" />
				<@s.param name="type" value="%{ordinal()}" />
			</@s.url>
			<@s.a href="%{linkID}">${name()}</@s.a>
		</li>
	</@s.iterator>
	</ul>
</div>

<div id="stats-table">
	<@display.table name="data" class="table" requestURI="" id="dataList" export=true pagesize=50>
	    <@display.column property="label" sortable=true titleKey="stats.category"/>
    	<@display.column property="id" titleKey="stats.count"/>
	    <#if (recordAction.length()>0)>
		  <@s.if test="#attr.dataList.id > 0">
	    	<@display.column property="count" sortable=true titleKey="stats.count" href="${recordAction}.html?resource_id=${resource_id?c}&type=${type}" media="html" paramId="id" paramProperty="id"/>
		  </@s.if>
		  <@s.else>
	    	<@display.column property="count" sortable=true titleKey="stats.count" href="${recordAction}.html?resource_id=${resource_id?c}&type=${type}" media="html" paramId="category" paramProperty="label"/>
		  </@s.else>
		<#else>
	    	<@display.column property="count" sortable=true titleKey="stats.count"/>
		</#if>
	    <@display.setProperty name="paging.banner.item_name"><@s.text name="stats.category"/></@display.setProperty>
	    <@display.setProperty name="paging.banner.items_name"><@s.text name="stats.categories"/></@display.setProperty>
	</@display.table>
</div>
<br/>
