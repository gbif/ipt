<head>
    <title>Thesaurus vocabularies</title>
	<meta name="decorator" content="fullsize"/>    
    <meta name="heading" content="Thesaurus vocabularies"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<table class="vocabularyListTable">
	<tr>
		<th><@s.text name="vocabulary.name"/></th>
		<th><@s.text name="vocabulary.uri"/></th>
	</tr>
<#list vocabularies as voc>
	<tr>
		<td><a href="vocabulary.html?id=${voc.id?c}">${voc.title}</a></td>
		<td>${voc.uri!}</td>
	</tr>
</#list>
</table>
