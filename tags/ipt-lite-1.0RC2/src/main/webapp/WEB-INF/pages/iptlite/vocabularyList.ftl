<head>
    <title>Extension vocabularies</title>
    <meta name="menu" content="ManageMenu"/>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="Extension vocabularies"/>
</head>

<p>
	<@s.text name="thesaurus.explaination.1"/>  
	<@s.text name="thesaurus.explaination.2"/>   
</p>

<table class="vocabularyListTable">
	<tr>
		<th><@s.text name="vocabulary.name"/></th>
		<th><@s.text name="vocabulary.uri"/></th>
	</tr>
<#list vocabularies as voc>
	<tr>
		<td><a href="vocab.html?id=${voc.id?c}">${voc.title}</a></td>
		<td>${voc.uri!}</td>
	</tr>
</#list>
</table>
