<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.config.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form id="configForm" cssClass="ftlTopForm" action="config" method="post">

	<@input name="baseUrl" keyBase="admin.config." size=80/>  
	<@input name="googleMapsKey" keyBase="admin.config." size=80/>  
	<@input name="analyticsKey" keyBase="admin.config." size=80/>  
	<@checkbox name="analyticsGbif" keyBase="admin.config." />  
	<@checkbox name="debug" keyBase="admin.config." />  
		
  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>	

</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
