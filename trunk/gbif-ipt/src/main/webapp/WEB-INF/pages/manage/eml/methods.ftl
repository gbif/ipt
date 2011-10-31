<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.methods.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<script type="text/javascript">
$(document).ready(function(){
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
<h1><@s.text name='manage.metadata.methods.title'/>: <a href="resource.do?r=${resource.shortname}"><em>${resource.title!resource.shortname}</em></a> </h1>
<@s.text name='manage.metadata.methods.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post">
	<div id="sampling" >
		<@text name="eml.studyExtent"  i18nkey="eml.studyExtent"/>
		<@text name="eml.sampleDescription" i18nkey="eml.sampleDescription"/>
	</div>
	<div id="qualitycontrol" >
		<@text name="eml.qualityControl" i18nkey="eml.qualityControl"/>
		<div class="newline"></div>
		<div class="horizontal_dotted_line_large_foo" id="separator"></div>
		<div class="newline"></div>
	</div>
	<div id="items">
		<#if eml.methodSteps??>
			<#list eml.methodSteps as item>
				<div id="item-${item_index}" class="item">
					<div class="newline"></div>
					<div class="right">
				      <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
				    </div>
				    <div class="newline"></div>
					<@text name="eml.methodSteps[${item_index}]" i18nkey="eml.methodSteps"/>
				  	<div class="newline"></div>
					<div class="horizontal_dotted_line_large_foo" id="separator"></div>
					<div class="newline"></div>
				</div>
			</#list>
		</#if>
	</div>
	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.methods.item'/></a></br></br>
	    <div class="newline"></div>
	    <div class="newline"></div>
	<div class="buttons">
		<@s.submit cssClass="button" name="save" key="button.save" />
		<@s.submit cssClass="button" name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
<form>
<div id="baseItem" class="item" style="display:none">
	<div class="newline"></div>
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.methods.item'/> ]</a>
	</div>
	<div class="newline"></div>
	<@text name="" i18nkey="eml.methodSteps"/>
	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>