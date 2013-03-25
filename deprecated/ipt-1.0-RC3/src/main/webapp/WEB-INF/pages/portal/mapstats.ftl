<#assign display=JspTaglibs["http://displaytag.sf.net"] />
<#include "/WEB-INF/pages/inc/globalVars.ftl">  

<head>
    <title><@s.text name="mapstats.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="occ"/>
	<meta name="heading" content="<@s.text name='mapstats.heading'/>"/>          
	<script>
	function updateMap(area){
		var params = {resourceId:${resourceId}, zoom:'true', area:area }; 
		var url = '<@s.url value="/ajax/occResourceStatsBy%{statsBy}.html"/>';
		var target = 'stats-map';	
		var myAjax = new Ajax.Updater(target, url, {method: 'get', parameters: params});
	};
	</script>
</head>

<!--<h1>Map Statistics</h1>
<div class="horizontal_dotted_line_large_foo"></div>-->
<div class="break20"></div>

<div id="stats-map">
	<img src="${chartUrl}" />
</div>

<div id="stats-menu">
	<ul class="actionmenu" style="text-align:center;">
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
	    	<@display.column property="count" sortable=true titleKey="stats.count" href="${recordAction}.html?resourceId=${resourceId?c}" media="html" paramId="id" paramProperty="id"/>
		<#else>
	    	<@display.column property="count" sortable=true titleKey="stats.count" />
	    </#if>	
	    <@display.setProperty name="paging.banner.item_name"><@s.text name="stats.country"/></@display.setProperty>
	    <@display.setProperty name="paging.banner.items_name"><@s.text name="stats.countries"/></@display.setProperty>
	</@display.table>
</div>
<br/>