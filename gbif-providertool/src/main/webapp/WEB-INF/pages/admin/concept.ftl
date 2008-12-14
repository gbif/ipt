<head>
    <title>${concept.type} Vocabulary</title>
    <meta name="heading" content="${concept.identifier}"/>
    <meta name="menu" content="MainMenu"/>
</head>

<table>	
 <tr>
	<th>Identifier</th>
	<td>${concept.identifier}</td>
 </tr>
 <tr>
	<th>Issued</th>
	<td>${concept.issued}</td>
 </tr>
 <tr>
	<th>Terms</th>
	<td>
		<ul class="plain">
		<#list terms as t>
			<li class="<#if t.accepted>accepted<#else>synonym</#if>">
				${t.title} (${t.lang})
			</li>
			<#assign lastTerm=t/>
		</#list>
		</ul>
	</td>
 </tr>
</table>
