<#include "/WEB-INF/pages/inc/header_setup.ftl">

<h1><@s.text name="admin.config.setup2.title"/></h1>
<p><@s.text name="admin.config.setup2.welcome"/></p>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="setup2.do" method="post">

	<@input name="user.email" />  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@input name="user.password" />  
	
	<#if !productionSettingAllowed>
		<@checkbox name="production" i18nkey="admin.config.setup2.production.disabled" disabled="true"/> 
		<@s.hidden id="production" name="production" value="false" />  
	<#else>
		<@checkbox name="production" i18nkey="admin.config.setup2.production" />  
	</#if>
	
	<@input name="baseURL" />

	  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
	  </div>	

</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
