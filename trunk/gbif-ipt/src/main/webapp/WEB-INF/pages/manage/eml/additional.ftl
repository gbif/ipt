<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.basic.title'/></title>
<script type="text/javascript">
	$(document).ready(function(){
		initHelp();
	});   
</script>	
 <#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.additional.title'/>: <em>${resource.title!resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.additional.intro'/></p>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">
	<div class="half">
	  	<@input name="eml.hierarchyLevel" i18nkey="eml.hierarchyLevel" disabled=true />
	  	<@input name="eml.pubDate" i18nkey="eml.pubDate" help="i18n" helpOptions={"MM/DD/YYYY":"MM/DD/YYYY",  "MM/DD/YY":"MM/DD/YY", "January 1, 1990":"January 1, 1990"} />
	</div>
	<@input name="eml.distributionUrl" i18nkey="eml.distributionUrl" />
  	<@text name="eml.purpose" i18nkey="eml.purpose"/>
  	<@text name="eml.intellectualRights" i18nkey="eml.intellectualRights" help="i18n"/>
  	<@text name="eml.additionalInfo" i18nkey="eml.additionalInfo"/>
  	<div class="buttons">
 		<@s.submit name="save" key="button.save"/>
 		<@s.submit name="cancel" key="button.cancel"/>
  	</div>
  	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">
