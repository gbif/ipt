<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="<@s.text name='config.heading'/>"/>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.core.min.js'/>"></script>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
	<script type="text/javascript" src="<@s.url value='/scripts/organization.json.js'/>"></script>
	<link rel="stylesheet" type="text/css" href="<@s.url value='/scripts/jquery.autocomplete.css'/>" />
	<script>
	function udpateOrg(data){
		$(".organisationKey").val(data.key);
		$("#orgTitle").val(data.name);
		$("#orgName").val(data.primaryContactName);
		$("#orgEmail").val(data.primaryContactEmail);
		$("#orgHomepage").val(data.homepageURL);
		$("#orgLatitude").val("");
		$("#orgLongitude").val("");
		$("#orgDescription").val(data.description);
	}
	$(document).ready(function(){
		//'${registryOrgUrl}'
		$('#orgLookupQ').autocomplete(orgs, {
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
		$("#orgLookupQ").result(function(event, data, formatted) {
			udpateOrg(data);
		});
		$(".external").attr("readonly","readonly");
		$("#newOrg").click(function(e) {
			e.preventDefault(); 
			$("#organisationKey").val("new");
			$(".external").val("");
			$(".external").removeAttr("readonly");
			alert("Your data will be registered as a new organisation with GBIF and send to your selected GBIF node for endorsement. Please enter data carefully.");
		});
		$("#editOrg").click(function(e) {
			e.preventDefault(); 
			$(".external").removeAttr("readonly");
		});
		$("#refreshOrg").click(function(e) {
			e.preventDefault();
			$.getJSON("${registryOrgUrl}/${config.org.uddiID!-1}",udpateOrg);        			 
		});
	  <#if !config.org.uddiID??>
	  	$("#newActions").show();
	  <#else>
	  	$("#newActions").hide();
	  </#if>
	});
	
	</script>
	<style>
		div.googlemap {
			float: left;
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

<@s.form id="providerCfg" action="saveConfig" method="post">


<h2 class="modifiedh2"><@s.text name="config.registry"/></h2>
<fieldset>
  <#if config.org.uddiID??>
	<div class="right">
		<input type="submit" class="button" id="editOrg" value="<@s.text name='config.editOrganisation'/>" theme="simple" />
	</div>
	<div class="right">
		<input type="submit" class="button" id="refreshOrg" value="<@s.text name='config.refreshOrganisation'/>" theme="simple" />
	</div>
  </#if>
	<div id="newActions">
        <div class="left">
			<@s.textfield id="orgLookupQ" key="config.orgLookup" value="" cssClass="text large"/>
        </div>
        <div class="right">
			<input type="submit" class="button" id="newOrg" value="<@s.text name='config.newOrganisation'/>" theme="simple" />
        </div>	
	</div>
	
	<div class="break"></div>
    <@s.hidden cssClass="organisationKey" name="organisationKey" value=""/>
    <div>
        <div class="left">
			<@s.textfield key="config.org.uddi" value="${config.org.uddiID!'Not registered with GBIF'}" readonly="true" cssClass="text large organisationKey"/>
        </div>
        <div>
			<@s.textfield key="config.orgPassword" required="true" cssClass="text large"/>
        </div>
	</div>
	<div>
		<@s.textfield id="orgTitle" key="config.org.title" required="true" cssClass="text xlarge external"/>
	</div>
    <div>
        <div class="left">
			<@s.textfield id="orgName" key="config.org.contactName" required="true" cssClass="text large external"/>
        </div>
        <div>
			<@s.textfield id="orgEmail" key="config.org.contactEmail" required="true" cssClass="text large external"/>
        </div>
	</div>
	<div class="left">    
		<div>
			<@s.textfield id="orgHomepage" key="config.org.link" required="false" cssClass="text large external"/>
		</div>
	    <div>
	        <div class="leftMedium">
				<@s.textfield id="orgLatitude" key="config.org.location.latitude" required="false" cssClass="text medium external"/>
	        </div>
	        <div>
				<@s.textfield id="orgLongitude" key="config.org.location.longitude" required="false" cssClass="text medium external"/>
	        </div>
	    </div>	    
	</div>
    <div class="googlemap">
		<#if (config.org.location)?? && cfg.googleMapsApiKey??>
			<a href="http://maps.google.de/maps?f=s&ie=UTF8&ll=${(config.org.location.latitude!0)?c},${(config.org.location.longitude!0)?c}&t=h&z=15"><img src="http://maps.google.com/staticmap?center=${(config.org.location.latitude!0)?c},${(config.org.location.longitude!0)?c}&zoom=5&size=325x95&key=${cfg.googleMapsApiKey}" /></a>	
		</#if>
    </div>
	<div style="clear:both">
		<@s.textarea id="orgDescription" key="config.org.description" cssClass="text xlarge external"/>
	</div>
  </fieldset>
<div class="horizontal_dotted_line_xlarge_soft_foo" ></div>

<h2><@s.text name="config.metadata"/></h2>
<fieldset>
	<@s.textfield key="config.ipt.uddi" value='${config.ipt.uddiID!"Not registered with GBIF"}' readonly="true" cssClass="text xlarge"/>
	<@s.textfield key="config.ipt.title" required="true" cssClass="text xlarge"/>
    <div>
        <div class="left">
			<@s.textfield key="config.ipt.contactName" required="true" cssClass="text large"/>
        </div>
        <div>
			<@s.textfield key="config.ipt.contactEmail" required="true" cssClass="text large"/>
        </div>
	</div>
	<div class="left">    
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
    <div class="googlemap">
		<#if (config.ipt.location)?? && cfg.googleMapsApiKey??>
			<a href="http://maps.google.de/maps?f=s&ie=UTF8&ll=${(config.ipt.location.latitude!0)?c},${((config.ipt.location.longitude)!0)?c}&t=h&z=15"><img src="http://maps.google.com/staticmap?center=${(config.ipt.location.latitude!0)?c},${((config.ipt.location.longitude)!0)?c}&zoom=5&size=325x95&key=${cfg.googleMapsApiKey}" /></a>	
		</#if>
    </div>
	<div style="clear:both">
		<@s.textarea key="config.ipt.description" cssClass="text xlarge"/>
	</div>
	<@s.textfield key="config.ipt.descriptionImage" required="false" cssClass="text xlarge"/>
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
	<div class="right">
		<@s.submit cssClass="button" name="updateGeoserver" method="updateGeoserver" key="button.geoserver" theme="simple"/>
	</div>
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
</@s.form>
