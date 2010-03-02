<head>
    <title>EML - <@s.text name="eml.resourceCreator"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.resourceCreator'/>"/>    
	<script>
	$(document).ready(function(){
		var url = '<@s.url value="/ajax/vocSelect.html"/>';
		// load language codes
		var params = {uri:"${languageVocUri}",alpha:true,empty:true};
		var id = "languageSelect";
		ajaxSelectVocabulary(url, id, params);
		// load country codes
		params = {uri:"${countryVocUri}",alpha:true,empty:true};
		id = "countrySelect";
		ajaxSelectVocabulary(url, id, params);
	});
	</script>
</head>

<div class="break10"></div>
<@s.form id="emlForm" action="creator" method="post" validate="false">
<fieldset>
	<div>
		<@s.select id="languageSelect" key="eml.language" list="{'${eml.language!}'}" required="true" cssClass="text medium"/>
	</div>
	<@s.hidden name="resourceId" value="${resourceId?c}"/>
	<@s.hidden name="nextPage" value="geocoverage"/>

	<div class="newline">
		<div class="leftxhalf">
			<@s.textfield key="eml.resourceCreator.firstName" label="%{getText('agent.firstName')}" required="true" cssClass="text xhalf" />
		</div>
		<div class="leftxhalf">
			<@s.textfield key="eml.resourceCreator.lastName" label="%{getText('agent.lastName')}" required="true" cssClass="text xhalf" />
		</div>
	</div>
	<div>
		<div class="leftxhalf">
			<@s.textfield key="eml.resourceCreator.organisation" label="%{getText('agent.organisation')}" required="false" cssClass="text xhalf"/>
		</div>
		<div class="leftxhalf">
			<@s.textfield key="eml.resourceCreator.position" label="%{getText('agent.position')}" required="false" cssClass="text xhalf"/>
		</div>
	</div>
	<div>
		<div class="leftxhalf">
			<@s.textfield key="eml.resourceCreator.phone" label="%{getText('agent.phone')}" required="false" cssClass="text xhalf"/>
		</div>
		<div class="leftxhalf">
			<@s.textfield key="eml.resourceCreator.email" label="%{getText('agent.email')}" required="true" cssClass="text xhalf"/>
		</div>
	</div>
	<div>
		<@s.textfield key="eml.resourceCreator.homepage" label="%{getText('agent.homepage')}" required="false" cssClass="text xlarge"/>
	</div>
	<div>	
		<@s.textfield key="eml.resourceCreator.address.address" label="%{getText('agent.address.address')}" required="false" cssClass="text xlarge"/>
	</div>	
	<div>
		<div class="leftMedium">
			<@s.textfield key="eml.resourceCreator.address.postalCode" label="%{getText('agent.address.postalCode')}" required="false" cssClass="text medium"/>
		</div>
		<div class="leftLarge">
			<@s.textfield key="eml.resourceCreator.address.city" label="%{getText('agent.address.city')}" required="false" cssClass="text large"/>
		</div>
	</div>
	<div>
		<div class="leftxhalf">
			<@s.textfield key="eml.resourceCreator.address.province" label="%{getText('agent.address.province')}" required="false" cssClass="text xhalf"/>
		</div>
		<div class="leftxhalf">
			<@s.select id="countrySelect" key="eml.resourceCreator.address.country" list="{'${(eml.getResourceCreator().address.country)!}'}" required="true" cssClass="text xhalf"/>
		</div>
	</div>
	<div class="breakRight"></div>
	<div class="breakRightButtons">
		<@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
		<@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
 	</div>
</fieldset>
</@s.form>
