<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.extension.title"/></title>

<script type="text/javascript">
$(document).ready(function(){
    $("input.form-reset").one("click", function () {
      $(this).val("");
    });
    $("#locale > a").click(function(e) {
        $("#availableLocales").toggle();
    })
	readUserPrefCookie();    
});
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.extension.title"/></h1>

<h3>Extensions currently installed in this IPT</h3>
<table>
	<tr>
		<th>Name</th>
		<th>rowType</th>
		<th>Type</th>
	</tr>

	<#list extensions as ext>	
	<tr>
		<td><a href="extension?id=${ext.rowType}">${ext.name}</a></td>
		<td>${ext.rowType}</td>
		<td>${ext.availableFor}</td>
	</tr>
	<tr>
		<td>${ext.description}</td>
	</tr>
	</#list>
	
</table>

<br/>
<p><a id="listExtensions" href="#">List available extensions</a> from the GBIF registry</p>
<table id="gbifResults">
</table>


<#include "/WEB-INF/pages/inc/footer.ftl">
