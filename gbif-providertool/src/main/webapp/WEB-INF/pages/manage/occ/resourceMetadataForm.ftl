<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="${occResource.title!}"/>
    <meta name="submenu" content="manage"/>
</head>

<@s.form id="occResourceForm" action="saveResource" enctype="multipart/form-data" method="post" validate="true">
<fieldset>
    <legend><@s.text name="occResourceOverview.metadata"/></legend>
    <@s.hidden name="resource_id" value="${(occResource.id)!}"/>
		<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
	<@s.textfield key="occResource.title" required="true" cssClass="text large"/>
    <div>
        <div class="left">
			<@s.textfield key="occResource.contactName" required="true" cssClass="text medium"/>
        </div>
        <div class="left">
			<@s.textfield key="occResource.contactEmail" required="true" cssClass="text medium"/>
        </div>
        <div>
		    <@s.file name="file" key="occResource.selectLogoFile" cssClass="text file" required="false"/>
        </div>
    </div>
	<@s.textfield key="occResource.link" required="false" cssClass="text xlarge"/>
	<@s.textfield key="occResource.emlUrl" required="false" cssClass="text xlarge"/>
	<@s.textarea key="occResource.description" cssClass="text xlarge"/>

    <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" name="eml" key="button.eml" theme="simple"/>
    <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
</fieldset>
  
</@s.form>

<script type="text/javascript">
    Form.focusFirstElement($("occResourceForm"));
</script>
