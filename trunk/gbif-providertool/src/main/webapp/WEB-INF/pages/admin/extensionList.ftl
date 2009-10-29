<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="<@s.text name='extensionList.heading'/>"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<p>
	<@s.text name="extension.explanation.1"/>  
	<@s.text name="extension.explanation.2"/>   
	<@s.text name="extension.explanation.3"/> 
</p>
<p>
	<a href="synchroniseExtensions.html"><@s.text name="extension.update"/></a>
</p>

<table class="extensionListTable">
	<tr>
		<th><@s.text name="extension.name"/></th>
		<th><@s.text name="extension.namespace"/></th>
		<th><@s.text name="extension.properties"/></th>
		<th><@s.text name="extension.install"/></th>
		<th><@s.text name="extension.link"/></th>
	</tr>
<#list extensions as e>
	<tr>
		<td><a href="extension.html?id=${e.id?c}">${e.title}</a></td>
		<td>${e.namespace}</td>
		<td>${e.properties?size}</td>
		<td><#if e.installed==true><img src="<@s.url value='/images/assets/bullet_green.gif'/>"/> yes<#else><img src="<@s.url value='/images/assets/bullet_delete.gif'/>"/> no</#if></td>
		<td><#if e.link??><img src="<@s.url value='/images/assets/bullet_blue.gif'/>"/><a href="${e.link}" target="_blank"> view info</a><#else><img src="<@s.url value='/images/assets/bullet_grey.gif'/>"/><@s.text name="extension.unavailable"/></#if></td>
	</tr>
</#list>
</table>
