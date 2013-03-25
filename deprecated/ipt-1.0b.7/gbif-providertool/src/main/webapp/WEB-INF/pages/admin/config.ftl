<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="<@s.text name='config.heading'/>"/>
</head>

<@s.form id="providerCfg" action="saveConfig" method="post">

<h2><@s.text name="config.metadata"/></h2>
<fieldset>
	<div>
		<@s.textfield key="config.title" required="true" cssClass="text xlarge"/>
	</div>
    <div class="googlemap">
		<#if (config.location)?? && cfg.googleMapsApiKey??>
			<a href="http://maps.google.de/maps?f=s&ie=UTF8&ll=${config.location.latitude!0},${config.location.longitude!0}&t=h&z=15"><img src="http://maps.google.com/staticmap?center=${config.location.latitude!0},${config.location.longitude!0}&zoom=5&size=95x95&key=${cfg.googleMapsApiKey}" /></a>	
		</#if>
    </div>
	<div>
		<@s.textfield key="config.link" required="true" cssClass="text large"/>
	</div>
    <div>
        <div class="leftMedium">
			<@s.textfield key="config.contactName" required="true" cssClass="text medium"/>
        </div>
        <div class="leftMedium">
			<@s.textfield key="config.contactEmail" required="true" cssClass="text medium"/>
        </div>
        <div class="leftMedium">
			<@s.textfield key="config.location.latitude" required="false" cssClass="text medium"/>
        </div>
        <div>
			<@s.textfield key="config.location.longitude" required="false" cssClass="text medium"/>
        </div>
    </div>	    
	<div>
		<@s.textfield key="config.descriptionImage" required="false" cssClass="text xlarge"/>
	</div>
	<div>
		<@s.textarea key="config.description" cssClass="text xlarge"/>
	</div>
  </fieldset>
<div class="horizontal_dotted_line_xlarge_soft_foo" ></div>

<h2 class="modifiedh2"><@s.text name="config.settings"/></h2>
  <fieldset>
	<@s.textfield key="config.dataDir" disabled="true" cssClass="text xlarge"/>
	<@s.textfield key="config.baseUrl" required="true" cssClass="text xlarge"/>
	<@s.textfield key="config.googleMapsApiKey" required="false" cssClass="text xlarge"/>
	<div>&nbsp;&nbsp;<a href="http://code.google.com/apis/maps/signup.html">Get Google Maps API key</a></div>
  </fieldset>
<div class="horizontal_dotted_line_xlarge_soft_foo"></div>  

<h2 class="modifiedh2"><@s.text name="config.geoserver"/></h2>
<fieldset>
    <div>
		<@s.textfield key="config.geoserverUrl" required="true" cssClass="text xlarge"/>
	</div>
	<div>
		<@s.textfield key="config.geoserverDataDir" required="true" cssClass="text xlarge"/>
	</div>	
    <div>
        <div class="left">
			<@s.textfield key="config.geoserverUser" required="true" cssClass="text xlarge"/>
        </div>
        <div class="left">
			<@s.textfield key="config.geoserverPass" required="true" cssClass="text xlarge"/>
        </div>
    </div>
  </fieldset>
<@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
<@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
<div class="right">
	<@s.submit cssClass="button" name="updateGeoserver" method="updateGeoserver" key="button.geoserver" theme="simple"/>
</div>
</@s.form>
