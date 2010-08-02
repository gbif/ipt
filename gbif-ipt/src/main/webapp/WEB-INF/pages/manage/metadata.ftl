<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>Metadata for <em>${ms.resource.title!ms.resource.shortname}</em></h1>

<p>You will have to fill in at least the basic metadata before you can make this resource public.</p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<@s.form cssClass="ftlTopForm" action="metadata.do" method="post">
  	<input type="hidden" name="id" value="${ms.resource.shortname}"/>
  	
  	<@input name="resource.title" keyBase="ms." size=80/>
  	<@text name="resource.description" keyBase="ms." size=120/>
  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	
</@s.form>


<#include "/WEB-INF/pages/inc/footer.ftl">
