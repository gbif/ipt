<#escape x as x?html>
<#setting url_escaping_charset="UTF-8">
<#include "/WEB-INF/pages/inc/header.ftl">
<script type="text/javascript">
$(document).ready(function(){
  loadReport();
  var reporter = setInterval(loadReport, 1000);
  function loadReport(){
	$("#report").load("${baseURL}/manage/report.do?r=${resource.shortname}", function() {
		if ($(".completed").length > 0){
			// stop timer and hide gif
			clearInterval(reporter);
		};	
	});
  }
});
</script>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>${resource.title!resource.shortname}</h1>

<div id="report">
</div>


<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>