[#ftl]
[#include "/WEB-INF/pages/inc/header_setup.ftl"]
<div class="grid_18">
<h1>[@s.text name="admin.config.setup2.title"/]</h1>
<p>[@s.text name="admin.config.setup2.welcome"/]</p>

			[@s.actionmessage/]
			[#if warnings?size>0]
			 <ul class="warnMessage">
			 [#list warnings as w]
	          <li><span>${w!}</span></li>
			 [/#list]
             </ul>
            [/#if]
			[@s.actionerror/]

[#include "/WEB-INF/pages/macros/forms.ftl"]
[@s.form cssClass="topForm half" action="setup2.do" method="post"]
	<input type="hidden" name="setup2" value="true" />

	<input type="hidden" name="ignoreUserValidation" value="${ignoreUserValidation}" />
	[@input name="user.email" disabled=(ignoreUserValidation==1) /]
	[@input name="user.firstname" disabled=(ignoreUserValidation==1) /]
	[@input name="user.lastname" disabled=(ignoreUserValidation==1) /]
	[@input name="user.password" type="password" disabled=(ignoreUserValidation==1) /]
	[@input name="password2" i18nkey="user.password2" type="password" disabled=(ignoreUserValidation==1) /]

<div id="iptMode" class="clearfix">
  <div class="radio">
    <h2 class="subTitle">[@s.text name="admin.config.setup2.production.title"/]</h2>
    [@s.text name="admin.config.setup2.production.help"/]<br/>
    [@s.text name="admin.config.setup2.production.mode"/]<br/>
    [@s.text name="admin.config.setup2.mode"/]
    [@s.radio name="modeSelected" list="modes" disabled=(cfg.devMode()) value="Test" /]
  </div>
</div>
	[@input name="baseURL" help="i18n" i18nkey="admin.config.baseUrl"/]
	[@input name="proxy" help="i18n" i18nkey="admin.config.proxy" /]

	  <div class="buttons">
 	[@s.submit cssClass="button" name="save" key="button.save"/]
	  </div>

[/@s.form]
</div>
</div>
[#include "/WEB-INF/pages/inc/footer.ftl"]
