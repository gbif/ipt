<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.user.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.user.title"/></h1>

<p><@s.text name="admin.users.intro"/></p>

<table class="simple" width="100%">
	<tr>
		<th>Name</th>
		<th>Email</th>
		<th>Role</th>
		<th>Last Login</th>
	</tr>

	<#list users as u>	
	<tr>
		<td><a href="user?id=${u.email}">${u.name}</a></td>
		<td>${u.email}</td>
		<td>${u.role}</td>
		<td>${(u.lastLogin?datetime?string.short)!"never"}</td>
	</tr>
	</#list>
	
</table>

<p>
	<a class="button" href="user.do">Create new account</a>
</p>


<#include "/WEB-INF/pages/inc/footer.ftl">
