<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<title><@s.text name='manage.metadata.basic.title'/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
	});   
</script>	
 <#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.additional.title'/>: <a href="resource.do?r=${resource.shortname}"><em>${resource.title!resource.shortname}</em></a> </h1>
<p><@s.text name='manage.metadata.additional.intro'/></p>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">
	<div class="halfcolumn">
	  	<@input name="eml.hierarchyLevel" i18nkey="eml.hierarchyLevel" help="i18n" disabled=true />
	</div>
	<div class="halfcolumn">
	  	<@input date=true name="eml.pubDate" i18nkey="eml.pubDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD",  "MM/DD/YYYY":"MM/DD/YYYY"} />
	</div>
	<div class="newline"></div>
	<@input name="eml.logoUrl" i18nkey="eml.logoUrl" help="i18n"/>
  	<@text name="eml.purpose" i18nkey="eml.purpose" help="i18n"/>
  	<@text name="eml.intellectualRights" i18nkey="eml.intellectualRights" help="i18n"/>
  	<@text name="eml.additionalInfo" i18nkey="eml.additionalInfo"/>
  	<div class="newline"></div>
  	<h2><@s.text name='manage.metadata.alternateIdentifiers.title'/></h2>
  	<p><@s.text name='manage.metadata.alternateIdentifiers.intro'/></p>
  	<div id="items">
		<#list eml.alternateIdentifiers as item>
			<div id="item-${item_index}" class="item">
			<div class="newline"></div>
			<div class="right">
				<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
		    </div>
		    <div class="newline"></div>
			<@input name="eml.alternateIdentifiers[${item_index}]" i18nkey="eml.alternateIdentifier" help="i18n"/>
			<div class="newline"></div>
			<div class="horizontal_dotted_line_large_foo" id="separator"></div>
			<div class="newline"></div>
		  	</div>
		</#list>
	</div>
  	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></a>
	
  	<div class="buttons">
 		<@s.submit cssClass="button" name="save" key="button.save"/>
 		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  	</div>
  	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
<div id="baseItem" class="item" style="display:none;">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
    </div>
	<@input name="alternateIdentifiers" i18nkey="eml.alternateIdentifier" help="i18n"/>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>