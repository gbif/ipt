<head>
    <title>${vocabulary.title} Vocabulary</title>
	<meta name="decorator" content="fullsize"/>    
    <meta name="heading" content="${vocabulary.title} Vocabulary"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<p>To participate in the discussion and definition of this vocabulary please visit:<br/>
${vocabulary.link!"<em>not available</em>"}
</p>
<p>Download vocabulary as <a href="vocabularyExport.xml?id=${vocabulary.id?c}">thesaurus xml</a></p>

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
