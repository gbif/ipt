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
		});
	}
	function udpateOrg(data){
	  	console.debug("Retrieved current organisation metadata:");
	  	console.debug(data);
		$(".organisationKey").val(data.key);
		$("#orgTitle").val(data.name);
		$("#orgNodeKey").val(data.endorsingNodeKey);
		if (data.endorsingNodeName.length>0){
			$("#orgNodeName").val(data.endorsingNodeName);
		}else{
			$("#orgNodeName").val("Being endorsed");
		}
		$("#orgName").val(data.primaryContactName);
		$("#orgEmail").val(data.primaryContactEmail);
		$("#orgHomepage").val(data.homepageURL);
		$("#orgDescription").val(data.description);
	}
	$(document).ready(function(){
	  <#if config.isIptRegistered()>
	  	<#-- the IPT is already registered. No way to change the organisation again -->
	  <#else>
	  	<#-- the IPT is not registered. Provide autocompletes for node & org selection -->
		$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}.json'/>", udpateOrgList);
		$("#orgTitle").attr("readonly","readonly").result(function(event, data, formatted) {
			udpateOrg(data);
			alert("You need to enter your organisations password before you can register anything on behalf of this organisation");
			$("#registerOrg").hide();
		});
		$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryNodeUrl}.json'/>", udpateNodeList);        
		$("#orgNodeName").attr("readonly","readonly").result(function(event, data, formatted) {
		  	console.debug(data);
			$("#orgNodeKey").val(data.nodeKey);
			$("#orgNodeName").val(data.nodeName);
		});
		$("#newOrg").click(function(e) {
			e.preventDefault(); 
			$(".organisationKey").val("");
			$(".external").val("");
			$("#orgPassword").val("").attr("readonly","readonly");
			alert("When you register the IPT, a new organisation will also be created and your selected GBIF node will be asked for endorsement.");
			$("#registerOrg").show();
		});
	  </#if>
	  <#if config.isOrgRegistered()>
		$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/${config.org.uddiID}.json'/>", udpateOrg);
	  <#else>        
	    <#if config.orgNode??>
		  $.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryNodeUrl}/${config.orgNode}.json'/>", function(data){
			$("#orgNodeName").val(data.nodeName);
		  	console.debug("Retrieved current GBIF node:");
		  	console.debug(data);
		  });
	    </#if>
		$("#registerOrg").click(function(e) {
		    if (! confirm("Are you sure you want to register this organisation with GBIF?")) {
				e.preventDefault();
		    }
		});
	  </#if>
	});
	
	</script>
</head>


<#include "/WEB-INF/pages/admin/configMenu.ftl">  

-${config.orgNode}-

<@s.form id="providerCfg" method="post">
<fieldset>
    <@s.hidden cssClass="organisationKey" name="organisationKey" value=""/>
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
  <#if config.isOrgRegistered()>
    <@s.submit id="updateOrg" cssClass="button" key="button.update" theme="simple"/>
  <#else>
    <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
    <@s.submit id="registerOrg" cssClass="button" key="button.register" method="register" theme="simple"/>
    <div class="right">
    	<a id="newOrg" href="#">Clear form</a>
    </div>
  </#if>

  </fieldset>

</@s.form>
