<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="title"/></title>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">


<table id="admintable">
	<tr>
	  <td>
		<a href="${baseURL}/admin/config.do"><img src="${baseURL}/images/icons/settings.png" /></a>
	  </td>
	  <td>
		<@s.text name="admin.home.editConfig"/>
	  </td>
	  <td width="250px">
	  <@s.form cssClass="topForm half" action="updateResourceMetadata.do" method="post">
			<@s.submit cssClass="button" name="updateMetadata" key="admin.home.updateMetadata"/>
		</@s.form>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/users.do"><img src="${baseURL}/images/icons/users.png" /></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.manageUsers"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/registration.do"><img src="${baseURL}/images/icons/registry.png" /></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.editRegistration"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<#if isRegistered><a href="${baseURL}/admin/organisations.do"><img src="${baseURL}/images/icons/organisation.png" /></a>	
		<#else><img src="${baseURL}/images/icons/organisation-grey.png" /></#if>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.editOrganisations"/>
		<#if !isRegistered><div class="small"><@s.text name="admin.home.editOrganisations.disabled"/></div></#if>
	  </td>
	</tr>

	<tr>
	  <td>
		<a href="${baseURL}/admin/extensions.do"><img src="${baseURL}/images/icons/extensions.png" /></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.manageExtensions"/>
	  </td>
	</tr>
	<tr>
	  <td>
		<a href="${baseURL}/admin/logs.do"><img src="${baseURL}/images/icons/announcement.png" /></a>
	  </td>
	  <td colspan="2">
		<@s.text name="admin.home.manageLogs"/>
	  </td>
	</tr>
</table>

<#include "/WEB-INF/pages/inc/footer.ftl">
