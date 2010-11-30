<#include "/WEB-INF/pages/inc/header_setup.ftl">

<h1><@s.text name="admin.config.setup2.title"/></h1>
<p><@s.text name="admin.config.setup2.welcome"/></p>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="setup2.do" method="post">
	<input type="hidden" name="setup2" value="true" />
	<@input name="user.email" />  
	<@input name="user.firstname" />  
	<@input name="user.lastname" />  
	<@input name="user.password" type="password"/>  
	<@input name="password2" i18nkey="user.password2" type="password"/>  

	<@checkbox name="production" i18nkey="admin.config.setup2.production" disabled=cfg.devMode() value="false" />
	  
	<@input name="baseURL" help="i18n" i18nkey="admin.config.baseUrl"/>
	<@input name="proxy" help="i18n" i18nkey="admin.config.proxy" />

	  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
	  </div>	

</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
