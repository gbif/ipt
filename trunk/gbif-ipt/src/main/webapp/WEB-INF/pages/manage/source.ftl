<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.source.title'/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.source.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.source.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm half" action="source.do" method="post">
  	<input type="hidden" name="id" value="${source.title!}" />
  	
  	<@input name="source.title" />
  	<@input name="source.encoding" />
  	<@input name="source.dateFormat" />
  	<#if source.fieldsTerminatedBy?exists>
	  	<#-- only for file sources -->
	  	<@input name="source.fieldsTerminatedBy" />
	  	<@input name="source.fieldsEnclosedBy" />
	  	<@input name="source.linesTerminatedBy" />
	  	<@input name="source.ignoreHeaderLines" />
	  	<@input name="source.dateFormat" />
  	<#else>
	  	<#-- only for sql sources -->
	  	<@input name="source.host" />
	  	<@input name="source.database" />
	  	<@input name="source.username" />
	  	<@input name="source.password" />
	  	<@input name="source.sql" />
  	</#if>

  <div class="small">  
 	${(source.lastModified?string)!}
 	${(source.file.getAbsolutePath())!}
  </div>
  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
