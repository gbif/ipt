<head>
    <title>EML - <@s.text name="eml.geographicCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.geographicCoverage'/>"/>    
	<script type="text/javascript" src="http://openlayers.org/dev/OpenLayers.js"></script>  
	<script type="text/javascript" src="<@s.url value='/scripts/map.js'/>"></script>
	<script type="text/javascript">  
	 $(document).ready(function(){
		loadMap();
	 });
	</script>
</head>

<!--<h1><@s.text name="eml.geographicCoverage"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>-->
<div class="break10"></div>
<@s.form id="geoForm" action="geocoverage" method="post" validate="false">
<fieldset>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="nextPage" value="taxcoverage"/>

	<div id="map" style="width:680px; height:256px; border:1px solid #ccc; margin-left:6px;"></div>
	<div>
		<div class="left">
			<@s.textfield id="bbox_left" key="eml.geographicCoverage.boundingCoordinates.min.longitude" label="%{getText('bbox.min.longitude')}" cssClass="text small" />
		</div>
		<div class="left">
			<@s.textfield id="bbox_bottom" key="eml.geographicCoverage.boundingCoordinates.min.latitude" label="%{getText('bbox.min.latitude')}" cssClass="text small" />
		</div>
		<div class="left">
			<@s.textfield id="bbox_right" key="eml.geographicCoverage.boundingCoordinates.max.longitude" label="%{getText('bbox.max.longitude')}" cssClass="text small" />
		</div>
		<div class="left">
			<@s.textfield id="bbox_top" key="eml.geographicCoverage.boundingCoordinates.max.latitude" label="%{getText('bbox.max.latitude')}" cssClass="text small" />
		</div>
	</div>
	
	<div class="newline"></div>
	<@s.textarea key="eml.geographicCoverage.description" required="false" cssClass="text xlarge"/>
</fieldset>

	<div class="break">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
	</div>
</@s.form>
