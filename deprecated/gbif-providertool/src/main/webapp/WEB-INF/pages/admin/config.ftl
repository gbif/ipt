<head>
  <title><@s.text name='config.heading'/></title>
  <meta name="menu" content="AdminMenu"/>
  <meta name="decorator" content="default"/>
  <meta name="heading" content="<@s.text name='config.heading'/>"/>
  <script
    src="http://www.google.com/jsapi?key=ABQIAAAAQmTfPsuZgXDEr012HM6trBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQTBMMPM0apn-CWBZ8nUq7oUL6nMQ"
    type="text/javascript">
  </script>

  <script
    language="Javascript"
    type="text/javascript">
//<![CDATA[  
  
  google.load("jquery", "1.4.2");
  function OnLoad() {      
    $('#provideCfg_save').click(function() {
        alert('Handler for .click() called.');
    });
  }
  google.setOnLoadCallback(OnLoad); 

//]]>
</script> 
</head>

<#include "/WEB-INF/pages/admin/configMenu.ftl">  

<@s.form id="providerCfg" action="config" method="post">
  <fieldset>
  <div class="leftxlarge">
    <@s.textfield key="config.baseUrl" required="true" cssClass="text xlarge"/>
    </div>
    <div class="leftxlarge">
    <@s.textfield key="config.dataDir" disabled="true" cssClass="text xlarge"/>
    </div>  
    <div class="leftxlarge">    
    <@s.submit cssClass="button" name="updateGeoserver" method="updateGeoserver" key="button.geoserver" theme="simple"/>
    </div>
    <div>
      <div class="leftMedium">
        <@s.select key="config.log4jFilename" list="{'production.xml','debug.xml'}" cssClass="text medium"/>
      </div>
      <div class="leftMedium" style="padding-left:10px">
        <li id="wwgrp_providerCfg_config_gbifAnalytics" class="wwgrp">
         <div id="wwlbl_providerCfg_config_gbifAnalytics" class="wwlbl">
            <label for="providerCfg_config_gbifAnalytics" class="desc"><@s.text name="config.gbifAnalytics"/></label>
         </div> 
         <div id="wwctrl_providerCfg_config_gbifAnalytics" class="wwctrl">
            <@s.checkbox key="config.gbifAnalytics" theme="simple"/>
         </div>
        </li>       
      </div>
    </div>
    <div class="newline">
    <@s.textfield key="config.googleMapsApiKey" required="false" cssClass="text xlarge"/>
    </div>
    <div>&nbsp;&nbsp;<a href="http://code.google.com/apis/maps/signup.html">Get Google Maps API key</a></div>
    <@s.textarea key="config.headerHtml" cssClass="text xlarge"/>
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
        <div class="leftxhalf">
            <@s.textfield key="config.geoserverUser" required="true" cssClass="text large"/>
        </div>
        <div class="leftxhalf">
            <@s.textfield key="config.geoserverPass" required="true" cssClass="text large"/>
        </div>
    </div>
  </fieldset>
  <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
  <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
</@s.form>
