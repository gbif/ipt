<head>
    <title><@s.text name='meta.resourcelist.title'/></title>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="meta"/>
    <meta name="heading" content="<@s.text name='resourceClass.${resourceType}'/>"/>
	<style>
	h2        {clear: both;}
	.col         {width: 100%;  margin: 1em 0; padding: 0; counter-reset: ol;}
	.col      li {float: left;  margin: 0;     padding: 0; list-style-type: none; width: 18em;  margin-right: 2.5%;}
	.col.two  li {width: 47.5%; margin-right: 2.5%	;}
	.col.four li {width: 22.5%; margin-right: 2.5%;}
	
	h1{margin-bottom: -5px;}
	</style>
	<script type="text/javascript" src="<@s.url value="/scripts/swfobject.js"/>" ></script>
	<script type="text/javascript">
	 function selectBoundigBox(minx,miny,maxx,maxy){
		$("#bbox_top").val(maxy);
		$("#bbox_bottom").val(miny);
		$("#bbox_left").val(minx);
		$("#bbox_right").val(maxx);
	 	geoSearchForm.submit();
	 }
	 function goToResource(id){
	 	document.location = ('<@s.url value="/resource.html?resourceId="/>'+id);
	 }
	   
	 $(document).ready(function(){
		// update keywords
		$("#tagindex ul li a").click(function(e){
			e.preventDefault(); 
			ajaxHtmlUpdate("<@s.url value="/ajax/keywords.html"/>", "#keywords", { prefix:$(this).html() });
		});
		var so = new SWFObject("<@s.url value="/scripts/IptResourcesMap.swf"/>", "swf", "690", "250", "9"); 
		so.addParam("allowFullScreen", "true");
		so.addVariable("swf", "");
		var data = '[<#list resources as r><#if (r.geoCoverage.min.x)??>{"id":${r.id?c},"title":"${r.title?js_string}","count":${r.recTotal?c!0},"minx":${r.geoCoverage.min.x?c},"maxx":${r.geoCoverage.max.x?c},"miny":${r.geoCoverage.min.y?c},"maxy":${r.geoCoverage.max.y?c}},</#if></#list>';
		data = data.substring(0, data.length-1) + "]";
		// hacking the string so that SWFObject passes the whole string to flash.
		// normal quotations break it, so we use || instead of " and \\|| for \"
		data = data.replace(/\\"/g, "\\\\||");
		data = data.replace(/"/g, "||");
		so.addVariable("data", data);
		so.addVariable("api_key", "${cfg.getGoogleMapsApiKey()}");
		so.write("map");
	});
	</script>
</head>


<div id="map"></div>

<div id="tagindex">

  <h2><@s.text name='meta.resourcelist.keywordindex'/></h2>	
  <ul class="indexmenu">
	<#assign fullAlphabet = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z']>
    <#list fullAlphabet as c>
     <#if alphabet?seq_contains(c)>
    	<li><a>${c}</a></li>
     <#else>
    	<li>${c}</li>
     </#if>
  	</#list>
  </ul>
  <div id="keywords">
	<#include "/WEB-INF/pages/ajax/keywords.ftl">  
  </div> 
</div>


<br class="clearfix" />

<#include "/WEB-INF/pages/inc/resourceTypeSelector.ftl">  
<#include "/WEB-INF/pages/inc/resourceList.ftl">  
