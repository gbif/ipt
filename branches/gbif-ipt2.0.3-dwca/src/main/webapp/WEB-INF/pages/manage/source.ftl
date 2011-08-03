<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.source.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmation.jquery.js"></script>
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
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.source.title'/>: <a href="resource.do?r=${resource.shortname}"><em>${resource.title!resource.shortname}</em></a></h1>
<p><@s.text name='manage.source.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="source.do" method="post">
  	<input type="hidden" name="r" value="${resource.shortname}" />
  	<input type="hidden" name="id" value="${id!}" />  	
  	<div class="halfcolumn">
	  	<@input name="source.name" help="i18n" disabled=id?has_content/>
	</div>
	<div class="halfcolumn">
		<div class="detailsSource">
			<table>
			  	<tr><th><@s.text name='manage.source.readable'/></th><td><img src="${baseURL}/images/<#if source.readable>good.gif" /><#else>bad.gif" /> ${problem!}</#if></td></tr>
			  	<tr><th><@s.text name='manage.source.columns'/></th><td>${source.columns!}</td></tr>
		  	  	<#if source.fieldsTerminatedBy?exists>
			  	<tr><th><@s.text name='manage.source.file'/></th><td>${(source.file.getAbsolutePath())!}</td></tr>
			  	<tr><th><@s.text name='manage.source.size'/></th><td>${source.fileSizeFormatted!"???"}</td></tr>
			  	<tr><th><@s.text name='manage.source.rows'/></th><td>${source.rows!"???"}</td></tr>
			  	<tr><th><@s.text name='manage.source.modified'/></th><td>${(source.lastModified?datetime?string)!}</td></tr>
			  	<#if (logExists)><tr><th><@s.text name='manage.source.source.log'/></th><td><a href="${baseURL}/sourcelog.do?r=${resource.shortname}&s=${source.name}"><@s.text name='manage.source.download'/></a></td></tr></#if>
			  	<#else>
		  		</#if>
			</table>
			<div class="buttons">
		 		<@s.submit cssClass="button" name="analyze" key="button.analyze"/>
		 		<@s.submit cssClass="button" id="peekBtn" name="peek" key="button.preview"/>
			</div>
		</div>
  	</div>
  	<#if source.isFileSource()>
	  	<#-- only for file sources -->
	  <div class="halfcolumn">
	  	<@input name="fileSource.ignoreHeaderLines" help="i18n" helpOptions={"0":"None","1":"Single Header row"}/>
  	  </div>
	  <div class="halfcolumn">
  	  </div>
  	  <div class="newline"></div>	
	  <div class="halfcolumn">
	  	<@input name="fileSource.fieldsTerminatedByEscaped" help="i18n" helpOptions={"\\t":"[ \\t ] Tab",",":"[ , ] Comma",";":"[ ; ] Semicolon","|":"[ | ] Pipe"}/>
	  </div>
	  <div class="halfcolumn">
	  	<@input name="fileSource.fieldsEnclosedByEscaped" help="i18n" helpOptions={"":"None","&quot;":"Double Quote","'":"Single Quote"}/>
  	  </div>
  	<#else>
	  	<#-- only for sql sources -->
	  <div class="half">
	    <@select name="rdbms" options=jdbcOptions value="${source.rdbms.name!}" i18nkey="sqlSource.rdbms" />  	  
	  </div>
	  <div class="halfcolumn">
	  	<@input name="sqlSource.host" help="i18n"/>
	  </div>
	  <div class="halfcolumn">
	  	<@input name="sqlSource.database" help="i18n"/>
	  </div>
	  <div class="halfcolumn">
	  	<@input name="sqlSource.username" />
	  </div>
	  <div class="halfcolumn">
	  	<@input name="sqlSource.password" type="password" />
  	  </div>
  	  <@text name="sqlSource.sql" help="i18n"/>
  	  <#if sqlSource.sql?has_content>
  	  <@label i18nkey="sqlSource.sqlLimited" >
  	  ${sqlSource.getSqlLimited(10)}
  	  </@label>
  	  </#if>
  	  
  	</#if>
    <div class="halfcolumn">
	  	<@input name="source.encoding" help="i18n" helpOptions={"UTF-8":"UTF-8","Latin1":"Latin 1","Cp1252":"Windows1252"}/>
	</div>
	<div class="halfcolumn">
	  	<@input name="source.dateFormat" help="i18n" helpOptions={"YYYY-MM-DD":"ISO format: YYYY-MM-DD","MM/DD/YYYY":"US dates: MM/DD/YYYY","DD.MM.YYYY":"DD.MM.YYYY"}/>
  	</div>
  <div class="newline"></div>	
  <div class="newline"></div>	
  <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
 	<#if id?exists>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	</#if>
 	<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
  </div>
</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>