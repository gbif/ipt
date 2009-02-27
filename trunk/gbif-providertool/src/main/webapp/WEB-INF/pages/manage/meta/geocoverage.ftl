<head>
    <title>EML - <@s.text name="eml.geographicCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.geographicCoverage'/>"/>    
	<script type="text/javascript" src="<@s.url value="/scripts/swfobject.js"/>" ></script>
	<script type="text/javascript">  
	 function selectBoundigBox(minx,miny,maxx,maxy){
		$("#bbox_top").val(maxy);
		$("#bbox_bottom").val(miny);
		$("#bbox_left").val(minx);
		$("#bbox_right").val(maxx);
	 }

	 $(document).ready(function(){
		var so = new SWFObject("<@s.url value="/scripts/IptResourcesMap.swf"/>", "swf", "690", "300", "9"); 
		so.addParam("allowFullScreen", "false");
		so.addVariable("swf", "");
		var data = "{'minx':${eml.geographicCoverage().boundingCoordinates.min.x},'maxx':${eml.geographicCoverage().boundingCoordinates.max.x},'miny':${eml.geographicCoverage().boundingCoordinates.min.y},'maxy':${eml.geographicCoverage().boundingCoordinates.max.y}}";
		so.addVariable("data", data);
		so.addVariable("api_key", "${cfg.getGoogleMapsApiKey()}");
		so.write("map");
	 });
	</script>
</head>

<!--<h1><@s.text name="eml.geographicCoverage"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>-->
<div class="break10"></div>

<div id="map">
</div>

<@s.form id="geoForm" action="geocoverage" method="post" validate="false">
<fieldset>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="nextPage" value="taxcoverage"/>
	<input type="hidden" id="bbox_left" name="bbox_left" value="eml.geographicCoverage.boundingCoordinates.min.x" />
	<input type="hidden" id="bbox_right" name="bbox_right" value="eml.geographicCoverage.boundingCoordinates.max.x" />
	<input type="hidden" id="bbox_bottom" name="bbox_bottom" value="eml.geographicCoverage.boundingCoordinates.min.y" />
	<input type="hidden" id="bbox_top" name="bbox_top" value="eml.geographicCoverage.boundingCoordinates.max.y" />
	<div class="newline"></div>
	<@s.textarea key="eml.geographicCoverage.description" required="false" cssClass="text xlarge"/>
</fieldset>

	<div class="break">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
	</div>
</@s.form>
