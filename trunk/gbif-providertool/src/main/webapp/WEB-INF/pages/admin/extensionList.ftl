<head>
    <title><@s.text name="occResourceList.title"/></title>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="<@s.text name='extensionList.heading'/>"/>
    <meta name="menu" content="MainMenu"/>
</head>
<div class="horizontal_dotted_line_xlarge"></div>
<table class="extensionListTable">
	<tr>
		<th><@s.text name="extension.name"/></th>
		<th><@s.text name="extension.type"/></th>
		<th><@s.text name="extension.properties"/></th>
		<th><@s.text name="extension.install"/></th>
		<th><@s.text name="extension.link"/></th>
	</tr>
<#list extensions as e>
	<tr>
		<td><a href="extension.html?id=${e.id?c}">${e.name}</a></td>
		<td>${e.type!""}</td>
		<td>${e.properties?size}</td>
		<!--<td>${e.installed?string}</td>-->
		<td><#if e.installed==true><img src="/images/assets/checktrue.png"/> yes<#else><img src="/images/assets/checkfalse.png"/> no</#if></td>
		<td><#if e.link??><img src="/images/assets/info_on.png"/><a href="${e.link}" target="_blank"> view info</a><#else><img src="/images/assets/info_off.png"/> unaviable</#if></td>
	</tr>
</#list>
</table>
