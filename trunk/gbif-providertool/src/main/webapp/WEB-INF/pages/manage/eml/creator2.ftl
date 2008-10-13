<@s.form id="emlForm" action="creator2" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.resourceCreator"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="backPage" value="creator"/>
	<@s.hidden name="nextPage" value="geocoverage"/>
	
	<@s.push value="eml.resourceCreator">
	<@s.select key="agent.role" list="roles" cssClass="text medium"/>
	<div>
		<div class="left">
			<@s.textfield key="agent.firstName" name="firstName" required="true" cssClass="text medium" />
		</div>
		<div class="left">
			<@s.textfield key="agent.lastName" value="lastName" required="true" cssClass="text large" />
		</div>
	</div>
	<@s.textfield key="agent.organisation" value="organisation" required="false" cssClass="text xlarge"/>
	<@s.textfield key="agent.position" required="false" cssClass="text xlarge"/>
	<div>
		<div class="left">
			<@s.textfield key="agent.phone" required="false" cssClass="text medium"/>
		</div>
		<div class="left">
			<@s.textfield key="agent.email" required="true" cssClass="text medium"/>
		</div>
	</div>
	<@s.textfield key="agent.homepage" required="false" cssClass="text xlarge"/>
	<@s.textfield key="agent.address.address" required="false" cssClass="text large"/>
	<div>
		<div class="left">
			<@s.textfield key="agent.address.postalCode" required="false" cssClass="text medium"/>
		</div>
		<div class="left">
			<@s.textfield key="agent.address.city" required="false" cssClass="text large"/>
		</div>
	</div>
	<@s.textfield key="agent.address.province" required="false" cssClass="text large"/>
	<@s.textfield key="agent.address.country" value="country" required="true" cssClass="text large"/>

    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.next" name="next" theme="simple"/>
	</@s.push>
</fieldset>
</@s.form>
