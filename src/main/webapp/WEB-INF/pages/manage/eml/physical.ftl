<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.physical.title'/></title>
<script type="text/javascript">
  $(document).ready(function(){
    initHelp();
  });
</script>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<#assign sideMenuEml=true />
<#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><span class="superscript"><@s.text name='manage.overview.title.label'/></span>
    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
</h1>
<div class="grid_17 suffix_1">
  <h2 class="subTitle"><@s.text name='manage.metadata.physical.title'/></h2>
  <form class="topForm" action="metadata-${section}.do" method="post">
    <p><@s.text name='manage.metadata.physical.intro'/></p>
	<@input name="eml.distributionUrl" i18nkey="eml.distributionUrl" type="url" />

  <div class="listBlock grid_17 suffix_1">
    <@textinline name="eml.physicalData.other" help="i18n"/>
      <div id="items">
        <#list eml.physicalData as item>
            <div id="item-${item_index}" class="item clearfix">
                <div class="right">
                    <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/> ]</a>
                </div>
                <div class="halfcolumn">
                  <@input name="eml.physicalData[${item_index}].name" i18nkey="eml.physicalData.name" requiredField=true />
                </div>
                <div class="halfcolumn">
                  <@input name="eml.physicalData[${item_index}].charset" i18nkey="eml.physicalData.charset" help="i18n" requiredField=true />
                </div>
                <div class="fullcolumn">
                  <@input name="eml.physicalData[${item_index}].distributionUrl" i18nkey="eml.physicalData.distributionUrl" help="i18n" requiredField=true type="url" />
                </div>
                <div class="halfcolumn">
                  <@input name="eml.physicalData[${item_index}].format" i18nkey="eml.physicalData.format" help="i18n" requiredField=true />
                </div>
                <div class="halfcolumn">
                  <@input name="eml.physicalData[${item_index}].formatVersion" i18nkey="eml.physicalData.formatVersion" help="i18n"/>
                </div>
            </div>
        </#list>
      </div>
      <div class="addNew"><a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.physical.item'/></a></div>
  </div>

	<div class="buttons">
		<@s.submit cssClass="button" name="save" key="button.save" />
		<@s.submit cssClass="button" name="cancel" key="button.cancel" />
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
</div>

<div id="baseItem" class="item clearfix" style="display:none;">
	<div class="right">
		<a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/> ]</a>
    </div>
	<div class="halfcolumn">
		<@input name="name" i18nkey="eml.physicalData.name" requiredField=true />
	</div>
	<div class="halfcolumn">
		<@input name="charset" i18nkey="eml.physicalData.charset" help="i18n" requiredField=true />
	</div>
	<@input name="distributionUrl" i18nkey="eml.physicalData.distributionUrl" help="i18n" requiredField=true />
	<div class="halfcolumn">
		<@input name="format" i18nkey="eml.physicalData.format" help="i18n" requiredField=true />
	</div>
	<div class="halfcolumn">
		<@input name="formatVersion" i18nkey="eml.physicalData.formatVersion" help="i18n"/>
	</div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
