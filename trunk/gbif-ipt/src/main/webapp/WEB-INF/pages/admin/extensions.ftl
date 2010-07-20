<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.extension.title"/></title>
<script type="text/javascript">
$(document).ready(function(){
});
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.extension.title"/></h1>

<h3>Extensions currently installed in this IPT</h3>
<table id="extensions" class="simple" width="100%">
	<tr>
		<th width="25%">Name</th>
		<th width="60%">Row Type</th>
		<th width="150%">Type</th>
	</tr>

	<#list extensions as ext>	
	<tr>
		<td><a href="extension?id=${ext.rowType}">${ext.name}</a></td>
		<td>${ext.rowType}</td>
		<td>${ext.availableFor}</td>
	</tr>
	<tr>
		<td colspan="2">${ext.description}</td>
		<td>
			<form action='extension.do' method='post'>
				<input type='hidden' name='id' value='${ext.rowType}' />
				<input type='submit' name='delete' value='Remove' />
			</form>
		</td>
	</tr>
	<tr class="linebreak"><td colspan="3"></td></tr>
	</#list>
	
	<#list gbrdsExtensions as ext>	
	<tr>
		<td><a href="${ext.url}">${ext.title}</a></td>
		<td>?</td>
		<td>?</td>
	</tr>
	<tr>
		<td colspan="2">No description</td>
		<td>
			<form action='extension.do' method='post'>
				<input type='hidden' name='id' value='${ext.url}' />
				<input type='submit' name='install' value='Install' />
			</form>
		</td>
	</tr>
	<tr class="linebreak"><td colspan="3"></td></tr>
	</#list>
	
</table>


<#include "/WEB-INF/pages/inc/footer.ftl">
