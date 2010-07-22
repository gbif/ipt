<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="account.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="account.title"/></h1>

<p>You can modify your account settings here.</p>
 
<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="ftlForm" action="account.do" method="post">
	<@s.hidden name="id" value="${user.email!}" required="true"/>

	<@input name="user.email" />  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@input name="user.password" />  
	  	
  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>	
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
