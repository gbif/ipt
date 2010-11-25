<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/inc/header_ui.ftl">
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
	<div class="half">
	  	<@input name="eml.hierarchyLevel" i18nkey="eml.hierarchyLevel" disabled=true />
	  	<@input date=true name="eml.pubDate" i18nkey="eml.pubDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD",  "MM/DD/YYYY":"MM/DD/YYYY"} />
	</div>
	<@input name="eml.distributionUrl" i18nkey="eml.distributionUrl" />
  	<@text name="eml.purpose" i18nkey="eml.purpose" help="i18n"/>
  	<@text name="eml.intellectualRights" i18nkey="eml.intellectualRights" help="i18n"/>
  	<@text name="eml.additionalInfo" i18nkey="eml.additionalInfo"/>
  	<div class="buttons">
 		<@s.submit cssClass="button" name="save" key="button.save"/>
 		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  	</div>
  	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
<#include "/WEB-INF/pages/inc/footer.ftl">
