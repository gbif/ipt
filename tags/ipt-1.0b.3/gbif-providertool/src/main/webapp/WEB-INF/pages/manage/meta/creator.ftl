<head>
    <title>EML - <@s.text name="eml.resourceCreator"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<@s.form id="emlForm" action="creator" method="post" validate="false">
	<@s.select key="eml.language" list="isoLanguageI18nCodeMap" required="true" cssClass="text medium"/>
<fieldset>
	<legend><@s.text name="eml.resourceCreator"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="nextPage" value="geocoverage"/>

	<div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.firstName" label="%{getText('agent.firstName')}" required="true" cssClass="text xhalf" />
		</div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.lastName" label="%{getText('agent.lastName')}" required="true" cssClass="text xhalf" />
		</div>
	</div>
	<div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.organisation" label="%{getText('agent.organisation')}" required="false" cssClass="text xhalf"/>
		</div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.position" label="%{getText('agent.position')}" required="false" cssClass="text xhalf"/>
		</div>
	</div>
	<div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.phone" label="%{getText('agent.phone')}" required="false" cssClass="text xhalf"/>
		</div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.email" label="%{getText('agent.email')}" required="true" cssClass="text xhalf"/>
		</div>
	</div>
	<@s.textfield key="eml.resourceCreator.homepage" label="%{getText('agent.homepage')}" required="false" cssClass="text xlarge"/>
	<@s.textfield key="eml.resourceCreator.address.address" label="%{getText('agent.address.address')}" required="false" cssClass="text xlarge"/>
	<div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.address.postalCode" label="%{getText('agent.address.postalCode')}" required="false" cssClass="text medium"/>
		</div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.address.city" label="%{getText('agent.address.city')}" required="false" cssClass="text large"/>
		</div>
	</div>
	<div>
		<div class="left">
			<@s.textfield key="eml.resourceCreator.address.province" label="%{getText('agent.address.province')}" required="false" cssClass="text xhalf"/>
		</div>
		<div class="left">
			<@s.select key="eml.resourceCreator.address.country" list="isoCountryI18nCodeMap" required="true" cssClass="text xhalf"/>
		</div>
	</div>
</fieldset>

	<div class="break" />
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</@s.form>
