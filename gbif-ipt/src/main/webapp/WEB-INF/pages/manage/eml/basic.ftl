<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.basic.title'/></title>
	<#assign sideMenuTitle="Sections" />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.basic.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>

<p><@s.text name='manage.metadata.basic.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="ftlTopForm" action="metadata-${section}.do" method="post">

  	<@input name="resource.title" keyBase="ms." size=80/>
  	<@text name="resource.description" keyBase="ms." size=80/>
  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	
</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
