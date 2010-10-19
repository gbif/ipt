<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.citations.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
 <#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<h1><@s.text name='manage.metadata.citations.title'/>: <em>${resource.title!resource.shortname}</em></h1>
<@s.text name='manage.metadata.citations.intro'/>
<form class="topForm" action="metadata-${section}.do" method="post"> 
	<div class="newline"></div>
	<div>
  		<@text name="eml.citation" />
	</div>
	<div class="newline"></div>
	<h2><@s.text name="manage.metadata.citations.bibliography"/></h2>
	<div id="separator" class="horizontal_dotted_line_large_foo"></div>
	<div id="items">
		<#list eml.bibliographicCitationSet.bibliographicCitations as item>
			<div id="item-${item_index}" class="item">
				<div class="newline"></div>
				<div class="right">
      				<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.citations.item'/> ]</a>
    			</div>
    			<div class="newline"></div>
					<@text name="eml.bibliographicCitationSet.bibliographicCitations[${item_index}]" i18nkey="eml.bibliographicCitationSet.bibliographicCitations" size=40/>
  				<div class="newline"></div>
				<div class="horizontal_dotted_line_large_foo" id="separator"></div>
				<div class="newline"></div>
  			</div>
		</#list>
	</div>
	<div class="newline"></div>
	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.citations.item'/></a>
	<div class="buttons">
		<@s.submit name="save" key="button.save" />
		<@s.submit name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />	
</form>
<div id="baseItem" class="item" style="display:none;">
	<div class="newline"></div>
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.citations.item'/> ]</a>
	</div>
	<div class="newline"></div>
		<@text name="bibliographicCitations" i18nkey="eml.bibliographicCitationSet.bibliographicCitations"  value="" size=40/>
	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">