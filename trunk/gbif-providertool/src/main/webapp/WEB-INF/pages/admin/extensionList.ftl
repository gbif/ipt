<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="heading" content="<@s.text name='extensionList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<table class="layout">
	<tr>
		<th><@s.text name="extension.name"/></th>
		<th><@s.text name="extension.type"/></th>
		<th><@s.text name="extension.properties"/></th>
		<th><@s.text name="extension.link"/></th>
		<th><@s.text name="extension.install"/></th>
	</tr>
<#list extensions as e>
	<tr>
		<td><a href="extensionDetail.html?id=${e.id?c}">${e.name}</a></td>
		<td>${e.type!""}</td>
		<td>${e.properties?size}</td>
		<td><#if e.link??><a href="${e.link}" target="_blank">info</a></#if></td>
		<td>${e.installed?string}</td>
	</tr>
</#list>
</table>
