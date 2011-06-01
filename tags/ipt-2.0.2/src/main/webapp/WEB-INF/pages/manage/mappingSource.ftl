<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.mapping.title'/></title>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
});   
</script>

 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>${mapping.extension.title}</h1>
<p>${mapping.extension.description}</p>
<#if mapping.extension.link?has_content>
<p><@s.text name="basic.link"/>: <a href="${mapping.extension.link}">${mapping.extension.link}</a></p>
</#if>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="mapping.do" method="post">
  	<input type="hidden" name="r" value="${resource.shortname}" />
  	<input type="hidden" name="id" value="${mapping.extension.rowType}" />
  	<input type="hidden" name="mid" value="${mid!}" />
  	<input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />

<h1><@s.text name='manage.mapping.source'/></h1>
<p><@s.text name='manage.mapping.source.help'/></p>

<@selectList name="source" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" />

  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<@s.submit cssClass="button" name="cancel" key="button.cancel" method="cancel"/>
  </div>

</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
