<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
	<style>
	#rssImage{
		top: 2px !important;
		position: relative;
	}
	</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="portal.home.title"/></h1>
<p>Public resources available through this IPT installation.</p> 

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

<p>The most recently updated resources are also available as an <a href="${baseURL}/rss.do">RSS feed</a> <img id="rssImage" src="${baseURL}/images/rss.png"/>.</p>

<#else>
	<p>No public resources existing.</p>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
