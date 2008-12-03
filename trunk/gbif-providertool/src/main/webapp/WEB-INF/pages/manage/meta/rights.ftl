<head>
    <title>EML - <@s.text name="eml.rights"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="eml"/>
</head>

<@s.form id="emlForm" action="rights" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.intellectualRights"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="backPage" value="tempcoverage"/>
	<@s.hidden name="nextPage" value="project"/>

	<@s.textarea key="eml.purpose" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.maintenance" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.intellectualRights" required="false" cssClass="text xlarge slim"/>
</fieldset>


	<div class="break" />
    <@s.submit cssClass="button" key="button.back" method="back" theme="simple"/>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.next" name="next" theme="simple"/>
</@s.form>
