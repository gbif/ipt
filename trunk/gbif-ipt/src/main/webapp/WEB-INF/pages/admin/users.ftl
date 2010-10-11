<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.user.title"/></title>
 <#assign currentMenu = "admin"/>
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
		<td><@s.text name="user.roles.${u.role?lower_case}"/></td>
		<td>${(u.lastLogin?datetime?string.short)!"never"}</td>
	</tr>
	</#list>
	
</table>

<p>
	<a href="user.do"><button><@s.text name="button.create"/></button></a>
</p>


<#include "/WEB-INF/pages/inc/footer.ftl">
