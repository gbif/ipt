<#include "/WEB-INF/pages/inc/header_setup.ftl">

<div class="grid_18 suffix_6">
<h1><@s.text name="admin.config.setup3.title"/></h1>
<p><@s.text name="admin.config.setup3.welcome"/></p>

<#include "/WEB-INF/pages/macros/forms.ftl">
<@s.form cssClass="topForm half" action="setup3.do" method="post">
	  <div class="buttons">
 	<@s.submit cssClass="button" name="continue" key="button.continue" method="continueHome"/>
	  </div>	

</@s.form>
</div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
