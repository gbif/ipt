<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="decorator" content="default"/>
    <meta name="heading" content="<@s.text name="config.registry"/>"/>
    <script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.core.min.js'/>"></script>
    <script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
    <script type="text/javascript" src="<@s.url value='/scripts/jquery-validate/jquery.validate.min.js'/>"></script>
    <link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />

    <script>
    <#--
      first get list of all organisations, then attach automplete event based on this list
      could do autocomplete via server call, but gets into problems sometimes. This is how it would be called:
      ${config.getBaseUrl()}/ajax/proxy.do?uri=${registryOrgUrl}.json
    -->
    var orgs;
    function udpateNodeList(data){
        $("#nodeLoading").hide();
      <#if !org.isRegistered()>
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
            //console.debug(data);
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
            var url = "<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/'/>"+data.key+'.json';
            $.getJSON(url, udpateOrg);
            alert("You need to enter your organisations password before you can register anything on behalf of this organisation");
            showWithKey();
        });
    }
    function udpateOrg(data){
        //console.debug(data);
        $(".organisationKey").val(data.key);
        updateResendLink(data.key);
        $("#orgTitle").val(data.name);
        $("#orgNodeKey").val(data.endorsingNodeKey);
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
        $("#orgNodeName").attr("readonly","readonly");
        updateResendLink( $("#orgKey").val() );
    }
    function showWithoutKey(){
        $(".btnWithoutKey").show();
        $(".btnWithKey").hide();
        $("#orgNodeName").removeAttr("readonly");
    }
    $(document).ready(function(){

        <#-- the IPT is not registered. Provide autocompletes for node & org selection -->
        $.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}.json'/>", udpateOrgList);
        $("#orgTitle").attr("readonly","readonly");
        $.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryNodeUrl}.json'/>", udpateNodeList);        
        $("#orgNodeName").attr("readonly","readonly");
        $("#btnNew").click(function(e) {
            e.preventDefault(); 
            $(".organisationKey").val("");
            $(".external").val("");
            $("#orgPassword").val("").attr("readonly","readonly");
            alert("<@s.text name='configorg.alert'/>");
            showWithoutKey();
        });

      <#if org.isRegistered()>
        $.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/${org.organizationKey}.json'/>", udpateOrg);
        showWithKey();
      <#else>        
        showWithoutKey();
        <#if org.nodeKey??>
          $.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryNodeUrl}/${org.nodeKey}.json'/>", function(data){
            $("#orgNodeName").val(data.nodeName);
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

<p>
<@s.text name='configorg.instructions'/>
</p>

<p>
<@s.text name='configorg.password.help'/>
<a id="btnResend" target="_blank" href="#"><@s.text name='configorg.password.resend'/></a>
<p>

<@s.form id="providerCfg" method="post">
<fieldset>
    <@s.hidden id="orgKey" cssClass="organisationKey" name="organisationKey" value=""/>
    <div class="leftxLarge">
        <@s.textfield id="orgTitle" key="org.name" required="true" cssClass="text xlarge external required"/>
        <span id="orgLoading">loading from registry <img src='<@s.url value="/images/ajax-loader.gif"/>'/></span>
    </div>  
    <div>
        <div class="leftxhalf">
            <@s.textfield key="org.organizationKey" value="${org.organizationKey!}" readonly="true" cssClass="text large organisationKey"/>
        </div>
        <div class="left">
            <@s.textfield id="orgNodeName" key="org.nodeName" required="true" cssClass="text medium external required"/>
            <span id="nodeLoading">loading from registry <img src='<@s.url value="/images/ajax-loader.gif"/>'/></span>
           <@s.hidden id="orgNodeKey" key="org.nodeKey" cssClass="external"/>
        </div>
        <div>
            <@s.textfield id="orgPassword" key="org.password" required="false" cssClass="text medium"/>           
        </div>        
    </div>
    <div>
        <div class="leftxhalf">
            <@s.textfield id="orgName" key="org.primaryContactName" required="true" cssClass="text large external required"/>
        </div>
        <div  class="leftxhalf">
            <@s.textfield id="orgEmail" key="org.primaryContactEmail" required="true" cssClass="text large external required email"/>
        </div>
    </div>
    <@s.textfield id="orgHomepage" key="org.homepageUrl" required="false" cssClass="text xlarge external"/>
    <@s.textarea id="orgDescription" key="org.description" cssClass="text xlarge external"/>

    <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
    <@s.submit id="btnRegister" cssClass="button btnWithoutKey" key="button.register" method="register" theme="simple"/>
    <div class="right btnWithKey">
      <#--
      <#if !config.isIptRegistered()>
        <a id="btnNew" href="#">Clear form</a> &nbsp;&nbsp;&nbsp;
      </#if>
      -->
       <a id="btnResend" target="_blank" href="#">Resend Password</a>
    </div>
  </fieldset>

</@s.form>