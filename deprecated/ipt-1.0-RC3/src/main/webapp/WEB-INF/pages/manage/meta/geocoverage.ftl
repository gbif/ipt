<head>
    <title>EML - <@s.text name="eml.geographicCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.geographicCoverage'/>"/>    
	<script type="text/javascript" src="<@s.url value="/scripts/swfobject.js"/>" ></script>
	<script type="text/javascript">  
	 function selectBoundigBox(minx,miny,maxx,maxy){
		$("#maxy").val(maxy);
		$("#miny").val(miny);
		$("#minx").val(minx);
		$("#maxx").val(maxx);
	 }

	 $(document).ready(function(){
		var so = new SWFObject("<@s.url value="/scripts/IptGeoCoverageMap.swf"/>", "swf", "690", "250", "9"); 
		so.addParam("allowFullScreen", "false");
		so.addVariable("swf", "");
		var data = "<#if (eml.geographicCoverage.boundingCoordinates.min.x)??>{'minx':${eml.geographicCoverage.boundingCoordinates.min.x},'maxx':${eml.geographicCoverage.boundingCoordinates.max.x},'miny':${eml.geographicCoverage.boundingCoordinates.min.y},'maxy':${eml.geographicCoverage.boundingCoordinates.max.y}}</#if>";
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
	<@s.hidden name="resourceId" value="${resourceId?c}"/>
	<@s.hidden name="nextPage" value="taxcoverage"/>
	<@s.hidden id="minx" key="eml.geographicCoverage.boundingCoordinates.min.longitude"/>
	<@s.hidden id="maxx" key="eml.geographicCoverage.boundingCoordinates.max.longitude" />
	<@s.hidden id="miny" key="eml.geographicCoverage.boundingCoordinates.min.latitude" />
	<@s.hidden id="maxy" key="eml.geographicCoverage.boundingCoordinates.max.latitude" />
	<div class="newline"></div>
	<@s.textarea key="eml.geographicCoverage.description" required="false" cssClass="text xlarge"/>
</fieldset>

	<div class="break">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
	</div>
</@s.form>
