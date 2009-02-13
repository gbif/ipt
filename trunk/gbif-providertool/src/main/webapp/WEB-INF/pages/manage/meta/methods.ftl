<head>
    <title>EML - <@s.text name="eml.methods"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<h1 class="modifiedh1"><@s.text name="eml.methods"/></h1>
<@s.form id="emlForm" action="methods" method="get" validate="false">
<fieldset>
	<legend><!--<@s.text name="eml.methods"/>--></legend>
	<@s.hidden name="resource_id" value="${resource_id}"/>

	<@s.textarea key="eml.methods" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.samplingDescription" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.qualityControl" required="false" cssClass="text xlarge slim"/>
</fieldset>
	
	<div class="break"></div>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.done" name="next" theme="simple"/>
</@s.form>