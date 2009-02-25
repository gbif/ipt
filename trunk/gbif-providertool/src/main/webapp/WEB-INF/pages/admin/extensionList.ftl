<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="<@s.text name='extensionList.heading'/>"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<p>
	<@s.text name="extension.explaination.1"/> 
	<@s.text name="extension.explaination.2"/> 
	<@s.text name="extension.explaination.3"/>
</p>
<p>
	<a href="synchroniseExtensions.html"><@s.text name="extension.update"/></a>
</p>

<table class="extensionListTable">
	<tr>
		<th><@s.text name="extension.name"/></th>
		<th><@s.text name="extension.properties"/></th>
		<th><@s.text name="extension.install"/></th>
		<th><@s.text name="extension.link"/></th>
	</tr>
<#list extensions as e>
	<tr>
		<td><a href="extension.html?id=${e.id?c}">${e.name}</a></td>
		<td>${e.properties?size}</td>
		<td><#if e.installed==true><img src="/images/assets/bullet_green.png"/> yes<#else><img src="/images/assets/bullet_delete.png"/> no</#if></td>
		<td><#if e.link??><img src="/images/assets/bullet_blue.png"/><a href="${e.link}" target="_blank"> view info</a><#else><img src="/images/assets/bullet_grey.png"/><@s.text name="extension.unavailable"/></#if></td>
	</tr>
</#list>
</table>
