<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<head>
    <title><@s.text name="stats.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="${resourceType}"/>
</head>


<h1><@s.text name='stats.chartstats'/></h1>
<div class="horizontal_dotted_line_large_foo"></div>
<div class="break20"></div>

<div id="stats-chart">
	<img src="${chartUrl}" width="${width}" height="${height}" />
</div>

<div id="stats-menu">
	<#if types??>
	<ul class="actionmenu">
	<#list types as t>
		<li>
			<a href="?resourceId=${resourceId}&zoom=true&type=${t.ordinal()}">${t.name()}</a>
		</li>
	</#list>
	</ul>
	</#if>
</div>
<div class="break79"></div>
<div id="stats-big-table">
	<@display.table name="data" class="table" id="drow" export=true pagesize=50>
	    <@display.column property="label" sortable=true titleKey="stats.category"/>
	    <#if recordAction??>
		  <#if (drow.id)??>
	    	<@display.column property="count" sortable=true titleKey="stats.count" href="${recordAction}.html?resourceId=${resourceId?c}&type=${type}" media="html" paramId="id" paramProperty="id"/>
	      <#else>
	    	<@display.column property="count" sortable=true titleKey="stats.count" href="${recordAction}.html?resourceId=${resourceId?c}&type=${type}" media="html" paramId="category" paramProperty="label"/>
		  </#if>
		<#else>
	    	<@display.column property="count" sortable=true titleKey="stats.count"/>
		</#if>
	    <@display.setProperty name="paging.banner.item_name"><@s.text name="stats.category"/></@display.setProperty>
	    <@display.setProperty name="paging.banner.items_name"><@s.text name="stats.categories"/></@display.setProperty>
	</@display.table>
</div>
<br/>