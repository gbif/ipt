<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="default"/>
    <meta name="heading" content="<@s.text name="config.registry"/>"/>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.core.min.js'/>"></script>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
	<link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />
	<script>
	<#--
	  first get list of all organisations, then attach automplete event based on this list
	  could do autocomplete via server call, but gets into problems sometimes. This is how it would be called:
	  ${config.getBaseUrl()}/ajax/proxy.do?uri=${registryOrgUrl}.json
	-->
	var orgs;
	function udpateNodeList(data){
	  <#if !config.isOrgRegistered()>
		$("#orgNodeName").removeAttr("readonly");
	  </#if>        
		$("#orgNodeName").autocomplete(data, {
			width:340, 
			minChars:1, 
			mustMatch:true, 
			matchContains:true,
			formatItem: function(row, i, max) {
				return row.nodeName;
			},
			formatResult: function(row) {
				return row.nodeName;
			}
		}).result( function(event, data, formatted) {
		  	console.debug(data);
			$("#orgNodeKey").val(data.nodeKey);
			$("#orgNodeName").val(data.nodeName);
		});
	}
	function udpateOrgList(data){
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
			udpateOrg(data);
			alert("You need to enter your organisations password before you can register anything on behalf of this organisation");
			showWithKey();
		});
	}
	function udpateOrg(data){
	  	console.debug(data);
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
		$("#btnResend").attr('href',"http://gbrds.gbif.org/registry/organization/"+key+"?op=password");
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
	  <#if config.isIptRegistered()>
	  	<#-- the IPT is already registered. No way to change the organisation again -->
	  <#else>
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
			alert("When you register the IPT, a new organisation will also be created and your selected GBIF node will be asked for endorsement.");
			showWithoutKey();
		});
	  </#if>
	  <#if config.isOrgRegistered()>
		$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/${config.org.uddiID}.json'/>", udpateOrg);
		showWithKey();
	  <#else>        
		showWithoutKey();
	    <#if config.orgNode??>
		  $.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryNodeUrl}/${config.orgNode}.json'/>", function(data){
			$("#orgNodeName").val(data.nodeName);
		  	console.debug(data);
		  });
	    </#if>
	  </#if>
	  <#-- execute always -->
		$("#btnRegister").click(function(e) {
		    if (! confirm("Are you sure you want to register this organisation with GBIF?")) {
				e.preventDefault();
		    }
		});
	});
	
	</script>
</head>


<#include "/WEB-INF/pages/admin/configMenu.ftl">  


<@s.form id="providerCfg" method="post">
<fieldset>
    <@s.hidden id="orgKey" cssClass="organisationKey" name="organisationKey" value=""/>
	<@s.textfield id="orgTitle" key="config.org.title" required="true" cssClass="text xlarge external"/>
    <div>
        <div class="leftxhalf">
			<@s.textfield key="config.org.uddi" name="config.gibts.nicht" value="${config.org.uddiID!organisationKey!'Not registered with GBIF'}" readonly="true" cssClass="text large organisationKey"/>
        </div>
        <div class="left">
			<@s.textfield id="orgNodeName" key="config.orgNodeName" required="true" cssClass="text medium external"/>
		    <@s.hidden id="orgNodeKey" key="config.orgNode" cssClass="external"/>
        </div>
        <div>
			<@s.textfield id="orgPassword" key="config.orgPassword" required="true" cssClass="text medium"/>
        </div>
	</div>
    <div>
        <div class="leftxhalf">
			<@s.textfield id="orgName" key="config.org.contactName" required="true" cssClass="text large external"/>
        </div>
        <div  class="leftxhalf">
			<@s.textfield id="orgEmail" key="config.org.contactEmail" required="true" cssClass="text large external"/>
        </div>
	</div>
	<@s.textfield id="orgHomepage" key="config.org.link" required="false" cssClass="text xlarge external"/>
	<@s.textarea id="orgDescription" key="config.org.description" cssClass="text xlarge external"/>

	<@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
    <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit id="btnRegister" cssClass="button btnWithoutKey" key="button.register" method="register" theme="simple"/>
    <div class="right btnWithKey">
	  <#if !config.isIptRegistered()>
	  	<#-- the IPT is already registered. No way to change the organisation again -->
    	<a id="btnNew" href="#">Clear form</a> &nbsp;&nbsp;&nbsp;
	  </#if>
    	<a id="btnResend" target="_blank" href="http://gbrds.gbif.org/registry/organization">Resend Password</a>
    </div>
  </fieldset>

</@s.form>

