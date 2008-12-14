<head>
    <title>${vocabulary.name()} Vocabulary</title>
    <meta name="heading" content="${vocabulary.name()} Vocabulary"/>
    <meta name="menu" content="MainMenu"/>
</head>

<table class="layout">
	<tr>
		<th><@s.text name="concept.identifier"/></th>
		<th><@s.text name="concept.issued"/></th>
	</tr>
<#list concepts as c>
	<tr>
		<td><a href="concept.html?id=${c.id?c}">${c.identifier}</a></td>
		<td>${c.issued?date}</td>
	</tr>
</#list>
</table>
