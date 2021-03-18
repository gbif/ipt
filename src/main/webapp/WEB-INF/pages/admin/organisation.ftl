<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-3.5.1.css">
<script src="${baseURL}/js/select2/select2-3.5.1.min.js" type="text/javascript"></script>
<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();

  $('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});

  $('select#organisation\\.key').select2({placeholder: '<@s.text name="admin.organisation.name.select"/>', width:"375px", allowClear: true});

	$('#organisation\\.key').change(function() {

  var orgName = $('#organisation\\.key :selected').text();
  $('#organisation\\.name').val(orgName);
  $('#organisation\\.alias').val(orgName);

	var emailContent = '<@s.text name="emails.request.organisation.association1"/>';
	emailContent += '<@s.text name="emails.request.organisation.association2"/>';
	emailContent += '<@s.text name="emails.request.organisation.association3"/>';
	emailContent += '<@s.text name="emails.request.organisation.association4"/>';
	emailContent += '<@s.text name="emails.request.organisation.association5"/>';
	emailContent += '<@s.text name="emails.request.organisation.association6"><@s.param>';
	emailContent += $("#organisation\\.key :selected").val();
	emailContent += '</@s.param></@s.text>';
	emailContent += '<@s.text name="emails.request.organisation.association7"/>';

		var url = "<@s.url value='${registryURL}organisation/'/>" + $('#organisation\\.key :selected').val() + ".json";
		$.getJSON(url,function(data){

			$('#organisation\\.primaryContactType').val(data.primaryContactType);
			$('#organisation\\.primaryContactName').val(data.primaryContactName);
			$('#organisation\\.primaryContactEmail').val(data.primaryContactEmail);
			$('#organisation\\.nodeKey').val(data.nodeKey);
			$('#organisation\\.nodeName').val(data.nodeName);
			
			//Create a contact link to prefill an email to request a password from an Organisation
			var contactLink = '<a href=\"mailto:';
			contactLink += data.primaryContactEmail;
			contactLink += '?subject=';
			contactLink += '<@s.text name="emails.request.organisation.association.subject"><@s.param>';
			contactLink += orgName;
			contactLink += '</@s.param></@s.text>';
			contactLink += '&body=';
			contactLink += emailContent;
			contactLink += '\">'; 
			contactLink += '<@s.text name="emails.request.organisation.association.footer"/>';
			contactLink += '</a> ';
			contactLink += orgName;
			$('#requestDetails').html(contactLink);
        	});				
	});
});
</script>
<title><@s.text name="title"/></title>

 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"> 
<div class="grid_18 suffix_6">

<@s.form id="organisationsForm" cssClass="topForm half" action="organisation.do" method="post" namespace="" includeContext="false">

	<#if id?has_content>
    <h1><@s.text name="admin.organisation.title"/></h1>
		<@input name="organisation.name" i18nkey="admin.organisation.name" type="text" disabled=true/>
		<@s.hidden name="organisation.key" id="organisation.key" required="true" />
		<@s.hidden name="id" id="id" required="true" />
    <!-- preserve other fields not edited -->
    <@s.hidden name="organisation.nodeName" id="organisation.nodeName" />
    <@s.hidden name="organisation.nodeKey" id="organisation.nodeKey" />
    <@s.hidden name="organisation.primaryContactPhone" id="organisation.primaryContactPhone" />
    <@s.hidden name="organisation.primaryContactEmail" id="organisation.primaryContactEmail" />
    <@s.hidden name="organisation.primaryContactAddress" id="organisation.primaryContactAddress" />
    <@s.hidden name="organisation.primaryContactDescription" id="organisation.primaryContactDescription" />
    <@s.hidden name="organisation.primaryContactName" id="organisation.primaryContactName" />
    <@s.hidden name="organisation.primaryContactType" id="organisation.primaryContactType" />
    <@s.hidden name="organisation.homepageURL" id="organisation.homepageURL" />
    <@s.hidden name="organisation.description" id="organisation.description" />
	<#else>
    <h1><@s.text name="admin.organisation.add.title"/></h1>
		<@s.hidden id="organisation.name" name="organisation.name" required="true" />
    <!-- preserve other fields not edited -->
    <@s.hidden name="organisation.nodeName" id="organisation.nodeName" />
    <@s.hidden name="organisation.nodeKey" id="organisation.nodeKey" />
    <@s.hidden name="organisation.primaryContactPhone" id="organisation.primaryContactPhone" />
    <@s.hidden name="organisation.primaryContactEmail" id="organisation.primaryContactEmail" />
    <@s.hidden name="organisation.primaryContactAddress" id="organisation.primaryContactAddress" />
    <@s.hidden name="organisation.primaryContactDescription" id="organisation.primaryContactDescription" />
    <@s.hidden name="organisation.primaryContactName" id="organisation.primaryContactName" />
    <@s.hidden name="organisation.primaryContactType" id="organisation.primaryContactType" />
    <@s.hidden name="organisation.homepageURL" id="organisation.homepageURL" />
    <@s.hidden name="organisation.description" id="organisation.description" />
    <img class="infoImg" src="${baseURL}/images/info.gif">
    <div class="info" style="display: none;"><@s.text name="admin.registration.intro"/>&nbsp;<@s.text name="admin.organisation.add.intro2"/></div>
    <@s.select id="organisation.key" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" disabled="false"/>
	</#if>		  
	<@input name="organisation.password" i18nkey="admin.organisation.password" type="password"/>
	<div id="requestDetails"></div>
	
	<@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text"/>
	<#if id?has_content>
		<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canPublish" value="organisation.canHost" help="i18n"/>
	<#else>
		<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canPublish" value="true" help="i18n"/>
	</#if>

  <div class="radio">
    <@s.text name="admin.organisation.doiRegistrationAgency"/>
      <img class="infoImg" src="${baseURL}/images/info.gif">
      <div class="info" style="display: none;"><@s.text name="admin.organisation.doiRegistrationAgency.help"/></div>
    <@s.fielderror>
      <@s.param value="%{'organisation.doiRegistrationAgency'}" />
    </@s.fielderror>
    <@s.radio name="organisation.doiRegistrationAgency" list="doiRegistrationAgencies" value="organisation.doiRegistrationAgency" help="i18n" />
  </div>

  <@input name="organisation.agencyAccountUsername" i18nkey="admin.organisation.doiRegistrationAgency.username" help="i18n" type="text"/>
  <@input name="organisation.agencyAccountPassword" i18nkey="admin.organisation.doiRegistrationAgency.password" help="i18n" type="password"/>
  <@input name="organisation.doiPrefix" i18nkey="admin.organisation.doiRegistrationAgency.prefix" help="i18n" type="text"/>
  <@checkbox name="organisation.agencyAccountPrimary" i18nkey="admin.organisation.doiAccount.activated" value="${organisation.agencyAccountPrimary?c}" help="i18n"/>

  <div class="buttons">
 	  <@s.submit name="save" key="button.save" cssClass="button"/>
 	  <#if id?has_content>
	    <@s.submit name="delete" key="button.delete" cssClass="button confirm"/>
	  </#if>
 	  <@s.submit name="cancel" key="button.cancel" cssClass="button"/>
  </div>
</@s.form>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
