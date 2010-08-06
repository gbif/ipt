<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript">
$(document).ready(function(){
	$('#organisation\\.key').click(function() {
		var orgName = $('#organisation\\.key :selected').text();
		$('#organisation\\.name').val(orgName);		
	});
});
</script>	
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.registration.title"/></h1>

<#if hostingOrganisation?exists>
	<p><@s.text name="admin.registration.registered1"/></p>
	<p><@s.text name="admin.registration.registered2"><@s.param><strong>${hostingOrganisation.name!}</strong></@s.param></@s.text></p>
	<p><@s.text name="admin.registration.registered3"/></p>
		
<#else>
	<p><@s.text name="admin.registration.intro"/></p>
	<p><@s.text name="admin.registration.intro2"/></p>
	<#include "/WEB-INF/pages/macros/forms.ftl"> 
	<@s.form cssClass="topForm half" action="registration" method="post">
		<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin." value="" size=15/>  
		<@input name="organisation.password" keyBase="admin." type="text"/>
		<@input name="organisation.alias" keyBase="admin." type="text"/>
		<@checkbox name="organisation.canHost" keyBase="admin."/>	
		<@s.hidden id="organisation.name" name="organisation.name" required="true" />
	   <div class="buttons">
	 	<@s.submit cssClass="button" name="save" key="button.save"/>
	 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
	  </div>	  
	</@s.form>
</#if>
<#include "/WEB-INF/pages/inc/footer.ftl">