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


    <div class="googlemap">
		<#if (config.location)?? && cfg.googleMapsApiKey??>
			<a href="http://maps.google.de/maps?f=s&ie=UTF8&ll=${config.location.latitude!0},${config.location.longitude!0}&t=h&z=15"><img src="http://maps.google.com/staticmap?center=${config.location.latitude!0},${config.location.longitude!0}&zoom=5&size=95x95&key=${cfg.googleMapsApiKey}" /></a>	
		</#if>
    </div>
	<@s.textfield key="occResource.link" required="false" cssClass="text large"/>
    <div>
        <div class="left">
			<@s.textfield key="occResource.contactName" required="true" cssClass="text medium"/>
        </div>
        <div class="left">
			<@s.textfield key="occResource.contactEmail" required="true" cssClass="text medium"/>
        </div>
        <div class="left">
			<@s.textfield key="occResource.location.latitude" required="false" cssClass="text small"/>
        </div>
        <div>
			<@s.textfield key="occResource.location.longitude" required="false" cssClass="text small"/>
        </div>


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
