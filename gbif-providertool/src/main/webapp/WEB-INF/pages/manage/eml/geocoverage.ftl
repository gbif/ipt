<@s.form id="emlForm" action="geocoverage" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.resourceCreator"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="backPage" value="creator"/>
	<@s.hidden name="nextPage" value="taxcoverage"/>

	<@s.select key="eml.resourceCreator.role" label="%{getText('agent.role')}" list="roles" cssClass="text medium"/>
	<@s.select key="eml.language" list="isoLanguageI18nCodeMap" cssClass="text medium"/>
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


	<div class="break" />
    <@s.submit cssClass="button" key="button.back" name="back" theme="simple"/>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.next" name="next" theme="simple"/>
</fieldset>
</@s.form>
