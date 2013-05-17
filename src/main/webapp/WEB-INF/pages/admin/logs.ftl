<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.home.manageLogs"/></title>
 <#assign currentMenu = "admin"/>
<script type="text/javascript">
$(document).ready(function(){
  $.get("${baseURL}/admin/logfile.do", {log:"admin"}, function(data){
  	$("#logs").text(data);
   });
  });
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">
<div class="grid_24">
<h1><@s.text name="admin.home.manageLogs"/></h1>

<h3 class="subTitle"><@s.text name="admin.logs.warn"/></h3>
<p><@s.text name="admin.logs.download"><@s.param>logfile.do?log=debug</@s.param></@s.text></p>

<pre id="logs">
</pre>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
