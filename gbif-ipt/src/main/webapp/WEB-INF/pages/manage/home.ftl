<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

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
		<th>Type</th>
		<th>Last modified</th>
		<th>Visible to</th>
		<th>Visibility options</th>
		<#-- see if the ADMIN has enabled registrations -->
		<#if registrationAllowed>
		<th>Registered</th>
		</#if>
	</tr>
<#list resources as r>
  <tr>
	<td>
		<button>Delete</button>
	</td>
	<td><a href="resource.do?r=${r.shortname}">${r.title!r.shortname}</a></td>
	<td>${r.type!"???"}</td>
	<td>${r.modified?date}</td>
	<td>
		<#if r.status=='PRIVATE'>
			You and ${r.managers.size()} others
		<#else>
			Everyone
		</#if>
	</td>
	<td>
		<#if r.status=='PRIVATE'>
			<button>Allow everyone</button>
		<#elseif r.status=='PUBLIC'>
			<button>Restrict to managers</button>
		</#if>
	</td>
	<#if registrationAllowed>
	<td>
		<#if r.status=='REGISTERED'>
			Yes
		<#elseif r.status=='PUBLIC'>
			<button>Register</button>
		<#else>
			No
		</#if>
	</td>
	</#if>
  </tr>
</#list>
</table>


<@s.form cssClass="ftlTopForm" action="create.do" method="get">
  <div class="buttons">
 	<@s.submit cssClass="button" name="create" key="manage.home.button.createNewResource"/>
  </div>	
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
