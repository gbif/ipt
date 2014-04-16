<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.home.manageUsers"/></title>
 <script type="text/javascript">

$(document).ready(function(){
	//Hack needed for Internet Explorer
	$('#create').click(function() {
		window.location='user.do';
	});	
	$('#cancel').click(function() {
		window.location='home.do';
	});	
});
</script>	
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="grid_24">
<h1><@s.text name="admin.home.manageUsers"/></h1>

<p><@s.text name="admin.users.intro"/></p>

<table id="user-list">
    <thead>
	<tr>
		<th><@s.text name="admin.users.name"/></th>
		<th><@s.text name="admin.users.email"/></th>
		<th><@s.text name="admin.users.role"/></th>
		<th><@s.text name="admin.users.last.login"/></th>
	</tr>
	</thead>
	<tbody>
    <#-- for counting even or odd rows -->
    <#function zebra index>
      <#if (index % 2) == 0>
        <#return "even" />
      <#else>
        <#return "odd" />
      </#if>
    </#function>

	<#list users as u>	
	<tr class="${zebra(u_index)}">
		<td><a href="user?id=${u.email}">${u.name}</a></td>
		<td>${u.email}</td>
		<td><@s.text name="user.roles.${u.role?lower_case}"/></td>
		<td>${(u.lastLogin?datetime?string.short)!"never"}</td>
	</tr>
	</#list>
	</tbody>
	
</table>

<p>
	<button id="create"><@s.text name="button.create"/></button>
	<button id="cancel"><@s.text name="button.cancel"/></button>
</p>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>