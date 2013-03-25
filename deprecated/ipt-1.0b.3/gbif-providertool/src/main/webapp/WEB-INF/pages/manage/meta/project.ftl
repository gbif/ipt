<head>
    <title>EML - <@s.text name="eml.researchProject"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<@s.form id="emlForm" action="project" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.researchProject"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="nextPage" value="methods"/>

	<@s.textfield key="eml.researchProject.title" cssClass="text xlarge" />
	<@s.textfield key="eml.researchProject.personnelOriginator.organisation" cssClass="text xlarge" />
	<@s.textarea key="eml.researchProject.abstract" cssClass="text xlarge slim" />
	<@s.textarea key="eml.researchProject.funding" cssClass="text xlarge slim" />
	<@s.textarea key="eml.researchProject.studyAreaDescription" cssClass="text xlarge slim" />
	<@s.textarea key="eml.researchProject.designDescription" cssClass="text xlarge slim" />
</fieldset>


	<div class="break" />
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</@s.form>
