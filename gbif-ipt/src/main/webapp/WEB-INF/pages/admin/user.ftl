<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.user.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.user.title"/></h1>
 
<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="ftlForm" action="user.do" method="post">
	<@s.hidden name="id" value="${user.email!}" required="true"/>

	<@input name="user.email" />  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@input name="user.password" />  
	<@select name="user.role" value=user.role?lower_case options={"user":"user.roles.user", "manager":"user.roles.manager", "admin":"user.roles.admin"}/>  
	  	
  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<@s.submit cssClass="button" name="delete" key="button.delete"/>
  </div>	
</@s.form>

<p>
	<a href="users.do">Back to user list</a>
</p>
<#include "/WEB-INF/pages/inc/footer.ftl">
