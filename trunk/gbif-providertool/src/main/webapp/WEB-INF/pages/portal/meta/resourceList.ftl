<head>
    <title>Metadata Repository</title>
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
	 function boundigBoxSearch(minx,miny,maxx,maxy){
		$("#bbox_top").val(maxy);
		$("#bbox_bottom").val(miny);
		$("#bbox_left").val(minx);
		$("#bbox_right").val(maxx);
	 	geoSearchForm.submit();
	 }
	 function goToResource(id){
	 	window.location('<@s.url value="/resource.html?resource_id="/>'+id);
	 }
	   
	 $(document).ready(function(){
		// update keywords
		$("#tagindex ul li a").click(function(e){
			e.preventDefault(); 
			ajaxHtmlUpdate("<@s.url value="/ajax/keywords.html"/>", "#keywords", { prefix:$(this).html() });
		});
		var so = new SWFObject("EOLSpeciesMap.swf", "swf", "100%", "100%", "9"); 
		so.addParam("allowFullScreen", "false");
		so.addVariable("swf", "");
		var data = "[<#list resources as r>{'id':${r.id},'title':'${r.title}','count':0,'minx':${r.geoCoverage.min.x},'maxx':${r.geoCoverage.max.x},'miny':${r.geoCoverage.min.y},'maxy':${r.geoCoverage.max.y}},</#list>";
		data = data.substring(0, data.length-1) + "]";
		so.addVariable("data", data);
		so.write("map");
	});
	</script>
</head>


<#include "/WEB-INF/pages/inc/resourceTypeSelector.ftl">  
<#include "/WEB-INF/pages/inc/resourceList.ftl">  

<div id="tagindex">

  <h2>Keyword Index</h2>	
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
<br class="clearfix" />


<div id="geosearch">
  <h2>Geospatial Search</h2>	
  <div id="map">
  
  </div>
  <@s.form id="geoSearchForm" action="geoSearch">
	<input type="hidden" id="bbox_top" name="bbox_top" value="" />
	<input type="hidden" id="bbox_bottom" name="bbox_bottom" value="" />
	<input type="hidden" id="bbox_left" name="bbox_left" value="" />
	<input type="hidden" id="bbox_right" name="bbox_right" value="" />
  </@s.form>
</div>
