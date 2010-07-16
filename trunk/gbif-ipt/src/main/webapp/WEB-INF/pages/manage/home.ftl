<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="manage.home.title"/></h1>

<p>
	Notes:
	<ul>
		<li>An administrator can grant privileges to multiple users to manage a single resource</li>
		<li>A resource may be visible to you and other managers only</li>
		<li>A resource may be made visible to everyone</li>
		<li>A registered resource is considered <b>published</b> through GBIF and therefore must remain visible to everyone</li>
		<li>Any resource may be deleted</li>
	</ul>
</p>

<!-- Quick hack - REDO this!!! Internationalise this!!! -->
<table>
	<tr>
		<th></th>
		<th>Name</th>
		<th>Organisation</th>
		<th>Created</th>
		<th>Last modified</th>
		<th>Visible to</th>
		<th>Visibility options</th>
		<!-- TODO - add a test to see if the ADMIN has enabled registrations!!! -->
		<th>Registered</th>
	</tr>
<#list resources as r>
  <tr>
	<td>
		<button>Delete</button>
	</td>
	<td>${r.title}</td>
	<td>${r.organisation.shortName}</td>
	<td>${r.created?date}</td>
	<td>${r.modified?date}</td>
	<td>
		<#if r.state=='PRIVATE'>
			You and ${r.managers.size()-1} others
		<#else>
			Everyone
		</#if>
	</td>
	<td>
		<#if r.state=='PRIVATE'>
			<button>Allow everyone</button>
		<#elseif r.state=='PUBLIC'>
			<button>Restrict to managers</button>
		</#if>
	</td>
	<!-- TODO - add a test to see if the ADMIN has enabled registrations!!! -->
	<td>
		<#if r.state=='REGISTERED'>
			Yes
		<#elseif r.state=='PUBLIC'>
			<button>Register</button>
		<#else>
			No
		</#if>
	</td>
  </tr>
</#list>
</table>

<p>
	<button><@s.text name="manage.home.button.createNewResource"/></button>
</p>

<#include "/WEB-INF/pages/inc/footer.ftl">
