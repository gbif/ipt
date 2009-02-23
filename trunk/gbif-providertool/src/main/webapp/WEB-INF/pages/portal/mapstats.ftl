<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<head>
    <title><@s.text name="occResourceOverview.title"/> Statistics</title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="occ"/>
	<script>
	function updateMap(area){
		var params = {resource_id:${resource_id}, zoom:'true', area:area }; 
		var url = '<@s.url value="/ajax/occResourceStatsBy%{statsBy}.html"/>';
		var target = 'stats-map';	
		var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
	};
	</script>
</head>


<div id="stats-map">
	<img src="${chartUrl}" />
</div>

<div id="stats-menu">
	<ul class="actionmenu">
	  <#list ["World","Africa","Asia","Europe","Middle East","South America","USA"] as area>
		<li>
			<a href="#" onclick="updateMap('${area?lower_case?replace(" ","_")}');return false">${area}</a>
		</li>
	  </#list>	
	</ul>
			
</div>
<div class="break79"></div>
<div id="stats-big-table">
	<@display.table name="data" class="table" requestURI="" id="dataList" export=true pagesize=50>
	    <@display.column property="label" sortable=true titleKey="stats.country"/>
	    <#if recordAction??>	    
	    	<@display.column property="count" sortable=true titleKey="stats.count" href="${recordAction}.html?resource_id=${resource_id?c}" media="html" paramId="id" paramProperty="id"/>
		<#else>
	    	<@display.column property="count" sortable=true titleKey="stats.count" />
	    </#if>	
	    <@display.setProperty name="paging.banner.item_name"><@s.text name="stats.country"/></@display.setProperty>
	    <@display.setProperty name="paging.banner.items_name"><@s.text name="stats.countries"/></@display.setProperty>
	</@display.table>
</div>
<br/>
