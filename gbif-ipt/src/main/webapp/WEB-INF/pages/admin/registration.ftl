<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.registration.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl"> 
<@s.form id="registrationForm" cssClass="ftlTopForm" action="associate" method="post">
	<@selectList name="organisationKey" options="organisations" objValue="key" objTitle="name" keyBase="admin.registration." value="" size=15/>  
	<@input name="organisationPassword" keyBase="admin.registration." type="text"/>
   <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>	  
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
