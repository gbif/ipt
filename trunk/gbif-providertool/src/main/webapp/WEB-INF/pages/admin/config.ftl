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
	$(document).ready(function(){
		//'/scripts/organization.json'
		$('#orgLookupQ').autocomplete(orgs, {
			width:300, 
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
			$(".organisationKey").val(data.key);
		});
		$(".external").attr("readonly","readonly");
		$("#newOrg").click(function(e) {
			e.preventDefault(); 
			$("#organisationKey").val("new");
			$(".external").removeAttr("readonly");
			alert("Your data will be registered as a new organisation with GBIF and send to your selected GBIF node for endorsement. Please enter data carefully.");
		});
		$("#editOrg").click(function(e) {
			e.preventDefault(); 
			$(".external").removeAttr("readonly");
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
  <#if (config.org.uddiID)??>
	<div>
        <div class="left">
			<@s.textfield id="orgLookupQ" key="config.orgLookup" value="" cssClass="text large"/>
        </div>
        <div class="right">
			<input type="submit" class="button" id="newOrg" value="<@s.text name='config.newOrganisation'/>" theme="simple" />
        </div>	
	</div>
  <#else>
		<div class="right">
			<input type="submit" class="button" id="editOrg" value="<@s.text name='config.editOrganisation'/>" theme="simple" />
		</div>
  </#if>
	
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
		<@s.textfield key="config.org.title" required="true" cssClass="text xlarge external"/>
	</div>
    <div>
        <div class="left">
			<@s.textfield key="config.org.contactName" required="true" cssClass="text large external"/>
        </div>
        <div>
			<@s.textfield key="config.org.contactEmail" required="true" cssClass="text large external"/>
        </div>
	</div>
	<div class="left">    
		<div>
			<@s.textfield key="config.org.link" required="false" cssClass="text large external"/>
		</div>
	    <div>
	        <div class="leftMedium">
				<@s.textfield key="config.org.location.latitude" required="false" cssClass="text medium external"/>
	        </div>
	        <div>
				<@s.textfield key="config.org.location.longitude" required="false" cssClass="text medium external"/>
	        </div>
	    </div>	    
	</div>
    <div class="googlemap">
		<#if (config.org.location)?? && cfg.googleMapsApiKey??>
			<a href="http://maps.google.de/maps?f=s&ie=UTF8&ll=${config.org.location.latitude?c!0},${config.org.location.longitude?c!0}&t=h&z=15"><img src="http://maps.google.com/staticmap?center=${config.org.location.latitude?c!0},${config.org.location.longitude?c!0}&zoom=5&size=325x95&key=${cfg.googleMapsApiKey}" /></a>	
		</#if>
    </div>
	<div style="clear:both">
		<@s.textarea key="config.org.description" cssClass="text xlarge external"/>
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
			<a href="http://maps.google.de/maps?f=s&ie=UTF8&ll=${config.ipt.location.latitude?c!0},${config.ipt.location.longitude?c!0}&t=h&z=15"><img src="http://maps.google.com/staticmap?center=${config.ipt.location.latitude?c!0},${config.ipt.location.longitude?c!0}&zoom=5&size=325x95&key=${cfg.googleMapsApiKey}" /></a>	
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
