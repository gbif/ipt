<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.home.title"/></h1>
<table>
	<tr>
	  <td>
		<img src="${baseURL}/images/icons/settings.png" />
	  </td>
	  <td>
		<a href="${baseURL}/admin/config.do"><@s.text name="admin.home.editConfig"/></a>
	  </td>
	</tr>
	<tr>
	  <td>
		<img src="${baseURL}/images/icons/users.png" />
	  </td>
	  <td>
		<a href="${baseURL}/admin/users.do"><@s.text name="admin.home.manageUsers"/></a>
	  </td>
	</tr>
	<tr>
	  <td>
		<img src="${baseURL}/images/icons/organisation.png" />
	  </td>
	  <td>
		<a href="${baseURL}/admin/organisation.do"><@s.text name="admin.home.editOrganisations"/></a>
	  </td>
	</tr>
	<tr>
	  <td>
		<img src="${baseURL}/images/icons/registry.png" />
	  </td>
	  <td>
		<a href="${baseURL}/admin/registration.do"><@s.text name="admin.home.editRegistration"/></a>
	  </td>
	</tr>
	<tr>
	  <td>
		<img src="${baseURL}/images/icons/extensions.png" />
	  </td>
	  <td>
		<a href="${baseURL}/admin/extensions.do"><@s.text name="admin.home.manageExtensions"/></a>
	  </td>
	</tr>
	<tr>
	  <td>
		<img src="${baseURL}/images/icons/announcement.png" />
	  </td>
	  <td>
		<a href="${baseURL}/admin/logs.do"><@s.text name="admin.home.manageLogs"/></a>
	  </td>
	</tr>
</table>

<#include "/WEB-INF/pages/inc/footer.ftl">
