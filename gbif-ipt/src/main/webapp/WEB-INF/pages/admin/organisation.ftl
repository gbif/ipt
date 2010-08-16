<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});

	$('#organisation\\.key').change(function() {
		var orgName = $('#organisation\\.key :selected').text();
	
	var emailContent = "Dear sir/madam,%0d%0d" 
		+ "I am trying to install an Integrated Publishing Toolkit (IPT) which is going to be hosted under your institution/organisation.%0d"
		+ "To continue with the installation, I will need to kindly ask you to provide me with your organisation's password as this is "
		+ "needed to complete the process%0d" 
		+ "In case you don't know this information, you can open this link into your browser to receive this information%0d%0d"
		+ "http://gbrds.gbif.org/registry/organisation/" + $('#organisation\\.key :selected').val() + "?op=password%0d%0d"  
		+ "Thank you for your attention.";
		
		
		$('#organisation\\.name').val(orgName);	
		$('#organisation\\.alias').val(orgName);	
		//TODO: URL is hardwired to the dev registry. This must be replaced.
		//TODO: Internationalise if possible the email's subject
		var url = "<@s.url value='http://gbrdsdev.gbif.org/registry/organisation/'/>" + $('#organisation\\.key :selected').val() + ".json";
		$.getJSON(url+"?callback=?",function(data){
			var contactLink = "<a href=\"mailto:" + data.primaryContactEmail 
					+ "?subject=Password request for " + orgName
					+ "&body=" + emailContent  + "\"> "  
					+ "Click here</a> to contact " + orgName;
			$('#requestDetails').html(contactLink);
        	});				
	});
});
</script>	
<title><@s.text name="title"/></title>


<#include "/WEB-INF/pages/inc/menu.ftl">


<h1><@s.text name="admin.organisation.title"/></h1>

<p><@s.text name="admin.organisation.intro"/></p>
<p><@s.text name="admin.organisation.intro2"/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"> 


<@s.form id="organisationsForm" cssClass="topForm half" action="organisation.do" method="post">
	
	<#if id?has_content>
		<@input name="organisation.name" i18nkey="admin.organisation.name" type="text" disabled=true/>
		<@s.hidden name="organisation.key" id="organisation.key" required="true" />
		<@s.hidden name="id" id="id" required="true" />
	<#else>
		<@s.hidden id="organisation.name" name="organisation.name" required="true" />
		<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" i18nkey="admin.organisation.key" size=15 />
	</#if>		  
	<@input name="organisation.password" i18nkey="admin.organisation.password" type="text"/>
	<div id="requestDetails"></div>
	
	<@input name="organisation.alias" i18nkey="admin.organisation.alias" type="text"/>
	<#if id?has_content>
		<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canHost" value="organisation.canHost"/>
	<#else>
		<@checkbox name="organisation.canHost" i18nkey="admin.organisation.canHost" value="true"/>
	</#if>
   <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<#if id?has_content>
	<@s.submit name="delete" key="button.delete" cssClass="confirm"/>
	</#if>		   	
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</@s.form>
<#include "/WEB-INF/pages/inc/footer.ftl">
