<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="default"/>
    <meta name="heading" content="<@s.text name="config.registry"/>"/>
    <script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.core.min.js'/>"></script>
    <script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
    <script type="text/javascript" src="<@s.url value='/scripts/jquery-validate/jquery.validate.min.js'/>"></script>
    <link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />

    <script>
    <#--
      first get list of all organisations, then attach autocomplete event based on this list
      could do autocomplete via server call, but gets into problems sometimes. This is how it would be called:
      ${config.getBaseUrl()}/ajax/proxy.do?uri=${registryOrgUrl}.json
    -->
    var orgs;
    function udpateNodeList(data){
        $("#nodeLoading").hide();
      <#if !config.isOrgRegistered()>
        $("#orgNodeName").removeAttr("readonly");
      </#if>        
        $("#orgNodeName").autocomplete(data, {
            width:340, 
            minChars:1, 
            mustMatch:true, 
            matchContains:true,
            formatItem: function(row, i, max) {
                return row.name;
            },
            formatResult: function(row) {
                return row.name;
            }
        }).result( function(event, data, formatted) {
            // console.debug(data);
            $("#orgNodeKey").val(data.key);
            $("#orgNodeName").val(data.name);
        });
    }
    function udpateOrgList(data){
        $("#orgLoading").hide();
        $("#orgTitle").removeAttr("readonly").autocomplete(data, {
            width:340, 
            minChars:1, 
            mustMatch:false, 
            matchContains:true,
            formatItem: function(row, i, max) {
                return row.name;
            },
            formatResult: function(row) {
                return row.name;
            }
        }).result( function(event, data, formatted) {
            var url = '${cfg.getBaseUrlContextPath()}/ajax/proxy.do?uri=${registryOrgUrl}/'+data.key+'.json';
            $.getJSON(url, udpateOrg);
//            alert("<@s.text name='config.ipt.passwordmessage'/>");
            showWithKey();
        });
    }
    function udpateOrg(data){
        // console.debug(data);
        $(".organisationKey").val(data.key);
        updateResendLink(data.key);
        $("#orgTitle").val(data.name);
        $("#orgNodeKey").val(data.nodeKey);
        if (data.nodeName.length>0){
            $("#orgNodeName").val(data.nodeName);
        }else{
            $("#orgNodeName").val("Being endorsed");
        }
        $("#orgName").val(data.primaryContactName);
        $("#orgEmail").val(data.primaryContactEmail);
        $("#orgHomepage").val(data.homepageURL);
        $("#orgDescription").val(data.description);
    }
    function updateResendLink(key){
        $("#btnResend").attr('href',"${registryOrgUrl}/"+key+"?op=password");
    }
    function showWithKey(){
        $(".btnWithoutKey").hide();
        $(".btnWithKey").show();
        $("#orgPasswordDiv").show();
 <#--       $("#orgNodeName").attr("readonly","readonly"); -->
        updateResendLink( $("#orgKey").val() );
    }
    function showWithoutKey(){
        $(".btnWithoutKey").show();
        $(".btnWithKey").hide();
        $("#orgPasswordDiv").hide();
<!--        $("#orgNodeName").removeAttr("readonly"); -->
    }
    $(document).ready(function(){
      <#if config.isIptRegistered()>
        $("#orgLoading").hide();
        $("#nodeLoading").hide();
        
        <#-- the IPT is already registered. No way to change the organisation again -->
      <#else>
        <#-- the IPT is not registered. Provide autocompletes for node & org selection -->
        $.getJSON('${cfg.getBaseUrlContextPath()}/ajax/proxy.do?uri=${registryOrgUrl}.json', udpateOrgList);
        $("#orgTitle").attr("readonly","readonly");
        $.getJSON('${cfg.getBaseUrlContextPath()}/ajax/proxy.do?uri=${registryNodeUrl}.json', udpateNodeList);        
// Don't want GBIF Node selector to be disabled. May still need to select a node.
//        $("#orgNodeName").attr("readonly","readonly");
        $("#btnNew").click(function(e) {
            e.preventDefault(); 
            $(".organisationKey").val("");
            $(".external").val("");
            $("#orgPassword").val("").attr("readonly","readonly");
            alert("<@s.text name='configorg.alert'/>");
            showWithoutKey();
        });
      </#if>
      <#if config.isOrgRegistered()>
        $.getJSON('${cfg.getBaseUrlContextPath()}/ajax/proxy.do?uri=${registryOrgUrl}/${config.org.uddiID}.json', udpateOrg);
        showWithKey();
      <#else>        
        showWithoutKey();
        <#if config.orgNode??>
          $.getJSON('${cfg.getBaseUrlContextPath()}/ajax/proxy.do?uri=${registryNodeUrl}/${config.orgNode}.json', function(data){
            $("#orgNodeName").val(data.nodeName);
            //console.debug(data);
          });
        </#if>
      </#if>
      <#-- execute always -->
        $("#btnRegister").click(function(e) {
            if (! confirm("<@s.text name='configorg.confirm'/>")) {
                e.preventDefault();
            }
        });
        <#-- form validation -->
        $("#providerCfg").validate();       
    });
    
    </script>
    <style>
        #orgLoading{
            position: relative;
            bottom: 26px;
            left: 12px;
        }
        #nodeLoading{
            position: relative;
            bottom: 26px;
            left: 12px;
        }
    </style>
</head>


<#include "/WEB-INF/pages/admin/configMenu.ftl">  

<p>
<@s.text name='configorg.instructions'/>
</p>

<p>
<@s.text name='configorg.password.help'/>
<#-- <p><a id="btnResend" target="_blank" href="#"><@s.text name='configorg.password.resend'/></a> -->
<p>

<@s.form id="providerCfg" method="post">
<fieldset>
    <div class="leftxLarge">
        <@s.textfield id="orgTitle" key="orgTitle" required="true" cssClass="text xlarge external required"/>
        <span id="orgLoading">loading from registry <img src='<@s.url value="/images/ajax-loader.gif"/>'/></span>
    </div>  
    <div>
        <div class="leftxhalf">
            <@s.textfield key="orgKey" value="${orgKey!''}" readonly="true" cssClass="text large organisationKey"/>
        </div>
        <div class="left">
            <@s.textfield id="orgNodeName" key="orgNodeName" required="true" cssClass="text medium external required"/>
            <span id="nodeLoading">loading from registry <img src='<@s.url value="/images/ajax-loader.gif"/>'/></span>
           <@s.hidden id="orgNodeKey" key="orgNode" cssClass="external"/>
        </div>
          <div class="leftMedium" id="orgPasswordDiv">
            <@s.textfield id="orgPassword" key="orgPassword" 
            label="%{getText('orgPassword')}" 
            required="false" cssClass="text medium"/>
          </div>
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield id="orgName" key="orgContactName" required="true" cssClass="text large external required"/>
        </div>
        <div  class="leftxhalf">
            <@s.textfield id="orgEmail" key="orgContactEmail" required="true" cssClass="text large external required email"/>
        </div>
    </div>
          <div class="left" id="orgHomePageDiv">
    <@s.textfield id="orgHomepage" key="orgHomepageUrl" required="false" cssClass="text xlarge external"/>
    </div>
          <div class="left" id="orgDescriptionDiv">
    <@s.textarea id="orgDescription" key="orgDescription" cssClass="text xlarge external"/>
    </div>
    
          <div class="left" id="orgButtonDiv">
    <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
    <@s.submit id="btnRegister" cssClass="button btnWithoutKey" key="button.register" method="register" theme="simple"/>
    </div>
    <div class="right btnWithKey">
      <#if !config.isIptRegistered()>
        <#-- the IPT is already registered. No way to change the organisation again -->
        <a id="btnNew" href="#">Clear form</a> &nbsp;&nbsp;&nbsp;
      </#if>
        <a id="btnResend" target="_blank" href="#"><@s.text name='configorg.password.resend'/></a>
    </div>
  </fieldset>

</@s.form>