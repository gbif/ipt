<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.home.title"/></h1>
<ul>
	<li><a href="${baseURL}/admin/config.do"><@s.text name="admin.home.editConfig"/></a></li>
	<li><a href="${baseURL}/admin/organisation.do"><@s.text name="admin.home.editOrganisations"/></a></li>
	<li><a href="${baseURL}/admin/registration.do"><@s.text name="admin.home.editRegistration"/></a></li>
	<li><a href="${baseURL}/admin/users.do"><@s.text name="admin.home.manageUsers"/></a></li>
	<li><a href="${baseURL}/admin/extensions.do"><@s.text name="admin.home.manageExtensions"/></a></li>
	<li><a href="${baseURL}/admin/vocabularies.do"><@s.text name="admin.home.manageVocabularies"/></a></li>
</ul>

<#include "/WEB-INF/pages/inc/footer.ftl">
