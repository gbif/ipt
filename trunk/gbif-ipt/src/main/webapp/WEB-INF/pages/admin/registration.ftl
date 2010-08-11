<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript">
$(document).ready(function(){
alert(${validatedBaseURL?string});
	$('#validate').click(function() {
		$("#baseURLStatus").html('<img src="${baseURL}/images/small-loader.gif">');
		var url = "<@s.url value='http://gbrdsdev.gbif.org/registry/ipt/validate.json?url=${baseURL}'/>";
		$.getJSON(url+"&callback=?",function(data){
			if(data.result==200) {
				$("#baseURLStatus").html("Valid!");
				$("#organisation\\.key").removeAttr("disabled");
				$("#organisation\\.password").removeAttr("disabled");
				$("#organisation\\.alias").removeAttr("disabled");
				$("#organisation\\.canHost").removeAttr("disabled");
				$("#save").removeAttr("disabled");
				$("#cancel").removeAttr("disabled");
			}
			else {
				$("#baseURLStatus").html("Not valid, callback failed! Please check your base url. (DEV--> fields are going to be activated for dev purposes)");
				
				//this needs to be removed in production
				$("#organisation\\.key").removeAttr("disabled");
				$("#organisation\\.password").removeAttr("readonly");
				$("#organisation\\.alias").removeAttr("readonly");
				$("#organisation\\.canHost").removeAttr("disabled");
				$("#save").removeAttr("disabled");
				$("#cancel").removeAttr("disabled");				
			}
		});	

		$('#organisation\\.key').click(function() {
			var orgName = $('#organisation\\.key :selected').text();
			$('#organisation\\.name').val(orgName);		
		});
	});		
});
</script>	
	<title><@s.text name="title"/></title>
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
		
			<@input name="registration.baseURL" keyBase="admin." type="text" value="${baseURL}" size=70 disabled=true/>
			<@s.submit cssClass="button" name="validate" id="validate" key="Validate"/>
		
		<div id="baseURLStatus"></div>
		<hr/>
	</#if>

	<p><@s.text name="admin.registration.intro"/></p>
	<p><@s.text name="admin.registration.intro2"/></p>
	
	<@s.form cssClass="topForm half" action="registration" method="post">
		<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin." value="" size=15 disabled=!validatedBaseURL/>  
		<@input name="organisation.password" keyBase="admin." type="text" disabled=!validatedBaseURL/>
		<@input name="organisation.alias" keyBase="admin." type="text" disabled=!validatedBaseURL/>
		<@checkbox name="organisation.canHost" keyBase="admin." disabled=!validatedBaseURL/>	
		<@s.hidden id="organisation.name" name="organisation.name" required="!validatedBaseURL" />
	   <div class="buttons">
	 	<@s.submit cssClass="button" name="save" id="save" key="button.save" disabled=!validatedBaseURL/>
	 	<@s.submit cssClass="button" name="cancel" id="cancel" key="button.cancel" disabled=!validatedBaseURL/>
	  </div>	  
	</@s.form>
</#if>
<#include "/WEB-INF/pages/inc/footer.ftl">