<head>
    <title>EML - <@s.text name="eml.geographicCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="eml"/>
</head>

<@s.form id="emlForm" action="geocoverage" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.geographicCoverage"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="backPage" value="creator"/>
	<@s.hidden name="nextPage" value="taxcoverage"/>

	<div id="map" style="width:512px; height:256px; border:1px solid #ccc;"></div>
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
	
	<div class="newline" />
	<@s.textarea key="eml.geographicCoverage.description" required="false" cssClass="text xlarge"/>
</fieldset>

	<#include "/WEB-INF/pages/inc/openLayersBBoxScript.ftl">  

	<div class="break" />
    <@s.submit cssClass="button" key="button.back" method="back" theme="simple"/>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.next" name="next" theme="simple"/>
</@s.form>
