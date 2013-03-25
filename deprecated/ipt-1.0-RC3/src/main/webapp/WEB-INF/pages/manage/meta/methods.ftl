<head>
    <title>EML - <@s.text name="eml.methods"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.methods'/>"/>    
</head>

<!--<h1><@s.text name="eml.methods"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>-->
<div class="break10"></div>
<@s.form id="emlForm" action="methods" method="get" validate="false">
<fieldset>
	<@s.hidden name="resourceId" value="${resourceId}"/>

	<@s.textarea key="eml.methods" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.samplingDescription" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.qualityControl" required="false" cssClass="text xlarge slim"/>
</fieldset>

<div class="breakRightButtons">
	<@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
	  <@s.submit cssClass="button" key="button.done" name="next" theme="simple"/>
</div>    
</@s.form>