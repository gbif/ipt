<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.methods.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$("#removeLink-0").hide();
	if("${eml.methodSteps?size}" == 0){
		$("#plus").click();
	}
});
</script>
<#assign sideMenuEml=true />
<#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>


<h1><span class="superscript"><@s.text name='manage.overview.title.label'/></span>
    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
</h1>
<div class="grid_17 suffix_1">
<h2 class="subTitle"><@s.text name='manage.metadata.methods.title'/></h2>
<form class="topForm" action="metadata-${section}.do" method="post">
    <p><@s.text name='manage.metadata.methods.intro'/></p>
	<div id="sampling" >
		<@text name="eml.studyExtent"  i18nkey="eml.studyExtent" help="i18n" requiredField=true />
		<@text name="eml.sampleDescription" i18nkey="eml.sampleDescription" help="i18n" requiredField=true />
	</div>
	<div id="qualitycontrol" >
		<@text name="eml.qualityControl" i18nkey="eml.qualityControl" help="i18n"/>
	</div>
	<div id="items">
		<#if eml.methodSteps??>
			<#list eml.methodSteps as item>
				<div id="item-${item_index}" class="item">
					<div class="right">
				      <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
				    </div>
					<@text name="eml.methodSteps[${item_index}]" i18nkey="eml.methodSteps" help="i18n" requiredField=true/>
				</div>
			</#list>
		</#if>
	</div>
	<div class="addNew"><a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.methods.item'/></a></div>
	<div class="buttons">
		<@s.submit cssClass="button" name="save" key="button.save" />
		<@s.submit cssClass="button" name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
<form>
</div>
<div id="baseItem" class="item" style="display:none">
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
	</div>
	<@text name="" i18nkey="eml.methodSteps" help="i18n" requiredField=true />
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
