<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.home.title"/></h1>
<table>
	<tr>
	  <td>
		<a href="${baseURL}/admin/config.do"><img src="${baseURL}/images/icons/settings.png" /></a>
	  </td>
	  <td>
		<@s.text name="admin.home.editConfig"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/users.do"><img src="${baseURL}/images/icons/users.png" /></a>
	  </td>
	  <td>
		<@s.text name="admin.home.manageUsers"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/organisations.do"><img src="${baseURL}/images/icons/organisation.png" /></a>
	  </td>
	  <td>
		<@s.text name="admin.home.editOrganisations"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/registration.do"><img src="${baseURL}/images/icons/registry.png" /></a>
	  </td>
	  <td>
		<@s.text name="admin.home.editRegistration"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/extensions.do"><img src="${baseURL}/images/icons/extensions.png" /></a>
	  </td>
	  <td>
		<@s.text name="admin.home.manageExtensions"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/logs.do"><img src="${baseURL}/images/icons/announcement.png" /></a>
	  </td>
	  <td>
		<@s.text name="admin.home.manageLogs"/>
	  </td>
	</tr>
</table>

<#include "/WEB-INF/pages/inc/footer.ftl">
