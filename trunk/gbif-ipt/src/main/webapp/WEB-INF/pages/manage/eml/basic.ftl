<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.basic.title'/></title>
	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.basic.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.basic.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">

  	<@input name="eml.title" />
  	<@text name="eml.description" />

	<div class="half">
	  	<@select name="resource.subtype" options=resourceTypes value="${resource.subtype!}" />
	  	<@select name="eml.language" options=languages value="${eml.language!}" />
  	</div>
	<div class="half">
	  	<@input name="eml.contact.firstName" />
	  	<@input name="eml.contact.lastName" />
  	</div>
	<div class="half">
	  	<@input name="eml.contact.email" />
	  	<@input name="eml.contact.phone" />
  	</div>
  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
