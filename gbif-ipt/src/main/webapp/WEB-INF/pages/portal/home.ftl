<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
	<style>
	#rssImage{
		top: 2px !important;
		position: relative;
	}
	.resourceminilogo{
		max-width: 30px;
		max-height: 30px;
 		width: expression(this.width > 30 ? "30px" : true);
		height: expression(this.height > 30 ? "30px" : true);
	}
	</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="portal.home.title"/></h1>
<p><@s.text name="portal.home.intro"/></p> 

<#if (resources?size>0)>
<table class="simple" width="100%">
	<tr>
		<th><@s.text name="portal.home.logo"/></th>
		<th><@s.text name="portal.home.name"/></th>
		<th><@s.text name="portal.home.organisation"/></th>
		<th><@s.text name="portal.home.type"/></th>
		<th><@s.text name="portal.home.records"/></th>
		<th><@s.text name="portal.home.modified"/></th>
	</tr>
<#list resources as r>
  <tr>
  	<td><#if r.eml.logoUrl?has_content><img class="resourceminilogo" src="${r.eml.logoUrl}" /></#if></td>
	<td><a href="resource.do?r=${r.shortname}"><#if r.title?has_content>${r.title}<#else>${r.shortname}</#if></a></td>
	<td>${(r.organisation.name)!"---"}</td>
	<td>${r.subtype!r.coreType!"---"}</td>
	<td>${r.recordsPublished!0}</td>
	<td>${r.modified?date}</td>
  </tr>
</#list>
</table>

<p><@s.text name="portal.home.feed"><@s.param>${baseURL}/rss.do</@s.param></@s.text> <img id="rssImage" src="${baseURL}/images/rss.png"/>.</p>

<#else>
	<p><@s.text name="portal.home.no.public"/></p>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>