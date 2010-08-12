<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.source.title'/></title>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
});   
</script>	
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.source.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.source.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="source.do" method="post">
  	<input type="hidden" name="id" value="${source.title!}" />
  	
  	<div class="half">
	  	<@input name="source.title" />
  	</div>
  	<div class="half">
	  	<@input name="source.encoding" help="Character encoding of the source data. If unkown try UTF8, Latin1 or Windows1252" helpOptions={"UTF-8":"UTF-8","Latin1":"Latin 1","Cp1252":"Windows1252"}/>
	  	<@input name="source.dateFormat" help="The date format used in the source data" helpOptions={"YYYY-MM-DD":"ISO format: YYYY-MM-DD","MM/DD/YYYY":"US dates: MM/DD/YYYY","DD.MM.YYYY":"DD.MM.YYYY"}/>
  	</div>
  	<#if source.fieldsTerminatedBy?exists>
	  	<#-- only for file sources -->
	  	<@input name="source.fieldsTerminatedBy" />
	  	<@input name="source.fieldsEnclosedBy" />
	  	<@input name="source.linesTerminatedBy" />
	  	<@input name="source.ignoreHeaderLines" />
	  	<@input name="source.dateFormat" />
  	<#else>
	  	<#-- only for sql sources -->
	  <div class="half">
	  	<@input name="source.host" />
	  	<@input name="source.database" />
  	  </div>
	  <div class="half">
	  	<@input name="source.username" />
	  	<@input name="source.password" />
  	  </div>
  	  <@text name="source.sql" />
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
