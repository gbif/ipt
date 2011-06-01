<#include "/WEB-INF/pages/inc/header_setup.ftl">

<h1><@s.text name="admin.config.setup2.title"/></h1>
<p><@s.text name="admin.config.setup2.welcome"/></p>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="setup2.do" method="post">
	<input type="hidden" name="setup2" value="true" />
	
	<input type="hidden" name="ignoreUserValidation" value=${ignoreUserValidation} />
	<@input name="user.email" disabled=(ignoreUserValidation==1) />  
	<@input name="user.firstname" disabled=(ignoreUserValidation==1) />  
	<@input name="user.lastname" disabled=(ignoreUserValidation==1) />  
	<@input name="user.password" type="password" disabled=(ignoreUserValidation==1) />  
	<@input name="password2" i18nkey="user.password2" type="password" disabled=(ignoreUserValidation==1) />  

	<@checkbox name="production" i18nkey="admin.config.setup2.production" help="i18n" disabled=(cfg.devMode() || ignoreUserValidation==1) value="false" />
	  
	<@input name="baseURL" help="i18n" i18nkey="admin.config.baseUrl"/>
	<@input name="proxy" help="i18n" i18nkey="admin.config.proxy" />

	  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
	  </div>	

</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
