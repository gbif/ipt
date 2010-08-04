<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.registration.title"/></h1>

<p><@s.text name="admin.registration.intro"/></p>
<p><@s.text name="admin.registration.intro2"/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"> 
<@s.form cssClass="topForm half" action="registration" method="post">
	<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin." value="" size=15/>  
	<@input name="organisation.password" keyBase="admin." type="text"/>
	<@input name="organisation.alias" keyBase="admin." type="text"/>
	<@checkbox name="organisation.canHost" keyBase="admin."/>	
   <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  </div>	  
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">