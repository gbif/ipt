<head>
    <title>About <@s.text name='provider.title'/></title>
    <meta name="menu" content="AboutMenu"/>
    <meta name="decorator" content="fullsize"/>
</head>

<h1>${cfg.ipt.title}</h1>

<div class="separator"></div>

<div id="about">
<a href="${cfg.ipt.link}"><img class="right" src="${cfg.descriptionImage}" /></a>

<table>
	<tr>
		<th>Homepage</th>
		<td><a href="${cfg.ipt.link}">${cfg.ipt.link}</a></td>
	</tr>
	<tr>
		<th>Contact</th>
		<td>${cfg.ipt.contactName} &lt;${cfg.ipt.contactEmail}&gt;</td>
	</tr>
</table>

<span>
${cfg.ipt.description}
</span>
</div>