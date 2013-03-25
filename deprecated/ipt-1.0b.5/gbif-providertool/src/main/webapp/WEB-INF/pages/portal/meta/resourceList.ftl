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

	#map {
	    width: 696px;
	    height: 256px;
	    border: 1px solid #ccc;
	}
	</style>
	<script type="text/javascript" src="http://openlayers.org/dev/OpenLayers.js"></script>  
	<script type="text/javascript" src="<@s.url value='/scripts/map.js'/>"></script>  
	<script type="text/javascript">  
	 $(document).ready(function(){
		loadMap();
		
		// update keywords
		$("#tagindex ul li a").click(function(e){
			e.preventDefault(); 
			ajaxHtmlUpdate("<@s.url value="/ajax/keywords.html"/>", "#keywords", { prefix:$(this).html() });
		});
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
	<div class="break"></div>
    <@s.submit cssClass="button" key="button.search" theme="simple"/>
  </@s.form>
</div>
