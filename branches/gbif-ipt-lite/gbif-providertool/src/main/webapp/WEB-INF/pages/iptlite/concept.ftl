<head>
    <title>${concept.vocabulary.title} Vocabulary</title>
    <meta name="heading" content="${concept.identifier}"/>
    <meta name="menu" content="ManageMenu"/>
	<meta name="submenu" content="manage_resource"/>    
</head>

<table>	
 <tr>
	<th><@s.text name="concept.identifier"/></th>
	<td>${concept.identifier}</td>
 </tr>
 <tr>
	<th><@s.text name="concept.uri"/></th>
	<td>${concept.uri}</td>
 </tr>
 <tr>
	<th><@s.text name="concept.link"/></th>
	<td>${concept.link!}</td>
 </tr>
 <tr>
	<th><@s.text name="concept.issued"/></th>
	<td>${concept.issued!}</td>
 </tr>
 <tr>
	<th><@s.text name="concept.terms"/></th>
	<td>
		<ul class="plain">
		<#list terms as t>
			<li class="<#if t.preferred>accepted<#else>synonym</#if>">
				${t.title} (${t.lang})
			</li>
			<#assign lastTerm=t/>
		</#list>
		</ul>
	</td>
 </tr>
</table>
