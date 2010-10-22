<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.user.title"/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});   
</script>	
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.user.title"/></h1>

<p><@s.text name="admin.user.intro"/></p>
<p><@s.text name="admin.user.intro2"/></p>

 
<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="user.do" method="post">
	<@s.hidden name="id" value="${user.email!}" required="true"/>

	<@input name="user.email" disabled=id?has_content/>  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@select name="user.role" value=user.role options={"User":"user.roles.user", "Manager":"user.roles.manager", "Publisher":"user.roles.publisher", "Admin":"user.roles.admin"}/>
	
	<#if user.email??>
		<br>
		<@s.text name="user.password"/>	   <@s.submit name="resetPassword" key="button.resetPassword" method="resetPassword"/>
	<#else>
		<@input name="user.password" type="password" />
	</#if>
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
