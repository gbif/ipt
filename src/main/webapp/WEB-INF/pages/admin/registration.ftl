<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript" src="${baseURL}/js/jquery/jquery-1.11.1.min.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-3.5.1.css">
<script src="${baseURL}/js/select2/select2-3.5.1.min.js" type="text/javascript"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $('#organisation\\.key').select2({placeholder: '<@s.text name="admin.organisation.name.select"/>', width:"375px", allowClear: true});
    });
</script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();

	$('#organisation\\.key').change(function() {
	
		var orgName = $('#organisation\\.key :selected').text();
		$('#organisation\\.name').val(orgName);
		$('#ipt\\.organisationKey').val($('#organisation\\.key :selected').val());

      var emailContent = '<@s.text name="emails.request.organisation.association1"/>';
      emailContent += '<@s.text name="emails.request.organisation.association2"/>';
      emailContent += '<@s.text name="emails.request.organisation.association3"/>';
      emailContent += '<@s.text name="emails.request.organisation.association4"/>';
      emailContent += '<@s.text name="emails.request.organisation.association5"/>';
      emailContent += '<@s.text name="emails.request.organisation.association6"><@s.param>';
      emailContent += $("#organisation\\.key :selected").val();
      emailContent += '</@s.param></@s.text>';
      emailContent += '<@s.text name="emails.request.organisation.association7"/>';

			$('#organisation\\.alias').val(orgName);	
			var url = "<@s.url value='${registryURL}organisation/'/>" + $('#organisation\\.key :selected').val() + ".json";
			$.getJSON(url+"?callback=?",function(data){
				
				$('#organisation\\.primaryContactType').val(data.primaryContactType);
				$('#organisation\\.primaryContactName').val(data.primaryContactName);
				$('#organisation\\.primaryContactEmail').val(data.primaryContactEmail);
				$('#organisation\\.nodeKey').val(data.nodeKey);
				$('#organisation\\.nodeName').val(data.nodeName);
				
				//Create a contact link to prefill an email to request a password from an Organisation
				var contactLink = '<a href=\"mailto:';
				contactLink += data.primaryContactEmail;
				contactLink += '?subject=';
				contactLink += '<@s.text name="emails.request.ipt.registration.subject"><@s.param>';
				contactLink += orgName;
				contactLink += '</@s.param></@s.text>';
				contactLink += '&body=';
				contactLink += emailContent;
				contactLink += '\">'; 
				contactLink += '<@s.text name="emails.request.ipt.registration.footer"/>';
				contactLink += '</a> ';
				contactLink += orgName;
				$('#requestDetails').html(contactLink);
	        });				
	});
	
	<#if validatedBaseURL>
		$('#registrationForm').show();
	</#if>
	$('#validate').click(function() {
		$("#baseURLStatus").html('<img src="${baseURL}/images/small-loader.gif">');
		var url = "https://tools.gbif.org/ws-validurl/?url=${baseURL}/&callback=?";
		$.getJSON(url,function(data){
			if(data.success==true) {
				$("#baseURLStatus").html("<@s.text name="admin.registration.validate.success"/>");
				$('#registrationForm').show(500);
			}else {
				<#if cfg.registryType=="DEVELOPMENT">
					$("#baseURLStatus").html("<@s.text name="admin.registration.validate.failed.development"/>");
					$('#registrationForm').show(500);
				<#else>
					$("#baseURLStatus").html("<@s.text name="admin.registration.validate.failed"/>");
				</#if>
			}
		});	

	});		
});
</script>	
	<title><@s.text name="title"/></title>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/macros/forms.ftl"> 
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="grid_18 suffix_6">
<h1><@s.text name="admin.home.editRegistration"/></h1>

<#-- If the hosting institution already exists, this IP has been registered. Don't present the register form -->

<#if hostingOrganisation?has_content>
    <form id="registration" class="topForm half" action="updateRegistration" method="post">
    	<p><@s.text name="admin.registration.registered1"/><br />
    	<@s.text name="admin.registration.registered2"><@s.param>${hostingOrganisation.name!"???"}</@s.param></@s.text></p>
    	<h3 class="subTitle"><@s.text name="admin.registration.links"/></h3>
    	<ul>
    	 <li><a href="${cfg.portalUrl}/installation/${registeredIpt.key}" target="_blank">${registeredIpt.name!"IPT"}</a></li>
        <#-- in prod mode link goes to /publisher (GBIF Portal), in dev mode link goes to /publisher (GBIF UAT Portal) -->
        <li><a href="${cfg.portalUrl}/publisher/${hostingOrganisation.key}" target="_blank">${hostingOrganisation.name!"Organisation"}</a></li>
      </ul>

		<@input name="registeredIpt.name" i18nkey="admin.ipt.name" type="text" requiredField=true />
		<@text name="registeredIpt.description" i18nkey="admin.ipt.description" requiredField=true />
		
		<#-- For future release. Will replace contact name below
		<@input name="registeredIpt.primaryContactFirstName" i18nkey="admin.ipt.primaryContactFirstName" type="text" />
		<@input name="registeredIpt.primaryContactLastName" i18nkey="admin.ipt.primaryContactLastName" type="text" /> 
		-->

		<@input name="registeredIpt.primaryContactName" i18nkey="admin.ipt.primaryContactName" type="text" requiredField=true />
		<@input name="registeredIpt.primaryContactEmail" i18nkey="admin.ipt.primaryContactEmail" type="text" requiredField=true />
		<div class="buttons">
			<@s.submit cssClass="button" name="update" id="update" key="button.updateRegistration" />
		 	<@s.submit cssClass="button" name="cancel" id="cancel" key="button.cancel"/>
		</div>		
	</form>
<#else>
	
	<#-- BASE URL has not been validated, disable the form -->
	<#if !validatedBaseURL>

		<p><@s.text name="admin.registration.test1"/></p>
		
			<@input name="registration.baseURL" i18nkey="admin.registration.baseURL" type="text" value="${baseURL}" size=70 disabled=true requiredField=true/>
			<@s.submit cssClass="button" name="validate" id="validate" key="admin.registration.validate"/>
		
		<div id="baseURLStatus"></div>
	</#if>
	
	<div id="registrationForm" style="display: none;" >
		<hr/>

		<@s.form cssClass="topForm half" action="registration" method="post" id="registrationForm" namespace="" includeContext="false">
			<@s.fielderror>
		    <@s.param value="%{'organisation.key'}" />
		  </@s.fielderror>
      <img class="infoImg" src="${baseURL}/images/info.gif">
      <div class="info" style="display: none;"><@s.text name="admin.registration.intro"/>&nbsp;<@s.text name="admin.registration.intro2"/></div>
      <@s.select cssClass="e1" id="organisation.key" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" size="15" disabled="false"/>

			<@input name="organisation.password" i18nkey="admin.organisation.password" type="password" help="i18n" maxlength=15 size=18 requiredField=true />
			<div id="requestDetails"></div>
			<@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text" />
			<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canPublish" value="true" help="i18n"/>				
			
			<@s.hidden id="organisation.primaryContactType" name="organisation.primaryContactType" />
			<@s.hidden id="organisation.primaryContactName" name="organisation.primaryContactName" />
			<@s.hidden id="organisation.primaryContactEmail" name="organisation.primaryContactEmail" />
			<@s.hidden id="organisation.nodeKey" name="organisation.nodeKey" />
			<@s.hidden id="organisation.nodeName" name="organisation.nodeName" />
						
			<@input name="ipt.name" i18nkey="admin.ipt.name" type="text" maxlength=255 size=150 requiredField=true />
			<@text name="ipt.description" i18nkey="admin.ipt.description" requiredField=true />

			<#-- For future release. Will replace contact name below
			<@input name="ipt.primaryContactFirstName" i18nkey="admin.ipt.primaryContactFirstName" type="text" />
			<@input name="ipt.primaryContactLastName" i18nkey="admin.ipt.primaryContactLastName" type="text" />
			-->			
			<@input name="ipt.primaryContactName" i18nkey="admin.ipt.primaryContactName" type="text" maxlength=255 requiredField=true />
			<@input name="ipt.primaryContactEmail" i18nkey="admin.ipt.primaryContactEmail" type="text" maxlength=254 requiredField=true />
			<@s.hidden id="admin.ipt.primaryContactType" name="ipt.primaryContactType" value="technical"/>
			
			<@input name="ipt.wsPassword" i18nkey="admin.ipt.password" type="password" help="i18n" maxlength=15 size=18 requiredField=true />
			<@s.hidden id="organisation.name" name="organisation.name" />
			<@s.hidden id="ipt.organisationKey" name="ipt.organisationKey" />
		   <div class="buttons">
		 	<@s.submit cssClass="button" name="save" id="save" key="button.save"/>
		 	<@s.submit cssClass="button" name="cancel" id="cancel" key="button.cancel"/>
		  </div>	  
		</@s.form>
	</div>
</#if>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>