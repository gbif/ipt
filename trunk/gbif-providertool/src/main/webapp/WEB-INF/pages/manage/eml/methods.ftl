<head>
    <title>EML - <@s.text name="eml.methods"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="eml"/>
</head>

<@s.form id="emlForm" action="methods" method="get" validate="false">
<fieldset>
	<legend><@s.text name="eml.methods"/></legend>
	<@s.hidden name="resource_id" value="${resource_id}"/>
	<@s.hidden name="backPage" value="project"/>

	<@s.textarea key="eml.methods" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.samplingDescription" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.qualityControl" required="false" cssClass="text xlarge slim"/>
</fieldset>
	
	<div class="break" />
    <@s.submit cssClass="button" key="button.back"   method="back" theme="simple"/>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.done" name="next" theme="simple"/>
</@s.form>