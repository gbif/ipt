<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.project.title'/></title>
<#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
	});   
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<h1><@s.text name='manage.metadata.project.title'/>: <a href="resource.do?r=${resource.title!resource.shortname}"><em>${resource.title!resource.shortname}</em></a> </h1>
<@s.text name='manage.metadata.project.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
	<@input name="eml.project.title"/>
	<div class="halfcolumn">
		<@input name="eml.project.personnel.firstName" />
	</div>
	<div class="halfcolumn">
		<@input name="eml.project.personnel.lastName" />
	</div>
	<div class="halfcolumn">
		<@select name="eml.project.personnel.role" value="${(eml.project.personnel.role)!}" options=roleOptions />
	</div>
	<div class="newline"></div>
	<@text name="eml.project.funding" help="i18n"/>
	<@text name="eml.project.studyAreaDescription.descriptorValue" help="i18n" />
	<@text name="eml.project.designDescription" help="i18n" />
	<div class="buttons">
  		<@s.submit name="save" key="button.save" />
  		<@s.submit name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">
