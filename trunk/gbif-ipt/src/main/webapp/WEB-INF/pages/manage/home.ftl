<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name="manage.home.title"/></h1>
<br/>

<#if (resources?size>0)>
<table class="simple" width="100%">
	<tr>
		<th>Name</th>
		<th>Type</th>
		<th>Records</th>
		<th>Last modified</th>
		<th>Visible to</th>
		<#-- see if the ADMIN has enabled registrations -->
		<#if registrationAllowed>
		<th>Registered</th>
		</#if>
	</tr>
<#list resources as r>
  <tr>
	<td><a href="resource.do?r=${r.shortname}">${r.title!r.shortname}</a></td>
	<td>${r.type!"---"}</td>
	<td>0</td>
	<td>${r.modified?date}</td>
	<td>
		<#if r.status=='PRIVATE'>
			You and ${(r.managers?size)!0} others
		<#else>
			Everyone
		</#if>
	</td>
	<#if registrationAllowed>
	<td>
		<#if r.status=='REGISTERED'>
			My Organisation
		<#else>
			Not Registered
		</#if>
	</td>
	</#if>
  </tr>
</#list>
</table>

<#else>
	<p>None, please create a resource or ask existing managers to add you to their resource(s).</p>
</#if>


<hr/>&nbsp;
<hr/>&nbsp;

<h2><@s.text name="manage.resource.create.title"/></h2>
<#include "create_new_resource.inc.ftl"/>


<#include "/WEB-INF/pages/inc/footer.ftl">
