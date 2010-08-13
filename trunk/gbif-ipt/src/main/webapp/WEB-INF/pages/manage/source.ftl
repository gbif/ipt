<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.source.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});   
</script>
	<style>
	div.details{
		padding-top: 20px;
	}
	</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.source.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.source.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="source.do" method="post">
  	<input type="hidden" name="id" value="${source.name!}" />
  	
  	<div class="half">
	  	<@input name="source.name" />
		<div class="details">
			<table>
			  	<tr><th>Readable</th><td><#if !source.readable><img src="${baseURL}/images/warning.gif" /></#if> ${source.readable?string}</td></tr>
			  	<tr><th>File</th><td>${(source.file.getAbsolutePath())!}</td></tr>
			  	<tr><th>Size</th><td>${source.fileSizeFormatted!}</td></tr>
			  	<tr><th>Rows</th><td>${source.rows!}</td></tr>
			  	<tr><th>Columns</th><td>${source.columns!}</td></tr>
			  	<tr><th>Modified</th><td>${(source.lastModified?string)!}</td></tr>
			</table>
		 	<@s.submit cssClass="small" name="analyze" key="button.analyze"/>
		</div>
  	</div>
  	<div class="half">
	  	<@input name="source.encoding" help="Character encoding of the source data. If unkown try UTF8, Latin1 or Windows1252" helpOptions={"UTF-8":"UTF-8","Latin1":"Latin 1","Cp1252":"Windows1252"}/>
	  	<@input name="source.dateFormat" help="The date format used in the source data" helpOptions={"YYYY-MM-DD":"ISO format: YYYY-MM-DD","MM/DD/YYYY":"US dates: MM/DD/YYYY","DD.MM.YYYY":"DD.MM.YYYY"}/>
  	</div>
  	<#if source.fieldsTerminatedBy?exists>
	  	<#-- only for file sources -->
	  <div class="half">
	  	<@input name="source.fieldsTerminatedBy" />
	  	<@input name="source.fieldsEnclosedBy" />
  	  </div>
	  <div class="half">
	  	<@input name="source.linesTerminatedBy" />
	  	<@input name="source.ignoreHeaderLines" />
  	  </div>
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
      	  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
