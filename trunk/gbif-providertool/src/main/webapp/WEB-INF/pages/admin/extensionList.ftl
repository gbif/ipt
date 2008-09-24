<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="heading" content="<@s.text name='extensionList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>

<table>
	<tr>
		<th><@s.text name="extension.name"/></th>
		<th><@s.text name="extension.type"/></th>
		<th><@s.text name="extension.propertyCount"/></th>
		<th><@s.text name="extension.link"/></th>
		<th><@s.text name="extension.install"/></th>
	</tr>
<#list extensions as e>
	<tr>
		<td><a href="extension.html?id=${e.id?c}">${e.name}</a></td>
		<td>${e.type!"core"}</td>
		<td>${e.properties?size}</td>
		<td><a href="${e.link}" target="_blank">info</a></td>
		<td><#if e.installed>stats <a href="delExtension.html?id=${e.id?c}" onclick="return confirmDelete('extension')">remove</a><#else><a href="addExtension.html?id=${e.id?c}">install</a></#if></td>
	</tr>
</#list>
</table>
