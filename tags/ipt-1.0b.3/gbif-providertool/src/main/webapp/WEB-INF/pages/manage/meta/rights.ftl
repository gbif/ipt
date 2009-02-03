<head>
    <title>EML - <@s.text name="eml.rights"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<@s.form id="emlForm" action="rights" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.intellectualRights"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="nextPage" value="project"/>

	<@s.textarea key="eml.purpose" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.maintenance" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.intellectualRights" required="false" cssClass="text xlarge slim"/>
	<p>Considering using a <a href="http://creativecommons.org/license/">Creative Commons</a> license?</p>
</fieldset>


	<div class="break" />
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</@s.form>
