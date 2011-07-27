<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>	
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="portal.home.title"/></h1>
<p><@s.text name="portal.home.intro"/></p> 

<#if (resources?size>0)>
<table id="resourcestable" class="sortable">
	<tr>
		<th class="sorttable_nosort"><@s.text name="portal.home.logo"/></th>
		<th><@s.text name="portal.home.name"/></th>
		<th><@s.text name="portal.home.organisation"/></th>
		<th><@s.text name="portal.home.type"/></th>
		<th><@s.text name="portal.home.records"/></th>
		<th><@s.text name="portal.home.modified"/></th>
		<th><@s.text name="portal.home.author"/></th>
	</tr>
<#list resources as r>
  <tr>
  	<td><#if r.eml.logoUrl?has_content><img class="resourceminilogo" src="${r.eml.logoUrl}" /></#if></td>
	<td><a href="resource.do?r=${r.shortname}"><#if r.title?has_content>${r.title}<#else>${r.shortname}</#if></a></td>
	<#-- if registrationAllowed -->
	<td>
		<#if r.status=='REGISTERED'>
			${r.organisation.name}
		<#else>
			<@s.text name="manage.home.not.registered"/>
		</#if>
	</td>
	<#-- >/#if -->
	<td>${r.subtype!r.coreType!"---"}</td>
	<td>${r.recordsPublished!0}</td>
	<td>${r.modified?date}</td>
	<td>${r.creator.firstname!} ${r.creator.lastname!}</td>
  </tr>
</#list>
</table>

<p><@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text> <img id="rssImage" src="${baseURL}/images/rss.png"/>.</p>

<#else>
	<p><@s.text name="portal.home.no.public"/></p>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>