<head>
    <title>About <@s.text name='provider.title'/></title>
    <meta name="menu" content="MainMenu"/>
    <meta name="decorator" content="fullsize"/>
</head>

<h1>${cfg.title}</h1>

<div class="separator"></div>

<div id="about">
<a href="${cfg.link}"><img class="right" src="${cfg.descriptionImage}" /></a>

<table>
	<tr>
		<th>Homepage</th>
		<td><a href="${cfg.link}">${cfg.link}</a></td>
	</tr>
	<tr>
		<th>Contact</th>
		<td>${cfg.contactName} &lt;${cfg.contactEmail}&gt;</td>
	</tr>
</table>

<span>
${cfg.description}
</span>
</div>