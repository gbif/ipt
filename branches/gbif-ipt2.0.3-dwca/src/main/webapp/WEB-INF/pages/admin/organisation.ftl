<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	
	var emailContent2 = '<@s.text name="emails.adding.organisation.association1"/>';
	emailContent2 += '<@s.text name="emails.adding.organisation.association2"/>';
	emailContent2 += '<@s.text name="emails.adding.organisation.association3"/>';
	emailContent2 += '<@s.text name="emails.adding.organisation.association4"/>';
	emailContent2 += '<@s.text name="emails.adding.organisation.association5"><@s.param>'
	emailContent2 += "${currentUser.firstname}";
	emailContent2 += '</@s.param></@s.text>';
	emailContent2 += '<@s.text name="emails.adding.organisation.association6"><@s.param>'
	emailContent2 += "${currentUser.email}";
	emailContent2 += '</@s.param></@s.text>';
	emailContent2 += '<@s.text name="emails.adding.organisation.association7"/>';
	emailContent2 += '<@s.text name="emails.adding.organisation.association8"/>';
	emailContent2 += "${currentUser.firstname}";
	
	//Email template for adding an associated organization
	var contactLink = '<a href=\"mailto:helpdesk@gbif.org';
	contactLink += '?subject=';
	contactLink += '<@s.text name="emails.adding.organisation.association.subject"></@s.text>';
	contactLink += '&body=';
	contactLink += emailContent2;
	contactLink += '\">'; 
	contactLink += '<@s.text name="admin.organisation.contact.helpdesk"/>';  
	contactLink += '</a> '; 
	$('#contactHelpdesk').html(contactLink);	
  
    $('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});

	$('#organisation\\.key').change(function() {
		var orgName = $('#organisation\\.key :selected').text();
	
	var emailContent = '<@s.text name="emails.request.organisation.association1"/>';
	emailContent += '<@s.text name="emails.request.organisation.association2"/>';
	emailContent += '<@s.text name="emails.request.organisation.association3"/>';
	emailContent += '<@s.text name="emails.request.organisation.association4"/>';
	emailContent += '<@s.text name="emails.request.organisation.association5"/>';
	emailContent += '<@s.text name="emails.request.organisation.association6"><@s.param>'
	emailContent += $("#organisation\\.key :selected").val();
	emailContent += '</@s.param></@s.text>';
	emailContent += '<@s.text name="emails.request.organisation.association7"/>';

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

<@s.form id="organisationsForm" cssClass="topForm half" action="organisation.do" method="post">
	
	<#if id?has_content>
<h1><@s.text name="admin.organisation.title"/></h1>
		<@input name="organisation.name" i18nkey="admin.organisation.name" type="text" disabled=true/>
		<@s.hidden name="organisation.key" id="organisation.key" required="true" />
		<@s.hidden name="id" id="id" required="true" />
	<#else>
<h1><@s.text name="admin.organisation.add.title"/></h1>
<p><@s.text name="admin.organisation.add.intro"/> <a id='contactHelpdesk' href=""><@s.text name="admin.organisation.contact.helpdesk"/></a></p>
<p><@s.text name="admin.organisation.add.intro2"/></p>
		<@s.hidden id="organisation.name" name="organisation.name" required="true" />
		<@s.hidden id="organisation.primaryContactType" name="organisation.primaryContactType" required="false" />
		<@s.hidden id="organisation.primaryContactName" name="organisation.primaryContactName" required="false" />
		<@s.hidden id="organisation.primaryContactEmail" name="organisation.primaryContactEmail" required="false" />
		<@s.hidden id="organisation.nodeKey" name="organisation.nodeKey" required="false" />
		<@s.hidden id="organisation.nodeName" name="organisation.nodeName" required="false" />
		<@s.select id="organisation.key" name="organisation.key" list="organisations" listKey="key" listValue="name" value="organisation.key" size="15" disabled="false"/>		
	</#if>		  
	<@input name="organisation.password" i18nkey="admin.organisation.password" type="password"/>
	<div id="requestDetails"></div>
	
	<@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text"/>
	<#if id?has_content>
		<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canHost" value="organisation.canHost"/>
	<#else>
		<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canHost" value="true"/>
	</#if>
   <div class="buttons">
 	<@s.submit name="save" key="button.save" cssClass="confirm"/>
 	<#if id?has_content>
	<@s.submit name="delete" key="button.delete" cssClass="confirm"/>
	</#if>		   	
 	<@s.submit name="cancel" key="button.cancel" cssClass="button"/>
  </div>
</@s.form>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>