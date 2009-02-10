<head>
    <title><@s.text name="search.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="tax"/>
</head>
	
<style>
	.modifiedh1{
		margin-top:-13px;
	}
</style>

<h1 class="modifiedh1">Your Search: ${q}</h1>  

<#include "/WEB-INF/pages/inc/taxonList.ftl">  
