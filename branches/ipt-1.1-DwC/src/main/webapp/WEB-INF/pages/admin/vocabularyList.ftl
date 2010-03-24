<head>
    <title><@s.text name='vocabularylist.title'/></title>
	<meta name="decorator" content="fullsize"/>    
    <meta name="heading" content="<@s.text name='vocabularylist.title'/>"/>
    <meta name="menu" content="AdminMenu"/>
</head>

<p>
	<@s.text name="thesaurus.explanation.1"/>  
	<@s.text name="thesaurus.explanation.2"/>   
	<@s.text name="thesaurus.explanation.3"/> 
</p>
<p>
	<a href="synchroniseThesauri.html"><@s.text name="thesaurus.update"/></a>
</p>
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
