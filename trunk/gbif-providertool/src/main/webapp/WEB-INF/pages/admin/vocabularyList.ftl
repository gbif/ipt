<head>
    <title>Thesaurus vocabularies</title>
    <meta name="heading" content="Thesaurus vocabularies"/>
    <meta name="menu" content="MainMenu"/>
</head>

<table class="layout">
	<tr>
		<th><@s.text name="vocabulary.name"/></th>
		<th><@s.text name="vocabulary.uri"/></th>
	</tr>
<#list vocabularies as voc>
	<tr>
		<td><a href="vocabulary.html?id=${voc.ordinal()?c}">${voc.name()}</a></td>
		<td>${voc.uri!}</td>
	</tr>
</#list>
</table>
