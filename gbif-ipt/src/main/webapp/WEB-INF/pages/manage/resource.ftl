<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>Configure <em>${ms.resource.shortname!}</em></h1>

<p>Welcome dear manager!</p>
<p>Please manage this great resource <strong><em>${ms.resource}</em></strong></p>

<h2>Test Persistency</h2>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<@s.form cssClass="ftlTopForm" action="resource.do" method="post">
  	<@input name="id" size=40 value="${ms.resource.shortname}"/>
  	<@input name="resource.title" keyBase="ms." size=80/>
  	<@select name="resource.type" keyBase="ms." options={"occ":"resource.type.occ", "tax":"resource.type.tax"} value="occ" />
  	<@text name="resource.description" keyBase="ms." size=120/>
  
  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>	
</@s.form>


<#include "/WEB-INF/pages/inc/footer.ftl">
