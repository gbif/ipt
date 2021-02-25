<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="account.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl">
<div class="grid_18 suffix_6">
<h1><@s.text name="account.title"/></h1>
<p><@s.text name="account.intro"/></p>
<p><@s.text name="account.email.cantChange"/></p>

<@s.form cssClass="topForm half" action="account" method="post" namespace="" includeContext="false">
	<@s.hidden name="id" value="${user.email!}" required="true"/>

	<@input name="user.email" disabled=true />  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@input name="user.password" i18nkey="user.password.new" type="password"/>
	<@input name="password2" i18nkey="user.password2" type="password"/>
	  
	<#assign val><@s.text name="user.roles.${user.role?lower_case}"/></#assign>
	<@readonly name="role" i18nkey="user.role" value=val />

  <div class="userManageButtons">
    <@s.submit cssClass="button" name="save" key="button.save"/>
 	  <@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  </div>
</@s.form>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
