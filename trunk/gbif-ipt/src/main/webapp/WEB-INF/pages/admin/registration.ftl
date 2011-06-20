<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript">
$(document).ready(function(){
	initHelp();

	$('#organisation\\.key').change(function() {
	
		var orgName = $('#organisation\\.key :selected').text();
		$('#organisation\\.name').val(orgName);
		$('#ipt\\.organisationKey').val($('#organisation\\.key :selected').val());
		
		var emailContent = '<@s.text name="emails.request.ipt.registration1"/>';
		emailContent += '<@s.text name="emails.request.ipt.registration2"/>';
		emailContent += '<@s.text name="emails.request.ipt.registration3"/>';
		emailContent += '<@s.text name="emails.request.ipt.registration4"/>';
		emailContent += '<@s.text name="emails.request.ipt.registration5"><@s.param>'
		emailContent += $("#organisation\\.key :selected").val();
		emailContent += '</@s.param></@s.text>';
		emailContent += '<@s.text name="emails.request.ipt.registration6"/>';
	
			$('#organisation\\.name').val(orgName);	
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
		var url = "http://tools.gbif.org/ws-validurl/?url=${baseURL}/&callback=?";
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
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.home.editRegistration"/></h1>

<#-- If the hosting institution already exists, this IP has been registered. Don't present the register form -->

<#if hostingOrganisation?exists>
	<p><@s.text name="admin.registration.registered1"/></p>
	<h3><@s.text name="admin.registration.registered2"><@s.param>${hostingOrganisation.name!"???"}</@s.param></@s.text></h3>
	<p><@s.text name="admin.registration.links"/></p>
	<ul>
	 <li><a href="${cfg.registryUrl}/browse/agent?uuid=${registeredIpt.key}">${registeredIpt.name!"IPT"}</a></li>
	 <li><a href="${cfg.registryUrl}/browse/agent?uuid=${hostingOrganisation.key}">${hostingOrganisation.name!"Organisation"}</a></li>
	</ul>
	<p><@s.text name="admin.registration.registered3"/></p>
<#else>
	<#include "/WEB-INF/pages/macros/forms.ftl"> 
	
	<#-- BASE URL has not been validated, disable the form -->
	<#if !validatedBaseURL>

		<p><@s.text name="admin.registration.test1"/></p>
		
			<@input name="registration.baseURL" i18nkey="admin.registration.baseURL" type="text" value="${baseURL}" size=70 disabled=true/>
			<@s.submit cssClass="button" name="validate" id="validate" key="admin.registration.validate"/>
		
		<div id="baseURLStatus"></div>
	</#if>
	
	<div id="registrationForm" style="display: none;" >
		<hr/>
		<p><@s.text name="admin.registration.intro"/></p>
		<p><@s.text name="admin.registration.intro2"/></p>
		
		<@s.form cssClass="topForm half" action="registration" method="post" id="registrationForm" >
		
			<@label i18nkey="admin.organisation.key">
			<@s.select id="organisation.key" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" size="15" disabled="false"/>
			</@label>
		
			<@input name="organisation.password" i18nkey="admin.organisation.password" type="password" help="i18n"/>
			<div id="requestDetails"></div>
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
			
			<@input name="ipt.wsPassword" i18nkey="admin.ipt.password" type="password" help="i18n"/>
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
</#escape>