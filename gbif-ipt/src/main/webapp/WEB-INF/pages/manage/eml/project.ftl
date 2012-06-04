<#escape x as x?html>
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


<h1><span class="superscript">Resource Title</span>
    <a class="tooltip" href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}
        <span class="classic">Click the title to go to the <em>resource overview</em> page.<div class="arrow-wrap"><div class="arrow"></div></div></span>
    </a>
</h1>
<div class="grid_17 suffix_1">
<h2 class="subTitle"><@s.text name='manage.metadata.project.title'/></h2>
<form class="topForm" action="metadata-${section}.do" method="post"> 
    <p><@s.text name='manage.metadata.project.intro'/></p>
	<@input name="eml.project.title"/>
	<div class="halfcolumn">
		<@input name="eml.project.personnel.firstName" />
	</div>
	<div class="halfcolumn">
		<@input name="eml.project.personnel.lastName" />
	</div>
	<div class="halfcolumn">
		<@select name="eml.project.personnel.role" value="${(eml.project.personnel.role)!}" help="i18n" options=roles />
	</div>
	<div class="clearfix"></div>
	<@text name="eml.project.funding" help="i18n"/>
	<@text name="eml.project.studyAreaDescription.descriptorValue" help="i18n" />
	<@text name="eml.project.designDescription" help="i18n" />
	<div class="buttons">
  		<@s.submit cssClass="button" name="save" key="button.save" />
  		<@s.submit cssClass="button" name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
