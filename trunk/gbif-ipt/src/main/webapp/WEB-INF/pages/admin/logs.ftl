<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.logs.title"/></title>
 <#assign currentMenu = "admin"/>
<script type="text/javascript">
$(document).ready(function(){
  $.get("${baseURL}/admin/logfile.do", {log:"admin"}, function(data){
  	$("#logs").text(data);
   });
  });
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.logs.title"/></h1>

<h2>IPT Log messages >= WARN</h2>
<p>Download the <a href="logfile.do?log=debug">complete log file</a></p>

<pre id="logs">
</pre>

<#include "/WEB-INF/pages/inc/footer.ftl">
