<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="MainMenu"/>
</head>

<h1><@s.text name='config.heading'/></h1>

<@s.form id="providerCfg" action="saveConfig" method="post">

  <fieldset>
    <legend><@s.text name="config.metadata"/></legend>
	<@s.textfield key="config.title" required="true" cssClass="text xlarge"/>
	<@s.textfield key="config.link" required="true" cssClass="text xlarge"/>
    <div>
        <div class="left">
			<@s.textfield key="config.contactName" required="true" cssClass="text medium"/>
        </div>
        <div>
			<@s.textfield key="config.contactEmail" required="true" cssClass="text large"/>
        </div>
    </div>
	<@s.textfield key="config.emlUrl" required="false" cssClass="text xlarge"/>
	<@s.textfield key="config.descriptionImage" required="false" cssClass="text xlarge"/>
	<@s.textarea key="config.description" cssClass="text xlarge"/>
  </fieldset>

<br/>
<br/>

  <fieldset>
    <legend><@s.text name="config.settings"/></legend>
	<@s.textfield key="config.dataDir" required="true" cssClass="text xlarge"/>
	<@s.textfield key="config.baseUrl" required="true" cssClass="text xlarge"/>
	<@s.textfield key="config.geoserverUrl" required="true" cssClass="text xlarge"/>
  </fieldset>
  
    <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
</@s.form>
