<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="menu" content="ManageMenu"/>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="<@s.text name='extensionList.heading'/>"/>
</head>

<p>
	<@s.text name="extension.explaination.1"/>  
	<@s.text name="extension.explaination.2"/>   
</p>

<table class="extensionListTable">
	<tr>
		<th><@s.text name="extension.name"/></th>
		<th><@s.text name="extension.properties"/></th>
		<th><@s.text name="extension.link"/></th>
	</tr>
<#list extensions as e>
  <#if e.installed==true>
	<tr>
		<td><a href="extension.html?id=${e.id?c}">${e.title}</a></td>
		<td>${e.properties?size}</td>
		<td><#if e.link??><img src="<@s.url value='/images/assets/bullet_blue.gif'/>"/><a href="${e.link}" target="_blank"> view info</a><#else><img src="<@s.url value='/images/assets/bullet_grey.gif'/>"/><@s.text name="extension.unavailable"/></#if></td>
	</tr>
  </#if>
</#list>
</table>
