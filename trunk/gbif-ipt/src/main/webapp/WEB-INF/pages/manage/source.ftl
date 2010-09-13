<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.source.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
	$("#peekBtn").click(function(e) {
		e.preventDefault();
		$("#modalcontent").load("peek.do?r=${resource.shortname}&id=${id!}");
		$("#modalbox").show();
    });
	$("#modalbox").click(function(e) {
		e.preventDefault();
		$("#modalbox").hide();
    });
});   
</script>
	<style>
	div.details{
		padding-top: 20px;
	}
	</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.source.title'/>: <em>${resource.title!resource.shortname}</em></h1>
<p><@s.text name='manage.source.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="source.do" method="post">
  	<input type="hidden" name="r" value="${resource.shortname}" />
  	<input type="hidden" name="id" value="${id!}" />
  	
  	<div class="half">
	  	<@input name="source.name" help="i18n" disabled=id?has_content/>
		<div class="details">
			<table>
			  	<tr><th>Readable</th><td><img src="${baseURL}/images/<#if source.readable>good.gif" /><#else>bad.gif" /> ${problem!}</#if></td></tr>
			  	<tr><th>Columns</th><td>${source.columns!}</td></tr>
		  	  	<#if source.fieldsTerminatedBy?exists>
			  	<tr><th>File</th><td>${(source.file.getAbsolutePath())!}</td></tr>
			  	<tr><th>Size</th><td>${source.fileSizeFormatted!"???"}</td></tr>
			  	<tr><th>Rows</th><td>${source.rows!"???"}</td></tr>
			  	<tr><th>Modified</th><td>${(source.lastModified?datetime?string)!}</td></tr>
		  		<#else>
		  		</#if>
			</table>
		 	<@s.submit name="analyze" key="button.analyze"/>
		 	<@s.submit id="peekBtn" name="peek" key="button.preview"/>
		</div>
  	</div>
  	<#if source.fieldsTerminatedBy?exists>
	  	<#-- only for file sources -->
	  <div class="half">
	  	<@input name="fileSource.ignoreHeaderLines" help="i18n" helpOptions={"0":"None","1":"Single Header row"}/>
  	  </div>
	  <div class="half">
	  	<@input name="fileSource.fieldsTerminatedBy" help="i18n" helpOptions={"\t":"Tabulator",",":"Comma",";":"Semicolon","|":"Pipe"}/>
	  	<@input name="fileSource.fieldsEnclosedBy" help="i18n" helpOptions={"&quot;":"Double Quote","'":"Single Quote"}/>
  	  </div>
  	<#else>
	  	<#-- only for sql sources -->
	  <div class="half">
	    <@select name="rdbms" options=jdbcOptions value="${source.rdbms.name}" i18nkey="sqlSource.rdbms" />  	  
  	  </div>
	  <div class="half">
	  	<@input name="sqlSource.host" help="i18n"/>
	  	<@input name="sqlSource.database" help="i18n"/>
  	  </div>
	  <div class="half">
	  	<@input name="sqlSource.username" />
	  	<@input name="sqlSource.password" />
  	  </div>
  	  <@text name="sqlSource.sql" help="i18n"/>
  	</#if>
  	<div class="half">
	  	<@input name="source.encoding" help="i18n" helpOptions={"UTF-8":"UTF-8","Latin1":"Latin 1","Cp1252":"Windows1252"}/>
	  	<@input name="source.dateFormat" help="i18n" helpOptions={"YYYY-MM-DD":"ISO format: YYYY-MM-DD","MM/DD/YYYY":"US dates: MM/DD/YYYY","DD.MM.YYYY":"DD.MM.YYYY"}/>
  	</div>
      	  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<#if id?exists>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	</#if>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
