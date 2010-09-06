<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="portal.home.title"/></h1>


<#if (resources?size>0)>
<table class="simple" width="100%">
	<tr>
		<th>Name</th>
		<th>Organisation</th>
		<th>Type</th>
		<th>Records</th>
		<th>Last modified</th>
	</tr>
<#list resources as r>
  <tr>
	<td><a href="resource.do?r=${r.shortname}">${r.title!r.shortname}</a></td>
	<td>${(r.organisation.name)!"---"}</td>
	<td>${r.type!"---"}</td>
	<td>0</td>
	<td>${r.modified?date}</td>
  </tr>
</#list>
</table>

<#else>
	<p>No public resources existing.</p>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
