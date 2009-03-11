<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="default"/>
    <meta name="heading" content="<@s.text name='config.heading'/>"/>
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
		$('#orgNodeName').autocomplete(data, {
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
		$('#orgTitle').autocomplete(data, {
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
		});
	}
	function udpateOrg(data){
		$(".organisationKey").val(data.key);
		$("#orgTitle").val(data.name);
		$("#orgNode").val(data.endorsingNodeKey);
		if (data.endorsingNodeName!=null){
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
		$("#orgTitle").addClass("external");
	  <#else>
	  	<#-- the IPT is not registered. Provide autocompletes for node & org selection -->
		$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}.json'/>", udpateOrgList);        
		$("#orgTitle").result(function(event, data, formatted) {
			udpateOrg(data);
		});
		$("#newOrg").click(function(e) {
			e.preventDefault(); 
			$(".organisationKey").val("");
			$(".external").val("").removeAttr("readonly");
			$("#orgPassword").val("").attr("readonly","readonly");
			$("#orgTitle").unbind().val("");
			$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryNodeUrl}.json'/>", udpateNodeList);        
			$("#orgNodeName").result(function(event, data, formatted) {
				$("#orgNode").val(data.key);
				$("#orgNodeName").val(data.name);
			});
			alert("When you register the IPT, a new organisation will also be created and your selected GBIF node will be asked for endorsement.");
			$("#registerOrg").show();
		});
	  </#if>
	  <#if config.isOrgRegistered()>
		$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/${config.org.uddiID!}.json'/>", udpateOrg);
	  <#else>        
		$(".external").attr("readonly","readonly");
		$("#registerOrg").hide().click(function(e) {
		    if (! confirm("Are you sure you want to register this organisation with GBIF?")) {
				e.preventDefault();
		    }
		});
	  </#if>
	});
	
	</script>
</head>


<#include "/WEB-INF/pages/admin/configMenu.ftl">  



<@s.form id="providerCfg" method="post">
<h2 class="modifiedh2"><@s.text name="config.registry"/></h2>
<fieldset>
    <@s.hidden cssClass="organisationKey" name="organisationKey" value=""/>
	<@s.textfield id="orgTitle" key="config.org.title" required="true" cssClass="text xlarge"/>
    <div>
        <div class="leftxhalf">
			<@s.textfield key="config.org.uddi" name="config.gibts.nicht" value="${config.org.uddiID!organisationKey!'Not registered with GBIF'}" readonly="true" cssClass="text large organisationKey"/>
        </div>
        <div class="left">
			<@s.textfield id="orgNodeName" key="config.orgNodeName" required="true" cssClass="text medium external"/>
		    <@s.hidden id="orgNodeKey" key="config.orgNode" value=""/>
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

  <#if config.isOrgRegistered()>
    <@s.submit id="updateOrg" cssClass="button" key="button.update" theme="simple"/>
  <#else>
	<div id="newActions">
		<a id="newOrg" href="#"><@s.text name='config.newOrganisation'/></a>
	</div>
    <@s.submit id="registerOrg" cssClass="button" key="button.registerOrg" theme="simple"/>
  </#if>

  </fieldset>

</@s.form>
