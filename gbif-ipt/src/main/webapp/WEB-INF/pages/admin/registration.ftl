<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript">
$(document).ready(function(){
	initHelp();

	$('#organisation\\.key').change(function() {
		var url = "<@s.url value='${registryURL}registry/organisation/'/>" + $('#organisation\\.key :selected').val() + ".json";
		$.getJSON(url+"?callback=?",function(data){
			
			$('#organisation\\.primaryContactType').val(data.primaryContactType);
			$('#organisation\\.primaryContactName').val(data.primaryContactName);
			$('#organisation\\.primaryContactEmail').val(data.primaryContactEmail);
			$('#organisation\\.nodeKey').val(data.nodeKey);
			$('#organisation\\.nodeName').val(data.nodeName);
			
			var contactLink = "<a href=\"mailto:" + data.primaryContactEmail 
					+ "?subject=Password request for " + orgName
					+ "&body=" + emailContent  + "\"> "  
					+ "Click here</a> to contact " + orgName;
			$('#requestDetails').html(contactLink);
        	});			
	});
			
	<#if !validatedBaseURL>
		$('#registrationForm').hide();
	</#if>
	$('#validate').click(function() {
		$("#baseURLStatus").html('<img src="${baseURL}/images/small-loader.gif">');
		var url = "<@s.url value='${registryURL}ipt/validate.json?url=${baseURL}'/>";
		$.getJSON(url+"&callback=?",function(data){
			if(data.result==200) {
				$('#registrationForm').show(500);
			}
			else {
				<#if cfg.registryType=="DEVELOPMENT">
					$("#baseURLStatus").html("Not valid, callback failed! Please check your base url. (DEV--> fields are going to be activated for dev purposes)");
					$('#registrationForm').show(500);
				<#else>
					$("#baseURLStatus").html("Not valid, callback failed! Please check your base url. ");
				</#if>
				
			}
		});	

		$('#organisation\\.key').click(function() {
			var orgName = $('#organisation\\.key :selected').text();
			$('#organisation\\.name').val(orgName);
			$('#ipt\\.organisationKey').val($('#organisation\\.key :selected').val());		
		});
	});		
});
</script>	
	<title><@s.text name="title"/></title>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.registration.title"/></h1>

<#-- If the hosting institution already exists, this IP has been registered. Don't present the register form -->

<#if hostingOrganisation?exists>
	<p><@s.text name="admin.registration.registered1"/></p>
	<p><@s.text name="admin.registration.registered2"><@s.param><strong>${hostingOrganisation.name!}</strong></@s.param></@s.text></p>
	<p><@s.text name="admin.registration.registered3"/></p>
<#else>
	<#include "/WEB-INF/pages/macros/forms.ftl"> 
	
	<#-- BASE URL has not been validated, disable the form -->
	<#if !validatedBaseURL>

		<p><@s.text name="admin.registration.test1"/></p>
		<p><@s.text name="admin.registration.test2"/></p>
		<p><@s.text name="admin.registration.test3"/></p>
		
			<@input name="registration.baseURL" i18nkey="admin.registration.baseURL" type="text" value="${baseURL}" size=70 disabled=true/>
			<@s.submit cssClass="button" name="validate" id="validate" key="Validate"/>
		
		<div id="baseURLStatus"></div>
	</#if>
	
	<div id="registrationForm">
		<hr/>
		<p><@s.text name="admin.registration.intro"/></p>
		<p><@s.text name="admin.registration.intro2"/></p>
		
		<@s.form cssClass="topForm half" action="registration" method="post" id="registrationForm" >
			<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" i18nkey="admin.organisation.key" value="" size=15/>  
			<@input name="organisation.password" i18nkey="admin.organisation.password" type="text" help="i18n"/>
			<@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text" />
			<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canHost" value="true" help="i18n"/>	
			
			<@s.hidden id="organisation.primaryContactType" name="organisation.primaryContactType" required="false" />
			<@s.hidden id="organisation.primaryContactName" name="organisation.primaryContactName" required="false" />
			<@s.hidden id="organisation.primaryContactEmail" name="organisation.primaryContactEmail" required="false" />
			<@s.hidden id="organisation.nodeKey" name="organisation.nodeKey" required="false" />
			<@s.hidden id="organisation.nodeName" name="organisation.nodeName" required="false" />			
						
			<@input name="ipt.name" i18nkey="admin.ipt.name" type="text" />
			<@text name="ipt.description" i18nkey="admin.ipt.description" />
			<@input name="ipt.primaryContactName" i18nkey="admin.ipt.primaryContactName" type="text" />
			<@input name="ipt.primaryContactEmail" i18nkey="admin.ipt.primaryContactEmail" type="text" />
			<@s.hidden id="admin.ipt.primaryContactType" name="ipt.primaryContactType" value="technical" />
			
			<@input name="ipt.wsPassword" i18nkey="admin.ipt.password" type="text" help="i18n"/>
			<@s.hidden id="organisation.name" name="organisation.name" />
			<@s.hidden id="ipt.organisationKey" name="ipt.organisationKey" />
		   <div class="buttons">
		 	<@s.submit cssClass="button" name="save" id="save" key="button.save"/>
		 	<@s.submit cssClass="button" name="cancel" id="cancel" key="button.cancel"/>
		  </div>	  
		</@s.form>
	</div>
</#if>
<#include "/WEB-INF/pages/inc/footer.ftl">