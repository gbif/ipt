<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript">
$(document).ready(function(){
	$('#organisation\\.key').click(function() {
		var orgName = $('#organisation\\.key :selected').text();
	
		$('#organisation\\.name').val(orgName);	
		$('#organisation\\.alias').val(orgName);	
		//TODO: URL is hardwired to the dev registry. This must be replaced.
		//TODO: Internationalise if possible the email's subject
		var url = "<@s.url value='http://gbrdsdev.gbif.org/registry/organisation/'/>" + $('#organisation\\.key :selected').val() + ".json";
		$.getJSON(url+"?callback=?",function(data){
			var contactLink = "<a href=\"mailto:" + data.primaryContactEmail 
					+ "?subject=Password request for " + orgName + " \"> "  
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
		<@input name="organisation.name" keyBase="admin." type="text" disabled=true/>
		<@s.hidden name="organisation.key" id="organisation.key" required="true" />
		<@s.hidden name="id" id="id" required="true" />
	<#else>
		<@s.hidden id="organisation.name" name="organisation.name" required="true" />
		<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin." size=15 />
	</#if>		  
	<@input name="organisation.password" keyBase="admin." type="text"/>
	<div id="requestDetails"></div>
	
	<@input name="organisation.alias" keyBase="admin." type="text"/>
	<@checkbox name="organisation.canHost" keyBase="admin."/>
	
   <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<#if id?has_content>
 	<@s.submit name="delete" key="button.delete"/>
	</#if>		   	
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</@s.form>
<#include "/WEB-INF/pages/inc/footer.ftl">
