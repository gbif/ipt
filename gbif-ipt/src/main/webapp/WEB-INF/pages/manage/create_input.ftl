<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1>Create New Resource</h1>

<@s.form cssClass="ftlTopForm" action="create.do" method="post">
  	<@input name="shortname" keyBase="manage.resource.create." size=40/>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="file"/>
	<label for="file"><@s.text name="manage.resource.create.file"/></label>
    <@s.file name="file" key="manage.resource.create.file" required="false"/>
  </div>
      
  <div class="buttons">
 	<@s.submit cssClass="button" name="create" key="manage.home.button.createNewResource"/>
  </div>	
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
