<head>
    <title><@s.text name='404.record'/></title>
    <meta name="resource" content="${resource.title!}"/>
    <meta name="submenu" content="${resourceType!'meta'}"/>
    <meta name="heading" content="<@s.text name='404.record'/>"/>
    <style>
	p.error {
		font-size:1em !important;
	}    
    </style>
</head>

<p></p>
<p class="error">We are sorry, but the requested record in resource ${resource.title} does not exist or is private</p>
