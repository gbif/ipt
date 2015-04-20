<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><#if "${newUser!}"=="no"><@s.text name="admin.user.title.edit"/><#else><@s.text name="admin.user.title.new"/></#if></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});   
</script>	
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">
<div class="grid_18 suffix_6">

<h1><#if "${newUser!}"=="no"><@s.text name="admin.user.title.edit"/><#else><@s.text name="admin.user.title.new"/></#if></h1>
<@s.form id="newuser" cssClass="topForm half" action="user.do" method="post" namespace="" includeContext="false">
<p><@s.text name="admin.user.intro"/></p>
<p><@s.text name="admin.user.intro2"/></p>

	<@s.hidden name="id" value="${user.email!}" required="true"/>

	<@input name="user.email" disabled=id?has_content/>  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@select name="user.role" value=user.role javaGetter=false options={"User":"user.roles.user", "Manager":"user.roles.manager", "Publisher":"user.roles.publisher", "Admin":"user.roles.admin"}/>

	<#if "${newUser!}"=="no">
	  <div class="userPasswordButtons">
		<@label i18nkey="user.password">
			<@s.submit cssClass="button" name="resetPassword" key="button.resetPassword" />
		</@label>
	  </div>	
	<#else>
		<@input name="user.password" type="password" />
		<@input name="password2" i18nkey="user.password2" type="password"/>  
	</#if>
  <div class="userManageButtons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<#if "${newUser!}"=="no"><@s.submit cssClass="confirm" name="delete" key="button.delete"/></#if>
 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  </div>	
</@s.form>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>