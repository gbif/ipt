<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.config.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="config" method="post">

	<@input name="baseUrl" i18nkey="admin.config.baseUrl" size=80/>
	<#--  
	<@input name="googleMapsKey" i18nkey="admin.config.googleMapsKey" size=80/>
	-->  
	<@input name="analyticsKey" i18nkey="admin.config.analyticsKey" size=80/>  
	<@checkbox name="analyticsGbif" i18nkey="admin.config.analyticsGbif" />  
	<@checkbox name="debug" i18nkey="admin.config.debug" />  
		
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	

</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
