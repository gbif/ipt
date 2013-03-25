<head>
    <title><@s.text name="taxon.title"/></title>
    <meta name="resource" content="${resource.title!}"/>
    <meta name="menu" content="ExplorerMenu"/>
    <meta name="submenu" content="${resourceType!'meta'}"/>
    <#if resource??>
      <meta name="heading" content="Record Not Found"/>
    <#else>
      <meta name="heading" content="Resource Not Found"/>
    </#if>
</head>
	

<#if resource??>
  <p class="error">The requested record in resource ${resource.title} does not exist.</p>
<#else>
  <p class="error">The requested resource does not exist.</p>
</#if>
