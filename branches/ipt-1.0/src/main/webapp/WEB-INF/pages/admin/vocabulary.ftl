<head>
    <title><@s.text name='vocabulary.title'/> ${vocabulary.title}</title>
	<meta name="decorator" content="fullsize"/>    
    <meta name="heading" content="<@s.text name='vocabulary.title'/> ${vocabulary.title}"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<p><@s.text name='vocabulary.instructions'/>
${vocabulary.link!"<em><@s.text name='vocabulary.notavailable'/></em>"}
</p>
<p><@s.text name='vocabulary.downloadas'/> <a href="vocabularyExport.xml?id=${vocabulary.id?c}"><@s.text name='vocabulary.xml'/></a></p>

<table class="vocabularyListTable">
	<tr>
		<th><@s.text name="concept.identifier"/></th>
		<th><@s.text name="concept.uri"/></th>
		<th><@s.text name="concept.issued"/></th>
	</tr>
<#list concepts as c>
	<tr>
		<td><a href="concept.html?id=${c.id?c}">${c.identifier}</a></td>
		<td>${c.uri}</td>
		<td>${c.issued?date}</td>
	</tr>
</#list>
</table>