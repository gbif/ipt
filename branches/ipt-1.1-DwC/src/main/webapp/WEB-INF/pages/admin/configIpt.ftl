<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="default"/>
    <meta name="heading" content="<@s.text name="config.metadata"/>"/>
    <script type="text/javascript" src="<@s.url value='/scripts/jquery-validate/jquery.validate.min.js'/>"></script>
    <script>
    $(document).ready(function(){
        $("#registerIpt").click(function(e) {
            if (! confirm("Are you sure you want to register this IPT with GBIF? Once you registered as part of an organisation you cannot link this installation to another organisation through the IPT but will have to get in touch with GBIF personally.")) {
                e.preventDefault();
            }
        }); 
        <#-- form validation -->
        $("#providerCfg").validate();       
    });
    
    </script>
    <style>
        img#googlemap {
            padding-top: 15px;
            padding-left:15px
        }   
    </style>        
</head>


<#include "/WEB-INF/pages/admin/configMenu.ftl">  
        
<#assign x>
  <#if config.ipt.description != ''>
    ${config.ipt.description}
  </#if>
</#assign>

<p>
<p>
<a href='http://code.google.com/p/gbif-providertoolkit/wiki/FinishingInstallation'><@s.text name='webapp.manual' /></a>
<p>

<@s.form id="providerCfg" method="post">

<fieldset>
    <div class="leftxLarge">    
        <@s.textfield key="config.ipt.uddi" value='${config.ipt.uddiID!"Not registered with GBIF"}' readonly="true" cssClass="text xlarge"/>
    </div>
    <div class="leftxLarge">
        <@s.textfield key="config.ipt.title" required="true" cssClass="text xlarge required"/>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield key="config.ipt.contactName" required="true" cssClass="text large required"/>
        </div>
        <div class="leftxhalf">
            <@s.textfield key="config.ipt.contactEmail" required="true" cssClass="text large required email"/>
        </div>
    </div>
    <div>
      <div class="leftxhalf">    
        <div>
            <@s.textfield key="config.ipt.link" cssClass="text large"/>
        </div>
        <div>
            <div class="leftMedium">
                <@s.textfield key="config.ipt.location.latitude" required="false" cssClass="text medium"/>
            </div>
            <div>
                <@s.textfield key="config.ipt.location.longitude" required="false" cssClass="text medium"/>
            </div>
        </div>      
      </div>
      <div class="leftxhalf">
        <#if (config.ipt.location)?? && config.ipt.location.latitude?? && config.ipt.location.longitude?? && cfg.googleMapsApiKey??>
            <a href="http://maps.google.de/maps?f=s&ie=UTF8&ll=${config.ipt.location.latitude?c},${config.ipt.location.longitude?c}&t=h&z=15"><img id="googlemap" src="http://maps.google.com/staticmap?center=${config.ipt.location.latitude?c},${config.ipt.location.longitude?c}&zoom=5&size=325x95&key=${cfg.googleMapsApiKey}" /></a>  
        <#else> 
            <img src="<@s.url value='/images/default_image_map.gif'/>"/>
        </#if>
      </div>
    </div>
    <div style="clear:both">        
        <@s.textarea key="config.ipt.description" value='${x}' required="true" cssClass="text xlarge required"/>
    </div>
    <div class="leftxLarge">
        <@s.textfield key="config.descriptionImage" cssClass="text xlarge"/>
    </div>
  </fieldset>

  <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
  <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
  <#if !config.ipt.uddiID?? && config.org.uddiID??>
    <@s.submit cssClass="button" id="registerIpt" key="button.register" method="register" theme="simple"/>
  </#if>
</@s.form>
