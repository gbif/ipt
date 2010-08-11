<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.basic.title'/></title>
	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.basic.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.basic.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">

  	<@input name="resource.title" keyBase="manage.metadata.basic." />
  	<@text name="resource.description" keyBase="manage.metadata.basic." />
  		
	<div class="half">
	  	<@select name="resource.type" keyBase='manage.metadata.basic.' options=resourceTypes value="${resource.type!}" />
	  	<@select name="eml.language" keyBase='manage.metadata.basic.' options=languages value="${eml.language!}" />
  	</div>
	<div class="half">
	  	<@input name='eml.contact.firstName' keyBase='manage.metadata.basic.'/>
	  	<@input name='eml.contact.lastName' keyBase='manage.metadata.basic.'/>
  	</div>
	<div class="half">
	  	<@input name='eml.contact.email' keyBase='manage.metadata.basic.' />
	  	<@input name='eml.contact.phone' keyBase='manage.metadata.basic.' />
  	</div>
  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
