<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.keywords.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
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
    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
</h1>
<div class="grid_17 suffix_1">
<h2 class="subTitle"><@s.text name='manage.metadata.keywords.title'/></h2>
<form class="topForm" action="metadata-${section}.do" method="post">
    <p><@s.text name='manage.metadata.keywords.intro'/></p>
	<div id="items">
		<#list eml.keywords as item>
			<div id="item-${item_index}" class="item">
			<div class="newline"></div>
			<div class="right">
				<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/> ]</a>
		    </div>
		    <div class="newline"></div>
			<@input name="eml.keywords[${item_index}].keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true />
			<@text name="eml.keywords[${item_index}].keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
		  	<div class="newline"></div>
			<div class="horizontal_dotted_line_large_foo" id="separator"></div>
			<div class="newline"></div>
		  	</div>
		</#list>
	</div>
	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.keywords.item'/></a>
	<div class="newline"></div>
	<div class="newline"></div>
	<div class="buttons">
		<@s.submit cssClass="button" name="save" key="button.save" />
		<@s.submit cssClass="button" name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
</div>
<div id="baseItem" class="item" style="display:none;">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/> ]</a>
    </div>
	<@input name="keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true/>
	<@text name="keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
