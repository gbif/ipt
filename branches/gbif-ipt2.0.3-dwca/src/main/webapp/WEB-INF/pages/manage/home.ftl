<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="title"/></title>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name="manage.home.title"/></h1>
<br/>

<#if (resources?size>0)>
<table id="resourcestable" class="sortable">
	<tr>
		<th><@s.text name="manage.home.name"/></th>
		<th><@s.text name="manage.home.type"/></th>
		<th><@s.text name="manage.home.records"/></th>
		<th><@s.text name="manage.home.last.modified"/></th>
		<th><@s.text name="manage.home.visible"/></th>
		<#-- see if the ADMIN has enabled registrations -->
		<#-- if registrationAllowed -->
		<th><@s.text name="manage.home.organisation"/></th>
		<#-- >/#if -->
	</tr>
<#list resources as r>
  <tr>
	<td><a href="resource.do?r=${r.shortname}"><if><#if r.title?has_content>${r.title}<#else>${r.shortname}</#if></a></td>
	<td>${r.subtype!r.coreType!"---"}</td>
	<td>${r.recordsPublished!0}</td>
	<td>${r.modified?date}</td>
	<td>
		<#if r.status=='PRIVATE'>
			<@s.text name="manage.home.visible.private"/>
		<#else>
			<@s.text name="manage.home.visible.public"/>
		</#if>
	</td>
	<#-- if registrationAllowed -->
	<td>
		<#if r.status=='REGISTERED'>
			${r.organisation.name}
		<#else>
			<@s.text name="manage.home.not.registered"/>
		</#if>
	</td>
	<#-- >/#if -->
  </tr>
</#list>
</table>

<#else>
	<p><@s.text name="manage.home.resources.none"/></p>
</#if>


<br/>
<br/>
<br/>

<h2><@s.text name="manage.resource.create.title"/></h2>
<#include "inc/create_new_resource.ftl"/>


<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
