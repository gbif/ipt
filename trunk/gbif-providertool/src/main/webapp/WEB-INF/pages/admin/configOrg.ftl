<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="default"/>
    <meta name="heading" content="<@s.text name='config.heading'/>"/>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.core.min.js'/>"></script>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
	<link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />
	<script>
	var orgs;
	// first get list of all organisations, then attach automplete event based on this list
	// could do autocomplete via server call, but gets into problems sometimes. This is how it would be called:
	// ${config.getBaseUrl()}/ajax/proxy.do?uri=${registryOrgUrl}.json?nix=1
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
	  <#if config.ipt.uddiID??>
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
	  <#if config.org.uddiID??>
		$.getJSON("<@s.url value='/ajax/proxy.do?uri=${registryOrgUrl}/${config.org.uddiID!}.json'/>", udpateOrg);        
	  </#if>
		$(".external").attr("readonly","readonly");
		$("#registerOrg").hide();
		$("#updateOrg").hide();
		$("#unlockUpdateOrg").click(function(e) {
			e.preventDefault(); 
			$(".external").removeAttr("readonly");
			$("#updateOrg").show();
			$("#unlockUpdateOrg").hide();
		});
		$("#registerOrg").click(function(e) {
		    if (! confirm("Are you sure you want to register this organisation with GBIF?")) {
				e.preventDefault();
		    }
		});
		$("#registerIpt").click(function(e) {
		    if (! confirm("Are you sure you want to register this IPT with GBIF? Once you registered as part of an organisation you cannot change this through the IPT but will have to get in touch with GBIF personally.")) {
				e.preventDefault();
		    }
		});	
	});
	
	</script>
	<style>
		img#googlemap {
			padding-top: 15px;
			padding-left:15px
		}	
		input[readonly] {
			background: #eaeaea;
		}
		textarea[readonly] {
			background: #eaeaea;
		}
	</style>	    
</head>


<content tag="contextmenu">
  <div id="actions">
	<label>Configuration</label>
	<ul class="plain">								
		<li><a href="<@s.url action='config'/>"> <@s.text name="config.registry"/> </a></li>
		<li><a href="<@s.url action='config'/>"> <@s.text name="config.metadata"/> </a></li>
		<li><a href="<@s.url action='config'/>"> <@s.text name="config.settings"/> </a></li>
	</ul>
  </div>
</content>


<@s.form id="providerCfg" action="saveConfig" method="post">
<h2 class="modifiedh2"><@s.text name="config.registry"/></h2>
<fieldset>
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
    <@s.hidden cssClass="organisationKey" name="organisationKey" value=""/>

  <#if !(config.ipt.uddiID??)>
	<div id="newActions">
		<a id="newOrg" href="#"><@s.text name='config.newOrganisation'/></a>
	</div>
    <@s.submit id="registerOrg" cssClass="button" key="button.registerOrg" method="registerOrg" theme="simple"/>
  <#else>
	<div>
		<a id="unlockUpdateOrg" href="#"><@s.text name='config.updateOrganisation'/></a>
	</div>
    <@s.submit id="updateOrg" cssClass="button" key="button.update" method="updateOrg" theme="simple"/>
  </#if>

  </fieldset>
  
<div class="horizontal_dotted_line_xlarge_soft_foo" ></div>



<h2><@s.text name="config.metadata"/></h2>
<fieldset>
	<@s.textfield key="config.ipt.uddi" value='${config.ipt.uddiID!"Not registered with GBIF"}' readonly="true" cssClass="text xlarge"/>
	<@s.textfield key="config.ipt.title" required="true" cssClass="text xlarge"/>
    <div>
        <div class="leftxhalf">
			<@s.textfield key="config.ipt.contactName" required="true" cssClass="text large"/>
        </div>
        <div class="leftxhalf">
			<@s.textfield key="config.ipt.contactEmail" required="true" cssClass="text large"/>
        </div>
	</div>
	<div>
	  <div class="leftxhalf">    
		<div>
			<@s.textfield key="config.ipt.link" required="true" cssClass="text large"/>
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
		<@s.textarea key="config.ipt.description" cssClass="text xlarge"/>
	</div>
	<@s.textfield key="config.ipt.descriptionImage" required="false" cssClass="text xlarge"/>
	
  <#if !config.ipt.uddiID??>
    <@s.submit cssClass="button" id="registerIpt" key="button.registerIpt" method="registerIpt" theme="simple"/>
  </#if>
  </fieldset>
<div class="horizontal_dotted_line_xlarge_soft_foo" ></div>


<h2 class="modifiedh2"><@s.text name="config.settings"/></h2>
  <fieldset>
	<@s.textfield key="config.baseUrl" required="true" cssClass="text xlarge"/>
	<@s.textfield key="config.dataDir" disabled="true" cssClass="text xlarge"/>
	<@s.submit cssClass="button" name="updateGeoserver" method="updateGeoserver" key="button.geoserver" theme="simple"/>
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
