<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="account.title"/></title>
 <#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="account.title"/></h1>

<p><@s.text name="account.intro"/></p>
<p><@s.text name="account.email.cantChange"/></p>
 
<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="account.do" method="post">
	<@s.hidden name="id" value="${user.email!}" required="true"/>

	<@input name="user.email" disabled=true />  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@input name="password" i18nkey="user.password.new" type="password" />
	<@input name="password2" i18nkey="user.password2" type="password"/>
	  
	<#assign val><@s.text name="user.roles.${user.role?lower_case}"/></#assign>
	<@readonly name="role" i18nkey="user.role" value=val />  
	
  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>	
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
