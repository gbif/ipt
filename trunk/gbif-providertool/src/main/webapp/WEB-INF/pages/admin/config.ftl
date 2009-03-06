<head>
    <title><@s.text name='config.heading'/></title>
    <meta name="menu" content="AdminMenu"/>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="<@s.text name='config.heading'/>"/>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery/ui.core.min.js'/>"></script>
	<script type="text/javascript" src="<@s.url value='/scripts/jquery.autocomplete.min.js'/>"></script>
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
		$(".external").attr("readonly","readonly");
	  <#if config.org.uddiID??>
		$.getJSON("${registryOrgUrl}/${config.org.uddiID!-1}",udpateOrg);        
	  	$("#newActions").hide();
	  </#if>
					 
		$('#orgLookupQ').autocomplete('${registryOrgUrl}', {
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
		$("#newOrg").click(function(e) {
			e.preventDefault(); 
			$("#organisationKey").val("new");
			$(".external").val("");
			alert("When you register the IPT, a new organisation will also be created and your selected GBIF node will be asked for endorsement.");
			$(".external").removeAttr("readonly");
		});
		$("#updateOrg").click(function(e) {
			e.preventDefault(); 
			$(".external").removeAttr("readonly");
		});
		$("#changeOrg").click(function(e) {
			e.preventDefault();
			$("#newActions").show();
		});
		$("#registerIpt").click(function(e) {
		    if (! confirm("Are you sure you want to register this IPT with GBIF?")) {
				e.preventDefault();
		    }
		});
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
	<div id="newActions">
		<@s.textfield id="orgLookupQ" key="config.orgLookup" value="" cssClass="text large"/>
		<a id="newOrg" href="#"><@s.text name='config.newOrganisation'/></a>
	</div>
    <@s.hidden cssClass="organisationKey" name="organisationKey" value=""/>
	
    <div>
        <div class="left">
			<@s.textfield key="config.org.uddi" value="${config.org.uddiID!'Not registered with GBIF'}" readonly="true" cssClass="text large organisationKey"/>
        </div>
        <div>
			<@s.textfield key="config.orgPassword" required="true" cssClass="text large"/>
        </div>
	</div>
	<@s.textfield id="orgTitle" key="config.org.title" required="true" cssClass="text xlarge external"/>
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
		<#else>	
			<img src="<@s.url value='/images/default_image_map.gif'/>"/>
		</#if>
    </div>
	<div style="clear:both">
		<@s.textarea id="orgDescription" key="config.org.description" cssClass="text xlarge external"/>
	</div>
  <#if config.org.uddiID??>
	<div>
		<a id="changeOrg" href="#"><@s.text name='config.changeOrganisation'/></a>
		<a id="updateOrg" href="#"><@s.text name='config.updateOrganisation'/></a>
	</div>
  </#if>
  <@s.submit cssClass="button" name="save" key="button.save" theme="simple"/>
  <@s.submit cssClass="button" name="cancel" key="button.cancel" theme="simple"/>
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
		<#else>	
			<img src="<@s.url value='/images/default_image_map.gif'/>"/>
		</#if>
    </div>
	<div style="clear:both">
		<@s.textarea key="config.ipt.description" cssClass="text xlarge"/>
	</div>
	<@s.textfield key="config.ipt.descriptionImage" required="false" cssClass="text xlarge"/>
	
    <@s.submit cssClass="button" id="registerIpt" key="button.registerIpt" method="register" theme="simple"/>
  </fieldset>
<div class="horizontal_dotted_line_xlarge_soft_foo" ></div>


<h2 class="modifiedh2"><@s.text name="config.settings"/></h2>
  <fieldset>
	<@s.textfield key="config.dataDir" disabled="true" cssClass="text xlarge"/>
	<@s.submit cssClass="button" name="updateGeoserver" method="updateGeoserver" key="button.geoserver" theme="simple"/>
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
</@s.form>
