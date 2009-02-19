<head>
    <title>EML - <@s.text name="eml.methods"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<h1><@s.text name="eml.methods"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>
<@s.form id="emlForm" action="methods" method="get" validate="false">
<fieldset>
	<legend><!--<@s.text name="eml.methods"/>--></legend>
	<@s.hidden name="resource_id" value="${resource_id}"/>

	<@s.textarea key="eml.methods" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.samplingDescription" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.qualityControl" required="false" cssClass="text xlarge slim"/>
</fieldset>
	
	<div class="breakRight">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.done" name="next" theme="simple"/>
	</div>    
</@s.form>