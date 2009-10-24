<head>
    <title>EML - <@s.text name="eml.rights"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.intellectualRights'/>"/>      
</head>

<!--<h1><@s.text name="eml.intellectualRights"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>-->
<div class="break10"></div>
<@s.form id="emlForm" action="rights" method="post" validate="false">
<fieldset>
	<@s.hidden name="resourceId" value="${resourceId?c}"/>
	<@s.hidden name="nextPage" value="project"/>

	<@s.textarea key="eml.purpose" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.maintenance" required="false" cssClass="text xlarge slim"/>
	<@s.textarea key="eml.intellectualRights" required="false" cssClass="text xlarge slim"/>
	<p style="font-size: 13px;">Considering using a <a href="http://creativecommons.org/license/">Creative Commons</a> license?</p>
</fieldset>


	<div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
	</div>
</@s.form>
